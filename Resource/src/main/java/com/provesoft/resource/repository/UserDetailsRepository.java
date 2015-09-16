package com.provesoft.resource.repository;


import com.provesoft.resource.entity.Organizations;
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

    UserDetails findByCompanyNameAndUserId(String companyName, Long userId);

    UserDetails findByCompanyNameAndEmail(String companyName, String email);

    List<UserDetails> findByCompanyNameAndUserIdIn(String companyName, List<Long> userId);

    Long countByCompanyNameAndPrimaryOrganization(String companyName, Organizations primaryOrganization);

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

    // Get userId by email
    @Query(
            "SELECT u.userId " +
            "FROM UserDetails u " +
            "WHERE u.companyName=:companyName " +
            "AND u.email=:email"
    )
    Long findUserIdByCompanyNameAndEmail(@Param("companyName") String companyName,
                                         @Param("email") String email);

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
