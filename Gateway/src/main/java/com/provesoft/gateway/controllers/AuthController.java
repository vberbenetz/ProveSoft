package com.provesoft.gateway.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.gateway.entity.*;
import com.provesoft.gateway.exceptions.ResourceNotFoundException;
import com.provesoft.gateway.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    UsersService usersService;

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


    @RequestMapping("/user")
    @ResponseBody
    public Map<String, Object> user(Principal user) {
        return Collections.<String, Object> singletonMap("name", user.getName());
    }

    @RequestMapping("/login")
    public String login() {
        return "forward:/";
    }

    @RequestMapping("/register")
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

            if (rawPassword.equals("pass123")) {

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
                Organizations defaultOrg = initialize(companyName);

                // Create user details for user
                UserDetails userDetails = new UserDetails(companyName, firstName, lastName, email, title, defaultOrg);
                userDetailsService.addUser(userDetails);

                return newUser;
            }

        }
        catch (IOException ioe) {
            throw new ResourceNotFoundException();
        }

        throw new ResourceNotFoundException();
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


}

