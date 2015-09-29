package com.provesoft.resource.service;

import com.provesoft.resource.entity.OrgUser;
import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service contains all routes and methods corresponding to Organizations and its mappings
 */
@Service
public class OrganizationsService {

    @Autowired
    OrganizationsRepository organizationsRepository;

    @Autowired
    OrgUserRepository orgUserRepository;

    @Autowired
    UserDetailsRepository userDetailsRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    RolePermissionsRepository rolePermissionsRepository;

    @Autowired
    UserPermissionsRepository userPermissionsRepository;


    /**
     * Method retrieves complete list of organizations for the entire company
     * @param companyName Company query parameter
     * @return List of Organizations
     */
    public List<Organizations> findByCompany(String companyName) {
        return organizationsRepository.findByCompanyNameOrderByOrganizationIdAsc(companyName);
    }

    /**
     * Method retrieves organizations by company and organization Id list.
     * @param companyName Company query parameter
     * @param organizationIds List of organization Ids
     * @return List of organizations
     */
    public List<Organizations> findByCompanyNameAndOrganizationIdList(String companyName, List<Long> organizationIds) {
        return organizationsRepository.findByCompanyNameAndOrganizationIdIn(companyName, organizationIds);
    }

    public Organizations findByOrganizationIdAndCompanyName(Long organizationId, String companyName) {
        return organizationsRepository.findByCompanyNameAndOrganizationId(companyName, organizationId);
    }

    /**
     * Method determines if the organization exists
     * @param companyName
     * @param organizationName
     * @return Boolean
     */
    public Boolean doesOrganizationExist(String companyName, String organizationName) {
        if (organizationsRepository.countByCompanyNameAndName(companyName, organizationName) > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Method checks to see if this organization is referred elsewhere in the app
     * @param companyName
     * @param organization
     * @return Boolean
     */
    public Boolean organizationInUse(String companyName, Organizations organization) {
        if (userDetailsRepository.countByCompanyNameAndPrimaryOrganization(companyName, organization) > 0) {
            return true;
        }
        else if ( orgUserRepository.countByKeyCompanyNameAndKeyOrganizationId(companyName, organization.getOrganizationId()) > 0 ) {
            return true;
        }
        else if ( documentRepository.countByCompanyNameAndOrganization(companyName, organization) > 0 ) {
            return true;
        }
        else if ( rolePermissionsRepository.countByKeyOrganizationId(organization.getOrganizationId()) > 0 ) {
            return true;
        }
        else if ( userPermissionsRepository.countByKeyOrganizationId(organization.getOrganizationId()) > 0 ) {
            return true;
        }
        else {
            return false;
        }
    }

    public Organizations saveOrg(Organizations organization) {
        return organizationsRepository.saveAndFlush(organization);
    }

    public void updateDescription(Long organizationId, String companyName, String newDescription) {
        organizationsRepository.updateDescription(organizationId, companyName, newDescription);
    }

    /**
     * Method deletes an Organization if it is not referred anywhere else
     * @param companyName
     * @param organization
     * @return Boolean
     */
    public Boolean removeOrganization(String companyName, Organizations organization) {
        if (!organizationInUse(companyName, organization)) {
            organizationsRepository.delete(organization);
            organizationsRepository.flush();
            return true;
        }
        else {
            return false;
        }
    }


    /* ---------------------- OrgUser -------------------- */

    public List<OrgUser> findByCompanyNameUserId(String companyName, Long userId) {
        return orgUserRepository.findByKeyCompanyNameAndKeyUserId(companyName, userId);
    }

    public List<OrgUser> findByCompanyNameOrgId(String companyName, Long orgId) {
        return orgUserRepository.findByKeyCompanyNameAndKeyOrganizationId(companyName, orgId);
    }

    public List<OrgUser> saveOrgUser(List<OrgUser> orgUser) { return orgUserRepository.save(orgUser); }

    public void deleteOrgUser(Long orgId, Long userId, String companyName) {
        orgUserRepository.deleteOrgUser(orgId, userId, companyName);
    }

    public void deleteAllOrgUserByUserId(String companyName, Long userId) {
        orgUserRepository.deleteAllOrgUserByUserId(companyName, userId);
    }

}
