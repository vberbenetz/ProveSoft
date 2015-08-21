package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentLike;
import com.provesoft.resource.entity.Document.DocumentLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentLikeRepository extends JpaRepository<DocumentLike, DocumentLikeKey> {
}
