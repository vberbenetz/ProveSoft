package com.provesoft.gateway.repository;

import com.provesoft.gateway.entity.RecoveryTokens;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecoveryTokensRepository extends JpaRepository<RecoveryTokens, String> {

    public RecoveryTokens findByToken(String token);

    public RecoveryTokens findByEmail(String email);
}
