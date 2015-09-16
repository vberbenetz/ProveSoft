package com.provesoft.resource.repository;

import com.provesoft.resource.entity.RolePermissions;
import com.provesoft.resource.entity.RolePermissionsKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RolePermissionsRepository extends JpaRepository<RolePermissions, RolePermissionsKey> {

    List<RolePermissions> findByKeyRoleId(Long roleId);

    Long countByKeyOrganizationId (Long organizationId);

    Long countByKeyRoleId (Long roleId);

    @Query (
            "DELETE FROM RolePermissions rp " +
            "WHERE rp.key.roleId=:roleId"
    )
    @Modifying
    @Transactional
    void deleteByRoleId(@Param("roleId") Long roleId);
}
