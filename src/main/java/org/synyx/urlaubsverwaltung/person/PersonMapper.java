package org.synyx.urlaubsverwaltung.person;

public class PersonMapper {

    public static PersonEntity toPersonEntity(Person person) {
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

    public static Person toPerson(PersonEntity personEntity) {

        final Person person = new Person(personEntity.getUsername(), personEntity.getLastName(), personEntity.getFirstName(), personEntity.getEmail());
        person.setId(personEntity.getId());
        person.setPassword(personEntity.getPassword());
        person.setPermissions(personEntity.getPermissions());
        person.setNotifications(personEntity.getNotifications());
        return person;
    }
}
