package com.provesoft.resource.service;

import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.repository.OrganizationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationsService {

    @Autowired
    OrganizationsRepository organizationsRepository;

    public List<Organizations> findByCompany(String companyName) {
        return organizationsRepository.findByCompanyName(companyName);
    }

    public Organizations findByOrganizationIdAndCompanyName(Long organizationId, String companyName) {
        return organizationsRepository.findByOrganizationIdAndCompanyName(organizationId, companyName);
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
}
