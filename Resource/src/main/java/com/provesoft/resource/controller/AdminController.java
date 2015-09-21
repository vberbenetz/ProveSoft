package com.provesoft.resource.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.resource.entity.*;
import com.provesoft.resource.entity.Document.Document;
import com.provesoft.resource.entity.Document.DocumentType;
import com.provesoft.resource.entity.SignoffPath.SignoffPath;
import com.provesoft.resource.entity.SignoffPath.SignoffPathKey;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.entity.SignoffPath.SignoffPathTemplateSteps;
import com.provesoft.resource.exceptions.BadRequestException;
import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.exceptions.InternalServerErrorException;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.*;
import com.provesoft.resource.utils.UserHelpers;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.transaction.TransactionRolledbackException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This class contains all entry points for all admin routes. It touches all other classes and methods
 * and consolidates all admin tasks in this class.
 */
@RestController
public class AdminController {

    @Autowired
    UsersService usersService;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    OrganizationsService organizationsService;

    @Autowired
    RolesService rolesService;

    @Autowired
    DocumentService documentService;

    @Autowired
    SystemSettingsService systemSettingsService;

    @Autowired
    SignoffPathService signoffPathService;

    @Autowired
    ApprovalService approvalService;


/* ------------------------------------------------------------------------------------------------------------------ */
/* --------------------------------------------- USERS RELATED ------------------------------------------------------ */
/* ------------------------------------------------------------------------------------------------------------------ */


    // -------------------------------------------------- GET ------------------------------------------------------- //

    /**
     * Method returns UserDetails for users depending on parameters:
     * 1) If all is true, return all users for this company
     * 2) If searchString exists, perform a wildcard search on the User's name
     * 3) If not parameters are passed in, return first 10 users
     * @param all Flag to indicate if all users need to be returned
     * @param searchString Partial or full search string which will use wildcards to search based on user first/last name
     * @param auth Authentication object
     * @return List of UserDetails
     */
    @RequestMapping(
            value = "/admin/user",
            method = RequestMethod.GET
    )
    public List<UserDetails> findUsers(@RequestParam(value = "all", required = false) Boolean all,
                                       @RequestParam(value = "searchString", required = false) String searchString,
                                       Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {
            String companyName = UserHelpers.getCompany(auth);

            if ( (all != null) && (all) ) {
                return userDetailsService.findAllByCompanyName(companyName);
            }

            if (searchString != null) {
                searchString = "%" + searchString + "%";    // Add wildcard values for search
                return userDetailsService.findByCompanyAndPartialName(companyName, searchString);
            }

            // Return first 10 by default
            return userDetailsService.findFirst10ByCompanyName(companyName);

        }

        throw new ForbiddenException();
    }


    // -------------------------------------------------- POST ------------------------------------------------------ //

    /**
     * Method creates a new user by the admin from the user management page. Default password and organization set.
     * @param json User payload
     * @param auth Authentication object
     * @return New UserDetails
     */
    @RequestMapping(value = "/admin/user",
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

                // Create user details
                newUser = userDetailsService.addUser(newUser);

                // Create new user gateway login
                Users newGatewayUser = new Users(newUser.getEmail(), "pass123", true);
                usersService.saveUser(newGatewayUser);

                // Create user authorities
                Authorities userAuth = new Authorities("ROLE_USER", newGatewayUser);
                Authorities companyAuth = new Authorities("__" + company, newGatewayUser);

                usersService.saveAuthority(userAuth);
                usersService.saveAuthority(companyAuth);

            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

            return newUser;
        }

