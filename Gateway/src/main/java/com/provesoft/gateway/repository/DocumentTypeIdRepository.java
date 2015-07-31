package com.provesoft.gateway.repository;

import com.provesoft.gateway.entity.DocumentTypeId;
import com.provesoft.gateway.entity.DocumentTypeIdKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypeIdRepository extends JpaRepository<DocumentTypeId, DocumentTypeIdKey> {
}
