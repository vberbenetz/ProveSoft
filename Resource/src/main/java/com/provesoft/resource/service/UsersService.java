package com.provesoft.resource.service;

import com.provesoft.resource.entity.Authorities;
import com.provesoft.resource.entity.Users;
import com.provesoft.resource.repository.AuthoritiesRepository;
import com.provesoft.resource.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsersService {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    AuthoritiesRepository authoritiesRepository;

    public Users saveUser(Users user) {
        return usersRepository.saveAndFlush(user);
    }

    public void saveAuthority(Authorities authority) {
        authoritiesRepository.saveAndFlush(authority);
    }

}
