package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Authorities;
import com.provesoft.resource.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AuthoritiesRepository extends JpaRepository<Authorities, Long> {

    List<Authorities> findByUser(Users user);

    @Query(
            "DELETE FROM Authorities a " +
            "WHERE a.user=:user"
    )
    @Modifying
    @Transactional
    void deleteByUsername(@Param(value = "user") Users user);
}
