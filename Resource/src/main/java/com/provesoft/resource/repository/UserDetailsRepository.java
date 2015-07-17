package com.provesoft.resource.repository;


import com.provesoft.resource.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {

    List<UserDetails> findAllByCompanyNameOrderByLastNameAsc(String companyName);

    List<UserDetails> findFirst10ByCompanyNameOrderByLastNameAsc(String companyName);

    String findCompanyNameByUserId(Long userId);

    UserDetails findByCompanyNameAndUserId(String companyName, Long userId);

    UserDetails findByCompanyNameAndUserName(String companyName, String userName);

    List<UserDetails> findByCompanyNameAndUserIdIn(String companyName, List<Long> userId);

    // Get subset of users based on search
    @Query(
            "SELECT DISTINCT u " +
            "FROM UserDetails u " +
            "WHERE u.companyName=:companyName " +
            "AND " +
                    "(u.firstName LIKE :name " +
                    "OR u.lastName LIKE :name) " +
            "ORDER BY u.lastName ASC"
    )
    List<UserDetails> findByCompanyAndPartialName(@Param("companyName") String companyName,
                                                  @Param("name") String name);

    // Update the primary organization id relating to the user
    @Query(
            "UPDATE UserDetails u " +
            "SET u.primaryOrgId=:primaryOrgId " +
            "WHERE u.companyName=:companyName " +
            "AND u.userId=:userId"
    )
    @Modifying
    @Transactional
    int updatePrimaryOrganization(@Param("primaryOrgId") Long primaryOrgId,
                                   @Param("userId") Long userId,
                                   @Param("companyName") String companyName);

    // Delete user by userId
    @Query(
            "DELETE FROM UserDetails u " +
            "WHERE u.userId=:userId " +
            "AND u.companyName=:companyName"
    )
    @Modifying
    @Transactional
    void deleteByUserId(@Param("companyName") String companyName,
                        @Param("userId") Long userId);
}
