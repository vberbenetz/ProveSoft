package com.provesoft.gateway.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Controller
public class AuthController {

    @RequestMapping(
            value = "/user",
            method = RequestMethod.GET
    )
    @ResponseBody
    public Map<String, Object> user(Principal user) {
        return Collections.<String, Object> singletonMap("name", user.getName());
    }




}

