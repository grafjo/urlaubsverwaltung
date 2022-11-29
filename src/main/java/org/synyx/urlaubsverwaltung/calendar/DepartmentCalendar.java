package org.synyx.urlaubsverwaltung.calendar;

import jakarta.persistence.GenerationType;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.annotations.OnDelete;
import org.hibernate.validator.constraints.Length;
import org.synyx.urlaubsverwaltung.person.Person;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import java.time.Period;

import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
class DepartmentCalendar {

    private static final int SECRET_LENGTH = 32;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "department_id")
    @OnDelete(action = CASCADE)
    private Integer departmentId;

    @NotNull
    @OneToOne
    private Person person;

    @Length(min = SECRET_LENGTH, max = SECRET_LENGTH)
    private String secret;

    @Convert(converter = PeriodConverter.class)
    private Period calendarPeriod;

    public DepartmentCalendar() {
        // for hibernate - do not use this
    }

    DepartmentCalendar(Integer departmentId, Person person) {
        generateSecret();
        this.departmentId = departmentId;
        this.person = person;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void generateSecret() {
        secret = RandomStringUtils.randomAlphanumeric(SECRET_LENGTH);
    }

    public String getSecret() {
        return secret;
    }

    public Period getCalendarPeriod() {
        return calendarPeriod;
    }

    public void setCalendarPeriod(Period calendarPeriod) {
        this.calendarPeriod = calendarPeriod;
    }
}
