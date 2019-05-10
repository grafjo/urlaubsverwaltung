package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static java.util.Optional.ofNullable;


/**
 * Implementation of interface {@link MailService}.
 */
@Service("mailService")
class MailServiceImpl implements MailService {

    private static final Locale LOCALE = Locale.GERMAN;
    private static final String MODEL_APPLICATION_URL = "applicationUrl";

    private final MessageSource messageSource;
    private final MailBuilder mailBuilder;
    private final MailSender mailSender;
    private final RecipientService recipientService;
    private final DepartmentService departmentService;
    private final MailOptionProvider mailOptionProvider;


    @Autowired
    MailServiceImpl(MessageSource messageSource, MailBuilder mailBuilder, MailSender mailSender,
                    RecipientService recipientService, DepartmentService departmentService, MailOptionProvider mailOptionProvider) {

        this.messageSource = messageSource;
        this.mailBuilder = mailBuilder;
        this.mailSender = mailSender;
        this.recipientService = recipientService;
        this.departmentService = departmentService;
        this.mailOptionProvider = mailOptionProvider;

    }

    @Override
    public void sendNewApplicationNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application, ofNullable(comment));
        model.put("departmentVacations",
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(),
                application.getStartDate(), application.getEndDate()));

        List<Person> recipients = recipientService.getRecipientsForAllowAndRemind(application);
        String subject = getTranslation("subject.application.applied.boss", application.getPerson().getNiceName());

        sendMailToEachRecipient(model, recipients, "new_applications", subject);
    }

    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }


    private void sendMailToEachRecipient(Map<String, Object> model, List<Person> recipients, String template,
        String subject) {

        for (Person recipient : recipients) {
            model.put("recipient", recipient);

            String text = mailBuilder.buildMailBody(template, model, LOCALE);
            mailSender.sendEmail(mailOptionProvider.getSender(), RecipientUtil.getMailAddresses(recipient), subject, text);
        }
    }

    private Map<String, Object> createModelForApplicationStatusChangeMail(Application application, Optional<ApplicationComment> optionalComment) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", getTranslation(application.getVacationType().getCategory().getMessageKey()));
        model.put("dayLength", getTranslation(application.getDayLength().name()));
        model.put(MODEL_APPLICATION_URL, mailOptionProvider.getApplicationUrl());

        optionalComment.ifPresent(applicationComment -> model.put("comment", applicationComment));

        return model;
    }


    @Override
    public void sendRemindBossNotification(Application application) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application, Optional.empty());

        List<Person> recipients = recipientService.getRecipientsForAllowAndRemind(application);
        sendMailToEachRecipient(model, recipients, "remind", getTranslation("subject.application.remind"));
    }


    @Override
    public void sendTemporaryAllowedNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application, ofNullable(comment));
        model.put("departmentVacations",
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(),
                application.getStartDate(), application.getEndDate()));

        // Inform user that the application for leave has been allowed temporary
        String textUser = mailBuilder.buildMailBody("temporary_allowed_user", model, LOCALE);
        mailSender.sendEmail(mailOptionProvider.getSender(), RecipientUtil.getMailAddresses(application.getPerson()),
            getTranslation("subject.application.temporaryAllowed.user"), textUser);

        // Inform second stage authorities that there is an application for leave that must be allowed
        List<Person> recipients = recipientService.getRecipientsForTemporaryAllow(application);
        sendMailToEachRecipient(model, recipients, "temporary_allowed_second_stage_authority",
            getTranslation("subject.application.temporaryAllowed.secondStage"));
    }


    @Override
    public void sendAllowedNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application, ofNullable(comment));

        // Inform user that the application for leave has been allowed
        String textUser = mailBuilder.buildMailBody("allowed_user", model, LOCALE);
        mailSender.sendEmail(mailOptionProvider.getSender(), RecipientUtil.getMailAddresses(application.getPerson()),
            getTranslation("subject.application.allowed.user"), textUser);

        // Inform office that there is a new allowed application for leave

        String textOffice = mailBuilder.buildMailBody("allowed_office", model, LOCALE);
        List<String> recipients = RecipientUtil.getMailAddresses(recipientService.getRecipientsWithNotificationType(MailNotification.NOTIFICATION_OFFICE));
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.application.allowed.office"), textOffice);
    }


    @Override
    public void sendRejectedNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application, ofNullable(comment));
        String text = mailBuilder.buildMailBody("rejected", model, LOCALE);
        List<String> recipients = RecipientUtil.getMailAddresses(application.getPerson());
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.application.rejected"), text);
    }


    @Override
    public void sendReferApplicationNotification(Application application, Person recipient, Person sender) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put(MODEL_APPLICATION_URL, mailOptionProvider.getApplicationUrl());
        model.put("recipient", recipient);
        model.put("sender", sender);

        String text = mailBuilder.buildMailBody("refer", model, LOCALE);
        List<String> recipients = RecipientUtil.getMailAddresses(recipient);
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.application.refer"), text);
    }


    @Override
    public void sendConfirmation(Application application, ApplicationComment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application, ofNullable(comment));
        String text = mailBuilder.buildMailBody("confirm", model, LOCALE);
        List<String> recipients = RecipientUtil.getMailAddresses(application.getPerson());
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.application.applied.user"), text);
    }


    @Override
    public void sendAppliedForLeaveByOfficeNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application, ofNullable(comment));
        String text = mailBuilder.buildMailBody("new_application_by_office", model, LOCALE);
        List<String> recipients = RecipientUtil.getMailAddresses(application.getPerson());
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.application.appliedByOffice"), text);
    }


    @Override
    public void sendCancelledByOfficeNotification(Application application, ApplicationComment comment) {

        Map<String, Object> model = createModelForApplicationStatusChangeMail(application, ofNullable(comment));

        String text = mailBuilder.buildMailBody("cancelled_by_office", model, LOCALE);

        List<String> recipients = RecipientUtil.getMailAddresses(application.getPerson());
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.application.cancelled.user"), text);
    }


    /**
     * Sends an email to the manager of the application to inform about a technical event, e.g. if an error occurred.
     *
     * @param  subject  of the email
     * @param  text  of the body of the email
     */
    private void sendTechnicalNotification(final String subject, final String text) {

        List<String> recipients = Collections.singletonList(mailOptionProvider.getAdministrator());
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, subject, text);
    }


    @Override
    public void sendCalendarSyncErrorNotification(String calendarName, Absence absence, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("absence", absence);
        model.put("exception", exception);

        String text = mailBuilder.buildMailBody("error_calendar_sync", model, LOCALE);

        sendTechnicalNotification(getTranslation("subject.error.calendar.sync"), text);
    }


    @Override
    public void sendCalendarUpdateErrorNotification(String calendarName, Absence absence, String eventId,
        String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("absence", absence);
        model.put("eventId", eventId);
        model.put("exception", exception);

        String text = mailBuilder.buildMailBody("error_calendar_update", model, LOCALE);

        sendTechnicalNotification(getTranslation("subject.error.calendar.update"), text);
    }


    @Override
    public void sendCalendarDeleteErrorNotification(String calendarName, String eventId, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("eventId", eventId);
        model.put("exception", exception);

        String text = mailBuilder.buildMailBody("error_calendar_delete", model, LOCALE);

        sendTechnicalNotification(getTranslation("subject.error.calendar.delete"), text);
    }


    @Override
    public void sendSuccessfullyUpdatedAccountsNotification(List<Account> updatedAccounts) {

        Map<String, Object> model = new HashMap<>();
        model.put("accounts", updatedAccounts);
        model.put("today", LocalDate.now(UTC));

        String text = mailBuilder.buildMailBody("updated_accounts", model, LOCALE);

        // send email to office for printing statistic
        List<String> recipients = RecipientUtil.getMailAddresses(recipientService.getRecipientsWithNotificationType(MailNotification.NOTIFICATION_OFFICE));
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.account.updatedRemainingDays"), text);

        // send email to manager to notify about update of accounts
        sendTechnicalNotification(getTranslation("subject.account.updatedRemainingDays"), text);
    }


    @Override
    public void sendSuccessfullyUpdatedSettingsNotification(Settings settings) {

        // TODO remove settings method object - hier überlegen warum ... nicht mehr ganz klar

        Map<String, Object> model = new HashMap<>();
        model.put(MODEL_APPLICATION_URL, mailOptionProvider.getApplicationUrl());
        model.put("host", mailOptionProvider.getMailServerHost());
        model.put("port", mailOptionProvider.getMailServerPort());

        String text = mailBuilder.buildMailBody("updated_settings", model, LOCALE);
        sendTechnicalNotification(getTranslation("subject.settings.updated"), text);
    }


    @Override
    public void sendSickNoteConvertedToVacationNotification(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put(MODEL_APPLICATION_URL, mailOptionProvider.getApplicationUrl());

        String text = mailBuilder.buildMailBody("sicknote_converted", model, LOCALE);
        mailSender.sendEmail(mailOptionProvider.getSender(), RecipientUtil.getMailAddresses(application.getPerson()), getTranslation("subject.sicknote.converted"), text);
    }


    @Override
    public void sendEndOfSickPayNotification(SickNote sickNote) {

        Map<String, Object> model = new HashMap<>();
        model.put("sickNote", sickNote);

        String text = mailBuilder.buildMailBody("sicknote_end_of_sick_pay", model, LOCALE);

        mailSender.sendEmail(mailOptionProvider.getSender(), RecipientUtil.getMailAddresses(sickNote.getPerson()), getTranslation("subject.sicknote.endOfSickPay"), text);
        mailSender.sendEmail(mailOptionProvider.getSender(), RecipientUtil.getMailAddresses(recipientService.getRecipientsWithNotificationType(MailNotification.NOTIFICATION_OFFICE)),
            getTranslation("subject.sicknote.endOfSickPay"), text);
    }


    @Override
    public void notifyHolidayReplacement(Application application) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("dayLength", messageSource.getMessage(application.getDayLength().name(), null, LOCALE));

        String text = mailBuilder.buildMailBody("notify_holiday_replacement", model, LOCALE);

        List<String> recipients = RecipientUtil.getMailAddresses(application.getHolidayReplacement());
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.application.holidayReplacement"), text);
    }


    @Override
    public void sendUserCreationNotification(Person person, String rawPassword) {

        Map<String, Object> model = new HashMap<>();
        model.put("person", person);
        model.put("rawPassword", rawPassword);
        model.put(MODEL_APPLICATION_URL, mailOptionProvider.getApplicationUrl());

        String text = mailBuilder.buildMailBody("user_creation", model, LOCALE);

        mailSender.sendEmail(mailOptionProvider.getSender(), RecipientUtil.getMailAddresses(person), getTranslation("subject.userCreation"), text);
    }


    @Override
    public void sendCancellationRequest(Application application, ApplicationComment createdComment) {

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", createdComment);
        model.put(MODEL_APPLICATION_URL, mailOptionProvider.getApplicationUrl());

        String text = mailBuilder.buildMailBody("application_cancellation_request", model, LOCALE);

        List<String> recipients = RecipientUtil.getMailAddresses(recipientService.getRecipientsWithNotificationType(MailNotification.NOTIFICATION_OFFICE));
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.application.cancellationRequest"), text);
    }


    @Override
    public void sendOvertimeNotification(Overtime overtime, OvertimeComment overtimeComment) {

        Map<String, Object> model = new HashMap<>();
        model.put("overtime", overtime);
        model.put("comment", overtimeComment);
        model.put(MODEL_APPLICATION_URL, mailOptionProvider.getApplicationUrl());

        String textOffice = mailBuilder.buildMailBody("overtime_office", model, LOCALE);
        List<String> recipients = RecipientUtil.getMailAddresses(recipientService.getRecipientsWithNotificationType(MailNotification.OVERTIME_NOTIFICATION_OFFICE));
        mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.overtime.created"), textOffice);
    }


    @Override
    public void sendRemindForWaitingApplicationsReminderNotification(List<Application> waitingApplications) {

        /**
         * whats happening here?
         *
         * application a
         * person p
         *
         * map application to list of boss/department head
         * a_1 -> (p_1, p_2); a_2 -> (p_1, p_3)
         *
         * collect list of application grouped by boss/department head
         * p_1 -> (a_1, a_2); p_2 -> (a_1); (p_3 -> a_2)
         *
         * See: http://stackoverflow.com/questions/33086686/java-8-stream-collect-and-group-by-objects-that-map-to-multiple-keys
         */
        Map<Person, List<Application>> applicationsPerRecipient = waitingApplications.stream()
                .flatMap(application ->
                            recipientService.getRecipientsForAllowAndRemind(application)
                            .stream()
                            .map(person -> new AbstractMap.SimpleEntry<>(person, application)))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        for (Map.Entry<Person, List<Application>> entry : applicationsPerRecipient.entrySet()) {

            List<Application> applications = entry.getValue();
            Person recipient = entry.getKey();

            Map<String, Object> model = new HashMap<>();
            model.put("applicationList", applications);
            model.put("recipient", recipient);
            model.put(MODEL_APPLICATION_URL, mailOptionProvider.getApplicationUrl());

            String msg = mailBuilder.buildMailBody("cron_remind", model, LOCALE);
            List<String> recipients = RecipientUtil.getMailAddresses(recipient);

            mailSender.sendEmail(mailOptionProvider.getSender(), recipients, getTranslation("subject.application.cronRemind"), msg);
        }
    }
}