        throw new ForbiddenException();
    }

    /**
     * Method updates and enables role permissions for user
     * @param userId User Id of user to update
     * @param roleIds Array of Role Ids to enable for user
     * @param auth Authentication object
     * @return List of UserPermissions
     */
    @RequestMapping(value = "/admin/user/permissions",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<UserPermissions> addUserPermissions (@RequestParam("userId") Long userId,
                                                     @RequestParam("roleIds") Long[] roleIds,
                                                     Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            List<Long> roleIdsToAdd = Arrays.asList(roleIds);
            List<Roles> rolesToAdd = new ArrayList<>();

            /* ------ Verify roles and user belong to this company and/or exist ------- */
            String companyName = UserHelpers.getCompany(auth);

            UserDetails userDetails = userDetailsService.findByCompanyNameAndUserId(companyName, userId);

            if (userDetails == null) {
                throw new ResourceNotFoundException();
            }

            for (Long roleId : roleIdsToAdd) {
                Roles role = rolesService.findByCompanyNameAndRoleId(companyName, roleId);

                if (role == null) {
                    throw new ResourceNotFoundException();
                }
                else {
                    rolesToAdd.add(role);
                }
            }
            /* ------------------------------------------ */

            // Fetch all permissions by role
            List<RolePermissions> rolePermissions = new ArrayList<>();

            for (Roles r : rolesToAdd) {
                rolePermissions.addAll(rolesService.findRolePermissionsByRoleId(r.getRoleId()));
            }

            // Generate UserPermission objs
            List<UserPermissions> newUserPermissions = new ArrayList<>();

            for (RolePermissions rp : rolePermissions) {
                UserPermissions newUserPermission = new UserPermissions(userId,
                        rp.getKey().getOrganizationId(),
                        rp.getViewPerm(),
                        rp.getRevisePerm(),
                        rp.getCommentPerm(),
                        rp.getAdminPerm() );

                newUserPermissions.add(newUserPermission);
            }

            List<UserPermissions> up = userDetailsService.addUserPermissions(newUserPermissions);

            // Associate roles with user
            addRoleUser(userId, roleIds, auth);

            return up;
        }

        throw new ForbiddenException();

    }

    /**
     * Method updates and sets a new primary organization for a user
     * @param userId User Id of user who has their primary organization updated
     * @param json Organization payload
     * @param auth Authentication object
     * @return UserDetails with updated primary organization
     */
    @RequestMapping(
            value = "/admin/user/primaryOrg",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserDetails updateUserPrimaryOrg(@RequestParam("userId") Long userId,
                                            @RequestBody String json,
                                            Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                Organizations org = mapper.readValue(json, Organizations.class);

                String companyName = UserHelpers.getCompany(auth);

                // Verify org and user belongs to company
                Organizations orgToAdd = organizationsService.findByOrganizationIdAndCompanyName(org.getOrganizationId(), companyName);
                UserDetails user = userDetailsService.findByCompanyNameAndUserId(companyName, userId);

                if (orgToAdd == null || user == null) {
                    throw new ResourceNotFoundException();
                }

                user.setPrimaryOrganization(orgToAdd);

                return userDetailsService.addUser(user);

            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

        }

        throw new ForbiddenException();
    }


    // -------------------------------------------------- PUT ------------------------------------------------------- //

    /**
     * Method updates certain user properties depending on parameters passed in
     * @param userId User Id of user getting their properties updated
     * @param alternateOrgIds Alternate organization Ids to replace user's current ones
     * @param roleIds Roles Ids to replace user's current ones
     * @param auth Authentication object
     * @return Empty ResponseEntity
     */
    @RequestMapping(value = "/admin/user/properties",
            method = RequestMethod.PUT
    )
    public ResponseEntity updateUserProperties ( @RequestParam(value = "userId", required = true) Long userId,
                                                 @RequestParam(value = "altOrgIds", required = false) Long[] alternateOrgIds,
                                                 @RequestParam(value = "roleIds", required = false) Long[] roleIds,
                                                 Authentication auth
    ) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            if ( (alternateOrgIds == null) && (roleIds == null) ) {
                throw new ResourceNotFoundException();
            }

            // Generate new list of OrgUsers for new alternate organizations
            if (alternateOrgIds != null) {
                List<Long> altOrgIds = Arrays.asList(alternateOrgIds);
                List<OrgUser> newAltOrgs = new ArrayList<>();

                for (Long id : altOrgIds) {
                    newAltOrgs.add(new OrgUser(id, userId, companyName));
                }

                organizationsService.saveOrgUser(newAltOrgs);
            }

            // Add roles to user
            if (roleIds != null) {

                addUserPermissions(userId, roleIds, auth);
            }

            return new ResponseEntity<>("{}", HttpStatus.OK);
        }

        throw new ForbiddenException();
    }

    // ------------------------------------------------- DELETE ----------------------------------------------------- //

    /**
     * Method deletes a user. This method is called by the front-end from the manage users panel.
     * Method will not delete the user who calls it, or another Super Admin.
     * 1) Delete all UserPermissions
     * 2) Delete all RoleUser associated with this user
     * 3) Delete all OrgUser associated with this user
     * 4) Delete UserDetails for this user
     * 5) Delete authorities
     * 6) Delete User (free up email)
     * @param userId User Id of user being deleted
     * @param auth Authentication object
     * @return Empty ResponseEntity
     */
    @RequestMapping(value = "/admin/user",
            method = RequestMethod.DELETE
    )
    public ResponseEntity deleteUser (@RequestParam(value = "userId", required = true) Long userId,
                                      Authentication auth
    ) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            UserDetails userDetails = userDetailsService.findByCompanyNameAndUserId(companyName, userId);

            if (userDetails == null) {
                throw new ResourceNotFoundException();
            }

            List<Authorities> authorities = usersService.getAuthorities(userDetails.getEmail());

            // Check if user is me. Do not delete
            if (userDetails.getEmail().equals(auth.getName())) {
                return new ResponseEntity<>("{\"errorCode\":1}", HttpStatus.BAD_REQUEST);
            }

            // Check if user is admin. Do not delete
            for (Authorities a : authorities) {
                if (a.getAuthority().equals("ROLE_SUPER_ADMIN")) {
                    return new ResponseEntity<>("{\"errorCode\":2}", HttpStatus.BAD_REQUEST);
                }
            }

            // Delete all user permissions
            userDetailsService.deleteAllPermissions(userDetails.getUserId());

            // Delete all OrgUser For User
            organizationsService.deleteAllOrgUserByUserId(companyName, userDetails.getUserId());

            // Delete all RoleUser For User
            rolesService.deleteAllRoleUserByUserId(companyName, userDetails.getUserId());

            // Delete UserDetails For User
            userDetailsService.deleteByUserId(companyName, userDetails.getUserId());

            // Delete Authorities
            usersService.deleteUser(userDetails.getEmail());

            // Delete User
            usersService.deleteUser(userDetails.getEmail());

            return new ResponseEntity<>("{}", HttpStatus.OK);
        }

        throw new ForbiddenException();
    }

    /**
     * Method deletes alternate organization Ids or role Ids associated with this user
     * @param userId User Id of user getting their ids removed
     * @param orgId Org Id of alternate organization id being removed from user
     * @param roleId Role Id of role being disassociated from user
     * @param auth Authentication object
     * @return Empty ResponseEntity
     */
    @RequestMapping(value = "/admin/user/properties",
            method = RequestMethod.DELETE
    )
    public ResponseEntity deleteUserProperties (@RequestParam(value = "userId", required = true) Long userId,
                                                @RequestParam(value = "orgId", required = false) Long orgId,
                                                @RequestParam(value = "roleId", required = false) Long roleId,
                                                Authentication auth
    ) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String company = UserHelpers.getCompany(auth);

            if ( (orgId == null) && (roleId == null) ) {
                return new ResponseEntity<>("{}", HttpStatus.NOT_FOUND);
            }

