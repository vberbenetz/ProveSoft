package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentCommentRepository extends JpaRepository<DocumentComment, Long> {

    List<DocumentComment> findFirst5ByCompanyNameAndDocumentIdOrderByDateDesc(String companyName, String documentId);
}
