
package org.synyx.urlaubsverwaltung.calendar;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.calendar.workingtime.WorkingTime;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author  Aljona Murygina
 */
public class OwnCalendarServiceTest {

    private OwnCalendarService instance;
    private JollydayCalendar jollydayCalendar;
    private Application application;

    public OwnCalendarServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        jollydayCalendar = new JollydayCalendar();
        instance = new OwnCalendarService(jollydayCalendar);

        application = new Application();
    }


    @After
    public void tearDown() {
    }


    /**
     * Test of getWeekDays method, of class OwnCalendarService.
     */
    @Test
    public void testGetWorkDays() {

        DateMidnight start = new DateMidnight(2011, 11, 16);
        DateMidnight end = new DateMidnight(2011, 11, 28);

        double returnValue = instance.getWeekDays(start, end);

        assertNotNull(returnValue);
        assertEquals(9.0, returnValue, 0.0);
    }


    /**
     * Test of getWorkDays method, of class OwnCalendarService.
     */
    @Test
    public void testGetVacationDays() {

        // testing for full days
        application.setHowLong(DayLength.FULL);

        // Testing for 2010: 17.12. is Friday, 31.12. is Friday
        // between these dates: 2 public holidays (25., 26.) plus 2*0.5 public holidays (24., 31.)
        // total days: 14
        // netto days: 10 (considering public holidays and weekends)

        DateMidnight start = new DateMidnight(2010, 12, 17);
        DateMidnight end = new DateMidnight(2010, 12, 31);

        BigDecimal returnValue = instance.getWorkDays(application.getHowLong(), start, end);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(10.0).setScale(2), returnValue);

        // Testing for 2009: 17.12. is Thursday, 31.12. ist Thursday
        // between these dates: 2 public holidays (25., 26.) plus 2*0.5 public holidays (24., 31.)
        // total days: 14
        // netto days: 9 (considering public holidays and weekends)

        start = new DateMidnight(2009, 12, 17);
        end = new DateMidnight(2009, 12, 31);

        returnValue = instance.getWorkDays(application.getHowLong(), start, end);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(9.0).setScale(2), returnValue);

        // start date and end date are not in the same year
        start = new DateMidnight(2011, 12, 26);
        end = new DateMidnight(2012, 1, 15);

        returnValue = instance.getWorkDays(application.getHowLong(), start, end);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(13.0).setScale(2), returnValue);

        // Labour Day (1st May)
        start = new DateMidnight(2009, 4, 27);
        end = new DateMidnight(2009, 5, 2);

        returnValue = instance.getWorkDays(application.getHowLong(), start, end);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(4.0).setScale(2), returnValue);

        // start date is Sunday, end date Saturday
        start = new DateMidnight(2011, 1, 2);
        end = new DateMidnight(2011, 1, 8);

        returnValue = instance.getWorkDays(application.getHowLong(), start, end);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(4.0).setScale(2), returnValue);

        // testing for half days
        application.setHowLong(DayLength.MORNING);

        start = new DateMidnight(2011, 1, 4);
        end = new DateMidnight(2011, 1, 8);

        returnValue = instance.getWorkDays(application.getHowLong(), start, end);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(1.5).setScale(2), returnValue);
    }


    @Test
    public void testGetPersonalWorkDays() {

        List<Integer> list = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.WEDNESDAY,
                DateTimeConstants.FRIDAY, DateTimeConstants.SATURDAY);

        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(list, DayLength.FULL);

        // 1 week (MON - SUN)
        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 25);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 1);

        BigDecimal returnValue = instance.getPersonalWorkDays(startDate, endDate, workingTime);

        assertEquals(BigDecimal.valueOf(4.0), returnValue);
    }


    @Test
    public void testGetPersonalWorkDaysOneDay() {

        List<Integer> list = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.WEDNESDAY,
                DateTimeConstants.FRIDAY, DateTimeConstants.SATURDAY);

        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(list, DayLength.FULL);

        // saturday
        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 30);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 30);

        BigDecimal returnValue = instance.getPersonalWorkDays(startDate, endDate, workingTime);

        assertEquals(BigDecimal.valueOf(1.0), returnValue);

        // tuesday
        startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 26);
        endDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 26);

        returnValue = instance.getPersonalWorkDays(startDate, endDate, workingTime);

        assertEquals(BigDecimal.valueOf(0), returnValue);
    }
}
