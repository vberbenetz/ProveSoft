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
        String host = request.getHeader("Host").split(":")[0];
        return new ModelAndView("redirect:" + "http://" + host + ":8080" + "/ui/");
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
