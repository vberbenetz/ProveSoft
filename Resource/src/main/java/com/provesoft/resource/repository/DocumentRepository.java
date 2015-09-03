package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, String> {

    Document findByCompanyNameAndId(String companyName, String id);

    List<Document> findByCompanyNameAndIdIn(String companyName, String[] ids);

    List<Document> findFirst10ByCompanyNameOrderByIdAsc(String companyName);

    List<Document> findByCompanyNameAndState(String companyName, String state);

    @Query(
            "SELECT d " +
            "FROM Document d " +
            "WHERE d.companyName=:companyName " +
            "AND d.title LIKE :title"
    )
    List<Document> searchByTitle (@Param("companyName") String companyName,
                                  @Param("title") String title);

    @Query(
            "SELECT d " +
            "FROM Document d " +
            "WHERE d.companyName=:companyName " +
            "AND d.id LIKE :id"
    )
    List<Document> searchById (@Param("companyName") String companyName,
                               @Param("id") String id);

    @Query(
            "SELECT d " +
            "FROM Document d " +
            "WHERE d.companyName=:companyName " +
            "AND d.organization.name LIKE :name"
    )
    List<Document> searchByOrganizationName (@Param("companyName") String companyName,
                                             @Param("name") String name);

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
    List<Document> wildCardSearch (@Param("companyName") String companyName,
                                   @Param("searchString") String searchString);

}
