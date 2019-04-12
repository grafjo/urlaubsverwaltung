package org.synyx.urlaubsverwaltung.mail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.synyx.urlaubsverwaltung.settings.SettingsService;


/**
 * Configuration to send mails - settings are configured via settings page.
 */
@ConditionalOnMissingBean(SpringBootConfiguredMailSender.class)
@Configuration
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
