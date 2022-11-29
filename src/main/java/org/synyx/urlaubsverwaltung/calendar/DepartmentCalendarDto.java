package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static org.synyx.urlaubsverwaltung.calendar.CalendarPeriodViewType.HALF_YEAR;

@Validated
public class DepartmentCalendarDto {

    @NotNull
    private int personId;
    @NotNull
    private int departmentId;
    @Size(min = 1)
    private String departmentName;
    @Size(min = 1)
    private String calendarUrl;
    @NotNull
    private CalendarPeriodViewType calendarPeriod = HALF_YEAR;

    /**
     * Whether this calendar is currently active/visible in the view or not.
     * Do not confuse this with app state like valid or invalid calendar.
     */
    private boolean active;

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getCalendarUrl() {
        return calendarUrl;
    }

    public void setCalendarUrl(String calendarUrl) {
        this.calendarUrl = calendarUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public CalendarPeriodViewType getCalendarPeriod() {
        return calendarPeriod;
    }

    public void setCalendarPeriod(CalendarPeriodViewType calendarPeriod) {
        this.calendarPeriod = calendarPeriod;
    }
}
