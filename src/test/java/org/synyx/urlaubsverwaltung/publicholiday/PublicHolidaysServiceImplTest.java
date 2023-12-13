package org.synyx.urlaubsverwaltung.publicholiday;

import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.of;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN_MUENCHEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BERLIN;

@ExtendWith(MockitoExtension.class)
class PublicHolidaysServiceImplTest {

    private PublicHolidaysService sut;

    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new PublicHolidaysServiceImpl(settingsService, Map.of("de", getHolidayManager()));
    }

    @Test
    void ensureCorrectWorkingDurationForWorkDay() {

        when(settingsService.getSettings()).thenReturn(new Settings());
        final LocalDate localDate = of(2013, Month.NOVEMBER, 27);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(localDate, GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForPublicHoliday() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 25), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ZERO));
    }

    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.FULL);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 24), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ONE));
    }

    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.FULL);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ONE));
    }

    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfMorning() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.MORNING);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 24), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.valueOf(0.5)));
    }

    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfNoon() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.NOON);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.valueOf(0.5)));
    }

    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfZero() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.ZERO);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 24), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ZERO));
    }

    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfZero() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.ZERO);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ZERO));
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, AUGUST, 15), GERMANY_BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, AUGUST, 15), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, DECEMBER, 15), GERMANY_BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsFullForCorpusChristi() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, MAY, 30), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsFullForAssumptionDayInBayernMunich() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBerlin() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBadenWuerttemberg() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsForChristmasEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, DECEMBER, 24), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.NOON));
    }

    @Test
    void ensureGetDayLengthReturnsForNewYearsEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.NOON));
    }

    @Test
    void ensureGetPublicHolidaysReturnsForNewYearsEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final List<PublicHoliday> publicHolidays = sut.getPublicHolidays(of(2019, DECEMBER, 30), of(2019, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(publicHolidays)
            .hasSize(1)
            .extracting(p -> p.dayLength()).containsExactly(DayLength.NOON);
    }

    // CHRISTMAS EVE
    //

    @Test
    void ensurePublicHolidaysWithWorkingTimeSettingsForChristmasEveMorning() {

        final LocalDate christmasEveDate = LocalDate.of(2022, DECEMBER, 24);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setWorkingDurationForChristmasEve(DayLength.MORNING);

        final Optional<PublicHoliday> actual = sut.getPublicHoliday(christmasEveDate, GERMANY_BADEN_WUERTTEMBERG, workingTimeSettings);

        assertThat(actual).isPresent();
        assertThat(actual.get()).satisfies(publicHoliday -> {
            assertThat(publicHoliday.date()).isEqualTo(christmasEveDate);
            assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.NOON);
        });
    }

    @Test
    void ensurePublicHolidaysWithWorkingTimeSettingsForChristmasEveNoon() {

        final LocalDate christmasEveDate = LocalDate.of(2022, DECEMBER, 24);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setWorkingDurationForChristmasEve(DayLength.NOON);

        final Optional<PublicHoliday> actual = sut.getPublicHoliday(christmasEveDate, GERMANY_BADEN_WUERTTEMBERG, workingTimeSettings);

        assertThat(actual).isPresent();
        assertThat(actual.get()).satisfies(publicHoliday -> {
            assertThat(publicHoliday.date()).isEqualTo(christmasEveDate);
            assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.MORNING);
        });
    }

    @Test
    void ensurePublicHolidaysWithWorkingTimeSettingsForChristmasEveFull() {

        final LocalDate christmasEveDate = LocalDate.of(2022, DECEMBER, 24);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setWorkingDurationForChristmasEve(DayLength.FULL);

        final Optional<PublicHoliday> actual = sut.getPublicHoliday(christmasEveDate, GERMANY_BADEN_WUERTTEMBERG, workingTimeSettings);

        assertThat(actual).isPresent();
        assertThat(actual.get()).satisfies(publicHoliday -> {
            assertThat(publicHoliday.date()).isEqualTo(christmasEveDate);
            assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.ZERO);
        });
    }

    @Test
    void ensurePublicHolidaysWithWorkingTimeSettingsForChristmasEveZero() {

        final LocalDate christmasEveDate = LocalDate.of(2022, DECEMBER, 24);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setWorkingDurationForChristmasEve(DayLength.ZERO);

        final Optional<PublicHoliday> actual = sut.getPublicHoliday(christmasEveDate, GERMANY_BADEN_WUERTTEMBERG, workingTimeSettings);

        assertThat(actual).isPresent();
        assertThat(actual.get()).satisfies(publicHoliday -> {
            assertThat(publicHoliday.date()).isEqualTo(christmasEveDate);
            assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL);
        });
    }

    // NEW YEARS EVE
    //

    @Test
    void ensurePublicHolidaysWithWorkingTimeSettingsForNewYearsEveMorning() {

        final LocalDate newYearsEveDate = LocalDate.of(2022, DECEMBER, 31);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setWorkingDurationForNewYearsEve(DayLength.MORNING);

        final Optional<PublicHoliday> actual = sut.getPublicHoliday(newYearsEveDate, GERMANY_BADEN_WUERTTEMBERG, workingTimeSettings);

        assertThat(actual).isPresent();
        assertThat(actual.get()).satisfies(publicHoliday -> {
            assertThat(publicHoliday.date()).isEqualTo(newYearsEveDate);
            assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.NOON);
        });
    }

    @Test
    void ensurePublicHolidaysWithWorkingTimeSettingsForNewYearsEveNoon() {

        final LocalDate newYearsEveDate = LocalDate.of(2022, DECEMBER, 31);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setWorkingDurationForNewYearsEve(DayLength.NOON);

        final Optional<PublicHoliday> actual = sut.getPublicHoliday(newYearsEveDate, GERMANY_BADEN_WUERTTEMBERG, workingTimeSettings);

        assertThat(actual).isPresent();
        assertThat(actual.get()).satisfies(publicHoliday -> {
            assertThat(publicHoliday.date()).isEqualTo(newYearsEveDate);
            assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.MORNING);
        });
    }

    @Test
    void ensurePublicHolidaysWithWorkingTimeSettingsForNewYearsEveFull() {

        final LocalDate newYearsEveDate = LocalDate.of(2022, DECEMBER, 31);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setWorkingDurationForNewYearsEve(DayLength.FULL);

        final Optional<PublicHoliday> actual = sut.getPublicHoliday(newYearsEveDate, GERMANY_BADEN_WUERTTEMBERG, workingTimeSettings);

        assertThat(actual).isPresent();
        assertThat(actual.get()).satisfies(publicHoliday -> {
            assertThat(publicHoliday.date()).isEqualTo(newYearsEveDate);
            assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.ZERO);
        });
    }

    @Test
    void ensurePublicHolidaysWithWorkingTimeSettingsForNewYearsEveZero() {

        final LocalDate newYearsEveDate = LocalDate.of(2022, DECEMBER, 31);

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setWorkingDurationForNewYearsEve(DayLength.ZERO);

        final Optional<PublicHoliday> actual = sut.getPublicHoliday(newYearsEveDate, GERMANY_BADEN_WUERTTEMBERG, workingTimeSettings);

        assertThat(actual).isPresent();
        assertThat(actual.get()).satisfies(publicHoliday -> {
            assertThat(publicHoliday.date()).isEqualTo(newYearsEveDate);
            assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL);
        });
    }


    private HolidayManager getHolidayManager() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL url = cl.getResource("Holidays_de.xml");
        return HolidayManager.getInstance(ManagerParameters.create(url));
    }
}
