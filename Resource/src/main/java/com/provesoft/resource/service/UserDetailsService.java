package com.provesoft.resource.service;

import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsService {

    @Autowired
    UserDetailsRepository userDetailsRepository;

    public List<UserDetails> findAllByCompanyName(String companyName) {
        return userDetailsRepository.findAllByCompanyNameOrderByLastNameAsc(companyName);
    }

    public List<UserDetails> findFirst10ByCompanyName(String companyName) {
        return userDetailsRepository.findFirst10ByCompanyNameOrderByLastNameAsc(companyName);
    }

    public List<UserDetails> findByCompanyNameAndUserIdList(String companyName, List<Long> userIds) {
        return userDetailsRepository.findByCompanyNameAndUserIdIn(companyName, userIds);
    }

    public List<UserDetails> findByCompanyAndPartialName(String companyName, String name) {
        List<UserDetails> results = userDetailsRepository.findByCompanyAndPartialName(companyName, name);
        int upToIndex = 0;

        if (results.size() < 20) {
            upToIndex = results.size();
        }
        else {
            upToIndex = 20;
        }

        return results.subList(0, upToIndex);
    }

    public String findCompanyByUserId(Long userId) {
        return userDetailsRepository.findCompanyNameByUserId(userId);
    }

    public UserDetails addUser(UserDetails newUser) {
        return userDetailsRepository.saveAndFlush(newUser);
    }

    public void updatePrimaryOrganization(Long primaryOrgId, Long userId, String companyName) {
        userDetailsRepository.updatePrimaryOrganization(primaryOrgId, userId, companyName);
    }

    public void deleteByUserId(Long userId, String companyName) {
        userDetailsRepository.deleteByUserId(userId, companyName);
    }
}
