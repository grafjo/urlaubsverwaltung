package org.synyx.urlaubsverwaltung.person;

import org.hibernate.annotations.LazyCollection;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;
import static javax.persistence.EnumType.STRING;
import static org.hibernate.annotations.LazyCollectionOption.FALSE;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.privilegedRoles;

@Entity(name="person")
public class PersonEntity {

    @Id
    @GeneratedValue
    private Integer id;
    private String username;
    private String password;
    private String lastName;
    private String firstName;
    private String email;

    @ElementCollection
    @LazyCollection(FALSE)
    @Enumerated(STRING)
    private List<Role> permissions = new ArrayList<>();

    @ElementCollection
    @LazyCollection(FALSE)
    @Enumerated(STRING)
    private List<MailNotification> notifications = new ArrayList<>();

    public PersonEntity() {
        /* OK */
    }

    public PersonEntity(String username, String lastName, String firstName, String email) {
        this.username = username;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Role> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Role> permissions) {
        this.permissions = permissions;
    }

    public List<MailNotification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<MailNotification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public String toString() {
        return "PersonEntity{id='" + getId() + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PersonEntity that = (PersonEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
