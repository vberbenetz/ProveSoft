package com.provesoft.resource.controller;

import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.service.OrganizationsService;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller encompasses all routes regarding organizations. Any missing methods will be found in
 * the AdminController because of sensitive information
 */
@RestController
public class OrganizationController {

    @Autowired
    OrganizationsService organizationsService;

    /* -------------------------------------------------------- */
    /* ------------------------ GET --------------------------- */
    /* -------------------------------------------------------- */

    /**
     * Retrieve organizations based on passed in parameters:
     * 1) By organization Ids array - get organizations based on this list
     * 2) No parameters - get all organizations for this company
     * @param orgIds
     * @param auth
     * @return List of Organizations
     */
    @RequestMapping(
            value = "/organization",
            method = RequestMethod.GET
    )
    public List<Organizations> getOrganizationsByList (@RequestParam(value = "orgIds", required = false) Long[] orgIds,
                                                       Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        if ( (orgIds != null) && (orgIds.length > 0) ) {
            return organizationsService.findByCompanyNameAndOrganizationIdList(companyName, Arrays.asList(orgIds));
        }

        // Return all organizations for this company
        return organizationsService.findByCompany(companyName);
    }

}
