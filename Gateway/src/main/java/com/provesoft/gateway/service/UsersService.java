package com.provesoft.gateway.service;

import com.provesoft.gateway.entity.Authorities;
import com.provesoft.gateway.entity.RecoveryTokens;
import com.provesoft.gateway.entity.Users;
import com.provesoft.gateway.exceptions.CompanyExistsException;
import com.provesoft.gateway.exceptions.UserExistsException;
import com.provesoft.gateway.repository.AuthoritiesRepository;
import com.provesoft.gateway.repository.RecoveryTokensRepository;
import com.provesoft.gateway.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.transaction.TransactionRolledbackException;
import java.util.List;

@Service
public class UsersService {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    AuthoritiesRepository authoritiesRepository;

    @Autowired
    RecoveryTokensRepository recoveryTokensRepository;


    public Users findUserByEmail(String email) {
        return usersRepository.findByUsername(email);
    }

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
        } catch (NullPointerException npe) {
            return false;
        }
    }

    public void saveAuthority(Authorities authority) {
        authoritiesRepository.saveAndFlush(authority);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Users checkAndSaveUser(Users newUser) throws TransactionRolledbackException, UserExistsException {
        if (!doesUserExist(newUser.getUsername())) {
            return usersRepository.saveAndFlush(newUser);
        }

        throw new UserExistsException();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Authorities checkAndSaveCompany(Authorities authority, String companyName) throws TransactionRolledbackException, CompanyExistsException {
        if (!doesCompanyExist(companyName)) {
            return authoritiesRepository.saveAndFlush(authority);
        }

        throw new CompanyExistsException();
    }

    public void updateUser(Users userToUpdate) {
        usersRepository.saveAndFlush(userToUpdate);
    }

    public void deleteUser(Users userToDelete) {
        usersRepository.delete(userToDelete);
        usersRepository.flush();
    }


    /* ------------------------- RecoveryTokens ----------------------------- */

    public RecoveryTokens findTokenByTokenId(String token) {
        return recoveryTokensRepository.findByToken(token);
    }

    public RecoveryTokens findTokenByEmail(String email) {
        return recoveryTokensRepository.findByEmail(email);
    }

    public void saveRecoveryToken(RecoveryTokens newToken) {
        recoveryTokensRepository.saveAndFlush(newToken);
    }

    public void deleteRecoveryToken(RecoveryTokens oldToken) {
        recoveryTokensRepository.delete(oldToken);
        recoveryTokensRepository.flush();
    }

}
