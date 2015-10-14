package com.provesoft.gateway.utils;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;

public class MailerService {

    private MailSender mailSender;

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendPasswordReset(String url, String userEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("info@provesoft.com");
        message.setTo(userEmail);
        message.setSubject("Provesoft Password Reset");
        message.setText(
                "Please go to the link below to reset your password: \n\n" + "http://" + url
        );
        mailSender.send(message);
    }
}
