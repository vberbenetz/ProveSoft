package com.provesoft.resource.repository;


import com.provesoft.resource.entity.RoleUser;
import com.provesoft.resource.entity.RoleUserKey;
import com.provesoft.resource.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoleUserRepository extends JpaRepository<RoleUser, RoleUserKey> {

    List<RoleUser> findByKeyCompanyNameAndKeyRoleId(String company, Long roleId);

    List<RoleUser> findByKeyCompanyNameAndKeyUserId(String companyName, Long userId);

    // Delete RoleUser
    @Query(
            "DELETE FROM RoleUser ru " +
            "WHERE ru.key.companyName=:companyName " +
            "AND ru.key.roleId=:roleId " +
            "AND ru.key.userId=:userId"
    )
    @Modifying
    @Transactional
    void deleteRoleUser(@Param(value="roleId") Long roleId,
                        @Param(value="userId") Long userId,
                        @Param(value="companyName") String companyName);

    // Delete all RoleUser by User
    @Query(
            "DELETE FROM RoleUser ru " +
            "WHERE ru.key.companyName=:companyName " +
            "AND ru.key.userId=:userId"
    )
    @Modifying
    @Transactional
    void deleteAllRoleUserByUser(@Param(value="userId") Long userId,
                                 @Param(value="companyName") String companyName);

    // Delete all RoleUser by Role Id
    @Query(
            "DELETE FROM RoleUser ru " +
            "WHERE ru.key.companyName=:companyName " +
            "AND ru.key.roleId=:roleId"
    )
    @Modifying
    @Transactional
    void deleteAllRoleUserByRoleId(@Param(value="roleId") Long roleId,
                                   @Param(value="companyName") String companyName);

}
