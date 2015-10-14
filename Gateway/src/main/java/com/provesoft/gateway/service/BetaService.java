package com.provesoft.gateway.service;

import com.provesoft.gateway.entity.BetaKeys;
import com.provesoft.gateway.repository.BetaKeysRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BetaService {

    @Autowired
    BetaKeysRepository betaKeysRepository;

    public BetaKeys findKeyByEmailAndBetaKey(String email, String key) {
        return betaKeysRepository.findByEmailAndBetaKey(email, key);
    }

    public void deleteBetaKey(BetaKeys betaKey) {
        betaKeysRepository.delete(betaKey);
        betaKeysRepository.flush();
    }
}
