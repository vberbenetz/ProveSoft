package com.provesoft.gateway.service;

import com.provesoft.gateway.entity.CompanyDetails;
import com.provesoft.gateway.repository.CompanyDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    @Autowired
    CompanyDetailsRepository companyDetailsRepository;

    public CompanyDetails createNewCompany(CompanyDetails cd) {
        return companyDetailsRepository.saveAndFlush(cd);
    }
}
