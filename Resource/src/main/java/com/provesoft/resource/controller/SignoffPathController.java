package com.provesoft.resource.controller;

import com.provesoft.resource.entity.SignoffPath.SignoffPath;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.entity.SignoffPath.SignoffPathTemplateSteps;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.SignoffPathService;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    /**
     * Method retrieves a single SignoffPath based on pathId, or a List of SignoffPaths based on organization Id
     * @param pathId SignoffPath Id
     * @param orgId Organization Id
     * @param auth Authentication object
     * @return ResponseEntity with a payload of SignoffPath or List of SignoffPath
     */
    @RequestMapping(
            value = "/signoffPath",
            method = RequestMethod.GET
    )
    public ResponseEntity<?> getSignoffPath(@RequestParam(value = "pathId", required = false) Long pathId,
                                            @RequestParam(value = "orgId", required = false) Long orgId,
                                            Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        if (pathId != null) {
            SignoffPath signoffPath = signoffPathService.findByCompanyNameAndPathId(companyName, pathId);
            return new ResponseEntity<>(signoffPath, HttpStatus.OK);
        }

        if (orgId != null) {
            List<SignoffPath> signoffPaths = signoffPathService.getPathsByOrganizationId(companyName, orgId);
            return new ResponseEntity<>(signoffPaths, HttpStatus.OK);
        }

        throw new ResourceNotFoundException();
    }

    /**
     * Method retrieves a list of SignoffPathSteps for a specific Document
     * @param documentId Document Id
     * @param auth Authentication object
     * @return List of SignoffPathSteps
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

    /**
     * Method retrieves a list of SignoffPathTemplateSteps for a specific path. This is applied to a SignoffPath
     * @param pathId SignoffPath Id
     * @param auth Authentication object
     * @return List of SignoffPathTemplateSteps
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

}
