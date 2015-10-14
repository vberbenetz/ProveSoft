package com.provesoft.resource.service;

import com.provesoft.resource.entity.BetaKeys;
import com.provesoft.resource.repository.BetaKeysRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BetaService {

    @Autowired
    BetaKeysRepository betaKeysRepository;

    public List<BetaKeys> findAllKeys() {
        return betaKeysRepository.findAllKeys();
    }

    public BetaKeys addNewKey(String email, String key) {
        return betaKeysRepository.saveAndFlush( new BetaKeys(email, key) );
    }

    public void removeKeyByEmail(String email) {
        betaKeysRepository.removeByEmail(email);
    }
}
