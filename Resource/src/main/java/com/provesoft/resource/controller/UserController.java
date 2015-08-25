package com.provesoft.resource.controller;

import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.UserDetailsService;
import com.provesoft.resource.utils.UserFirstLastNamePkg;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<UserDetails> getUserDetails (@RequestParam(value = "userIds", required = false) Long[] userIds,
                                             Authentication auth) {

        // Get user details for userId list
        if (userIds != null) {
            String companyName = UserHelpers.getCompany(auth);

            return userDetailsService.findByCompanyNameAndUserIdList(companyName, Arrays.asList(userIds));
        }

        throw new ResourceNotFoundException();
    }

}
