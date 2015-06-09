package com.provesoft.ui.controllers;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String redirect() {
        return "forward:/ui/";
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