// TODO: ADD CHECK TO VERIFY ORGID BELONGS TO COMPANY
            if (orgId != null) {
                organizationsService.deleteOrgUser(orgId, userId, company);
            }

            if (roleId != null) {
                rolesService.deleteRoleUser(company, userId, roleId);
            }

            return new ResponseEntity<>("{}", HttpStatus.OK);
        }

        throw new ForbiddenException();
    }

    /**
     * Method deletes permissions for User in a certain role
     * @param userId User Id who is getting permissions revoked
     * @param roleId Role Id to which permissions map to
     * @param auth Authentication object
     * @return Empty ResponseEntity
     */
    @RequestMapping(value = "/admin/user/permissions",
            method = RequestMethod.DELETE
    )
    public ResponseEntity removeUserRole (@RequestParam("userId") Long userId,
                                          @RequestParam("roleId") Long roleId,
                                          Authentication auth)
    {
        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            // Confirm user exists and belongs to company
            UserDetails userDetails = userDetailsService.findByCompanyNameAndUserId(companyName, userId);

            if (userDetails == null) {
                throw new ResourceNotFoundException();
            }

            // Confirm role exists and belongs to company
            Roles role = rolesService.findByCompanyNameAndRoleId(companyName, roleId);

            if (role == null) {
                throw new ResourceNotFoundException();
            }

            // Get role permissions and create user permission objects to delete
            List<UserPermissions> userPermissionsToDelete = new ArrayList<>();
            List<RolePermissions> rolePermissions = rolesService.findRolePermissionsByRoleId(role.getRoleId());
            for (RolePermissions rp : rolePermissions) {
                UserPermissions up = new UserPermissions(userDetails.getUserId(),
                        rp.getKey().getOrganizationId(),
                        rp.getViewPerm(),
                        rp.getRevisePerm(),
                        rp.getCommentPerm(),
                        rp.getAdminPerm());
                userPermissionsToDelete.add(up);
            }

            userDetailsService.deleteList(userPermissionsToDelete);

            return new ResponseEntity<>("{}", HttpStatus.OK);
        }

        throw new ForbiddenException();
    }


