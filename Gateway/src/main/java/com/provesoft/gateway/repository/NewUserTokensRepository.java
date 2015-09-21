package com.provesoft.gateway.repository;

import com.provesoft.gateway.entity.NewUserTokens;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewUserTokensRepository extends JpaRepository<NewUserTokens, String> {

    NewUserTokens findByToken(String token);
}
