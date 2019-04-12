package org.synyx.urlaubsverwaltung.mail.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.synyx.urlaubsverwaltung.mail.MailSender;
import org.synyx.urlaubsverwaltung.mail.WebConfiguredMailSender;
import org.synyx.urlaubsverwaltung.settings.SettingsService;


/**
 * Configuration to send mails.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.mail", name = "host", matchIfMissing = true)
public class MailConfig {

    @Bean
    public JavaMailSenderImpl javaMailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean
    public MailSender webConfiguredMailSender(SettingsService settingsService) {
        return new WebConfiguredMailSender(javaMailSender(), settingsService);
    }

}