/* ------------------------------------------------------------------------------------------------------------------ */
/* ------------------------------------------- ORGANIZATIONS RELATED ------------------------------------------------ */
/* ------------------------------------------------------------------------------------------------------------------ */


    // -------------------------------------------------- GET ------------------------------------------------------- //

    /**
     * Method retrieves organization by organization Id or by default (if no param) all organizations for company
     * @param orgId Organization Id of organization to find
     * @param auth Authentication object
     * @return ResponseEntity with Organization or List of Organizations as payload
     */
    @RequestMapping(
            value = "/admin/organization",
            method = RequestMethod.GET
    )
    public ResponseEntity<?> findOrganization(@RequestParam(value = "orgId", required = false) Long orgId,
                                              Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {
            String companyName = UserHelpers.getCompany(auth);

            if (orgId != null) {
                Organizations o = organizationsService.findByOrganizationIdAndCompanyName(orgId, companyName);
                return new ResponseEntity<>(o, HttpStatus.OK);
            }

            // Return all organization by default
            List<Organizations> oList = organizationsService.findByCompany(companyName);
            return new ResponseEntity<>(oList, HttpStatus.OK);
        }

        throw new ForbiddenException();
    }


    // -------------------------------------------------- POST ------------------------------------------------------ //

    /**
     * Method creates a new organization based on passed in payload from organization management in the frontend
     * @param json Organization payload
     * @param auth Authentication object
     * @return Organizations
     */
    @RequestMapping(value = "/admin/organization",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Organizations addOrganization (@RequestBody String json,
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

                return organizationsService.saveOrg(organization);
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }
        }

        throw new ForbiddenException();
    }

    // -------------------------------------------------- PUT ------------------------------------------------------- //

    /**
     * Method updates the organizations description
     * @param orgId Organization Id of organization receiving new description
     * @param newDescription Description to replace existing one
     * @param auth Authentication object
     */
    @RequestMapping(value = "/admin/organization",
            method = RequestMethod.PUT
    )
    public void updateOrganizationDescription(@RequestParam("orgId") Long orgId,
                                              @RequestParam("newDescription") String newDescription,
                                              Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String company = UserHelpers.getCompany(auth);

            organizationsService.updateDescription(orgId, company, newDescription);
        }

        throw new ForbiddenException();
    }

    // ------------------------------------------------- DELETE ----------------------------------------------------- //

    @RequestMapping(
            value = "/admin/organization",
            method = RequestMethod.DELETE
    )
    public ResponseEntity removeOrganization (@RequestParam("organizationId") Long organizationId,
                                              Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            // Check if organization belongs to company
            Organizations organization = organizationsService.findByOrganizationIdAndCompanyName(organizationId, companyName);

            if (organization == null) {
                throw new BadRequestException();
            }

            if (organizationsService.removeOrganization(companyName, organization)) {
                return new ResponseEntity<>("{\"deleted\":true}", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("{\"deleted\":false}", HttpStatus.BAD_REQUEST);
            }
        }

        throw new ForbiddenException();
    }


/* ------------------------------------------------------------------------------------------------------------------ */
/* ------------------------------------------------ ROLE RELATED ---------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /**
     * Method retrieves all roles for the company
     * @param auth Authentication object
     * @return List of Roles
     */
    @RequestMapping(
            value = "/admin/role/all",
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

    // -------------------------------------------------- POST ------------------------------------------------------ //

    /**
     * Method creates a new role for the company via the role management panel in the admin frontend
     * @param json Role payload
     * @param auth Authentication object
     * @return Roles
     */
    @RequestMapping(value = "/admin/role",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Roles addRole (@RequestBody String json,
                          Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Roles role = mapper.readValue(json, Roles.class);

                // Append company name
                String company = UserHelpers.getCompany(auth);

                role.setCompanyName(company);

                return rolesService.saveRole(role);
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }
        }

        throw new ForbiddenException();
    }

    /**
     * Add RolePermissions as dictated by the newly created Role (method called right after a new Role is created)
     * @param json RolePermissions payload
     * @param auth Authentication object
     * @return List of RolePermissions
     */
    @RequestMapping(value = "/admin/role/permissions",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<RolePermissions> addRolePermissions (@RequestBody String json,
                                                     Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                List<RolePermissions> rolePermissionsList = mapper.readValue(json, new TypeReference<List<RolePermissions>>() {
                });

                /* ----- Verify all roles and organizations belong to this user's company ----- */
                String companyName = UserHelpers.getCompany(auth);

                List<Long> roleIds = new ArrayList<>();
                List<Long> organizationIds = new ArrayList<>();

                for (RolePermissions r : rolePermissionsList) {

                    // Add roleId if not present in list
                    int numRoleIds = roleIds.size();
                    for (Long rId : roleIds) {
                        if ( rId.equals(r.getKey().getRoleId()) ) {
                            break;
                        }
                        --numRoleIds;
                    }
                    if ( (numRoleIds == 0) || (roleIds.size() == 0) ) {
                        roleIds.add(r.getKey().getRoleId());
                    }

                    // Add organizationId if not present in list
                    int numOrgIds = organizationIds.size();
                    for (Long oId : organizationIds) {
                        if ( oId.equals(r.getKey().getOrganizationId()) ) {
                            break;
                        }
                        --numOrgIds;
                    }
                    if ( (numOrgIds == 0) || (organizationIds.size() == 0) ) {
                        organizationIds.add(r.getKey().getOrganizationId());
                    }

                }

                List<Roles> roles = rolesService.findByCompanyNameAndRoleIdList(companyName, roleIds);
                List<Organizations> organizations = organizationsService.findByCompanyNameAndOrganizationIdList(companyName, organizationIds);

                // Compare sizes with retrieved list.
                // If mismatch then role or org doesn't exist, or exists for another company
                if ( (roleIds.size() != roles.size()) || (organizationIds.size() != organizations.size()) ) {
                    throw new ForbiddenException();
                }
                /* ------------------------------------------- */

                return rolesService.saveRolePermissions(rolePermissionsList);

            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

        }

        throw new ForbiddenException();
    }

    // -------------------------------------------------- PUT ------------------------------------------------------- //

    // ------------------------------------------------- DELETE ----------------------------------------------------- //

    /**
     * Method deletes a role if it is not being referenced anywhere else
     * @param roleId Role id of role to be deleted
     * @param auth Authentication object
     * @return ResponseEntity
     */
    @RequestMapping(
            value = "/admin/role",
            method = RequestMethod.DELETE
    )
    public ResponseEntity removeRole (@RequestParam("roleId") Long roleId,
                                      Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            // Check if role belongs to company
            Roles role = rolesService.findByCompanyNameAndRoleId(companyName, roleId);

            if (role == null) {
                throw new BadRequestException();
            }

            if (rolesService.removeRole(companyName, role)) {
                return new ResponseEntity<>("{\"deleted\":true}", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("{\"deleted\":false}", HttpStatus.BAD_REQUEST);
            }
        }

        throw new ForbiddenException();
    }


