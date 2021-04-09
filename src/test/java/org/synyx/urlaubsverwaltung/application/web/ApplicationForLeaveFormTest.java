package org.synyx.urlaubsverwaltung.application.web;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.dao.HolidayReplacementEntity;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

import static java.math.BigDecimal.ONE;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationForLeaveFormTest {

    private final Clock clock = Clock.systemUTC();

    @Test
    void ensureGeneratedFullDayApplicationForLeaveHasCorrectPeriod() {

        final LocalDate startDate = LocalDate.now(clock);
        final LocalDate endDate = startDate.plusDays(3);

        final ApplicationForLeaveForm form = new ApplicationForLeaveForm();

        form.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        form.setDayLength(DayLength.FULL);
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setHolidayReplacements(emptyList());

        final Application application = form.generateApplicationForLeave();

        assertThat(application.getStartDate()).isEqualTo(startDate);
        assertThat(application.getEndDate()).isEqualTo(endDate);
        assertThat(application.getDayLength()).isEqualTo(DayLength.FULL);
    }

    @Test
    void ensureGeneratedHalfDayApplicationForLeaveHasCorrectPeriod() {

        final LocalDate now = LocalDate.now(clock);

        final ApplicationForLeaveForm form = new ApplicationForLeaveForm();
        form.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        form.setDayLength(DayLength.MORNING);
        form.setStartDate(now);
        form.setEndDate(now);
        form.setHolidayReplacements(emptyList());

        final Application application = form.generateApplicationForLeave();
        assertThat(application.getStartDate()).isEqualTo(now);
        assertThat(application.getEndDate()).isEqualTo(now);
        assertThat(application.getDayLength()).isEqualTo(DayLength.MORNING);
    }

    @Test
    void ensureSettingNullHourDurationWithBuilderDoesNotSetHoursAndMinutes() {
        final ApplicationForLeaveForm form = new ApplicationForLeaveForm.Builder()
            .hoursAndMinutes(null)
            .build();

        assertThat(form.getHours()).isNull();
        assertThat(form.getMinutes()).isNull();
    }

    @Test
    void ensureGeneratedApplicationForLeaveWithDecimalOvertimeReduction() {

        final VacationType overtime = TestDataCreator.createVacationType(VacationCategory.OVERTIME);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person holidayReplacement = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final HolidayReplacementDto holidayReplacementDto = new HolidayReplacementDto();
        holidayReplacementDto.setId(33);
        holidayReplacementDto.setPerson(holidayReplacement);

        final ApplicationForLeaveForm form = new ApplicationForLeaveForm();
        form.setPerson(person);
        form.setDayLength(DayLength.FULL);
        form.setAddress("Musterstr. 39");
        form.setComment("Kommentar");
        form.setReason("Deshalb");
        form.setTeamInformed(true);
        form.setVacationType(overtime);
        form.setHours(BigDecimal.valueOf(1.25));
        form.setHolidayReplacements(List.of(holidayReplacementDto));

        final HolidayReplacementEntity expectedReplacementEntity = new HolidayReplacementEntity();
        expectedReplacementEntity.setPerson(holidayReplacement);

        final Application application = form.generateApplicationForLeave();
        assertThat(application.getPerson()).isEqualTo(person);
        assertThat(application.getHolidayReplacements()).contains(expectedReplacementEntity);
        assertThat(application.getDayLength()).isEqualTo(DayLength.FULL);
        assertThat(application.getAddress()).isEqualTo("Musterstr. 39");
        assertThat(application.getReason()).isEqualTo("Deshalb");
        assertThat(application.getVacationType().getMessageKey()).isEqualTo(overtime.getMessageKey());
        assertThat(application.getHours()).isEqualTo(Duration.ofMinutes(75));
        assertThat(application.isTeamInformed()).isTrue();
    }

    @Test
    void ensureGeneratedApplicationForLeaveWithHoursAndMinutesOvertimeReduction() {

        final VacationType overtime = TestDataCreator.createVacationType(VacationCategory.OVERTIME);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person holidayReplacement = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final HolidayReplacementDto holidayReplacementDto = new HolidayReplacementDto();
        holidayReplacementDto.setId(33);
        holidayReplacementDto.setPerson(holidayReplacement);

        final ApplicationForLeaveForm form = new ApplicationForLeaveForm();
        form.setPerson(person);
        form.setDayLength(DayLength.FULL);
        form.setAddress("Musterstr. 39");
        form.setComment("Kommentar");
        form.setReason("Deshalb");
        form.setTeamInformed(true);
        form.setVacationType(overtime);
        form.setHours(BigDecimal.valueOf(1));
        form.setMinutes(15);
        form.setHolidayReplacements(List.of(holidayReplacementDto));

        final HolidayReplacementEntity expectedReplacementEntity = new HolidayReplacementEntity();
        expectedReplacementEntity.setPerson(holidayReplacement);

        final Application application = form.generateApplicationForLeave();
        assertThat(application.getPerson()).isEqualTo(person);
        assertThat(application.getHolidayReplacements()).contains(expectedReplacementEntity);
        assertThat(application.getDayLength()).isEqualTo(DayLength.FULL);
        assertThat(application.getAddress()).isEqualTo("Musterstr. 39");
        assertThat(application.getReason()).isEqualTo("Deshalb");
        assertThat(application.getVacationType().getMessageKey()).isEqualTo(overtime.getMessageKey());
        assertThat(application.getHours()).isEqualTo(Duration.ofMinutes(75));
        assertThat(application.isTeamInformed()).isTrue();
    }

    @Test
    void ensureGeneratedApplicationForLeaveHasNullHoursForOtherVacationTypeThanOvertime() {

        Consumer<VacationType> assertHoursAreNotSet = (type) -> {
            final ApplicationForLeaveForm form = new ApplicationForLeaveForm();
            form.setVacationType(type);
            form.setHours(ONE);
            form.setHolidayReplacements(emptyList());

            final Application application = form.generateApplicationForLeave();
            assertThat(application.getHours()).isNull();
        };

        final VacationType holiday = TestDataCreator.createVacationType(VacationCategory.HOLIDAY);
        final VacationType specialLeave = TestDataCreator.createVacationType(VacationCategory.SPECIALLEAVE);
        final VacationType unpaidLeave = TestDataCreator.createVacationType(VacationCategory.UNPAIDLEAVE);

        assertHoursAreNotSet.accept(holiday);
        assertHoursAreNotSet.accept(specialLeave);
        assertHoursAreNotSet.accept(unpaidLeave);
    }

    @Test
    void ensureBuilderSetsAllPropertiesCorrectly() {

        final Person person = new Person();
        final Person holidayReplacement = new Person();

        final LocalDate startDate = LocalDate.now().minusDays(10);
        final Time startTime = Time.valueOf(LocalTime.now().minusHours(5));

        final LocalDate endDate = LocalDate.now().minusDays(2);
        final Time endTime = Time.valueOf(LocalTime.now().minusHours(7));

        final VacationType vacationType = new VacationType();

        final HolidayReplacementDto holidayReplacementDto = new HolidayReplacementDto();

        ApplicationForLeaveForm form = new ApplicationForLeaveForm.Builder()
            .person(person)
            .startDate(startDate)
            .startTime(startTime)
            .endDate(endDate)
            .endTime(endTime)
            .vacationType(vacationType)
            .dayLength(DayLength.ZERO)
            .hoursAndMinutes(Duration.ofMinutes(75))
            .reason("Good one.")
            .holidayReplacements(List.of(holidayReplacementDto))
            .address("Gartenstrasse 67")
            .teamInformed(true)
            .comment("Welcome!")
            .build();

        assertThat(form.getPerson()).isEqualTo(person);
        assertThat(form.getStartDate()).isEqualTo(startDate);
        assertThat(form.getStartTime()).isEqualTo(startTime);
        assertThat(form.getEndDate()).isEqualTo(endDate);
        assertThat(form.getEndTime()).isEqualTo(endTime);
        assertThat(form.getVacationType()).isEqualTo(vacationType);
        assertThat(form.getDayLength()).isEqualTo(DayLength.ZERO);
        assertThat(form.getHours()).isEqualTo(ONE);
        assertThat(form.getMinutes()).isEqualTo(15);
        assertThat(form.getReason()).isEqualTo("Good one.");
        assertThat(form.getHolidayReplacements()).contains(holidayReplacementDto);
        assertThat(form.getAddress()).isEqualTo("Gartenstrasse 67");
        assertThat(form.isTeamInformed()).isTrue();
        assertThat(form.getComment()).isEqualTo("Welcome!");
    }

    @Test
    void ensureEmptyStartDateIsoValue() {

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();

        applicationForLeaveForm.setStartDate(null);

        assertThat(applicationForLeaveForm.getStartDateIsoValue()).isEmpty();
    }

    @Test
    void ensureStartDateIsoValue() {

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();

        applicationForLeaveForm.setStartDate(LocalDate.parse("2020-10-30"));

        assertThat(applicationForLeaveForm.getStartDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureEmptyEndDateIsoValue() {

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();

        applicationForLeaveForm.setEndDate(null);

        assertThat(applicationForLeaveForm.getEndDateIsoValue()).isEmpty();
    }

    @Test
    void ensureEndDateIsoValue() {

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();

        applicationForLeaveForm.setEndDate(LocalDate.parse("2020-10-30"));

        assertThat(applicationForLeaveForm.getEndDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureCorrectDurationCalculation() {
        assertThat(formWithOvertime(ONE, 15).getOvertimeReduction()).isEqualTo(Duration.ofMinutes(75));
        assertThat(formWithOvertime(BigDecimal.valueOf(1.25), 0).getOvertimeReduction()).isEqualTo(Duration.ofMinutes(75));
        assertThat(formWithOvertime(BigDecimal.valueOf(1.25), null).getOvertimeReduction()).isEqualTo(Duration.ofMinutes(75));
        assertThat(formWithOvertime(null, 75).getOvertimeReduction()).isEqualTo(Duration.ofMinutes(75));
        assertThat(formWithOvertime(null, null).getOvertimeReduction()).isNull();
        assertThat(formWithOvertime(ONE, 75).getOvertimeReduction()).isEqualTo(Duration.ofMinutes(135));
    }

    @Test
    void toStringTest() {

        final Person person = new Person();
        final Person holidayReplacement = new Person();

        final LocalDate startDate = LocalDate.MIN;
        final Time startTime = Time.valueOf(LocalTime.MIN);

        final LocalDate endDate = LocalDate.MAX;
        final Time endTime = Time.valueOf(LocalTime.MAX);

        final VacationType vacationType = new VacationType();

        final HolidayReplacementDto replacementDto = new HolidayReplacementDto();
        replacementDto.setPerson(holidayReplacement);

        final ApplicationForLeaveForm form = new ApplicationForLeaveForm.Builder()
            .person(person)
            .startDate(startDate)
            .startTime(startTime)
            .endDate(endDate)
            .endTime(endTime)
            .vacationType(vacationType)
            .dayLength(DayLength.ZERO)
            .hoursAndMinutes(Duration.ZERO)
            .reason("Reason")
            .holidayReplacements(List.of(replacementDto))
            .address("Address")
            .teamInformed(true)
            .comment("Comment")
            .build();

        assertThat(form).hasToString("ApplicationForLeaveForm{person=Person{id='null'}, startDate=-999999999-01-01, " +
            "startTime=00:00:00, endDate=+999999999-12-31, endTime=23:59:59, vacationType=VacationType{" +
            "category=null, messageKey='null'}, dayLength=ZERO, hours=0, minutes=0, holidayReplacements=[" +
            "HolidayReplacementDto{id=null, person=Person{id='null'}, note='null'}], address='Address', " +
            "teamInformed=true}");
    }

    private ApplicationForLeaveForm formWithOvertime(BigDecimal hours, Integer minutes) {
        final ApplicationForLeaveForm form = new ApplicationForLeaveForm();
        form.setHours(hours);
        form.setMinutes(minutes);
        return form;
    }
}
