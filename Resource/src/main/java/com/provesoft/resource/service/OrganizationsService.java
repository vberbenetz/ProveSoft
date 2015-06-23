package com.provesoft.resource.service;

import com.provesoft.resource.entity.OrgUser;
import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.repository.OrgUserRepository;
import com.provesoft.resource.repository.OrganizationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationsService {

    @Autowired
    OrganizationsRepository organizationsRepository;

    @Autowired
    OrgUserRepository orgUserRepository;

    public List<Organizations> findByCompany(String companyName) {
        return organizationsRepository.findByCompanyNameOrderByOrganizationIdAsc(companyName);
    }

    public List<Organizations> findByCompanyNameAndOrganizationIdList(String companyName, List<Long> organizationIds) {
        return organizationsRepository.findByCompanyNameAndOrganizationIdIn(companyName, organizationIds);
    }

    public Organizations findByOrganizationIdAndCompanyName(Long organizationId, String companyName) {
        return organizationsRepository.findByOrganizationIdAndCompanyName(organizationId, companyName);
    }

    public Organizations findByOrganizationNameAndCompanyName(String organizationName, String companyName) {
        return organizationsRepository.findByNameAndCompanyName(organizationName, companyName);
    }

    public void saveOrg(Organizations organization) {
        organizationsRepository.save(organization);
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

    public void saveOrgUser(List<OrgUser> orgUser) { orgUserRepository.save(orgUser); }

}
