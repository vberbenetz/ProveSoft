package com.provesoft.resource.repository;

import com.provesoft.resource.entity.BetaKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BetaKeysRepository extends JpaRepository<BetaKeys, String> {

    @Query(
            "SELECT bk " +
            "FROM BetaKeys bk"
    )
    List<BetaKeys> findAllKeys();

    @Query(
            "DELETE FROM BetaKeys bk " +
            "WHERE bk.email=:email"
    )
    @Modifying
    @Transactional
    void removeByEmail(@Param("email") String email);
}
