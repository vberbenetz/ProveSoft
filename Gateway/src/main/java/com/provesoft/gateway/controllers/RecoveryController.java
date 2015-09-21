package com.provesoft.gateway.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.gateway.ExternalConfiguration;
import com.provesoft.gateway.entity.RecoveryTokens;
import com.provesoft.gateway.entity.Users;
import com.provesoft.gateway.exceptions.BadRequestException;
import com.provesoft.gateway.service.UsersService;
import com.provesoft.gateway.utils.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;


@RestController
public class RecoveryController {

    @Autowired
    UsersService usersService;

    @Autowired
    MailerService mailerService;

    @Autowired
    ExternalConfiguration externalConfiguration;

    // Forgot Password Reset Request
    @RequestMapping (
            value = "/pr",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String passwordRecoveryRequest(@RequestBody String json) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            String email = rootNode.get("email").textValue();

            // Check regular users if email exists
            if (usersService.doesUserExist(email)) {
                // Generate random url for password recovery
                SecureRandom random = new SecureRandom();

                String token = new BigInteger(256, random).toString(32);
                String recoveryURL = externalConfiguration.getAbsoluteUrl() + "/?r=" + token;

                // Save token to recovery table
                RecoveryTokens newToken = new RecoveryTokens(email, token);

                // Send recovery email
                mailerService.sendPasswordReset(recoveryURL, email);
                usersService.saveRecoveryToken(newToken);
            }

        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return json;
    }

    // Reset password
    @RequestMapping (
            value = "/ps",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String passwordReset(@RequestBody String json) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            String email = rootNode.get("email").textValue();
            String password = rootNode.get("password").textValue();
            String token = rootNode.get("token").textValue();

            RecoveryTokens recoveryTokens = usersService.findTokenByTokenId(token);

            // Verify token date
            LocalDateTime tokenGenDate = recoveryTokens.getGenDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            Long tokenAge = ChronoUnit.HOURS.between(tokenGenDate, LocalDateTime.now());

            if (tokenAge > externalConfiguration.getRecoveryTokenHourExpiry()) {
                throw new BadRequestException();
            }

            // Verify username
            if (recoveryTokens.getEmail().equals(email)) {

                // Hash and encrypt password
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String hashedPassword = passwordEncoder.encode(password);

                // Change user's password
                Users user = usersService.findUserByEmail(email);
                user.setPassword(hashedPassword);
                usersService.updateUser(user);

                // Remove recovery token
                usersService.deleteRecoveryToken(recoveryTokens);

            }
            else {
                throw new BadRequestException();
            }

        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return json;
    }

}
