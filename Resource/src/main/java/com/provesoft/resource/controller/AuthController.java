package com.provesoft.resource.controller;

import com.provesoft.resource.utils.AuthPkg;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthController {

    @RequestMapping(
            value = "/user/auth",
            method = RequestMethod.GET
    )
    public AuthPkg findAllOrganizations(Authentication auth) {

        return new AuthPkg(auth);
    }
}