/* ------------------------------------------------------------------------------------------------------------------ */
/* ---------------------------------------------- ORGUSER RELATED --------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /**
     * Method retrieves User if orgId parameter present or Organizations if userId parameter present
     * @param userId User Id for which Organizations are queried by
     * @param orgId Organization Id for which Users are queried by
     * @param auth Authentication object
     * @return List of Objects (UserDetails or Organizations)
     */
    @RequestMapping(
            value = "/admin/orgUser",
            method = RequestMethod.GET
    )
    public List<? extends Object> findOrgsByUserIdOrOrgId (@RequestParam(value = "userId", required = false) Long userId,
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

    // -------------------------------------------------- POST ------------------------------------------------------ //


    /**
     * Method creates organization user relationship mappings
     * @param json OrgUser payload
     * @param auth Authentication object
     * @return List of OrgUsers
     */
    @RequestMapping(value = "/admin/orgUser",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<OrgUser> addAdditionalOrgs (@RequestBody String json,
                                            Authentication auth) {

// TODO: MAY HAVE A SECURITY ISSUE BECAUSE IDS ARE NOT CHECKED AGAINST WHETHER USER HAS RIGHTS TO THEIR COMPANY

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

                return organizationsService.saveOrgUser(orgUsersList);
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

        }

        throw new ForbiddenException();
    }

    // -------------------------------------------------- PUT ------------------------------------------------------- //

    // ------------------------------------------------- DELETE ----------------------------------------------------- //


/* ------------------------------------------------------------------------------------------------------------------ */
/* --------------------------------------------- ROLEUSER RELATED --------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /**
     * Method retrieves a list of RoleUsers either by userId or roleId
     * @param userId User Id query parameter for RoleUser
     * @param roleId Role Id query parameter for RoleUser
     * @param auth Authentication object
     * @return List of RoleUser
     */
    @RequestMapping(
            value = "/admin/roleUser",
            method = RequestMethod.GET
    )
    public List<RoleUser> getRoleUserById (@RequestParam(value = "userId", required = false) Long userId,
                                           @RequestParam(value = "roleId", required = false) Long roleId,
                                           Authentication auth) {
        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            if ( (userId == null) && (roleId == null) ) {
                throw new ResourceNotFoundException();
            }

            String companyName = UserHelpers.getCompany(auth);

            if (userId != null) {
                UserDetails userDetails = userDetailsService.findByCompanyNameAndUserId(companyName, userId);

                if (userDetails != null) {
                    return rolesService.findRoleUserByUserId(userDetails.getUserId());
                }

                throw new ResourceNotFoundException();
            }

            // RoleId has a value only
            else {
                Roles role = rolesService.findByCompanyNameAndRoleId(companyName, roleId);

                if (role != null) {
                    return rolesService.findRoleUserByRoleId(role.getRoleId());
                }

                throw new ResourceNotFoundException();
            }
        }

        throw new ForbiddenException();
    }

    // -------------------------------------------------- POST ------------------------------------------------------ //

    /**
     * Create a new list of Role/User relationship mappings.
     * @param userId User Id of user for mapping
     * @param roleIds Role Id of role for mapping
     * @param auth Authentication object
     * @return List of RoleUser
     */
    @RequestMapping(value = "/admin/roleUser",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<RoleUser> addRoleUser (@RequestParam("userId") Long userId,
                                       @RequestParam("roleIds") Long[] roleIds,
                                       Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            List<Long> roleIdsToAdd = Arrays.asList(roleIds);
            List<RoleUser> roleUsersToAdd = new ArrayList<>();

            /* ------ Verify roles and user belong to this company and/or exist ------- */
            String companyName = UserHelpers.getCompany(auth);

            UserDetails userDetails = userDetailsService.findByCompanyNameAndUserId(companyName, userId);

            if (userDetails == null) {
                throw new ResourceNotFoundException();
            }

            for (Long roleId : roleIdsToAdd) {
                Roles role = rolesService.findByCompanyNameAndRoleId(companyName, roleId);

                if (role == null) {
                    throw new ResourceNotFoundException();
                }
                else {
                    roleUsersToAdd.add( new RoleUser(companyName, userDetails.getUserId(), role.getRoleId()) );
                }
            }

            return rolesService.addRoleUsers(roleUsersToAdd);
        }

        throw new ForbiddenException();
    }

    // -------------------------------------------------- PUT ------------------------------------------------------- //

    // ------------------------------------------------- DELETE ----------------------------------------------------- //


