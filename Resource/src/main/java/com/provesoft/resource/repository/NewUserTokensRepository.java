package com.provesoft.resource.repository;

import com.provesoft.resource.entity.NewUserTokens;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewUserTokensRepository extends JpaRepository<NewUserTokens, String> {

    public NewUserTokens findByToken(String token);

    public NewUserTokens findByEmail(String email);
}
