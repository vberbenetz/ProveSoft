package com.provesoft.resource.repository;


import com.provesoft.resource.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {

    List<UserDetails> findAllByCompanyNameOrderByLastNameAsc(String companyName);

    List<UserDetails> findFirst10ByCompanyNameOrderByLastNameAsc(String companyName);

    String findCompanyNameByUserId(Long userId);

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

}
