package org.synyx.urlaubsverwaltung.calendar;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.validator.constraints.Length;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Period;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
class PersonCalendar {

    private static final int SECRET_LENGTH = 32;

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "person_calendar_generator")
    @SequenceGenerator(name = "person_calendar_generator", sequenceName = "person_calendar_id_seq")
    private Long id;

    @NotNull
    @OneToOne
    private Person person;

    @NotNull
    @Length(min = SECRET_LENGTH, max = SECRET_LENGTH)
    private String secret;

    @NotNull
    @Convert(converter = PeriodConverter.class)
    private Period calendarPeriod;

    public PersonCalendar() {
        // for hibernate - do not use this
    }

    protected PersonCalendar(Person person) {
        generateSecret();
        this.person = person;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void generateSecret() {
        secret = RandomStringUtils.secure().nextAlphanumeric(SECRET_LENGTH);
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
