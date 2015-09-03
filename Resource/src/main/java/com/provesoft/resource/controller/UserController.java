package com.provesoft.resource.controller;

import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.UserDetailsService;
import com.provesoft.resource.utils.AuthPkg;
import com.provesoft.resource.utils.ProfilePicturePkg;
import com.provesoft.resource.utils.UserFirstLastNamePkg;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
