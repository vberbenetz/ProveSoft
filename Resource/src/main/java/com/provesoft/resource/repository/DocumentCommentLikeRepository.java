package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentCommentLike;
import com.provesoft.resource.entity.Document.DocumentCommentLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentCommentLikeRepository extends JpaRepository<DocumentCommentLike, DocumentCommentLikeKey> {

    Long countByKeyCompanyNameAndKeyDocumentCommentId(String companyName, Long documentCommentId);

    List<DocumentCommentLike> findByKeyCompanyNameAndKeyDocumentCommentIdIn(String companyName, Long[] documentCommentIds);
}
