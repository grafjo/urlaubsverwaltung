package org.synyx.urlaubsverwaltung.person;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class PersonMailServiceIT extends TestContainersBase {

    @RegisterExtension
    public final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private PersonMailService sut;
    @Autowired
    private PersonService personService;

    @Test
    void ensureOfficeWithNotificationsGetMailNewPersonIsCreated() throws MessagingException, IOException {

        final Person createdPerson = new Person("user", "Müller", "Lieschen", "lieschen12@example.org");
        createdPerson.setId(1);

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(singletonList(OFFICE));
        office.setNotifications(singletonList(NOTIFICATION_OFFICE));
        personService.save(office);

        sut.sendPersonCreationNotification(new PersonCreatedEvent(personService, createdPerson.getId(), createdPerson.getNiceName()));

        // was email sent to office?
        assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail()).length).isOne();

        // check attributes
        final Message msg = greenMail.getReceivedMessagesForDomain(office.getEmail())[0];
        assertThat(msg.getSubject()).contains("Ein neuer Benutzer wurde erstellt");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        final String text = (String) msg.getContent();
        assertThat(text).contains("Hallo Marlene Muster");
        assertThat(text).contains("es wurde ein neuer Benutzer erstellt.");
        assertThat(text).contains("Lieschen Müller");
        assertThat(text).contains("Du kannst unter folgender Adresse die Einstellungen des Benutzers einsehen und anpassen:");
        assertThat(text).contains("/web/person/1");
    }
}
