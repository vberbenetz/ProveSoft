package com.provesoft.resource.repository;

import com.provesoft.resource.entity.OrgUser;
import com.provesoft.resource.entity.OrgUserKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface OrgUserRepository extends JpaRepository<OrgUser, OrgUserKey> {

    List<OrgUser> findByKeyCompanyNameAndKeyUserId(String companyName, Long userId);

    List<OrgUser> findByKeyCompanyNameAndKeyOrganizationId(String companyName, Long organizationId);

    // Delete OrgUser
    @Query(
            "DELETE FROM OrgUser ou " +
            "WHERE ou.key.companyName=:companyName " +
            "AND ou.key.organizationId=:orgId " +
            "AND ou.key.userId=:userId"
    )
    @Modifying
    @Transactional
    void deleteOrgUser(@Param(value="orgId") Long orgId,
                       @Param(value="userId") Long userId,
                       @Param(value="companyName") String companyName);

    // Delete all OrgUser by UserId
    @Query(
            "DELETE FROM OrgUser ou " +
            "WHERE ou.key.companyName=:companyName " +
            "AND ou.key.userId=:userId"
    )
    @Modifying
    @Transactional
    void deleteAllOrgUserByUserId(@Param(value="companyName") String companyName,
                                  @Param(value="userId") Long userId);
}
