package com.provesoft.gateway.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.gateway.ExternalConfiguration;
import com.provesoft.gateway.entity.*;
import com.provesoft.gateway.exceptions.*;
import com.provesoft.gateway.service.*;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.TransactionRolledbackException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class RegistrationController {

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

    @Autowired
    ExternalConfiguration externalConfiguration;

    @Autowired
    BetaService betaService;

    @RequestMapping(
            value = "/register",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Users register(@RequestBody String json) {

        Users newUser = null;

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

            String betaKey = rootNode.get("betaKey").textValue();

            // Validate input
            if (!validateRegistrationFields(firstName, lastName, email, companyName, title, rawPassword, plan)) {
                throw new BadRequestException();
            }

            // Check if beta key exists and matches email
            BetaKeys keyObj = betaService.findKeyByEmailAndBetaKey(email, betaKey);

            if (keyObj == null) {
                throw new BadRequestException();
            }

            // Hash and encrypt password
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(rawPassword);

            // Create new user
            newUser = new Users(email, hashedPassword, true);

            // Atmoic check and add of new user
            // Retry if deadlock occurs until the resource becomes free or timeout occurs
            for (long stop=System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(30L); stop > System.currentTimeMillis();) {
                try {
                    // The user save does an atomic check and save of the user (See UsersService for more details)
                    usersService.checkAndSaveUser(newUser);
                    break;
                }
                catch (CannotAcquireLockException | LockAcquisitionException | TransactionRolledbackException ex) {

                    // Sleep and try to get resource again
                    try {
                        Thread.sleep(5L);
                    }
                    catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
                catch (UserExistsException uee) {
                    throw uee;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    throw new InternalServerErrorException();
                }
            }

            // Create authorities
            Authorities newCompanyAuth = new Authorities("__" + companyName, newUser);
            Authorities newUserAuth = new Authorities("ROLE_USER", newUser);
            Authorities newAdminAuth = new Authorities("ROLE_SUPER_ADMIN", newUser);

            // Atomic check and save of company. If company exists as an authority, unwind registration process
            // Retry if deadlock occurs until the resource becomes free or timeout occurs
            for (long stop=System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(30L); stop > System.currentTimeMillis();) {
                try {
                    // The user save does an atomic check and save of the user (See UsersService for more details)
                    usersService.checkAndSaveCompany(newCompanyAuth, companyName);
                    break;
                }
                catch (CannotAcquireLockException | LockAcquisitionException | TransactionRolledbackException ex) {

                    // Sleep and try to get resource again
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
                catch (CompanyExistsException cee) {
                    throw cee;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    throw new InternalServerErrorException();
                }
            }

            usersService.saveAuthority(newUserAuth);
            usersService.saveAuthority(newAdminAuth);

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

            // Create folder directory for company
            Boolean result = new File(externalConfiguration.getFileUploadDirectory() + companyName).mkdir();
            if (!result) {
                throw new InternalServerErrorException("Error creating company new directory");
            }

            // Delete beta key
            betaService.deleteBetaKey(keyObj);

            return newUser;

        }
        catch (IOException | NullPointerException ex) {
            throw new BadRequestException();
        }
        catch (UserExistsException uee) {
            throw new ConflictException();
        }
        catch (CompanyExistsException cee) {
            // Delete newly added user to prevent their email from being stuck in purgatory without a company
            usersService.deleteUser(newUser);

            throw new ConflictException();
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

        // Upper and lower case letters, numbers and spaces
        String alphaNumSpaceRegex = "^[a-zA-Z0-9 ]+$";

        // Email format
        String emailRegex = "^([\\w-]+(?:\\.[\\w-]+)*)@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$";

        // Min 8 characters
        // 1 Uppercase
        // 1 Lowercase
        // 1 Number
        // 1 Special Character
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";

        // Trim whitespace from companyName
        companyName = companyName.trim();

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
                        (companyName.equals("")) ||
                        (!companyName.matches(alphaNumSpaceRegex)) ) {
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

    @RequestMapping(
            value = "/checkBK",
            method = RequestMethod.GET
    )
    @ResponseBody
    public Map<String, Boolean> checkBetaKey (@RequestParam(value = "email") String email,
                                              @RequestParam(value = "betaKey") String betaKey) {

        Map<String, Boolean> retMap = new HashMap<>();
        if (betaService.findKeyByEmailAndBetaKey(email, betaKey) != null) {
            retMap.put("valid", true);
        }
        else {
            retMap.put("valid", false);
        }

        return retMap;
    }

    @RequestMapping(
            value = "/tokenReg",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity userRegistrationViaToken (@RequestBody String json) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            String email = rootNode.get("email").textValue();
            String password = rootNode.get("password").textValue();
            String token = rootNode.get("token").textValue();

            NewUserTokens newUserToken = usersService.findNewUserTokenByTokenId(token);

            // Verify token date
            LocalDateTime tokenGenDate = newUserToken.getGenDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            Long tokenAge = ChronoUnit.HOURS.between(tokenGenDate, LocalDateTime.now());

            if (tokenAge > externalConfiguration.getRegistrationTokenHourExpiry()) {
                throw new BadRequestException();
            }

            // Verify username
            if (newUserToken.getEmail().equals(email)) {
                // Hash and encrypt password
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String hashedPassword = passwordEncoder.encode(password);

                // Change user's password and enable the user
                Users user = usersService.findUserByEmail(email);
                user.setPassword(hashedPassword);
                user.setEnabled(true);
                usersService.updateUser(user);

                // Remove recovery token
                usersService.deleteNewUserToken(newUserToken);

                return new ResponseEntity<>("{}", HttpStatus.OK);
            }
            else {
                throw new BadRequestException();
            }
        }
        catch (IOException ioe) {
            throw new BadRequestException();
        }

    }
}
