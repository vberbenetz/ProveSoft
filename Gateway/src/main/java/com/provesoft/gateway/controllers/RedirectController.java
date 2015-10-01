package com.provesoft.gateway.controllers;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class RedirectController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public ModelAndView redirect(HttpServletRequest request) {
        String serverUrl = request.getRequestURL().toString().split("/")[2].split(":")[0];
        String errorCode = request.getAttribute("javax.servlet.error.status_code").toString();

        // User not authenticated
        if ("401".equals(errorCode)) {
            return new ModelAndView("redirect:" + "http://" + serverUrl + ":8080" + "/");
        }
        else {
            return new ModelAndView("redirect:" + "http://" + serverUrl + ":8080" + "/ui/");
        }


        /*
        // Remote server deployment
        if ("401".equals(errorCode)) {
            return new ModelAndView("redirect:" + "http://" + serverBaseUrl + "/");
        }
        else {
            return new ModelAndView("redirect:" + "http://" + serverBaseUrl + "/ui/");
        }
        */

    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
