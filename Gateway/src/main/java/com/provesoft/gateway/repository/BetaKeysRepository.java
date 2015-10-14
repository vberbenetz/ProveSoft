package com.provesoft.gateway.repository;

import com.provesoft.gateway.entity.BetaKeys;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetaKeysRepository extends JpaRepository<BetaKeys, String> {

    BetaKeys findByEmailAndBetaKey(String email, String betaKey);
}
