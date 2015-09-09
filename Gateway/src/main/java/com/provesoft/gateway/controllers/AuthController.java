package com.provesoft.gateway.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.gateway.entity.*;
import com.provesoft.gateway.exceptions.BadRequestException;
import com.provesoft.gateway.exceptions.ResourceNotFoundException;
import com.provesoft.gateway.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class AuthController {

    @Autowired
    UsersService usersService;

    @Autowired
    CompanyService companyService;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    OrganizationsService organizationsService;

    @Autowired
    DocumentService documentService;

    @Autowired
    SignoffPathIdService signoffPathIdService;

    @Autowired
    SystemSettingsService systemSettingsService;


    @RequestMapping(
            value = "/user",
            method = RequestMethod.GET
    )
    @ResponseBody
    public Map<String, Object> user(Principal user) {
        return Collections.<String, Object> singletonMap("name", user.getName());
    }

    @RequestMapping("/login")
    public String login() {
        return "forward:/";
    }

    @RequestMapping(
            value = "/register",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Users register(@RequestBody String json) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            String firstName = rootNode.get("firstName").textValue();
            String lastName = rootNode.get("lastName").textValue();
            String email = rootNode.get("email").textValue();
            String companyName = rootNode.get("companyName").textValue();
            String title = rootNode.get("title").textValue();
            String rawPassword = rootNode.get("password").textValue();
            String plan = rootNode.get("plan").textValue();

            // Validate input
            if (!validateRegistrationFields(firstName, lastName, email, companyName, title, rawPassword, plan)) {
                throw new BadRequestException();
            }

            // Hash and encrypt password
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(rawPassword);

            // Add gateway user
            Users newUser = new Users(email, hashedPassword, true);
            usersService.saveUser(newUser);

            // Add authorities
            Authorities newUserAuth = new Authorities("ROLE_USER", newUser);
            Authorities newAdminAuth = new Authorities("ROLE_SUPER_ADMIN", newUser);
            Authorities newCompanyAuth = new Authorities("__" + companyName, newUser);
            usersService.saveAuthority(newUserAuth);
            usersService.saveAuthority(newAdminAuth);
            usersService.saveAuthority(newCompanyAuth);

            // Initialize the company
            Long numberOfLicenses = 3L;
            if (plan.equals("paid")) {
                numberOfLicenses = 20L;
            }
            CompanyDetails cd = new CompanyDetails(companyName, numberOfLicenses, plan);
            companyService.createNewCompany(cd);
            Organizations defaultOrg = initialize(companyName);

            // Create user details for user
            UserDetails userDetails = new UserDetails(companyName, firstName, lastName, email, title, defaultOrg);
            userDetailsService.addUser(userDetails);

            return newUser;

        }
        catch (IOException ioe) {
            throw new BadRequestException();
        }

    }

    private Boolean validateRegistrationFields (String firstName,
                                                String lastName,
                                                String email,
                                                String companyName,
                                                String title,
                                                String password,
                                                String plan) {

        // Upper and lower case letters
        String alphaRegex = "^[a-zA-Z]+$";

        // Email format
        String emailRegex = "^([\\w-]+(?:\\.[\\w-]+)*)@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$";

        // Min 8 characters
        // 1 Uppercase
        // 1 Lowercase
        // 1 Number
        // 1 Special Character
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";

        // First name validation
        if (firstName == null) {
            return false;
        }
        if (
                (firstName.length() == 0) ||
                (firstName.length() > 254) ||
                (firstName.equals("")) ||
                (!firstName.matches(alphaRegex)) ) {
            return false;
        }

        // Last name validation
        if (lastName == null) {
            return false;
        }
        if (
                (lastName.length() == 0) ||
                (lastName.length() > 254) ||
                (lastName.equals("")) ||
                (!lastName.matches(alphaRegex)) ) {
            return false;
        }

        // Email validation
        if (email == null) {
            return false;
        }
        if (
                (email.length() ==0) ||
                (email.length() > 254) ||
                (email.equals("")) ||
                (!email.matches(emailRegex)) ) {
            return false;
        }

        // Company validation
        if (companyName == null) {
            return false;
        }
        if (
                (companyName.length() == 0) ||
                (companyName.length() > 254) ||
                (companyName.equals("")) ) {
            return false;
        }

        // Check if email or company already exist
        Map<String, Boolean> emailCompanyCheck = doesXexist(email, companyName);
        if (emailCompanyCheck.get("emailExists") || emailCompanyCheck.get("companyExists")) {
            return false;
        }

        // Title validation
        if (title == null) {
            return false;
        }
        if ( (title.length() == 0) || (title.length() > 254) || (title.equals("")) ) {
            return false;
        }

        // Password validation
        if (password == null) {
            return false;
        }
        if (!password.matches(passwordRegex) || (password.length() > 250)) {
            return false;
        }

        if (plan == null) {
            return false;
        }
        if ( (plan.length() == 0) || (plan.length() > 254) || plan.equals("") ) {
            return false;
        }

        return true;
    }

    /*
        Perform all the initialization steps for a new company
     */
    private Organizations initialize(String companyName) {

        // Initialize signoffPathId for new company
        SignoffPathId intialSignoffPathId = new SignoffPathId(companyName, 1L);
        signoffPathIdService.intializeSignoffPathId(intialSignoffPathId);

        SystemSettings redlineSetting = new SystemSettings(companyName, "redline", "off");
        SystemSettings signoff = new SystemSettings(companyName, "signoff", "off");

        systemSettingsService.saveSetting(redlineSetting);
        systemSettingsService.saveSetting(signoff);

        // Initialize Default Organization
        Organizations organization = new Organizations("Default", companyName, "Default Organization");
        organization = organizationsService.saveOrg(organization);

        // Initialize Default DocumentType
        DocumentType docType = new DocumentType(companyName, "Training Instructions", "Training Document", "TI", 10, 1L);
        documentService.addDocumentType(docType);

        return organization;
    }

    @RequestMapping(
            value = "/check",
            method = RequestMethod.GET
    )
    @ResponseBody
    public Map<String, Boolean> doesXexist (@RequestParam(value = "email") String email,
                                            @RequestParam(value = "companyName") String companyName) {

        if ( (email != null) && (companyName != null) ) {
            companyName = "__" + companyName;

            Map<String, Boolean> retMap = new HashMap<>();

            if ( !usersService.doesCompanyExist(companyName) ) {
                retMap.put("companyExists", false);
            }
            else {
                retMap.put("companyExists", true);
            }

            if ( !usersService.doesUserExist(email) ) {
                retMap.put("emailExists", false);
            }
            else {
                retMap.put("emailExists", true);
            }

            return retMap;
        }

        throw new BadRequestException();
    }


}

