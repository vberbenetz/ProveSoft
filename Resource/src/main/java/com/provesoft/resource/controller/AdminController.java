package com.provesoft.resource.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.resource.entity.*;
import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.OrganizationsService;
import com.provesoft.resource.service.RolesService;
import com.provesoft.resource.service.UserDetailsService;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AdminController {

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    OrganizationsService organizationsService;

    @Autowired
    RolesService rolesService;


    /* -------------------------------------------------------- */
    /* ------------------------ GET --------------------------- */
    /* -------------------------------------------------------- */

    // ---------- Users ----------- //

    @RequestMapping(
            value = "/admin/users/all",
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
            value = "/admin/users/first10",
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

    @RequestMapping(
            value = "/admin/users/wild-search",
            method = RequestMethod.GET
    )
    public List<UserDetails> findUserByPartialName(@RequestParam("name") String name,
                                                   Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            // Retrieve their company
            String company = UserHelpers.getCompany(auth);

            if (company != null) {
                name = "%" + name + "%";    // Add wildcard values for search
                return userDetailsService.findByCompanyAndPartialName(company, name);
            }

            throw new ResourceNotFoundException();
        }

        throw new ForbiddenException();
    }


    // ---------- Organizations ----------- //

    @RequestMapping(
            value = "/admin/organizations/all",
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
            value = "/admin/organizations/single",
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


    // ---------- Roles ----------- //

    @RequestMapping(
            value = "/admin/roles/all",
            method = RequestMethod.GET
    )
    public List<Roles> findAllRoles(Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            // Retrieve their company
            String company = UserHelpers.getCompany(auth);

            if (company != null) {
                return rolesService.findByCompanyName(company);
            }

            throw new ResourceNotFoundException();
        }

        throw new ForbiddenException();
    }


    // ------------- OrgUser -------------- //

    @RequestMapping(
            value = "/admin/orgUser",
            method = RequestMethod.GET
    )
    public List<? extends Object> findOrgsByUserIdOrOrgId(@RequestParam(value = "userId", required = false) Long userId,
                                                            @RequestParam(value = "orgId", required = false) Long orgId,
                                                            Authentication auth) {

        if ( (userId == null) && (orgId == null) ) {
            throw new ResourceNotFoundException();
        }

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            // Retrieve their company
            String company = UserHelpers.getCompany(auth);

            if (company != null) {

                if ( (userId != null) && (orgId != null) ) {
                    throw new ResourceNotFoundException();
                }

                else if (userId != null) {

                    // Get userId to organizationId mapping
                    List<OrgUser> orgUsers = organizationsService.findByCompanyNameUserId(company, userId);
                    List<Long> organizationIds = new ArrayList<>();

                    for (OrgUser o : orgUsers) {
                        organizationIds.add(o.getKey().getOrganizationId());
                    }

                    if (organizationIds.size() == 0) {
                        return organizationIds;
                    }

                    // Get list of organizations belonging to the user
                    return organizationsService.findByCompanyNameAndOrganizationIdList(company, organizationIds);
                }

                else {

                    // Get userId to orgId mapping
                    List<OrgUser> orgUsers = organizationsService.findByCompanyNameOrgId(company, orgId);
                    List<Long> userIds = new ArrayList<>();

                    for (OrgUser o : orgUsers) {
                        userIds.add(o.getKey().getUserId());
                    }

                    if (userIds.size() == 0) {
                        return userIds;
                    }

                    // Get list of users belonging to the organization
                    return userDetailsService.findByCompanyNameAndUserIdList(company, userIds);
                }
            }

            throw new ResourceNotFoundException();
        }

        throw new ForbiddenException();
    }


    // --------------- RoleUser --------------- //

    @RequestMapping(
            value = "/admin/roleUser",
            method = RequestMethod.GET
    )
    public List<? extends Object> findAllRolesByUserIdOrRoleId(@RequestParam(value = "userId", required = false) Long userId,
                                                               @RequestParam(value = "roleId", required = false) Long roleId,
                                                               Authentication auth) {

        if ( (userId == null) && (roleId == null) ) {
            throw new ResourceNotFoundException();
        }

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            // Retrieve their company
            String company = UserHelpers.getCompany(auth);

            if (company != null) {

                if ( (userId != null) && (roleId != null) ) {
                    throw new ResourceNotFoundException();
                }

                else if (userId != null) {

                    // Get userId to roleId mapping
                    List<RoleUser> roleUsers = rolesService.findByCompanyNameAndUserId(company, userId);
                    List<Long> roleIds = new ArrayList<>();

                    for (RoleUser r : roleUsers) {
                        roleIds.add(r.getKey().getRoleId());
                    }

                    if (roleIds.size() == 0) {
                        return roleIds;
                    }

                    // Get list of roles belonging to the user
                    return rolesService.findByCompanyNameAndRoleIdList(company, roleIds);
                }

                else {

                    // Get userId to roleId mapping
                    List<RoleUser> roleUsers = rolesService.findByCompanyNameAndRoleId(company, roleId);
                    List<Long> userIds = new ArrayList<>();

                    for (RoleUser r : roleUsers) {
                        userIds.add(r.getKey().getUserId());
                    }

                    if (userIds.size() == 0) {
                        return userIds;
                    }

                    // Get list of users belonging to the role
                    return userDetailsService.findByCompanyNameAndUserIdList(company, userIds);
                }
            }

            throw new ResourceNotFoundException();
        }

        throw new ForbiddenException();
    }


    /* --------------------------------------------------------- */
    /* ------------------------ POST --------------------------- */
    /* --------------------------------------------------------- */

    // ---------- Users ----------- //

    /* Create a new user */
    @RequestMapping(value = "/admin/users/single",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserDetails addUser (@RequestBody String json,
                           Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            UserDetails newUser;
            ObjectMapper mapper = new ObjectMapper();
            try {
                newUser = mapper.readValue(json, UserDetails.class);
                String company = UserHelpers.getCompany(auth);

                newUser.setCompanyName(company);

                userDetailsService.addUser(newUser);

            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

            return newUser;
        }

        throw new ForbiddenException();
    }


    // ---------- Organizations ----------- //

    /* Create a new organization */
    @RequestMapping(value = "/admin/organizations/single",
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

                // Append company name
                String company = UserHelpers.getCompany(auth);

                // Check if organization exists
// TODO: CHECK IF ORGANIZATION EXISTS

                // Sanitize description to remove any newline characters
                organization.setDescription( organization.getDescription().replaceAll("(?:\\n|\\r)", " ") );

                organization.setCompanyName(company);

                organizationsService.saveOrg(organization);
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

            return json;
        }

        throw new ForbiddenException();
    }


    // ---------- Roles ----------- //

    /* Create a new organization */
    @RequestMapping(value = "/admin/roles/single",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String addRole (@RequestBody String json,
                                   Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Roles role = mapper.readValue(json, Roles.class);

                // Append company name
                String company = UserHelpers.getCompany(auth);

                // Check if role exists
// TODO: CHECK IF ROLE EXISTS

                role.setCompanyName(company);

                rolesService.saveRole(role);
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

            return json;
        }

        throw new ForbiddenException();
    }


    /* --------------- OrgUser ---------------- */

    /* Create additional organization mappings */
    @RequestMapping(value = "/admin/orgUser",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String addAdditionalOrgs (@RequestBody String json,
                                        Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                List<OrgUser> orgUsersList = mapper.readValue( json, new TypeReference<List<OrgUser>>() { } );

                // Append company name
                String company = UserHelpers.getCompany(auth);

                for (OrgUser o : orgUsersList) {
                    OrgUserKey key = o.getKey();
                    key.setCompanyName(company);
                    o.setKey(key);
                }

                organizationsService.saveOrgUser(orgUsersList);
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

            return json;
        }

        throw new ForbiddenException();
    }


    /* --------------- RoleUser ---------------- */

    /* Create additional organization mappings */
    @RequestMapping(value = "/admin/roleUser",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String addUserRoles (@RequestBody String json,
                                      Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                List<RoleUser> roleUsersList = mapper.readValue( json, new TypeReference<List<RoleUser>>() { } );

                // Append company name
                String company = UserHelpers.getCompany(auth);

                for (RoleUser r : roleUsersList) {
                    RoleUserKey key = r.getKey();
                    key.setCompanyName(company);
                    r.setKey(key);
                }

                rolesService.saveRoleUser(roleUsersList);
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
    @RequestMapping(value = "/admin/organizations/single",
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
    @RequestMapping(value = "/admin/users/single",
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

    @RequestMapping(value = "/admin/organizations/single",
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
