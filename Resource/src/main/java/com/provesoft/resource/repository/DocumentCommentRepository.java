package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentCommentRepository extends JpaRepository<DocumentComment, Long> {

    @Query(
            "SELECT dc " +
            "FROM DocumentComment dc " +
            "WHERE dc.companyName=:companyName " +
            "AND dc.documentId=:documentId " +
            "AND dc.parentCommentId IS NULL " +
            "ORDER BY dc.date DESC"
    )
    List<DocumentComment> findFirst5ParentsByCompanyNameAndDocumentIdOrderByDateDesc(@Param("companyName") String companyName,
                                                                                     @Param("documentId") String documentId);

    @Query(
            "SELECT dc " +
            "FROM DocumentComment dc " +
            "WHERE dc.companyName=:companyName " +
            "AND dc.parentCommentId IN :parentCommentIds " +
            "ORDER BY dc.parentCommentId DESC, dc.id ASC"
    )
    List<DocumentComment> findChildrenByCompanyNameAndParentDocumentIdList(@Param("companyName") String companyName,
                                                                           @Param("parentCommentIds") Long[] parentCommentIds);

    @Query(
            "SELECT dc " +
            "FROM DocumentComment dc " +
            "WHERE dc.companyName=:companyName " +
            "AND dc.parentCommentId IS NULL " +
            "ORDER by dc.date DESC"
    )
    List<DocumentComment> findFirst5ParentsByCompanyNameOrderByDateDesc(@Param("companyName") String companyName);
}
