package com.provesoft.gateway.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {

    @Bean
    public MailerService mailerService() {
        MailerService mailerService = new MailerService();
        mailerService.setMailSender(mailSender());
        return mailerService;
    }

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("info@provesoft.com");
        mailSender.setPassword("Pr0v3$0ftP@55w0rd;");

        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.starttls.enable", "true");
        javaMailProperties.put("mail.smtp.auth", "true");

        mailSender.setJavaMailProperties(javaMailProperties);

        return mailSender;
    }
}
