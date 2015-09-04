package com.provesoft.resource.service;

import com.provesoft.resource.entity.OrgUser;
import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.repository.OrgUserRepository;
import com.provesoft.resource.repository.OrganizationsRepository;
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
        return organizationsRepository.findByOrganizationIdAndCompanyName(organizationId, companyName);
    }

    public Organizations findByOrganizationNameAndCompanyName(String organizationName, String companyName) {
        return organizationsRepository.findByNameAndCompanyName(organizationName, companyName);
    }

    public Organizations saveOrg(Organizations organization) {
        return organizationsRepository.saveAndFlush(organization);
    }

    public void updateDescription(Long organizationId, String companyName, String newDescription) {
        organizationsRepository.updateDescription(organizationId, companyName, newDescription);
    }

    public void deleteOrg(Organizations organization) {
        organizationsRepository.delete(organization);
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
