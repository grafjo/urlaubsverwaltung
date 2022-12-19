package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPersonEntity;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

    private PersonServiceImpl sut;

    @Mock
    private PersonRepository personRepository;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private WorkingTimeWriteService workingTimeWriteService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Captor
    private ArgumentCaptor<PersonDisabledEvent> personDisabledEventArgumentCaptor;
    @Captor
    private ArgumentCaptor<PersonCreatedEvent> personCreatedEventArgumentCaptor;
    private final ArgumentCaptor<PersonDeletedEvent> personDeletedEventArgumentCaptor = ArgumentCaptor.forClass(PersonDeletedEvent.class);

    @BeforeEach
    void setUp() {
        sut = new PersonServiceImpl(personRepository, accountInteractionService, workingTimeWriteService, applicationEventPublisher);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ensureDefaultAccountAndWorkingTimeCreation() {
        when(personRepository.save(any(PersonEntity.class))).thenReturn(new PersonEntity());

        sut.create("rick", "Grimes", "Rick", "rick@grimes.de", emptyList(), emptyList());
        verify(accountInteractionService).createDefaultAccount(any(Person.class));
        verify(workingTimeWriteService).createDefaultWorkingTime(any(Person.class));
    }

    @Test
    void ensurePersonCreatedEventIsFired() {

        final Person activePerson = createPerson("my person", USER);
        activePerson.setId(1);

        final PersonEntity personEntity = createPersonEntity(activePerson.getUsername(), USER);
        personEntity.setId(1);

        when(personRepository.save(personEntity)).thenReturn(personEntity);

        sut.create(activePerson);

        verify(applicationEventPublisher).publishEvent(personCreatedEventArgumentCaptor.capture());
        assertThat(personCreatedEventArgumentCaptor.getValue().getPersonId())
            .isEqualTo(activePerson.getId());
    }

    @Test
    void ensureCreatedPersonHasCorrectAttributes() {

        final Person person = new Person("rick", "Grimes", "Rick", "rick@grimes.de");
        person.setId(1);
        person.setPermissions(asList(USER, BOSS));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        final PersonEntity personEntity = new PersonEntity();
        personEntity.setId(person.getId());
        personEntity.setUsername(person.getUsername());
        personEntity.setLastName(person.getLastName());
        personEntity.setFirstName(person.getFirstName());
        personEntity.setEmail(person.getEmail());
        personEntity.setPermissions(person.getPermissions());
        personEntity.setNotifications(person.getNotifications());

        when(personRepository.save(any())).thenReturn(personEntity);

        final Person createdPerson = sut.create(person);
        assertThat(createdPerson.getUsername()).isEqualTo("rick");
        assertThat(createdPerson.getFirstName()).isEqualTo("Rick");
        assertThat(createdPerson.getLastName()).isEqualTo("Grimes");
        assertThat(createdPerson.getEmail()).isEqualTo("rick@grimes.de");

        assertThat(createdPerson.getNotifications())
            .hasSize(2)
            .contains(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL);

        assertThat(createdPerson.getPermissions())
            .hasSize(2)
            .contains(USER, BOSS);

        verify(accountInteractionService).createDefaultAccount(createdPerson);
        verify(workingTimeWriteService).createDefaultWorkingTime(createdPerson);
    }

    @Test
    void ensureCreatedPersonIsPersisted() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        final PersonEntity personEntity = new PersonEntity();
        personEntity.setId(person.getId());
        personEntity.setUsername(person.getUsername());
        personEntity.setLastName(person.getLastName());
        personEntity.setFirstName(person.getFirstName());
        personEntity.setEmail(person.getEmail());

        when(personRepository.save(any())).thenReturn(personEntity);

        final Person savedPerson = sut.create(person);
        assertThat(savedPerson).isEqualTo(person);
    }

    @Test
    void ensureNotificationIsSendForCreatedPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);

        final PersonEntity personEntity = new PersonEntity();
        personEntity.setId(person.getId());
        personEntity.setUsername(person.getUsername());
        personEntity.setLastName(person.getLastName());
        personEntity.setFirstName(person.getFirstName());
        personEntity.setEmail(person.getEmail());

        when(personRepository.save(personEntity)).thenReturn(personEntity);

        final Person createdPerson = sut.create(person);

        verify(applicationEventPublisher).publishEvent(personCreatedEventArgumentCaptor.capture());

        final PersonCreatedEvent personCreatedEvent = personCreatedEventArgumentCaptor.getValue();
        assertThat(personCreatedEvent.getSource()).isEqualTo(sut);
        assertThat(personCreatedEvent.getPersonId()).isEqualTo(createdPerson.getId());
        assertThat(personCreatedEvent.getPersonNiceName()).isEqualTo(createdPerson.getNiceName());
    }

    @Test
    void ensureUpdatedPersonIsPersisted() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);

        final PersonEntity personEntity = new PersonEntity();
        personEntity.setId(person.getId());
        personEntity.setUsername(person.getUsername());
        personEntity.setLastName(person.getLastName());
        personEntity.setFirstName(person.getFirstName());
        personEntity.setEmail(person.getEmail());

        when(personRepository.save(personEntity)).thenReturn(personEntity);

        sut.update(person);
        verify(personRepository).save(personEntity);
    }

    @Test
    void ensureThrowsIfPersonToBeUpdatedHasNoID() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(null);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.update(person));
    }

    @Test
    void ensureSaveCallsCorrectDaoMethod() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        final PersonEntity personEntity = new PersonEntity();
        personEntity.setId(person.getId());
        personEntity.setUsername(person.getUsername());
        personEntity.setLastName(person.getLastName());
        personEntity.setFirstName(person.getFirstName());
        personEntity.setEmail(person.getEmail());

        when(personRepository.save(any())).thenReturn(personEntity);

        final Person savedPerson = sut.create(person);
        assertThat(savedPerson).isEqualTo(person);
    }

    @Test
    void ensureGetPersonByIDCallsCorrectDaoMethod() {

        sut.getPersonByID(123);
        verify(personRepository).findById(123);
    }

    @Test
    void ensureGetPersonByLoginCallsCorrectDaoMethod() {
        final String username = "foo";
        sut.getPersonByUsername(username);

        verify(personRepository).findByUsername(username);
    }

    @Test
    void ensureGetPersonByMailAddressDelegatesToRepository() {
        final String mailAddress = "foo@bar.test";
        sut.getPersonByMailAddress(mailAddress);

        verify(personRepository).findByEmail(mailAddress);
    }

    @Test
    void ensureGetActivePersonsReturnsOnlyPersonsThatHaveNotInactiveRole() {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setId(3);
        user.setPermissions(List.of(USER));
        final PersonEntity userEntity = new PersonEntity();
        userEntity.setId(user.getId());
        userEntity.setUsername(user.getUsername());
        userEntity.setLastName(user.getLastName());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setEmail(user.getEmail());
        userEntity.setPermissions(user.getPermissions());

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setId(2);
        boss.setPermissions(asList(USER, BOSS));
        final PersonEntity bossEntity = new PersonEntity();
        bossEntity.setId(boss.getId());
        bossEntity.setUsername(boss.getUsername());
        bossEntity.setLastName(boss.getLastName());
        bossEntity.setFirstName(boss.getFirstName());
        bossEntity.setEmail(boss.getEmail());
        bossEntity.setPermissions(boss.getPermissions());

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setId(1);
        office.setPermissions(asList(USER, BOSS, OFFICE));
        final PersonEntity officeEntity = new PersonEntity();
        officeEntity.setId(office.getId());
        officeEntity.setUsername(office.getUsername());
        officeEntity.setLastName(office.getLastName());
        officeEntity.setFirstName(office.getFirstName());
        officeEntity.setEmail(office.getEmail());
        officeEntity.setPermissions(office.getPermissions());

        when(personRepository.findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(INACTIVE)).thenReturn(List.of(userEntity, bossEntity, officeEntity));

        final List<Person> activePersons = sut.getActivePersons();
        assertThat(activePersons)
            .hasSize(3)
            .contains(user)
            .contains(boss)
            .contains(office);
    }

    @Test
    void ensureGetActivePersonsPage() {

        final Page<PersonEntity> pageEntity = Page.empty();
        final PageRequest pageRequest = PageRequest.of(1, 100);
        final PageableSearchQuery personPageableSearchQuery = new PageableSearchQuery(pageRequest, "name-query");

        when(personRepository.findByPermissionsNotContainingAndByNiceNameContainingIgnoreCase(INACTIVE, "name-query", pageRequest)).thenReturn(pageEntity);

        final Page<Person> actual = sut.getActivePersons(personPageableSearchQuery);
        assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    void ensureGetInactivePersonsPage() {

        final Page<PersonEntity> pageEntity = Page.empty();
        final PageRequest pageRequest = PageRequest.of(1, 100, Sort.by(Sort.Direction.ASC, "firstName"));
        final PageableSearchQuery personPageableSearchQuery = new PageableSearchQuery(pageRequest, "name-query");

        // currently a hard coded pageRequest is used in implementation
        final PageRequest pageRequestInternal = PageRequest.of(1, 100, Sort.Direction.ASC, "firstName", "lastName");
        when(personRepository.findByPermissionsContainingAndNiceNameContainingIgnoreCase(INACTIVE, "name-query", pageRequestInternal)).thenReturn(pageEntity);

        final Page<Person> actual = sut.getInactivePersons(personPageableSearchQuery);
        assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    void ensureGetPersonsByRoleReturnsOnlyPersonsWithTheGivenRole() {

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setId(1);
        boss.setPermissions(asList(USER, BOSS));

        final PersonEntity bossEntity = new PersonEntity();
        bossEntity.setId(boss.getId());
        bossEntity.setUsername(boss.getUsername());
        bossEntity.setLastName(boss.getLastName());
        bossEntity.setFirstName(boss.getFirstName());
        bossEntity.setEmail(boss.getEmail());
        bossEntity.setPermissions(boss.getPermissions());

        final Person bossOffice = new Person("muster", "Muster", "Marlene", "muster@example.org");
        bossOffice.setId(2);
        bossOffice.setPermissions(asList(USER, BOSS, OFFICE));

        final PersonEntity bossOfficeEntity = new PersonEntity();
        bossOfficeEntity.setId(bossOffice.getId());
        bossOfficeEntity.setUsername(bossOffice.getUsername());
        bossOfficeEntity.setLastName(bossOffice.getLastName());
        bossOfficeEntity.setFirstName(bossOffice.getFirstName());
        bossOfficeEntity.setEmail(bossOffice.getEmail());
        bossOfficeEntity.setPermissions(bossOffice.getPermissions());

        when(personRepository.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(BOSS, INACTIVE)).thenReturn(asList(bossEntity, bossOfficeEntity));

        final List<Person> filteredList = sut.getActivePersonsByRole(BOSS);
        assertThat(filteredList)
            .hasSize(2)
            .contains(boss)
            .contains(bossOffice);
    }

    @Test
    void ensureGetPersonsByNotificationTypeReturnsOnlyPersonsWithTheGivenNotificationType() {

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setId(1);
        boss.setPermissions(asList(USER, BOSS));
        boss.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        final PersonEntity bossEntity = new PersonEntity();
        bossEntity.setId(boss.getId());
        bossEntity.setUsername(boss.getUsername());
        bossEntity.setLastName(boss.getLastName());
        bossEntity.setFirstName(boss.getFirstName());
        bossEntity.setEmail(boss.getEmail());
        bossEntity.setPermissions(boss.getPermissions());
        bossEntity.setNotifications(boss.getNotifications());

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setId(2);
        office.setPermissions(asList(USER, BOSS, OFFICE));
        office.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL, NOTIFICATION_OFFICE));

        final PersonEntity officeEntity = new PersonEntity();
        officeEntity.setId(office.getId());
        officeEntity.setUsername(office.getUsername());
        officeEntity.setLastName(office.getLastName());
        officeEntity.setFirstName(office.getFirstName());
        officeEntity.setEmail(office.getEmail());
        officeEntity.setPermissions(office.getPermissions());
        officeEntity.setNotifications(office.getNotifications());

        when(personRepository.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(INACTIVE, NOTIFICATION_BOSS_ALL)).thenReturn(List.of(bossEntity, officeEntity));

        final List<Person> filteredList = sut.getActivePersonsWithNotificationType(NOTIFICATION_BOSS_ALL);
        assertThat(filteredList)
            .hasSize(2)
            .contains(boss)
            .contains(office);
    }

    @Test
    void ensureThrowsIfNoPersonCanBeFoundForTheCurrentlySignedInUser() {
        assertThatIllegalStateException()
            .isThrownBy(() -> sut.getSignedInUser());
    }

    @Test
    void ensureReturnsPersonForCurrentlySignedInUser() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);

        final PersonEntity personEntity = new PersonEntity();
        personEntity.setId(person.getId());
        personEntity.setUsername(person.getUsername());
        personEntity.setLastName(person.getLastName());
        personEntity.setFirstName(person.getFirstName());
        personEntity.setEmail(person.getEmail());

        when(personRepository.findByUsername("muster")).thenReturn(Optional.of(personEntity));

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(person.getUsername());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        final Person signedInUser = sut.getSignedInUser();
        assertThat(signedInUser).isEqualTo(person);
    }

    @Test
    void ensureThrowsIllegalOnNullAuthentication() {
        assertThatIllegalStateException()
            .isThrownBy(() -> sut.getSignedInUser());
    }

    @Test
    void ensureCanAppointPersonAsOfficeUser() {

        when(personRepository.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(OFFICE, INACTIVE)).thenReturn(emptyList());
        when(personRepository.save(any())).then(returnsFirstArg());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);
        assertThat(personWithOfficeRole.getPermissions())
            .hasSize(2)
            .contains(USER, OFFICE);
    }

    @Test
    void ensureCanNotAppointPersonAsOfficeUser() {

        final Person officePerson = new Person();
        officePerson.setPermissions(List.of(OFFICE));

        final PersonEntity officePersonEntity = new PersonEntity();
        officePersonEntity.setPermissions(officePerson.getPermissions());


        when(personRepository.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(OFFICE, INACTIVE)).thenReturn(List.of(officePersonEntity));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);
        assertThat(personWithOfficeRole.getPermissions())
            .containsOnly(USER);
    }

    @Test
    void ensurePersonUpdatedEventIsFiredAfterUpdate() {

        final Person activePerson = createPerson("active person", USER);
        activePerson.setId(1);

        final PersonEntity activePersonEntity = createPersonEntity(activePerson.getUsername(), USER);
        activePersonEntity.setId(activePerson.getId());

        when(personRepository.save(activePersonEntity)).thenReturn(activePersonEntity);

        sut.update(activePerson);
        verify(applicationEventPublisher).publishEvent(any(PersonUpdatedEvent.class));
    }

    @Test
    void ensurePersonDisabledEventIsFiredAfterPersonUpdate() {

        final Person inactivePerson = createPerson("inactive person", INACTIVE);
        inactivePerson.setId(1);

        final PersonEntity inactivePersonEntity = createPersonEntity(inactivePerson.getUsername(), INACTIVE);
        inactivePersonEntity.setId(inactivePerson.getId());

        when(personRepository.save(inactivePersonEntity)).thenReturn(inactivePersonEntity);

        sut.update(inactivePerson);
        verify(applicationEventPublisher).publishEvent(any(PersonDisabledEvent.class));
    }

    @Test
    void ensurePersonDisabledEventIsNotFiredAfterPersonUpdateAndRoleNotInactive() {

        final Person inactivePerson = createPerson("inactive person", USER);
        inactivePerson.setId(1);

        final PersonEntity inactivePersonEntity = createPersonEntity(inactivePerson.getUsername(), USER);
        inactivePersonEntity.setId(inactivePerson.getId());


        when(personRepository.save(inactivePersonEntity)).thenReturn(inactivePersonEntity);

        sut.update(inactivePerson);
        verify(applicationEventPublisher, never()).publishEvent(any(PersonDisabledEvent.class));
    }

    @Test
    void numberOfActivePersons() {

        when(personRepository.countByPermissionsNotContaining(INACTIVE)).thenReturn(2);

        final int numberOfActivePersons = sut.numberOfActivePersons();
        assertThat(numberOfActivePersons).isEqualTo(2);
    }

    @Test
    void deletesExistingPersonDelegatesAndSendsEvent() {

        final Person signedInUser = new Person("signedInUser", "signed", "in", "user@example.org");

        final Person person = new Person();
        final int personId = 42;
        person.setId(personId);

        final PersonEntity personEntity = new PersonEntity();
        personEntity.setId(personId);
        when(personRepository.existsById(personId)).thenReturn(true);

        sut.delete(person, signedInUser);

        final InOrder inOrder = inOrder(applicationEventPublisher, accountInteractionService, workingTimeWriteService, personRepository);

        inOrder.verify(personRepository).existsById(42);
        inOrder.verify(applicationEventPublisher).publishEvent(personDeletedEventArgumentCaptor.capture());
        assertThat(personDeletedEventArgumentCaptor.getValue().getPerson())
            .isEqualTo(person);

        inOrder.verify(accountInteractionService).deleteAllByPerson(person);
        inOrder.verify(workingTimeWriteService).deleteAllByPerson(person);
        inOrder.verify(personRepository).deleteById(personId);
    }

    @Test
    void deletingNotExistingPersonThrowsException() {
        final Person signedInUser = new Person("signedInUser", "signed", "in", "user@example.org");

        final Person person = new Person();
        person.setId(1);
        assertThatThrownBy(() -> sut.delete(person, signedInUser)).isInstanceOf(IllegalArgumentException.class);

        verify(personRepository).existsById(1);
        verifyNoMoreInteractions(applicationEventPublisher, workingTimeWriteService, accountInteractionService, personRepository);
    }

    @Test
    void numberOfPersonsWithRoleWithoutId() {

        when(personRepository.countByPermissionsContainingAndIdNotIn(OFFICE, List.of(1))).thenReturn(2);

        final int numberOfOfficeExceptId = sut.numberOfPersonsWithOfficeRoleExcludingPerson(1);
        assertThat(numberOfOfficeExceptId).isEqualTo(2);
    }
}
