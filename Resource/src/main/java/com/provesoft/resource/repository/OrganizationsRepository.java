package com.provesoft.resource.repository;


import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrganizationsRepository extends JpaRepository<Organizations, Long> {

    List<Organizations> findByCompanyNameOrderByOrganizationIdAsc(String companyName);

    List<Organizations> findByCompanyNameAndOrganizationIdIn(String companyName, List<Long> organizationIds);

    Organizations findByOrganizationIdAndCompanyName(Long organizationId, String companyName);

    Organizations findByNameAndCompanyName(String name, String companyName);

    // Update description
    @Query(
            "UPDATE Organizations o " +
            "SET o.description=:newDescription " +
            "WHERE o.organizationId=:organizationId " +
            "AND o.companyName=:companyName"
    )
    @Modifying
    @Transactional
    void updateDescription(@Param(value="organizationId") Long organizationId,
                           @Param(value="companyName") String companyName,
                           @Param(value="newDescription") String newDescription);
    
}
