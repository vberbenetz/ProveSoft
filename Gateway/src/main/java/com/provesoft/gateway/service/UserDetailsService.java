package com.provesoft.gateway.service;

import com.provesoft.gateway.entity.UserDetails;
import com.provesoft.gateway.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService {

    @Autowired
    UserDetailsRepository userDetailsRepository;

    public UserDetails addUser(UserDetails newUser) {
        return userDetailsRepository.saveAndFlush(newUser);
    }

}
