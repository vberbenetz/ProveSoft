package com.provesoft.gateway.service;

import com.provesoft.gateway.entity.Authorities;
import com.provesoft.gateway.entity.Users;
import com.provesoft.gateway.repository.AuthoritiesRepository;
import com.provesoft.gateway.repository.UsersRepository;
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
