package com.provesoft.resource.controller;

import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.service.OrganizationsService;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrganizationController {

    @Autowired
    OrganizationsService organizationsService;

    /* -------------------------------------------------------- */
    /* ------------------------ GET --------------------------- */
    /* -------------------------------------------------------- */

    @RequestMapping(value = "/organization",
            method = RequestMethod.GET
    )
    public List<Organizations> getOrganizations (Authentication auth) {

        // Get all organizations by the user's company
// TODO: ONLY RETRIEVE RESULTS WHICH THE USER HAS PERMISSIONS FOR
        String companyName = UserHelpers.getCompany(auth);

        return organizationsService.findByCompany(companyName);
    }
}
