package com.provesoft.gateway.service;

import com.provesoft.gateway.entity.Organizations;
import com.provesoft.gateway.repository.OrganizationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationsService {

    @Autowired
    OrganizationsRepository organizationsRepository;

    public Organizations saveOrg(Organizations organization) {
        return organizationsRepository.saveAndFlush(organization);
    }

}
