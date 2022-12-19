package org.synyx.urlaubsverwaltung.person;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.privilegedRoles;

/**
 * This class describes a person.
 */
public class Person {

    private Integer id;

    private String username;
    private String password;
    private String lastName;
    private String firstName;
    private String email;

    private List<Role> permissions;

    private List<MailNotification> notifications;

    public Person() {
        /* OK */
    }

    public Person(String username, String lastName, String firstName, String email) {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public void setPermissions(List<Role> permissions) {
        this.permissions = permissions;
    }

    public List<Role> getPermissions() {
        if (permissions == null) {
            return emptyList();
        }

        return unmodifiableList(permissions);
    }

    public boolean hasRole(final Role role) {
        return getPermissions().stream()
            .anyMatch(permission -> permission.equals(role));
    }

    public boolean isInactive() {
        return hasRole(INACTIVE);
    }

    public boolean isActive() {
        return !isInactive();
    }

    public boolean isPrivileged() {
        return getPermissions().stream()
            .anyMatch(role -> privilegedRoles().contains(role));
    }

    public List<MailNotification> getNotifications() {
        if (notifications == null) {
            notifications = emptyList();
        }

        return unmodifiableList(notifications);
    }

    public void setNotifications(List<MailNotification> notifications) {
        this.notifications = notifications;
    }

    public boolean hasNotificationType(final MailNotification notification) {
        return getNotifications().stream()
            .anyMatch(element -> element.equals(notification));
    }

    public String getNiceName() {

        final StringBuilder builder = new StringBuilder();
        if (hasText(this.firstName)) {
            builder.append(this.firstName);
            builder.append(" ");
        }
        if (hasText(this.lastName)) {
            builder.append(this.lastName);
        }

        final String niceName = builder.toString().trim();
        if (!hasText(niceName)) {
            return "---";
        }

        return niceName;
    }

    public String getGravatarURL() {
        if (hasText(this.email)) {
            return GravatarUtil.createImgURL(this.email);
        }

        return "";
    }

    @Override
    public String toString() {
        return "Person{id='" + getId() + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Person that = (Person) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
