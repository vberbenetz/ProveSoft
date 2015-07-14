package com.provesoft.resource.repository;

import com.provesoft.resource.entity.UserPermissions;
import com.provesoft.resource.entity.UserPermissionsKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserPermissionsRepository extends JpaRepository<UserPermissions, UserPermissionsKey> {

    List<UserPermissions> findByKeyUserId(Long userId);

    @Query(
            "DELETE FROM UserPermissions up " +
            "WHERE up.key.userId=:userId"
    )
    @Modifying
    @Transactional
    void deleteByUserId(@Param("userId") Long userId);

}
