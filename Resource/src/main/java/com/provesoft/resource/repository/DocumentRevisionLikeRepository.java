package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentRevisionLike;
import com.provesoft.resource.entity.Document.DocumentRevisionLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRevisionLikeRepository extends JpaRepository<DocumentRevisionLike, DocumentRevisionLikeKey> {

    List<DocumentRevisionLike> findByKeyCompanyNameAndKeyDocumentIdAndKeyRevisionId(String companyName,
                                                                                    String documentId,
                                                                                    String revisionId);
}
