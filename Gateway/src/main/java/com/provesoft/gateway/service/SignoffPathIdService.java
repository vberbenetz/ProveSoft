package com.provesoft.gateway.service;

import com.provesoft.gateway.entity.SignoffPathId;
import com.provesoft.gateway.repository.SignoffPathIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignoffPathIdService {

    @Autowired
    SignoffPathIdRepository signoffPathIdRepository;

    public void intializeSignoffPathId(SignoffPathId signoffPathId) {
        signoffPathIdRepository.saveAndFlush(signoffPathId);
    }

}
