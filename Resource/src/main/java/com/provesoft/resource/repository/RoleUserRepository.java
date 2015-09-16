package com.provesoft.resource.repository;

import com.provesoft.resource.entity.RoleUser;
import com.provesoft.resource.entity.RoleUserKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoleUserRepository extends JpaRepository<RoleUser, RoleUserKey> {

    List<RoleUser> findByKeyUserId(Long userId);

    List<RoleUser> findByKeyRoleId(Long roleId);

    Long countByKeyCompanyNameAndKeyRoleId (String companyName, Long roleId);

    @Query (
            "DELETE FROM RoleUser ru " +
            "WHERE ru.key.companyName=:companyName " +
            "AND ru.key.userId=:userId " +
            "AND ru.key.roleId=:roleId"
    )
    @Modifying
    @Transactional
    void deleteRoleUser(@Param("companyName") String companyName,
                        @Param("userId") Long userId,
                        @Param("roleId") Long roleId);

    @Query (
            "DELETE FROM RoleUser ru " +
            "WHERE ru.key.companyName=:companyName " +
            "AND ru.key.userId=:userId"
    )
    @Modifying
    @Transactional
    void deleteAllByUserId(@Param("companyName") String companyName,
                           @Param("userId") Long userId);

}
