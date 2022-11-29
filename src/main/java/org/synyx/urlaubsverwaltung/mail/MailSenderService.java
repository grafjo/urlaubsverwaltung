package org.synyx.urlaubsverwaltung.mail;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class MailSenderService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final JavaMailSender mailSender;

    @Autowired
    MailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a mail with the given subject and text to the given recipients.
     *
     * @param from       mail address from where the mail is sent
     * @param recipients mail addresses where the mail should be sent to
     * @param subject    mail subject
     * @param text       mail body
     */
    void sendEmail(String from, List<String> recipients, String subject, String text) {

        if (recipients == null || recipients.isEmpty()) {
            LOG.warn("Could not send email to empty recipients!");
            return;
        }

        final SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(from);
        mailMessage.setTo(recipients.toArray(new String[0]));
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        send(mailMessage);
    }

    /**
     * Send a mail with the given subject and text to the given recipients.
     *
     * @param from            mail address from where the mail is sent
     * @param recipients      mail addresses where the mail should be sent to
     * @param subject         mail subject
     * @param text            mail body
     * @param mailAttachments List of attachments to add to the mail
     */
    void sendEmail(String from, List<String> recipients, String subject, String text, List<MailAttachment> mailAttachments) {

        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(recipients.toArray(new String[]{}));
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(text);

            for (MailAttachment mailAttachment : mailAttachments) {
                helper.addAttachment(mailAttachment.getName(), mailAttachment.getContent());
            }
        } catch (MessagingException e) {
            LOG.error("Sending email to {} failed", recipients, e);
        }
        mailSender.send(mimeMessage);
    }

    private void send(SimpleMailMessage message) {
        try {

            mailSender.send(message);

            if (LOG.isDebugEnabled()) {
                for (String recipient : message.getTo()) {
                    LOG.debug("Sent email to {}", recipient);
                }
                LOG.debug("To={}\n\nSubject={}\n\nText={}",
                    Arrays.toString(message.getTo()), message.getSubject(), message.getText());
            }
        } catch (MailException ex) {
            for (String recipient : message.getTo()) {
                LOG.error("Sending email to {} failed", recipient, ex);
            }
        }
    }
}