/* ------------------------------------------------------------------------------------------------------------------ */
/* ----------------------------------------------- DOCUMENT RELATED ------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /**
     * Method retrieves all documents based on their state
     * @param state Document state (Released, Pending)
     * @param auth Authentication object
     * @return List of Document
     */
    @RequestMapping(
            value = "/admin/document",
            method = RequestMethod.GET
    )
    public List<Document> getDocumentByState(@RequestParam(value = "state", required = false) String state,
                                             Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {
            String companyName = UserHelpers.getCompany(auth);

            if (state != null) {
                return documentService.findDocumentByState(companyName, state);
            }
            else {
                throw new ResourceNotFoundException();
            }

        }

        throw new ForbiddenException();
    }


/* ------------------------------------------------------------------------------------------------------------------ */
/* ------------------------------------------- DOCUMENTTYPE RELATED ------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /**
     * Method retrieves DocumentType by document type id
     * @param id DocumentType id query parameter
     * @param auth Authentication object
     * @return List of DocumentType
     */
    @RequestMapping(value = "/admin/document/type",
            method = RequestMethod.GET
    )
    public List<DocumentType> getDocumentTypes (@RequestParam(value = "id", required = false) Long id,
                                                Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            // Fetch by Id
            if (id != null) {
// TODO: ---------------
            }

            // Fetch all
            else {
                return documentService.findDocumentTypeByCompanyName(companyName);
            }
        }

        throw new ForbiddenException();
    }

    // -------------------------------------------------- POST ------------------------------------------------------ //

    /**
     * Method creates a new DocumentType based on DocumentType payload
     * @param json DocumentType payload
     * @param auth Authentication object
     * @return New DocumentType
     */
    @RequestMapping(value = "/admin/document/type",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public DocumentType createNewDocumentType (@RequestBody String json,
                                               Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                DocumentType documentType = mapper.readValue(json, DocumentType.class);

                // Append company name
                String companyName = UserHelpers.getCompany(auth);

                // Append current suffix
                documentType.setCurrentSuffix( documentType.getStartingNumber() );

                documentType.setCompanyName(companyName);

                // Generate new document type.
                // Automatically generate new entry to maintain Id in DocumentTypeId table (done within the service)
                return documentService.addDocumentType(documentType);
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }
        }

        throw new ForbiddenException();
    }

    // -------------------------------------------------- PUT ------------------------------------------------------- //

    // ------------------------------------------------- DELETE ----------------------------------------------------- //

    @RequestMapping(
            value = "/admin/document/type",
            method = RequestMethod.DELETE
    )
    public ResponseEntity removeDocumentType (@RequestParam("documentTypeId") Long documentTypeId,
                                              Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            // Check if documentType belongs to company
            DocumentType documentType = documentService.findDocumentTypeByCompanyNameAndId(companyName, documentTypeId);

            if (documentType == null) {
                throw new BadRequestException();
            }

            if (documentService.removeDocumentType(companyName, documentType)) {
                return new ResponseEntity<>("{\"deleted\":true}", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("{\"deleted\":false}", HttpStatus.BAD_REQUEST);
            }
        }

        throw new ForbiddenException();
    }


