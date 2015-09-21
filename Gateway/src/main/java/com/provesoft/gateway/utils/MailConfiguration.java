package com.provesoft.gateway.utils;

import com.provesoft.gateway.ExternalConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {

    @Autowired
    ExternalConfiguration externalConfiguration;

    @Bean
    public MailerService mailerService() {
        MailerService mailerService = new MailerService();
        mailerService.setMailSender(mailSender());
        return mailerService;
    }

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(externalConfiguration.getHost());
        mailSender.setPort(externalConfiguration.getPort());
        mailSender.setUsername(externalConfiguration.getUser());
        mailSender.setPassword(externalConfiguration.getPassword());

        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.starttls.enable", "true");
        javaMailProperties.put("mail.smtp.auth", "true");

        mailSender.setJavaMailProperties(javaMailProperties);

        return mailSender;
    }
}
