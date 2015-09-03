package com.provesoft.resource.service;

import com.provesoft.resource.entity.Authorities;
import com.provesoft.resource.entity.Users;
import com.provesoft.resource.repository.AuthoritiesRepository;
import com.provesoft.resource.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service contains all methods related to Users
 */
@Service
public class UsersService {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    AuthoritiesRepository authoritiesRepository;

    /**
     * Save a new user. Used by admin when adding a new user
     * @param user
     * @return Users
     */
    public Users saveUser(Users user) {
        return usersRepository.saveAndFlush(user);
    }

    /**
     * Save authority corresponding to user. Done when admin adds a new user
     * @param authority
     */
    public void saveAuthority(Authorities authority) {
        authoritiesRepository.saveAndFlush(authority);
    }

}