/* ------------------------------------------------------------------------------------------------------------------ */
/* ------------------------------------------- PERMISSIONS RELATED -------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /**
     * Method retrieves UserPermissions if userId parameter present. Retrieves RolePermissions if roleId is present
     * @param userId User Id query parameter for UserPermissions
     * @param roleId Role Id query parameter for RolePermissions
     * @param auth Authentication object
     * @return List of Object (UserPermissions or RolePermissions)
     */
    @RequestMapping(
            value = "/admin/permissions",
            method = RequestMethod.GET
    )
    public List<? extends Object> getPermissionsById (@RequestParam(value = "userId", required = false) Long userId,
                                                      @RequestParam(value = "roleId", required = false) Long roleId,
                                                      Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            if ( (userId == null) && (roleId == null) ) {
                throw new ResourceNotFoundException();
            }

            String companyName = UserHelpers.getCompany(auth);

            if (userId != null) {
                UserDetails userDetails = userDetailsService.findByCompanyNameAndUserId(companyName, userId);

                // Use the returned userId from the userDetails instead of the passed in parameter.
                // The returned user is guaranteed to be part of the admin's company due to the fetch by company and userId.
                // If null if returned for userDetails, then it indicates user doesn't exist or not part of the company.
                if (userDetails != null) {
                    return userDetailsService.findUserPermissionsByUserId(userDetails.getUserId());
                }

                throw new ResourceNotFoundException();
            }

            // RoleId has a value only
            else {
                Roles role = rolesService.findByCompanyNameAndRoleId(companyName, roleId);

                // Use the returned roleId from the role instead of the passed in parameter.
                // The returned role is guaranteed to be part of the admin's company due to the fetch by company and roleId.
                // If null if returned for role, then it indicates user doesn't exist or not part of the company.
                if (role != null) {
                    return rolesService.findRolePermissionsByRoleId(role.getRoleId());
                }

                throw new ResourceNotFoundException();
            }
        }

        throw new ForbiddenException();
    }


/* ------------------------------------------------------------------------------------------------------------------ */
/* ---------------------------------------------- SETTINGS RELATED -------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    // -------------------------------------------------- POST ------------------------------------------------------ //

    /**
     * Method saves changes to a SystemSetting from the admin frontend panel
     * @param json SystemSetting payload
     * @param auth Authentication object
     * @return SystemSetting
     */
    @RequestMapping(
            value = "/admin/setting",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SystemSettings saveSystemSetting(@RequestBody String json,
                                            Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                SystemSettings systemSetting = mapper.readValue(json, SystemSettings.class);

                // Append company name
                String company = UserHelpers.getCompany(auth);

                SystemSettingsKey key = systemSetting.getKey();
                key.setCompanyName(company);
                systemSetting.setKey(key);

                return systemSettingsService.saveSetting(systemSetting);
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }
        }

        throw new ForbiddenException();
    }


