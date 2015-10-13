package com.provesoft.ui.controllers;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Controller
public class RedirectController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public ModelAndView redirect(HttpServletRequest request) {

        String serverUrl = request.getRequestURL().toString().split("/")[2].split(":")[0];
        return new ModelAndView("redirect:" + "http://" + serverUrl + ":8080" + "/ui/");

        // Deployed server
        //return new ModelAndView("redirect:" + "http://" + serverUrl + "/ui/");

    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
