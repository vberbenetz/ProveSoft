package com.provesoft.resource.utils;

import com.provesoft.resource.entity.UserDetails;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;

public class MailerService {

    private MailSender mailSender;

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendNewUser(String url, UserDetails user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("info@provesoft.com");
        message.setTo(user.getEmail());
        message.setSubject("Welcome to ProveSoft!");
        message.setText(
                "Hello " + user.getFirstName() + " " + user.getLastName() + "," + "\n\n" +
                "This email was sent to you because you have been registered by your company's administrator " +
                "to join the ProveSoft platform." +
                "Please go to: " + url + " to complete your registration."
        );
        mailSender.send(message);
    }
}