/* ------------------------------------------------------------------------------------------------------------------ */
/* ------------------------------------------- SIGNOFFPATHS RELATED ------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /**
     * Method retrieves first 10 SignoffPaths for company.
     * @param auth Authentication object
     * @return List of SignoffPath
     */
    @RequestMapping(
            value = "/admin/signoffPath/first10",
            method = RequestMethod.GET
    )
    public List<SignoffPath> getFirst10SignoffPaths(Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            return signoffPathService.findFirst10ByCompanyName(companyName);
        }

        throw new ForbiddenException();
    }


    // -------------------------------------------------- POST ------------------------------------------------------ //

    /**
     * Method creates a new SignoffPath
     * @param userId User Id of user who will be initial step of the SignoffPath
     * @param json SignoffPath payload
     * @param auth Authentication object
     * @return SignoffPath
     */
    @RequestMapping(
            value = "/admin/signoffPath",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SignoffPath createNewSignoffPath(@RequestParam("userId") Long userId,
                                            @RequestBody String json,
                                            Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                SignoffPath signoffPath = mapper.readValue(json, SignoffPath.class);

                String companyName = UserHelpers.getCompany(auth);

                // Get user for initial path step, and check if they belong to company
                UserDetails user = userDetailsService.findByCompanyNameAndUserId(companyName, userId);

                if (user == null) {
                    throw new ResourceNotFoundException();
                }

                SignoffPathKey key = new SignoffPathKey(companyName, null);

                // Get new pathId
                // Retry if deadlock occurs until the resource becomes free or timeout occurs
                for (long stop=System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(30L); stop > System.currentTimeMillis();) {
                    try {
                        Long pathId = signoffPathService.getAndIncrementSignoffPathId(companyName).getKey().getPathId();
                        key.setPathId(pathId);
                        signoffPath.setKey(key);

                        SignoffPath newlyCreatedPath = signoffPathService.createNewPath(signoffPath);

                        // Create initial START step
                        SignoffPathTemplateSteps newStep = new SignoffPathTemplateSteps(companyName, pathId, "START", user);
                        signoffPathService.createNewTemplateStep(newStep);

                        return newlyCreatedPath;

                    }
                    catch (CannotAcquireLockException | LockAcquisitionException | TransactionRolledbackException ex) {

                        // Sleep and try to get resource again
                        try {
                            Thread.sleep(5L);
                        }
                        catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        throw new InternalServerErrorException();
                    }
                }

            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new InternalServerErrorException();
            }
        }

        throw new ForbiddenException();
    }

    /**
     * Method creates new SignoffPathTemplateSteps
     * @param json SignoffPathTemplateSteps payload
     * @param auth Authentication object
     * @return List of SignoffPathTemplateSteps
     */
    @RequestMapping(
            value = "/admin/signoffPath/steps/template",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<SignoffPathTemplateSteps> createNewSignoffPathTemplateSteps(@RequestBody String json,
                                                                            Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                List<SignoffPathTemplateSteps> signoffPathTemplateSteps = mapper.readValue(json, new TypeReference<List<SignoffPathTemplateSteps>>() {
                });

                String companyName = UserHelpers.getCompany(auth);

                for (SignoffPathTemplateSteps s : signoffPathTemplateSteps) {
                    s.setCompanyName(companyName);
                }

                signoffPathTemplateSteps = signoffPathService.createNewTemplateSteps(signoffPathTemplateSteps);

                return signoffPathTemplateSteps;
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new InternalServerErrorException();
            }
        }

        throw new ForbiddenException();
    }

    /**
     * Method creates additional SignoffPathSteps for this specific Document (does not update template or other docs)
     * @param documentId Document Id for which additional SignoffPathSteps are to be added
     * @param json SignoffPathSteps payload
     * @param auth Authentication object
     * @return List of SignoffPathSteps
     */
    @RequestMapping(
            value = "/admin/signoffPath/steps",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<SignoffPathSteps> createNewSignoffPathSteps(@RequestParam("documentId") String documentId,
                                                            @RequestBody String json,
                                                            Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                List<SignoffPathSteps> signoffPathSteps = mapper.readValue(json, new TypeReference<List<SignoffPathSteps>>() {
                });

                String companyName = UserHelpers.getCompany(auth);

                for (SignoffPathSteps s : signoffPathSteps) {
                    s.setCompanyName(companyName);
                    s.setDocumentId(documentId);
                    s.setApproved(false);
                    s.setTemplateId(null);
                }

                signoffPathSteps = signoffPathService.createNewStepsForDocRev(signoffPathSteps);

                return signoffPathSteps;
            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new InternalServerErrorException();
            }
        }

        throw new ForbiddenException();
    }


    // -------------------------------------------------- PUT ------------------------------------------------------- //

    // ------------------------------------------------- DELETE ----------------------------------------------------- //

    @RequestMapping(
            value = "/admin/signoffPath",
            method = RequestMethod.DELETE
    )
    public void deleteSignoffPath(@RequestParam("pathId") Long pathId,
                                  Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);
            SignoffPath sfp = signoffPathService.findByCompanyNameAndPathId(companyName, pathId);

            if (sfp == null) {
                throw new BadRequestException();
            }

            signoffPathService.deleteSignoffPath(companyName, sfp);
        }
        else {
            throw new ForbiddenException();
        }
    }

    /**
     * Method removes specific SignoffPathSteps from a SignoffPathTemplate. All documents in "Pending" state which have
     * had the original template applied will not see these changes reflected for them until next revision cycle.
     * @param pathId SignoffPath Id for which template steps need to be removed from
     * @param stepIds Array of SignoffPathStep Ids to be removed from SignoffPath
     * @param auth Authentication object
     */
    @RequestMapping(
            value = "/admin/signoffPath/steps/template",
            method = RequestMethod.DELETE
    )
    public void removeSignoffPathStep(@RequestParam("pathId") Long pathId,
                                      @RequestParam("stepIds") Long[] stepIds,
                                      Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            ArrayList<Long> stepIdList = new ArrayList<>(Arrays.asList(stepIds));

            // Retrieve and remove 'START' step if present
            List<SignoffPathTemplateSteps> stepsForPath = signoffPathService.getTemplateStepsForPath(companyName, pathId);

            if (stepsForPath == null) {
                throw new ResourceNotFoundException();
            }

            List<SignoffPathTemplateSteps> stepsToDelete = new ArrayList<>();

            // Add steps for deletion from path which are not "START" status and who are part of stepId list
            for (SignoffPathTemplateSteps s : stepsForPath) {
                if ( !s.getAction().equals("START") ) {
                    for (Long stepId : stepIdList) {
                        if (stepId.equals(s.getId())) {
                            stepsToDelete.add(s);
                        }
                    }
                }
            }

            signoffPathService.deleteTemplateSignoffSteps(stepsToDelete);

            return;
        }

        throw new ForbiddenException();
    }


}
