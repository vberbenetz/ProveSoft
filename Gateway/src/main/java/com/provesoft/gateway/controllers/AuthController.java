package com.provesoft.gateway.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.gateway.entity.Authorities;
import com.provesoft.gateway.entity.SignoffPathId;
import com.provesoft.gateway.entity.Users;
import com.provesoft.gateway.exceptions.ResourceNotFoundException;
import com.provesoft.gateway.service.SignoffPathIdService;
import com.provesoft.gateway.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    SignoffPathIdService signoffPathIdService;

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
            String email = rootNode.get("email").textValue();
            String companyName = rootNode.get("companyName").textValue();
            String password = rootNode.get("password").textValue();

            if (password.equals("pass123")) {

                // Add gateway user
                Users newUser = new Users(email, password, true);
                usersService.saveUser(newUser);

                // Add authorities
                Authorities newUserAuth = new Authorities("ROLE_USER", newUser);
                Authorities newAdminAuth = new Authorities("ROLE_SUPER_ADMIN", newUser);
                Authorities newCompanyAuth = new Authorities("__" + companyName, newUser);
                usersService.saveAuthority(newUserAuth);
                usersService.saveAuthority(newAdminAuth);
                usersService.saveAuthority(newCompanyAuth);

                // Initialize signoffPathId for new company
                SignoffPathId intialSignoffPathId = new SignoffPathId(companyName, 1L);
                signoffPathIdService.intializeSignoffPathId(intialSignoffPathId);

                return newUser;
            }

        }
        catch (IOException ioe) {
            throw new ResourceNotFoundException();
        }

        throw new ResourceNotFoundException();
    }

}

