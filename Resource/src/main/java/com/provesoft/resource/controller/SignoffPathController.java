package com.provesoft.resource.controller;

import com.provesoft.resource.entity.SignoffPath.SignoffPath;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.entity.SignoffPath.SignoffPathTemplateSteps;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
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
        Retrieve signoff path by pathId
     */
    @RequestMapping(
            value = "/signoffPath",
            method = RequestMethod.GET
    )
    public SignoffPath getSignoffPath(@RequestParam("pathId") Long pathId, Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        return signoffPathService.findByCompanyNameAndPathId(companyName, pathId);
    }

    /*
        Retrieve all signoff paths by query parameter
     */
    @RequestMapping(
            value = "/signoffPath/multi",
            method = RequestMethod.GET
    )
    public List<SignoffPath> getSignoffPaths(@RequestParam(value = "orgId", required = false) Long orgId,
                                             Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        if (orgId != null) {
            return signoffPathService.getPathsByOrganizationId(companyName, orgId);
        }

        throw new ResourceNotFoundException();
    }

    /*
        Retrieve all corresponding template SignoffPathSteps
     */
    @RequestMapping(
            value = "/signoffPath/steps/template",
            method = RequestMethod.GET
    )
    public List<SignoffPathTemplateSteps> getTemplatePathSteps(@RequestParam("pathId") Long pathId,
                                                               Authentication auth) {
        String companyName = UserHelpers.getCompany(auth);

        return signoffPathService.getTemplateStepsForPath(companyName, pathId);
    }

    /*
        Retrieve all corresponding signoff path steps
     */
    @RequestMapping(
            value = "/signoffPath/steps",
            method = RequestMethod.GET
    )
    public List<SignoffPathSteps> getPathSteps(@RequestParam("documentId") String documentId,
                                               Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        return signoffPathService.getStepsForDocument(companyName, documentId);
    }

}
