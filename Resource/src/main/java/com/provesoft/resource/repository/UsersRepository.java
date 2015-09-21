package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UsersRepository extends JpaRepository<Users, String> {

    Users findByUsername(String username);

    @Query(
            "DELETE FROM Users u " +
            "WHERE u.username=:username"
    )
    @Modifying
    @Transactional
    void deleteByUsername(@Param(value = "username") String username);
}
