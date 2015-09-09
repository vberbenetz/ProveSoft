package com.provesoft.gateway.service;

import com.provesoft.gateway.entity.Authorities;
import com.provesoft.gateway.entity.Users;
import com.provesoft.gateway.repository.AuthoritiesRepository;
import com.provesoft.gateway.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    AuthoritiesRepository authoritiesRepository;

    public Boolean doesUserExist(String email) {
        if (usersRepository.findByUsername(email) == null) {
            return false;
        }
        else {
            return true;
        }
    }

    public Boolean doesCompanyExist(String companyName) {
        List<Authorities> companyAuth = authoritiesRepository.findByAuthority(companyName);

        try {
            if ((companyAuth == null) || (companyAuth.size() == 0)) {
                return false;
            } else {
                return true;
            }
        }
        catch (NullPointerException npe) {
            return false;
        }
    }

    public Users saveUser(Users user) {
        return usersRepository.saveAndFlush(user);
    }

    public void saveAuthority(Authorities authority) {
        authoritiesRepository.saveAndFlush(authority);
    }

}
