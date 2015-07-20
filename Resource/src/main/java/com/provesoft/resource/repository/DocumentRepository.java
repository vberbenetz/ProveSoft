package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, String> {

    Document findByCompanyNameAndId(String companyName, String id);

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

}