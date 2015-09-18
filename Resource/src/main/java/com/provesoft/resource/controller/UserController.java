package com.provesoft.resource.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.entity.Users;
import com.provesoft.resource.exceptions.BadRequestException;
import com.provesoft.resource.service.UserDetailsService;
import com.provesoft.resource.service.UsersService;
import com.provesoft.resource.utils.AuthPkg;
import com.provesoft.resource.utils.ProfilePicturePkg;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Controller deals with everything related to the User.
 * Some sensitive methods encompassing the User can only be found in the AdminController
 */
@RestController
public class UserController {
    
    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    UsersService usersService;

    /**
     * Method retrieves an AuthPkg used by the security in the frontend (Angular) to check if user is authenicated.
     * @param auth Authentication object
     * @return AuthPkg
     * @see AuthPkg
     */
    @RequestMapping(
            value = "/user/auth",
            method = RequestMethod.GET
    )
    public AuthPkg findUser(Authentication auth) {

        return new AuthPkg(auth);
    }

    @RequestMapping(
            value = "/user/details",
            method = RequestMethod.GET
    )
    public ResponseEntity<?> getUserDetails (@RequestParam(value = "userIds", required = false) Long[] userIds,
                                             Authentication auth) {

        // Get user details for userId list
        if (userIds != null) {
            String companyName = UserHelpers.getCompany(auth);

            List<UserDetails> udList = userDetailsService.findByCompanyNameAndUserIdList(companyName, Arrays.asList(userIds));

            return new ResponseEntity<>(udList, HttpStatus.OK);
        }

        // Get user details for my user
        else {
            String companyName = UserHelpers.getCompany(auth);
            UserDetails ud = userDetailsService.findByCompanyNameAndEmail(companyName, auth.getName());
            return new ResponseEntity<>(ud, HttpStatus.OK);
        }
    }

    /**
     * Method retrieves profile pictures for a list of users or for this user, depending on which parameters are passed
     * in
     * @param userIds Array of userIds
     * @param auth Authentication object
     * @return ResponseEntity containing ProfilePicturePkg as its payload
     * @see ProfilePicturePkg
     */
    @RequestMapping(
            value = "/user/profilePic",
            method = RequestMethod.GET
    )
    public ResponseEntity<?> getProfilePicture (@RequestParam(value = "userIds", required = false) Long[] userIds,
                                                Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        if ( (userIds != null) && (userIds.length > 0) ) {
            List<ProfilePicturePkg> ppp = userDetailsService.findProfilePicturesByIds(companyName, userIds);
            return new ResponseEntity<Object>(ppp, HttpStatus.OK);
        }
        else {
            String email = auth.getName();
            Long userId = userDetailsService.findUserIdByCompanyNameAndEmail(companyName, email);
            ProfilePicturePkg ppp = userDetailsService.findProfilePictureForUser(companyName, userId);
            return new ResponseEntity<Object>(ppp, HttpStatus.OK);
        }
    }

    /**
     * Method accepts a payload of an old and new password. Old password is compared against current password to
     * validate user. The new password is then hashed and updated.
     * @param json POST payload
     * @param auth Authentication object
     * @return ResponseEntity with success or error information
     */
    @RequestMapping(
            value = "/user/pr",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity resetUserPassword (@RequestBody String json,
                                             Authentication auth) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            String oldPassword = rootNode.get("oldPassword").textValue();
            String newPassword = rootNode.get("newPassword").textValue();

            String myEmail = auth.getName();

            Users me = usersService.getUser(myEmail);

            // If old password matches, update user
            if (BCrypt.checkpw(oldPassword, me.getPassword())) {
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String hashedNewPassword = passwordEncoder.encode(newPassword);
                me.setPassword(hashedNewPassword);
                usersService.saveUser(me);

                return new ResponseEntity<>("{}", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("{\"errorCode\":1}", HttpStatus.BAD_REQUEST);
            }
        }
        catch (IOException ioe) {
            throw new BadRequestException();
        }
    }

}
