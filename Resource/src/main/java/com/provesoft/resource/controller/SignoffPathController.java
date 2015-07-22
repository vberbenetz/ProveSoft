package com.provesoft.resource.controller;

import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.service.SignoffPathService;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SignoffPathController {

    @Autowired
    SignoffPathService signoffPathService;

    /*
        Retrieve all corresponding signoff path steps
     */
    @RequestMapping(
            value = "/signoffPath/steps",
            method = RequestMethod.GET
    )
    public List<SignoffPathSteps> getPathSteps(@RequestParam("pathId") Long pathId,
                                               Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        return signoffPathService.getStepsForPath(companyName, pathId);
    }
}
