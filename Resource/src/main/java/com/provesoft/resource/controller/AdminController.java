package com.provesoft.resource.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.resource.entity.*;
import com.provesoft.resource.entity.Document.DocumentType;
import com.provesoft.resource.entity.SignoffPath.SignoffPath;
import com.provesoft.resource.entity.SignoffPath.SignoffPathKey;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
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

@RestController
public class AdminController {

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


/* ------------------------------------------------------------------------------------------------------------------ */
/* --------------------------------------------- USERS RELATED ------------------------------------------------------ */
/* ------------------------------------------------------------------------------------------------------------------ */


    // -------------------------------------------------- GET ------------------------------------------------------- //

    /*
        Retrieve all users part of the admin's company
     */
    @RequestMapping(
            value = "/admin/user/all",
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

    /*
        Retrieve first 10 users sorted alphabetically
     */
    @RequestMapping(
            value = "/admin/user/first10",
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

    /*
        Retrieve user by partial first or last name.
        User for wild-card search of users.
     */
    @RequestMapping(
            value = "/admin/user/wildSearch",
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


    // -------------------------------------------------- POST ------------------------------------------------------ //

    /*
        Create a new user. Only generates UserDetails and not actual login credentials.
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

                userDetailsService.addUser(newUser);

            }
            catch (IOException | NullPointerException ex) {
                throw new ResourceNotFoundException();
            }

            return newUser;
        }

        throw new ForbiddenException();
    }

    /*
        Add UserPermissions to user with userId, based on rolePermissions of roleId.
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

    // -------------------------------------------------- PUT ------------------------------------------------------- //

    /*
        Update user organizations and/or roles
    */
    @RequestMapping(value = "/admin/user/properties",
            method = RequestMethod.PUT
    )
    public ResponseEntity updateUserProperties ( @RequestParam(value = "userId", required = true) Long userId,
                                                 @RequestParam(value = "primaryOrgId", required = false) Long primaryOrgId,
                                                 @RequestParam(value = "altOrgIds", required = false) Long[] alternateOrgIds,
                                                 @RequestParam(value = "roleIds", required = false) Long[] roleIds,
                                                 Authentication auth
    ) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            if ( (primaryOrgId == null) && (alternateOrgIds == null) && (roleIds == null) ) {
                throw new ResourceNotFoundException();
            }

            if (primaryOrgId != null) {
                userDetailsService.updatePrimaryOrganization(primaryOrgId, userId, companyName);
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

    /*
        Delete user.
        DOES NOT DELETE USER FROM THE GATEWAY DB

        1) Delete all User permissions
        2) Delete all roleUser associations
        3) Delete all orgUser associations
        4) Delete UserDetails
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

            // Delete all user permissions
            userDetailsService.deleteAllPermissions(userDetails.getUserId());

            // Delete all OrgUser For User
            organizationsService.deleteAllOrgUserByUserId(companyName, userDetails.getUserId());

            // Delete all RoleUser For User
            rolesService.deleteAllRoleUserByUserId(companyName, userDetails.getUserId());

            // Delete UserDetails For User
            userDetailsService.deleteByUserId(companyName, userDetails.getUserId());

            return new ResponseEntity<>("{}", HttpStatus.OK);
        }

        throw new ForbiddenException();
    }

    /*
        Remove user's alternate organizations and/or roles pointing to user
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

    /*
        Remove UserPermissions for user of userId based on roleId
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

    /*
        Retrieve all organizations by the admin's company
     */
    @RequestMapping(
            value = "/admin/organization/all",
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

    /*
        Retrieve single organization by organizationId
     */
    @RequestMapping(
            value = "/admin/organization",
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

    // -------------------------------------------------- POST ------------------------------------------------------ //

    /*
        Create a new Organization
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

    /*
        Update organization description
    */
    @RequestMapping(value = "/admin/organization",
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

    // ------------------------------------------------- DELETE ----------------------------------------------------- //

    /*
        Delete organization
     */
    @RequestMapping(value = "/admin/organization",
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


/* ------------------------------------------------------------------------------------------------------------------ */
/* ------------------------------------------------ ROLE RELATED ---------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /*
        Retrieve all roles by admin's company
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

    /*
        Create new Role (not including RolePermissions)
     */
    @RequestMapping(value = "/admin/role",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Roles createRole (@RequestBody String json,
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

    /*
        Add RolePermissions as dictated by the newly created Role (runs right after new role is created)
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
                List<RolePermissions> rolePermissionsList = mapper.readValue(json, new TypeReference<List<RolePermissions>>() { } );

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

    /*
         1) All role permissions are deleted
         2) All role templates are deleted
         3) User permissions that were assigned by this role remain
      */
    @RequestMapping(value = "/admin/role",
            method = RequestMethod.DELETE
    )
    public ResponseEntity deleteRole (@RequestParam("roleId") Long roleId,
                                      Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            Roles role = rolesService.findByCompanyNameAndRoleId(companyName, roleId);

            if (role == null) {
                throw new ResourceNotFoundException();
            }

            // Delete all role permissions
            rolesService.deleteAllRolePermissionsByRoleId(roleId);

            // Delete role
            rolesService.deleteRole(role);

            return new ResponseEntity<>("{}", HttpStatus.OK);
        }

        throw new ForbiddenException();

    }


/* ------------------------------------------------------------------------------------------------------------------ */
/* ---------------------------------------------- ORGUSER RELATED --------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /*
        Retrieve either Users by organizationId or Organizations by userId
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

    // TODO: MAY HAVE A SECURITY ISSUE BECAUSE IDS ARE NOT CHECKED AGAINST WHETHER USER HAS RIGHTS TO THEIR COMPANY
    /*
        Create additional organization mappings
    */
    @RequestMapping(value = "/admin/orgUser",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<OrgUser> addAdditionalOrgs (@RequestBody String json,
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

    /*
        Retrieve list of RoleUsers by userId or roleId
     */
    @RequestMapping(
            value = "/admin/roleUser",
            method = RequestMethod.GET
    )
    public List<? extends Object> getRoleUserById (@RequestParam(value = "userId", required = false) Long userId,
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
/* ------------------------------------------- DOCUMENTTYPE RELATED ------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /*
        Retrieve DocumentType object by documentTypeId
     */
    @RequestMapping(value = "/admin/documentType",
            method = RequestMethod.GET
    )
    public List<DocumentType> getDocumentTypes (@RequestParam(value = "id", required = false) Long id,
                                                Authentication auth) {

        // Check if super admin
        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            // Fetch by Id
            if (id != null) {

            }

            // Fetch all
            else {
                return documentService.findByCompanyName(companyName);
            }
        }

        throw new ForbiddenException();
    }

    // -------------------------------------------------- POST ------------------------------------------------------ //

    /*
        Create new document type
    */
    @RequestMapping(value = "/admin/documentType",
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


/* ------------------------------------------------------------------------------------------------------------------ */
/* ------------------------------------------- PERMISSIONS RELATED -------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    /*
        Retrieve either list of UserPermissions by userId or RolePermissions by roleId
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
/* ------------------------------------------- PERMISSIONS RELATED -------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------------------------ */

    // -------------------------------------------------- GET ------------------------------------------------------- //

    // -------------------------------------------------- POST ------------------------------------------------------ //

    /*
        Add new system setting
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

    /*
        Retrieve subset of all company signoff paths
     */
    @RequestMapping(
            value = "/admin/signoffpath/first10",
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

    /*
        Create new signoff path
     */
    @RequestMapping(
            value = "/admin/signoffpath",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SignoffPath createNewSignoffPath(@RequestBody String json,
                                            Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                SignoffPath signoffPath = mapper.readValue(json, SignoffPath.class);

                SignoffPathKey key = signoffPath.getKey();

                // Append company name
                String companyName = UserHelpers.getCompany(auth);
                key.setCompanyName(companyName);

                // Get new pathId
                // Retry if deadlock occurs until the resource becomes free or timeout occurs
                for (long stop=System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(30L); stop > System.currentTimeMillis();) {
                    try {
                        Long pathId = signoffPathService.getAndIncrementSignoffPathId(companyName).getKey().getPathId();
                        key.setPathId(pathId);

                        return signoffPathService.createNewPath(signoffPath);
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

    /*
        Create new signoff path step
     */
    @RequestMapping(
            value = "/admin/signoffpath",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SignoffPathSteps createNewSignoffPathStep(@RequestBody String json,
                                                Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                SignoffPathSteps signoffPathStep = mapper.readValue(json, SignoffPathSteps.class);

                String companyName = UserHelpers.getCompany(auth);
                signoffPathStep.setCompanyName(companyName);

                signoffPathStep = signoffPathService.createNewStep(signoffPathStep);

                signoffPathService.appendToPathSeq(companyName, signoffPathStep.getPathId(), signoffPathStep.getId());

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














}
