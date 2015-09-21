package com.provesoft.resource.service;

import com.provesoft.resource.entity.Authorities;
import com.provesoft.resource.entity.NewUserTokens;
import com.provesoft.resource.entity.Users;
import com.provesoft.resource.repository.AuthoritiesRepository;
import com.provesoft.resource.repository.NewUserTokensRepository;
import com.provesoft.resource.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service contains all methods related to Users
 */
@Service
public class UsersService {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    AuthoritiesRepository authoritiesRepository;

    @Autowired
    NewUserTokensRepository newUserTokensRepository;

    /**
     * Retrieve a user by email
     * @param email
     * @return
     */
    public Users getUser(String email) {
        return usersRepository.findByUsername(email);
    }

    /**
     * Method checks if a user with that email already exists
     * @param email
     * @return Boolean
     */
    public Boolean doesUserExist(String email) {
        if (usersRepository.findByUsername(email) == null) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Retrieve list of authorities for user
     * @param email
     * @return
     */
    public List<Authorities> getAuthorities(String email) {
        Users user = usersRepository.findByUsername(email);
        return authoritiesRepository.findByUser(user);
    }



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

    /**
     * Method saves a new recovery token for the addition of a new user
     * @param newUserToken
     * @return
     */
    public NewUserTokens saveNewUserToken(NewUserTokens newUserToken) {
        return newUserTokensRepository.saveAndFlush(newUserToken);
    }



    /**
     * Delete a user from the system (email is freed up)
     * @param email
     */
    public void deleteUser(String email) {
        usersRepository.deleteByUsername(email);
    }

    /**
     * Delete all authorities for a user
     * @param email
     */
    public void deleteAuthorities(String email) {
        Users user = usersRepository.findByUsername(email);
        authoritiesRepository.deleteByUsername(user);
    }

}
