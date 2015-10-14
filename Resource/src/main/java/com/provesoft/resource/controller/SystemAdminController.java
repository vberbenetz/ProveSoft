package com.provesoft.resource.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.resource.ExternalConfiguration;
import com.provesoft.resource.entity.BetaKeys;
import com.provesoft.resource.exceptions.BadRequestException;
import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.service.BetaService;
import com.provesoft.resource.service.UsersService;
import com.provesoft.resource.utils.MailerService;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

@RestController
public class SystemAdminController {

    @Autowired
    ExternalConfiguration externalConfiguration;

    @Autowired
    MailerService mailerService;

    @Autowired
    UsersService usersService;

    @Autowired
    BetaService betaService;

    // ---------------------------------------- BETA Key Generation ------------------------------------------------- //

    /**
     * Retrieve all unaccepted beta keys for application
     * @param auth
     * @return
     */
    @RequestMapping(
            value = "/system/admin/bk",
            method = RequestMethod.GET
    )
    public List<BetaKeys> getAllBetaKeys(Authentication auth) {

        if (UserHelpers.isSystemAdmin(auth)) {
            return betaService.findAllKeys();
        }

        throw new ForbiddenException();
    }


    /**
     * Method generates a new beta key for the corresponding email provided
     * @param json
     * @param auth
     * @return
     */
    @RequestMapping(
            value = "/system/admin/bk",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BetaKeys sendBetaKey(@RequestBody String json,
                                Authentication auth) {

        if (UserHelpers.isSystemAdmin(auth)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode rootNode = mapper.readTree(json);
                String email = rootNode.get("email").textValue();

                // Check regular users if email exists
                if (usersService.doesUserExist(email)) {
                    throw new BadRequestException("User with that email already exists");
                }

                // Generate beta key
                SecureRandom random = new SecureRandom();

                String key = new BigInteger(256, random).toString(32);
                BetaKeys newKey = betaService.addNewKey(email, key);

                String recoveryURL = externalConfiguration.getAbsoluteUrl() + "/?bkr";

                // Send recovery email
                mailerService.sendBetaKey(recoveryURL, email, key);

                return newKey;

            }
            catch (IOException ioe) {
                throw new BadRequestException();
            }

        }

        throw new ForbiddenException();
    }

    /**
     * Method removes an unactived beta key by a user's email
     * @param email
     * @param auth
     * @return
     */
    @RequestMapping(
            value = "/system/admin/bk",
            method = RequestMethod.DELETE
    )
    public ResponseEntity revokeBetaKey (@RequestParam("email") String email,
                                         Authentication auth) {

        if (UserHelpers.isSystemAdmin(auth)) {
            betaService.removeKeyByEmail(email);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        throw new ForbiddenException();
    }
}
