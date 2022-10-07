package org.synyx.urlaubsverwaltung.application.application;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentService;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMapping;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToCancelApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.ALLOWED_DIRECTLY;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.APPLIED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.CANCELLED_DIRECTLY;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.CANCEL_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.CANCEL_REQUESTED_DECLINED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.EDITED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.REVOKED;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.VACATION;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;

@Service
@Transactional
class ApplicationInteractionServiceImpl implements ApplicationInteractionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final int MIN_DAYS_LEFT_BEFORE_REMINDING_IS_POSSIBLE = 2;

    private final ApplicationService applicationService;
    private final AccountInteractionService accountInteractionService;
    private final ApplicationCommentService commentService;
    private final ApplicationMailService applicationMailService;
    private final CalendarSyncService calendarSyncService;
    private final AbsenceMappingService absenceMappingService;
    private final TimeSettings timeSettings;
    private final DepartmentService departmentService;
    private final Clock clock;

    @Autowired
    ApplicationInteractionServiceImpl(ApplicationService applicationService,
                                      ApplicationCommentService commentService,
                                      AccountInteractionService accountInteractionService,
                                      ApplicationMailService applicationMailService,
                                      CalendarSyncService calendarSyncService,
                                      AbsenceMappingService absenceMappingService,
                                      SettingsService settingsService,
                                      DepartmentService departmentService, Clock clock) {

        this.applicationService = applicationService;
        this.commentService = commentService;
        this.accountInteractionService = accountInteractionService;
        this.applicationMailService = applicationMailService;
        this.calendarSyncService = calendarSyncService;
        this.absenceMappingService = absenceMappingService;
        this.timeSettings = settingsService.getSettings().getTimeSettings();
        this.departmentService = departmentService;
        this.clock = clock;
    }

    @Override
    public Application apply(Application application, Person applier, Optional<String> comment) {

        final Person person = application.getPerson();
        final List<Department> departments = departmentService.getAssignedDepartmentsOfMember(person);
        // check if a two stage approval is set for the Department
        departments.stream()
            .filter(Department::isTwoStageApproval)
            .forEach(department -> application.setTwoStageApproval(true));

        application.setStatus(WAITING);
        application.setApplier(applier);
        application.setApplicationDate(LocalDate.now(clock));

        final Application savedApplication = applicationService.save(application);

        LOG.info("Created application for leave: {}", savedApplication);

        // COMMENT
        final ApplicationComment createdComment = commentService.create(savedApplication, APPLIED, comment, applier);

        // EMAILS
        if (person.equals(applier)) {
            // person himself applies for leave
            // person gets a confirmation email with the data of the application for leave
            applicationMailService.sendConfirmation(savedApplication, createdComment);
        } else {
            // The person gets an email that someone else has applied for leave on behalf
            applicationMailService.sendAppliedForLeaveByManagementNotification(savedApplication, createdComment);
        }

        // relevant management person gets email that a new application for leave has been created
        applicationMailService.sendNewApplicationNotification(savedApplication, createdComment);

        // send email to replacement to inform beforehand the confirmation
        for (HolidayReplacementEntity holidayReplacement : savedApplication.getHolidayReplacements()) {
            applicationMailService.notifyHolidayReplacementForApply(holidayReplacement, savedApplication);
        }

        // update remaining vacation days (if there is already a holidays account for next year)
        accountInteractionService.updateRemainingVacationDays(savedApplication.getStartDate().getYear(), person);

        if (calendarSyncService.isRealProviderConfigured()) {
            final AbsenceTimeConfiguration absenceTimeConfiguration = new AbsenceTimeConfiguration(timeSettings);
            final Absence absence = new Absence(savedApplication.getPerson(), savedApplication.getPeriod(), absenceTimeConfiguration);
            calendarSyncService.addAbsence(absence)
                .ifPresent(eventId -> absenceMappingService.create(savedApplication.getId(), VACATION, eventId));
        }
        return savedApplication;
    }

    @Override
    public Application allow(Application application, Person privilegedUser, Optional<String> comment) throws NotPrivilegedToApproveException {

        // Boss is a very mighty dude
        if (privilegedUser.hasRole(BOSS)) {
            return allowFinally(application, privilegedUser, comment);
        }

        // Second stage authority has almost the same power (except on own applications)
        final boolean isSecondStageAuthorityOfPerson = departmentService.isSecondStageAuthorityAllowedToManagePerson(privilegedUser, application.getPerson());
        final boolean isOwnApplication = application.getPerson().equals(privilegedUser);
        if (isSecondStageAuthorityOfPerson && !isOwnApplication) {
            return allowFinally(application, privilegedUser, comment);
        }

        // Department head can be mighty only in some cases
        final boolean isDepartmentHeadOfPerson = departmentService.isDepartmentHeadAllowedToManagePerson(privilegedUser, application.getPerson());
        final boolean isPersonSecondStageAuthorityOfApprover = departmentService.isSecondStageAuthorityAllowedToManagePerson(application.getPerson(), privilegedUser);
        if (isDepartmentHeadOfPerson && !isOwnApplication && !isPersonSecondStageAuthorityOfApprover) {
            if (application.isTwoStageApproval()) {
                return allowTemporary(application, privilegedUser, comment);
            }
            return allowFinally(application, privilegedUser, comment);
        }

        throw new NotPrivilegedToApproveException(format(
            "because is not department is %s " +
                "or is own application %s " +
                "or is the application of ssa %s", isDepartmentHeadOfPerson, isOwnApplication, isPersonSecondStageAuthorityOfApprover));
    }

    @Override
    public Application directAllow(Application application, Person applier, Optional<String> comment) {

        application.setStatus(ALLOWED);
        application.setApplier(applier);
        application.setApplicationDate(LocalDate.now(clock));

        final Application savedApplication = applicationService.save(application);
        LOG.info("Created application for leave and allow directly via not required approval: {}", savedApplication);

        // COMMENT
        final ApplicationComment createdComment = commentService.create(savedApplication, ALLOWED_DIRECTLY, comment, applier);

        // EMAILS
        final Person person = application.getPerson();
        if (person.equals(applier)) {
            // person himself applies for leave
            // person gets a confirmation email with the data of the application for leave
            applicationMailService.sendConfirmationAllowedDirectly(savedApplication, createdComment);
        } else {
            // The person gets an email that someone else has applied for leave on behalf
            applicationMailService.sendConfirmationAllowedDirectlyByManagement(savedApplication, createdComment);
        }

        // relevant management person gets email that a new directly allowed application for leave has been created
        applicationMailService.sendNewDirectlyAllowedApplicationNotification(savedApplication, createdComment);

        // send email to replacement to inform beforehand the confirmation
        for (HolidayReplacementEntity holidayReplacement : savedApplication.getHolidayReplacements()) {
            applicationMailService.notifyHolidayReplacementAboutDirectlyAllowedApplication(holidayReplacement, savedApplication);
        }

        // update remaining vacation days (if there is already a holidays account for next year
        // TODO - wann brachen wir das? Nur wenn die category HOLIDAY ist?
        accountInteractionService.updateRemainingVacationDays(savedApplication.getStartDate().getYear(), person);

        if (calendarSyncService.isRealProviderConfigured()) {
            final AbsenceTimeConfiguration absenceTimeConfiguration = new AbsenceTimeConfiguration(timeSettings);
            final Absence absence = new Absence(savedApplication.getPerson(), savedApplication.getPeriod(), absenceTimeConfiguration);
            calendarSyncService.addAbsence(absence)
                .ifPresent(eventId -> absenceMappingService.create(savedApplication.getId(), VACATION, eventId));
        }
        return savedApplication;
    }


    private Application allowTemporary(Application applicationForLeave, Person privilegedUser, Optional<String> comment) {

        final boolean alreadyAllowed = applicationForLeave.hasStatus(TEMPORARY_ALLOWED) || applicationForLeave.hasStatus(ALLOWED);
        if (alreadyAllowed) {
            // Early return - do nothing if expected status already set
            LOG.info("Application for leave is already in an allowed status, do nothing: {}", applicationForLeave);

            return applicationForLeave;
        }

        applicationForLeave.setStatus(TEMPORARY_ALLOWED);
        applicationForLeave.setBoss(privilegedUser);
        applicationForLeave.setEditedDate(LocalDate.now(clock));
        final Application savedApplication = applicationService.save(applicationForLeave);

        LOG.info("Temporary allowed application for leave: {}", savedApplication);

        final ApplicationComment createdComment = commentService.create(savedApplication,
            ApplicationCommentAction.TEMPORARY_ALLOWED, comment, privilegedUser);

        applicationMailService.sendTemporaryAllowedNotification(savedApplication, createdComment);

        return savedApplication;
    }

    private Application allowFinally(Application applicationForLeave, Person privilegedUser, Optional<String> comment) {

        if (applicationForLeave.hasStatus(ALLOWED)) {
            // Early return - do nothing if expected status already set
            LOG.info("Application for leave is already in an allowed status, do nothing: {}", applicationForLeave);
            return applicationForLeave;
        }

        applicationForLeave.setStatus(ALLOWED);
        applicationForLeave.setBoss(privilegedUser);
        applicationForLeave.setEditedDate(LocalDate.now(clock));
        final Application savedApplication = applicationService.save(applicationForLeave);

        LOG.info("Allowed application for leave: {}", savedApplication);

        final ApplicationComment createdComment = commentService.create(savedApplication, ApplicationCommentAction.ALLOWED,
            comment, privilegedUser);

        applicationMailService.sendAllowedNotification(savedApplication, createdComment);

        for (HolidayReplacementEntity holidayReplacement : savedApplication.getHolidayReplacements()) {
            applicationMailService.notifyHolidayReplacementAllow(holidayReplacement, savedApplication);
        }

        return savedApplication;
    }


    @Override
    public Application reject(Application application, Person privilegedUser, Optional<String> comment) {

        application.setStatus(REJECTED);
        application.setBoss(privilegedUser);
        application.setEditedDate(LocalDate.now(clock));
        final Application savedApplication = applicationService.save(application);

        LOG.info("Rejected application for leave: {}", savedApplication);

        final ApplicationComment createdComment = commentService.create(savedApplication, ApplicationCommentAction.REJECTED, comment,
            privilegedUser);

        applicationMailService.sendRejectedNotification(savedApplication, createdComment);

        for (HolidayReplacementEntity holidayReplacement : savedApplication.getHolidayReplacements()) {
            applicationMailService.notifyHolidayReplacementAboutCancellation(holidayReplacement, savedApplication);
        }

        final Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(savedApplication.getId(), VACATION);
        if (absenceMapping.isPresent()) {
            calendarSyncService.deleteAbsence(absenceMapping.get().getEventId());
            absenceMappingService.delete(absenceMapping.get());
        }

        return application;
    }


    @Override
    public Application cancel(Application application, Person canceller, Optional<String> comment) {

        final Person person = application.getPerson();

        application.setCanceller(canceller);
        application.setCancelDate(LocalDate.now(clock));

        if (application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED) || application.hasStatus(TEMPORARY_ALLOWED)) {
            cancelApplication(application, canceller, comment);
        } else if (application.hasStatus(WAITING)) {
            revokeApplication(application, canceller, comment);
        }

        accountInteractionService.updateRemainingVacationDays(application.getStartDate().getYear(), person);

        final Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(application.getId(), VACATION);
        if (absenceMapping.isPresent()) {
            calendarSyncService.deleteAbsence(absenceMapping.get().getEventId());
            absenceMappingService.delete(absenceMapping.get());
        }

        return application;
    }

    @Override
    public Application directCancel(Application application, Person canceller, Optional<String> comment) {

        application.setStatus(ApplicationStatus.CANCELLED);
        application.setCanceller(canceller);
        application.setCancelDate(LocalDate.now(clock));

        final Application savedApplication = applicationService.save(application);
        LOG.info("Cancelled application for leave without approval (directly): {}", savedApplication);

        // Comment
        final ApplicationComment createdComment = commentService.create(savedApplication, CANCELLED_DIRECTLY, comment, canceller);

        // E-Mails
        final Person person = application.getPerson();
        if (person.equals(canceller)) {
            // person himself applies for leave
            // person gets a confirmation email with the data of the application for leave
            applicationMailService.sendCancelledDirectlyConfirmationByApplicant(savedApplication, createdComment);
        } else {
            // The person gets an email that someone else has applied for leave on behalf
            applicationMailService.sendCancelledDirectlyConfirmationByManagement(savedApplication, createdComment);
        }

        applicationMailService.sendCancelledDirectlyInformationToRecipientOfInterest(savedApplication, createdComment);

        for (HolidayReplacementEntity holidayReplacement : savedApplication.getHolidayReplacements()) {
            applicationMailService.notifyHolidayReplacementAboutCancellation(holidayReplacement, savedApplication);
        }

        // update remaining vacation days (if there is already a holidays account for next year
        // TODO - wann brachen wir das? Nur wenn die category HOLIDAY ist?
        accountInteractionService.updateRemainingVacationDays(savedApplication.getStartDate().getYear(), person);

        return savedApplication;
    }

    private void revokeApplication(Application application, Person canceller, Optional<String> comment) {

        application.setStatus(ApplicationStatus.REVOKED);
        final Application savedApplication = applicationService.save(application);
        LOG.info("Revoked application for leave: {}", savedApplication);

        final ApplicationComment savedComment = commentService.create(savedApplication, REVOKED, comment, canceller);
        applicationMailService.sendRevokedNotifications(application, savedComment);

        for (HolidayReplacementEntity holidayReplacement : savedApplication.getHolidayReplacements()) {
            applicationMailService.notifyHolidayReplacementAboutCancellation(holidayReplacement, savedApplication);
        }
    }


    private void cancelApplication(Application application, Person canceller, Optional<String> comment) {

        final boolean isDepartmentHeadOfPerson = departmentService.isDepartmentHeadAllowedToManagePerson(canceller, application.getPerson());
        final boolean isSecondStageAuthorityOfPerson = departmentService.isSecondStageAuthorityAllowedToManagePerson(canceller, application.getPerson());
        if (isAllowedToCancelApplication(application, canceller, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson)) {
            /*
             * Only management with the role application_cancel can cancel allowed applications for leave directly,
             * users have to request cancellation
             */
            application.setStatus(ApplicationStatus.CANCELLED);
            final Application savedApplication = applicationService.save(application);

            LOG.info("Cancelled application for leave: {}", savedApplication);

            final ApplicationComment savedComment = commentService.create(savedApplication, CANCELLED, comment, canceller);
            applicationMailService.sendCancelledConfirmationByManagement(savedApplication, savedComment);

            for (HolidayReplacementEntity holidayReplacement : savedApplication.getHolidayReplacements()) {
                applicationMailService.notifyHolidayReplacementAboutCancellation(holidayReplacement, savedApplication);
            }
        } else {
            /*
             * Users cannot cancel already allowed applications directly.
             * Their comment status will be CANCEL_REQUESTED
             * and the application status will be ALLOWED_CANCELLATION_REQUESTED until
             * someone approves the request.
             */
            application.setStatus(ALLOWED_CANCELLATION_REQUESTED);
            final Application savedApplication = applicationService.save(application);

            LOG.info("Request cancellation of application for leave: {}", savedApplication);

            final ApplicationComment createdComment = commentService.create(savedApplication, CANCEL_REQUESTED, comment, canceller);
            applicationMailService.sendCancellationRequest(savedApplication, createdComment);
        }
    }

    @Override
    public Application declineCancellationRequest(Application applicationForLeave, Person person, Optional<String> comment) {

        if (applicationForLeave.getStatus().compareTo(ALLOWED_CANCELLATION_REQUESTED) != 0) {
            throw new DeclineCancellationRequestedApplicationForLeaveNotAllowedException(format("Cannot cancel the cancellation " +
                "request of the application for leave with id %d because the status is %s and not " +
                "allowed_cancellation_requested.", applicationForLeave.getId(), applicationForLeave.getStatus()));
        }

        applicationForLeave.setStatus(ALLOWED);
        applicationForLeave.setEditedDate(LocalDate.now(clock));
        final Application savedApplication = applicationService.save(applicationForLeave);

        final ApplicationComment applicationComment = commentService.create(savedApplication, CANCEL_REQUESTED_DECLINED, comment, person);

        applicationMailService.sendDeclinedCancellationRequestApplicationNotification(savedApplication, applicationComment);

        return savedApplication;
    }

    @Override
    public Application createFromConvertedSickNote(Application application, Person creator) {

        // create an application for leave that is allowed directly
        application.setApplier(creator);
        application.setStatus(ALLOWED);

        final Application savedApplication = applicationService.save(application);

        commentService.create(savedApplication, ApplicationCommentAction.CONVERTED, Optional.empty(), creator);
        applicationMailService.sendSickNoteConvertedToVacationNotification(savedApplication);

        return savedApplication;
    }

    @Override
    public Application remind(Application application) throws RemindAlreadySentException, ImpatientAboutApplicationForLeaveProcessException {

        final LocalDate remindDate = application.getRemindDate();
        if (remindDate == null) {
            final LocalDate minDateForNotification = application.getApplicationDate().plusDays(MIN_DAYS_LEFT_BEFORE_REMINDING_IS_POSSIBLE);

            if (minDateForNotification.isAfter(LocalDate.now(clock))) {
                throw new ImpatientAboutApplicationForLeaveProcessException("It's too early to remind the bosses!");
            }
        }

        if (remindDate != null && remindDate.isEqual(LocalDate.now(clock))) {
            throw new RemindAlreadySentException("Reminding is possible maximum one time per day!");
        }

        applicationMailService.sendRemindBossNotification(application);

        application.setRemindDate(LocalDate.now(clock));
        return applicationService.save(application);
    }

    @Override
    public Application refer(Application application, Person recipient, Person sender) {

        commentService.create(application, ApplicationCommentAction.REFERRED, Optional.of(recipient.getNiceName()), sender);
        applicationMailService.sendReferApplicationNotification(application, recipient, sender);

        return application;
    }

    @Override
    public Optional<Application> get(Integer applicationId) {
        return applicationService.getApplicationById(applicationId);
    }

    @Override
    public Application edit(Application oldApplication, Application editedApplication, Person person, Optional<String> comment) {

        if (oldApplication.getStatus().compareTo(WAITING) != 0) {
            throw new EditApplicationForLeaveNotAllowedException(format("Cannot edit application for leave " +
                "with id %d because the status is %s and not waiting.", oldApplication.getId(), oldApplication.getStatus()));
        }

        if (!oldApplication.getPerson().equals(editedApplication.getPerson())) {
            throw new EditApplicationForLeaveNotAllowedException("Cannot change person of exiting application during edit.");
        }

        editedApplication.setStatus(WAITING);
        editedApplication.setEditedDate(LocalDate.now(clock));
        final Application savedEditedApplication = applicationService.save(editedApplication);

        commentService.create(savedEditedApplication, EDITED, comment, person);

        applicationMailService.sendEditedApplicationNotification(savedEditedApplication, person);

        final List<HolidayReplacementEntity> addedReplacements = replacementAdded(oldApplication, savedEditedApplication);
        final List<HolidayReplacementEntity> deletedReplacements = replacementDeleted(oldApplication, savedEditedApplication);

        if (relevantEntriesChanged(oldApplication, savedEditedApplication)) {
            final List<HolidayReplacementEntity> oldReplacements = oldApplication.getHolidayReplacements();
            final List<HolidayReplacementEntity> stillExistingReplacements = savedEditedApplication.getHolidayReplacements()
                .stream()
                .filter(oldReplacements::contains)
                .collect(toList());
            for (HolidayReplacementEntity replacement : stillExistingReplacements) {
                applicationMailService.notifyHolidayReplacementAboutEdit(replacement, savedEditedApplication);
            }
        }

        for (HolidayReplacementEntity replacement : addedReplacements) {
            applicationMailService.notifyHolidayReplacementForApply(replacement, savedEditedApplication);
        }

        for (HolidayReplacementEntity replacement : deletedReplacements) {
            applicationMailService.notifyHolidayReplacementAboutCancellation(replacement, savedEditedApplication);
        }

        return savedEditedApplication;
    }

    @Override
    @EventListener
    public void deleteAllByPerson(PersonDeletedEvent event) {
        final Person personToBeDeleted = event.getPerson();
        commentService.deleteByApplicationPerson(personToBeDeleted);
        commentService.deleteCommentAuthor(personToBeDeleted);
        applicationService.deleteApplicationsByPerson(personToBeDeleted);
    }

    private List<HolidayReplacementEntity> replacementAdded(Application oldApplication, Application savedEditedApplication) {
        final List<HolidayReplacementEntity> oldReplacements = oldApplication.getHolidayReplacements();
        return savedEditedApplication.getHolidayReplacements()
            .stream()
            .filter(not(oldReplacements::contains))
            .collect(toList());
    }

    private List<HolidayReplacementEntity> replacementDeleted(Application oldApplication, Application savedEditedApplication) {
        final List<HolidayReplacementEntity> newReplacements = savedEditedApplication.getHolidayReplacements();
        return oldApplication.getHolidayReplacements()
            .stream()
            .filter(not(newReplacements::contains))
            .collect(toList());
    }

    private boolean relevantEntriesChanged(Application oldApplication, Application savedEditedApplication) {
        return !oldApplication.getStartDate().equals(savedEditedApplication.getStartDate())
            || !oldApplication.getEndDate().equals(savedEditedApplication.getEndDate())
            || !oldApplication.getDayLength().equals(savedEditedApplication.getDayLength());
    }
}
