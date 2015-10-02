package com.provesoft.resource.utils;

import com.provesoft.resource.entity.Document.Document;
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

    @Async
    public void sendRevisionRejection(UserDetails changeUser, UserDetails rejectingUser, Document document) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("info@provesoft.com");
        message.setTo(changeUser.getEmail());
        message.setSubject("Document Change Rejected");
        message.setText(
                "Your document change has been been rejected.\n\n" +
                "Document Id: " + document.getId() + "\n" +
                "Document Title: " + document.getTitle() + "\n" +
                "Organization: " + document.getOrganization().getName() + "\n" +
                "Rejecting User: " + rejectingUser.getFirstName() + " " + rejectingUser.getLastName() + "\n"
        );
        mailSender.send(message);
    }
}
