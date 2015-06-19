package com.provesoft.resource.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.OrganizationsService;
import com.provesoft.resource.service.UserDetailsService;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class AdminController {

    @Autowired
    OrganizationsService organizationsService;

    @Autowired
    UserDetailsService userDetailsService;

    /* -------------------------------------------------------- */
    /* ------------------------ GET --------------------------- */
    /* -------------------------------------------------------- */

    // ---------- Organizations ----------- //

    @RequestMapping(
            value = "/organizations/all",
            method = RequestMethod.GET
    )
    public List<Organizations> findAllOrganizations(Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            // Retrieve their company
            String company = UserHelpers.getCompany(auth);

            if (company != null) {
                return organizationsService.findByCompany(company);
            }

            throw new ResourceNotFoundException();
        }

        throw new ForbiddenException();
    }

    /* Get organization by Id */
    @RequestMapping(
            value = "/organizations/single",
            method = RequestMethod.GET
    )
    public Organizations findOrganizationById(@RequestParam("orgId") Long orgId,
                                                          Authentication auth)
    {
        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            // Retrieve their company
            String company = UserHelpers.getCompany(auth);

            if (company != null) {
                return organizationsService.findByOrganizationIdAndCompanyName(orgId, company);
            }

            throw new ResourceNotFoundException();
        }

        throw new ForbiddenException();
    }


    // ---------- Users ----------- //

    @RequestMapping(
            value = "/users/all",
            method = RequestMethod.GET
    )
    public List<UserDetails> findAllUsers(Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            // Retrieve their company
            String company = UserHelpers.getCompany(auth);

            if (company != null) {
                return userDetailsService.findAllByCompanyName(company);
            }

            throw new ResourceNotFoundException();
        }

        throw new ForbiddenException();
    }

    @RequestMapping(
            value = "/users/first10",
            method = RequestMethod.GET
    )
    public List<UserDetails> findFirst10ByCompanyName(Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            // Retrieve their company
            String company = UserHelpers.getCompany(auth);

            if (company != null) {
                return userDetailsService.findFirst10ByCompanyName(company);
            }

            throw new ResourceNotFoundException();
        }

        throw new ForbiddenException();
    }


    /* --------------------------------------------------------- */
    /* ------------------------ POST --------------------------- */
    /* --------------------------------------------------------- */

    // ---------- Organizations ----------- //

    /* Create a new organization */
    @RequestMapping(value = "/organizations/single",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String addOrganization (@RequestBody String json,
                                   Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Organizations organization = mapper.readValue(json, Organizations.class);

                // Check if user is updating for their own company
                String company = UserHelpers.getCompany(auth);
                if ( company.equals(organization.getCompanyName()) ) {
                    organizationsService.saveOrg(organization);
                }
                else {
                    throw new ForbiddenException();
                }
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

            return json;
        }

        throw new ForbiddenException();
    }

    // ---------- Users ----------- //

    /* Create a new user */
    @RequestMapping(value = "/users/single",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String addUser (@RequestBody String json,
                           Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                UserDetails user = mapper.readValue(json, UserDetails.class);

                // Check if user is updating for their own company
                String company = UserHelpers.getCompany(auth);
                if ( company.equals(user.getCompanyName()) ) {
                    userDetailsService.addUser(user);
                }
                else {
                    throw new ForbiddenException();
                }
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

            return json;
        }

        throw new ForbiddenException();
    }

    /* ----------------------------------------------------------- */
    /* ------------------------ UPDATE --------------------------- */
    /* ----------------------------------------------------------- */

    // ---------- Organizations ----------- //

    /* Update organization description */
    @RequestMapping(value = "/organizations/single",
            method = RequestMethod.PUT
    )
    public void updateDescription(@RequestParam("orgId") Long orgId,
                            @RequestParam("newDescription") String newDescription,
                            Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String company = UserHelpers.getCompany(auth);

            organizationsService.updateDescription(orgId, company, newDescription);
        }

        throw new ForbiddenException();
    }


    /* Add user to organization members */
    @RequestMapping(value = "/users/single",
            method = RequestMethod.PUT
    )
    public void addOrganizationMember(@RequestParam("orgId") Long orgId,
                                        @RequestParam("userId") Long userId,
                                        Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String company = UserHelpers.getCompany(auth);

            //organizationsService.updateDescription(orgId, company, newDescription);
        }

        throw new ForbiddenException();
    }


    /* ----------------------------------------------------------- */
    /* ------------------------ DELETE --------------------------- */
    /* ----------------------------------------------------------- */

    @RequestMapping(value = "/organizations/single",
                    method = RequestMethod.DELETE,
                    consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public void deleteOrganization (@RequestBody String json,
                                   Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Organizations organization = mapper.readValue(json, Organizations.class);

                // Check if admin is deleting from their own company
                String company = UserHelpers.getCompany(auth);
                if ( company.equals(organization.getCompanyName())) {
                    organizationsService.deleteOrg(organization);
                }
                else {
                    throw new ForbiddenException();
                }
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

        }

        throw new ForbiddenException();

    }



}
