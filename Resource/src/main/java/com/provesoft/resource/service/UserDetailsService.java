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
        return userDetailsRepository.findAllByCompanyName(companyName);
    }

    public String findCompanyByUserId(Long userId) {
        return userDetailsRepository.findCompanyNameByUserId(userId);
    }

    public void addUser(UserDetails newUser) {
        userDetailsRepository.save(newUser);
    }
}
