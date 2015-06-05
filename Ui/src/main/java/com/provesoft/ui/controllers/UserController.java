package com.provesoft.ui.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;


@Controller
public class UserController {

    @RequestMapping("/user")
    @ResponseBody
    public Map<String, String> user(Principal user) {
        return Collections.singletonMap("name", user.getName());
    }

}
