package com.provesoft.resource.repository;


import com.provesoft.resource.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RolesRepository extends JpaRepository<Roles, Long> {

    List<Roles> findByCompanyName(String companyName);

    Roles findByCompanyNameAndRoleId(String companyName, Long roleId);

    List<Roles> findByCompanyNameAndRoleIdIn(String company, List<Long> roleIds);

    @Query(
            "DELETE FROM Roles r " +
            "WHERE r.companyName=:companyName " +
            "AND r.roleId=:roleId"
    )
    @Modifying
    @Transactional
    void deleteRoleByRoleId(@Param("roleId") Long roleId, @Param("companyName") String companyName);
}
