package com.provesoft.resource.repository;

import com.provesoft.resource.entity.BetaKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BetaKeysRepository extends JpaRepository<BetaKeys, String> {

    @Query(
            "DELETE FROM BetaKeys bk " +
            "WHERE bk.email=:email"
    )
    @Transactional
    @Modifying
    void removeByEmail(@Param("email") String email);
}
