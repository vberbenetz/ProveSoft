package com.provesoft.ui.controllers;

import com.provesoft.ui.ExternalConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class RedirectController implements ErrorController {

    @Autowired
    ExternalConfiguration externalConfiguration;

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public ModelAndView redirect(HttpServletRequest request) {

        String serverUrl = "localhost";
        return new ModelAndView("redirect:" + "http://" + serverUrl + ":8080" + "/ui/");

        // Deployed server
        // String serverUrl = externalConfiguration.getAbsoluteUrl();
        // return new ModelAndView("redirect:" + "http://" + serverUrl + "/ui/");

    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
