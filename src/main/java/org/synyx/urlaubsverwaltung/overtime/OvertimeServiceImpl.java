package org.synyx.urlaubsverwaltung.overtime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.Duration.ZERO;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.CREATED;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.EDITED;

/**
 * @since 2.11.0
 */
@Transactional
@Service
class OvertimeServiceImpl implements OvertimeService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final OvertimeRepository overtimeRepository;
    private final OvertimeCommentRepository overtimeCommentRepository;
    private final ApplicationService applicationService;
    private final OvertimeMailService overtimeMailService;
    private final Clock clock;

    @Autowired
    public OvertimeServiceImpl(OvertimeRepository overtimeRepository, OvertimeCommentRepository overtimeCommentRepository,
                               ApplicationService applicationService, OvertimeMailService overtimeMailService,
                               Clock clock) {

        this.overtimeRepository = overtimeRepository;
        this.overtimeCommentRepository = overtimeCommentRepository;
        this.applicationService = applicationService;
        this.overtimeMailService = overtimeMailService;
        this.clock = clock;
    }

    @Override
    public List<Overtime> getOvertimeRecordsForPerson(Person person) {
        return overtimeRepository.findByPerson(person);
    }

    @Override
    public List<Overtime> getOvertimeRecordsForPersonAndYear(Person person, int year) {
        return overtimeRepository.findByPersonAndPeriod(person, DateUtil.getFirstDayOfYear(year), DateUtil.getLastDayOfYear(year));
    }

    @Override
    public Overtime record(Overtime overtime, Optional<String> comment, Person author) {

        final boolean isNewOvertime = overtime.getId() == null;

        // save overtime record
        overtime.onUpdate();
        overtimeRepository.save(overtime);

        // save comment
        final OvertimeCommentAction action = isNewOvertime ? CREATED : EDITED;
        OvertimeComment overtimeComment = new OvertimeComment(author, overtime, action, clock);
        comment.ifPresent(overtimeComment::setText);

        overtimeCommentRepository.save(overtimeComment);

        overtimeMailService.sendOvertimeNotification(overtime, overtimeComment);

        LOG.info("{} overtime record: {}", isNewOvertime ? "Created" : "Updated", overtime);

        return overtime;
    }

    @Override
    public Optional<Overtime> getOvertimeById(Integer id) {
        return overtimeRepository.findById(id);
    }

    @Override
    public List<OvertimeComment> getCommentsForOvertime(Overtime overtime) {
        return overtimeCommentRepository.findByOvertime(overtime);
    }

    @Override
    public Duration getTotalOvertimeForPersonAndYear(Person person, int year) {
        Assert.isTrue(year > 0, "Year must be a valid number.");

        final List<Overtime> overtimeRecords = getOvertimeRecordsForPersonAndYear(person, year);

        Duration totalOvertime = ZERO;
        for (Overtime record : overtimeRecords) {
            totalOvertime = totalOvertime.plus(record.getDuration());
        }

        return totalOvertime;
    }

    @Override
    public Duration getLeftOvertimeForPerson(Person person) {
        final Duration totalOvertime = getTotalOvertimeForPerson(person);
        final Duration overtimeReduction = applicationService.getTotalOvertimeReductionOfPerson(person);

        return totalOvertime.minus(overtimeReduction);
    }

    private Duration getTotalOvertimeForPerson(Person person) {

        final Long totalOvertime = Optional.ofNullable(overtimeRepository.calculateTotalHoursForPerson(person))
            .map(aDouble -> Math.round(aDouble * 60))
            .orElse(0L);
        return Duration.of(totalOvertime, ChronoUnit.MINUTES);

    }
}
