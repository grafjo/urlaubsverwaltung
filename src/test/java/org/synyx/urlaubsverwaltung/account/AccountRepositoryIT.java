package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonMapper;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.person.PersonMapper.toPersonEntity;

@SpringBootTest
@Transactional
class AccountRepositoryIT extends TestContainersBase {

    @Autowired
    private AccountRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    void ensureUniqueConstraintOfPersonAndValidFrom() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.create(person);

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final AccountEntity accountEntity = new AccountEntity(toPersonEntity(savedPerson), validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        sut.save(accountEntity);

        final LocalDate validFrom2 = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo2 = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate2 = LocalDate.of(2014, APRIL, 1);
        final AccountEntity accountEntity2 = new AccountEntity(toPersonEntity(savedPerson), validFrom2, validTo2, true, expiryDate2, TEN, TEN, TEN, "comment 2");
        assertThatThrownBy(() -> sut.save(accountEntity2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }


    @Test
    void ensureFindAccountByYearAndPersons() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person savedPerson = personService.create(person);

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);

        final AccountEntity accountToFind = new AccountEntity(toPersonEntity(savedPerson), validFrom, validTo, null, expiryDate, TEN, TEN, TEN, "comment");
        final AccountEntity savedAccountToFind = sut.save(accountToFind);

        final Person otherPerson = new Person("otherPerson", "other", "person", "other@example.org");
        final Person savedOtherPerson = personService.create(otherPerson);
        final AccountEntity otherAccountToFind = new AccountEntity(toPersonEntity(savedOtherPerson), validFrom, validTo, null, expiryDate, TEN, TEN, TEN, "comment");
        final AccountEntity savedOtherAccountToFind = sut.save(otherAccountToFind);

        /* Do not find these accounts */
        final Person personNotInSearch = new Person("personNotInSearch", "notInSearch", "person", "notInSearch@example.org");
        final Person savedPersonNotInSearch = personService.create(personNotInSearch);
        final AccountEntity accountWrongPerson = new AccountEntity(toPersonEntity(savedPersonNotInSearch), validFrom, validTo, null, expiryDate, TEN, TEN, TEN, "comment");
        sut.save(accountWrongPerson);

        final LocalDate validFrom2015 = LocalDate.of(2015, JANUARY, 1);
        final LocalDate validTo2015 = LocalDate.of(2015, DECEMBER, 31);
        final LocalDate expiryDate2015 = LocalDate.of(2015, APRIL, 1);
        final AccountEntity accountWrongYear = new AccountEntity(toPersonEntity(savedPerson), validFrom2015, validTo2015, null, expiryDate2015, TEN, TEN, TEN, "comment");
        sut.save(accountWrongYear);

        assertThat(sut.findAccountByYearAndPersons(2014, List.of(savedPerson, savedOtherPerson)))
            .containsExactly(savedAccountToFind, savedOtherAccountToFind);
    }
}
