package com.sp.web.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
@Service
public class EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    private static final String FROM_ADDRESS = "kianisina1@gmail.com";
    @Autowired
    private JavaMailSender emailSender;
   
    public void sendResetInstructions(String to, String resetToken, String resetUrl) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setFrom(FROM_ADDRESS);
            helper.setSubject("Password Reset Instructions");
            helper.setText("To reset your password, please click on the following link: "
                + resetUrl + "?token=" + resetToken);
            emailSender.send(message);
            LOGGER.info("Reset instructions sent to: {}", to);
        } catch (MessagingException e) {
            LOGGER.error("Failed to send reset instructions to: {}", to, e);
        }
    }
   
    public void sendRepAuftragLink(String to, String abschlussUrl) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setFrom(FROM_ADDRESS);
            helper.setSubject("Link zum Abschließen des Reparaturauftrags");
            helper.setText("Bitte klicken Sie auf den folgenden Link, um den Auftrag abzuschließen:  " + abschlussUrl);
            emailSender.send(message);
            LOGGER.info("Abschlusslink gesendet an: {}", to);
        } catch (MessagingException e) {
            LOGGER.error("Fehler beim Senden des Linkes an: {}", to, e);
        }
    }
}