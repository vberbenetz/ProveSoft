package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.Document;
import com.provesoft.resource.entity.Document.DocumentType;
import com.provesoft.resource.entity.Organizations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, String> {

    Document findByCompanyNameAndId(String companyName, String id);

    List<Document> findByCompanyNameAndIdIn(String companyName, String[] ids);

    List<Document> findFirst10ByCompanyNameAndStateNotOrderByIdAsc(String companyName, String state);

    List<Document> findByCompanyNameAndState(String companyName, String state);

    @Query(
            "SELECT d " +
            "FROM Document d " +
            "WHERE d.companyName=:companyName " +
            "AND d.organization.organizationId=:organizationId " +
            "ORDER BY d.documentType.name ASC, d.id ASC"
    )
    List<Document> findByOrganizationId (@Param("companyName") String companyName,
                                         @Param("organizationId") Long organizationId);

    @Query(
            "SELECT DISTINCT d " +
            "FROM Document d " +
            "WHERE d.companyName=:companyName " +
            "AND (" +
                "d.title LIKE :searchString " +
                "OR d.id LIKE :searchString " +
                "OR d.organization.name LIKE :searchString " +
            ")"
    )
    List<Document> wildCardSearchWithObsolete (@Param("companyName") String companyName,
                                               @Param("searchString") String searchString);

    @Query(
            "SELECT DISTINCT d " +
            "FROM Document d " +
            "WHERE d.companyName=:companyName " +
            "AND (" +
            "d.title LIKE :searchString " +
            "OR d.id LIKE :searchString " +
            "OR d.organization.name LIKE :searchString " +
            ") " +
            "AND d.state<>'Obsolete'"
    )
    List<Document> wildCardSearchNoObsolete (@Param("companyName") String companyName,
                                             @Param("searchString") String searchString);

    Long countByCompanyNameAndDocumentType (@Param("companyName") String companyName,
                                            @Param("documentType") DocumentType documentType);

    Long countByCompanyNameAndOrganization(@Param("companyName") String companyName,
                                           @Param("organization") Organizations organization);

    Long countByCompanyNameAndSignoffPathId(@Param("companyName") String companyName,
                                            @Param("signoffPathId") Long signoffPathId);

}
