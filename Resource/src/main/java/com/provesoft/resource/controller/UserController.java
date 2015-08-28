package com.provesoft.resource.controller;

import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.UserDetailsService;
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

@RestController
public class UserController {
    
    @Autowired
    UserDetailsService userDetailsService;

    @RequestMapping(
            value = "/userDetails",
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

    @RequestMapping(
            value = "/user/profilePic",
            method = RequestMethod.GET
    )
    public ProfilePicturePkg getProfilePicture (Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);
        String email = auth.getName();
        Long userId = userDetailsService.findUserIdByCompanyNameAndEmail(companyName, email);

        return userDetailsService.findProfilePictureForUser(companyName, userId);
    }

    @RequestMapping(
            value = "/user/profilePicByIds",
            method = RequestMethod.GET
    )
    public List<ProfilePicturePkg> getProfilePictureById (@RequestParam("userIds") Long[] userIds,
                                                          Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);
        return userDetailsService.findProfilePicturesByIds(companyName, userIds);
    }

}
