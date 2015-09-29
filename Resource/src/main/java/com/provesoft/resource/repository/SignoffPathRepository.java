package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SignoffPath.SignoffPath;
import com.provesoft.resource.entity.SignoffPath.SignoffPathKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SignoffPathRepository extends JpaRepository<SignoffPath, SignoffPathKey> {

    SignoffPath findByKeyCompanyNameAndKeyPathId(String companyName, Long pathId);

    List<SignoffPath> findFirst10ByKeyCompanyNameAndKeyPathIdLikeOrderByKeyPathIdAsc(String companyName, Long pathId);

    List<SignoffPath> findFirst10ByKeyCompanyNameAndNameLikeOrderByNameAsc(String companyName, String name);

    List<SignoffPath> findFirst10ByKeyCompanyNameOrderByKeyPathIdAsc(String companyName);

    Long countByKeyCompanyNameAndName(String companyName, String name);

    @Query(
            "SELECT s " +
            "FROM SignoffPath s " +
            "WHERE s.key.companyName=:companyName " +
            "AND (" +
                "(s.organization.organizationId=:organizationId " +
                "AND s.applyToAll=false) " +
                "OR s.applyToAll=true " +
            ")"
    )
    List<SignoffPath> getPathsByCompanyNameAndOrganizationId(@Param("companyName") String companyName,
                                                             @Param("organizationId") Long organizationId);
}
