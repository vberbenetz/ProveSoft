package com.provesoft.resource.service;

import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.entity.UserPermissions;
import com.provesoft.resource.repository.UserDetailsRepository;
import com.provesoft.resource.repository.UserPermissionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsService {

    @Autowired
    UserDetailsRepository userDetailsRepository;

    @Autowired
    UserPermissionsRepository userPermissionsRepository;


    public List<UserDetails> findAllByCompanyName(String companyName) {
        return userDetailsRepository.findAllByCompanyNameOrderByLastNameAsc(companyName);
    }

    public List<UserDetails> findFirst10ByCompanyName(String companyName) {
        return userDetailsRepository.findFirst10ByCompanyNameOrderByLastNameAsc(companyName);
    }

    public UserDetails findByCompanyNameAndUserId(String companyName, Long userId) {
        return userDetailsRepository.findByCompanyNameAndUserId(companyName, userId);
    }

    public UserDetails findByCompanyNameAndEmail(String companyName, String email) {
        return userDetailsRepository.findByCompanyNameAndEmail(companyName, email);
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

    public void deleteByUserId(String companyName, Long userId) {
        userDetailsRepository.deleteByUserId(companyName, userId);
    }


    /* ---------------------- UserPermissions -------------------- */

    public List<UserPermissions> findUserPermissionsByUserId(Long userId) {
        return userPermissionsRepository.findByKeyUserId(userId);
    }

    public List<UserPermissions> addUserPermissions(List<UserPermissions> userPermissions) {
        List<UserPermissions> savedUserPermissions = userPermissionsRepository.save(userPermissions);
        userPermissionsRepository.flush();
        return savedUserPermissions;
    }

    public void deleteList(List<UserPermissions> userPermissions) {
        userPermissionsRepository.deleteInBatch(userPermissions);
        userPermissionsRepository.flush();
    }

    public void deleteAllPermissions(Long userId) {
        userPermissionsRepository.deleteByUserId(userId);
    }
}
