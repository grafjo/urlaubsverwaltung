package org.synyx.urlaubsverwaltung.person;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class PersonRepositoryIT extends TestContainersBase {

    @Autowired
    private PersonRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    void countPersonByPermissionsIsNot() {

        final Person marlene = createPerson("marlene", "Muster", "Marlene", "muster@example.org", List.of(USER, INACTIVE));
        personService.create(marlene);

        final Person peter = createPerson("peter", "Muster", "Peter", "peter@example.org", List.of(USER, OFFICE));
        personService.create(peter);

        final Person bettina = createPerson("bettina", "Muster", "bettina", "bettina@example.org", List.of(USER));
        personService.create(bettina);

        final int countOfActivePersons = sut.countByPermissionsNotContaining(INACTIVE);
        assertThat(countOfActivePersons).isEqualTo(2);
    }

    @Test
    void ensureToFindPersonsWithRoleWithoutTheId() {

        final Person marlene = createPerson("marlene", "Muster", "Marlene", "muster@example.org", List.of(USER, INACTIVE));
        personService.create(marlene);

        final Person peter = createPerson("peter", "Muster", "Peter", "peter@example.org", List.of(USER, OFFICE));
        personService.create(peter);

        final Person simone = createPerson("simone", "Muster", "Peter", "simone@example.org", List.of(USER, OFFICE));
        personService.create(simone);

        final Person bettina = createPerson("bettina", "Muster", "bettina", "bettina@example.org", List.of(USER, OFFICE));
        final Person savedBettina = personService.create(bettina);

        final Integer id = savedBettina.getId();
        final int countOfActivePersons = sut.countByPermissionsContainingAndIdNotIn(OFFICE, List.of(id));
        assertThat(countOfActivePersons).isEqualTo(2);
    }

    @Test
    void findByPersonByPermissionsNotContaining() {

        final Person marlene = createPerson("marlene", "Muster", "Marlene", "muster@example.org", List.of(USER, INACTIVE));
        personService.create(marlene);

        final Person peter = createPerson("peter", "Muster", "Peter", "peter@example.org", List.of(USER, OFFICE));
        personService.create(peter);

        final Person bettina = createPerson("bettina", "Muster", "bettina", "bettina@example.org", List.of(USER));
        personService.create(bettina);

        final List<PersonEntity> notInactivePersons = sut.findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(INACTIVE);
        assertThat(notInactivePersons).containsExactly(toPersonEntity(bettina), toPersonEntity(peter));
    }

    @Test
    void ensureFindByPersonByPermissionsNotContainingOrderingIsCorrect() {

        final Person xenia = createPerson("xenia", "Basta", "xenia", "xenia@example.org", List.of(USER));
        personService.create(xenia);

        final Person peter = createPerson("peter", "Muster", "Peter", "peter@example.org", List.of(USER, OFFICE));
        personService.create(peter);

        final Person bettina = createPerson("bettina", "Muster", "bettina", "bettina@example.org", List.of(USER));
        personService.create(bettina);

        final List<PersonEntity> notInactivePersons = sut.findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(INACTIVE);
        assertThat(notInactivePersons).containsExactly(toPersonEntity(bettina), toPersonEntity(peter), toPersonEntity(xenia));
    }

    @Test
    void findByPersonByPermissionsContaining() {

        final Person marlene = createPerson("marlene", "Muster", "Marlene", "muster@example.org", List.of(USER, INACTIVE));
        personService.create(marlene);

        final Person peter = createPerson("peter", "Muster", "Peter", "peter@example.org", List.of(USER, OFFICE));
        personService.create(peter);

        final Person bettina = createPerson("bettina", "Muster", "bettina", "bettina@example.org", List.of(USER));
        personService.create(bettina);

        final List<PersonEntity> personsWithOfficeRole = sut.findByPermissionsContainingOrderByFirstNameAscLastNameAsc(OFFICE);
        assertThat(personsWithOfficeRole).containsExactly(toPersonEntity(peter));
    }

    @Test
    void ensureFindByPersonByPermissionsContainingOrderingIsCorrect() {

        final Person xenia = createPerson("xenia", "Basta", "xenia", "xenia@example.org", List.of(USER));
        personService.create(xenia);

        final Person peter = createPerson("peter", "Muster", "Peter", "peter@example.org", List.of(USER));
        personService.create(peter);

        final Person bettina = createPerson("bettina", "Muster", "bettina", "bettina@example.org", List.of(USER));
        personService.create(bettina);

        final List<PersonEntity> personsWithUserRole = sut.findByPermissionsContainingOrderByFirstNameAscLastNameAsc(USER);
        assertThat(personsWithUserRole).containsExactly(toPersonEntity(bettina), toPersonEntity(peter), toPersonEntity(xenia));
    }

    @Test
    void ensureFindByPersonByPermissionsContainingAndNotContaining() {

        final Person marlene = createPerson("marlene", "Muster", "Marlene", "muster@example.org", List.of(USER, OFFICE, INACTIVE));
        personService.create(marlene);

        final Person peter = createPerson("peter", "Muster", "Peter", "peter@example.org", List.of(USER, OFFICE));
        personService.create(peter);

        final Person bettina = createPerson("bettina", "Muster", "bettina", "bettina@example.org", List.of(USER));
        personService.create(bettina);

        final List<PersonEntity> personsWithOfficeRole = sut.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(OFFICE, INACTIVE);
        assertThat(personsWithOfficeRole).containsExactly(toPersonEntity(peter));
    }

    @Test
    void ensureFindByPersonByPermissionsContainingAndNotContainingOrderingIsCorrect() {

        final Person xenia = createPerson("xenia", "Basta", "xenia", "xenia@example.org", List.of(USER));
        personService.create(xenia);

        final Person peter = createPerson("peter", "Muster", "Peter", "peter@example.org", List.of(USER));
        personService.create(peter);

        final Person bettina = createPerson("bettina", "Muster", "bettina", "bettina@example.org", List.of(USER));
        personService.create(bettina);

        final List<PersonEntity> personsWithUserRole = sut.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(USER, INACTIVE);
        assertThat(personsWithUserRole).containsExactly(toPersonEntity(bettina), toPersonEntity(peter), toPersonEntity(xenia));
    }

    @Test
    void ensureFindByPersonByPermissionsNotContainingAndContainingNotification() {

        final Person marlene = createPerson("marlene", "Muster", "Marlene", "muster@example.org", List.of(USER, OFFICE, INACTIVE));
        marlene.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.create(marlene);

        final Person peter = createPerson("peter", "Muster", "Peter", "peter@example.org", List.of(USER, OFFICE));
        peter.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.create(peter);

        final Person bettina = createPerson("bettina", "Muster", "bettina", "bettina@example.org", List.of(USER));
        bettina.setNotifications(List.of(NOTIFICATION_USER));
        personService.create(bettina);

        final List<PersonEntity> personsWithOfficeRole = sut.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(INACTIVE, NOTIFICATION_OFFICE);
        assertThat(personsWithOfficeRole).containsExactly(toPersonEntity(peter));
    }

    @Test
    void ensureFindByPersonByPermissionsContainingAndContainingNotificationsOrderingIsCorrect() {

        final Person xenia = createPerson("xenia", "Basta", "xenia", "xenia@example.org", List.of(USER));
        xenia.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.create(xenia);

        final Person peter = createPerson("peter", "Muster", "Peter", "peter@example.org", List.of(USER));
        peter.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.create(peter);

        final Person bettina = createPerson("bettina", "Muster", "bettina", "bettina@example.org", List.of(USER));
        bettina.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.create(bettina);

        final List<PersonEntity> personsWithUserRole = sut.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(INACTIVE, NOTIFICATION_OFFICE);
        assertThat(personsWithUserRole).containsExactly(toPersonEntity(bettina), toPersonEntity(peter), toPersonEntity(xenia));
    }

    @Test
    void ensureFindByPermissionsNotContainingAndByNiceNameContainingIgnoreCase() {

        final Person xenia = createPerson("username_1", "Basta", "xenia", "xenia@example.org", List.of(USER));
        personService.create(xenia);

        final Person peter = createPerson("username_2", "Muster", "Peter", "peter@example.org", List.of(USER));
        personService.create(peter);

        final Person mustafa = createPerson("username_3", "Tunichtgut", "Mustafa", "mustafa@example.org", List.of(INACTIVE));
        personService.create(mustafa);

        final Person rosamund = createPerson("username_4", "Hatgoldimmund", "Rosamund", "rosamund@example.org", List.of(USER));
        personService.create(rosamund);

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final Page<PersonEntity> actual = sut.findByPermissionsNotContainingAndByNiceNameContainingIgnoreCase(INACTIVE, "mu", pageRequest);

        assertThat(actual.getContent()).containsExactly(toPersonEntity(peter), toPersonEntity(rosamund));
    }

    @Test
    void ensureFindByPermissionsContainingAndNiceNameContainingIgnoreCase() {

        final Person xenia = createPerson("username_1", "Basta", "xenia", "xenia@example.org", List.of(USER));
        personService.create(xenia);

        final Person peter = createPerson("username_2", "Muster", "Peter", "peter@example.org", List.of(USER));
        personService.create(peter);

        final Person mustafa = createPerson("username_3", "Tunichtgut", "Mustafa", "mustafa@example.org", List.of(INACTIVE));
        personService.create(mustafa);

        final Person rosamund = createPerson("username_4", "Hatgoldimmund", "Rosamund", "rosamund@example.org", List.of(USER));
        personService.create(rosamund);

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final Page<PersonEntity> actual = sut.findByPermissionsContainingAndNiceNameContainingIgnoreCase(INACTIVE, "mu", pageRequest);

        assertThat(actual.getContent()).containsExactly(toPersonEntity(mustafa));
    }

    @NotNull
    private static Person createPerson(String username, String lastName, String firstName, String email, List<Role> permissions) {

        final Person person = new Person(username, lastName, firstName, email);
        person.setPermissions(permissions);
        return person;
    }

    private static PersonEntity toPersonEntity(Person person) {
        final PersonEntity entity = new PersonEntity();
        entity.setId(person.getId());
        entity.setUsername(person.getUsername());
        entity.setPassword(person.getPassword());
        entity.setLastName(person.getLastName());
        entity.setFirstName(person.getFirstName());
        entity.setEmail(person.getEmail());
        entity.setPermissions(person.getPermissions());
        entity.setNotifications(person.getNotifications());

        return entity;
    }
}



