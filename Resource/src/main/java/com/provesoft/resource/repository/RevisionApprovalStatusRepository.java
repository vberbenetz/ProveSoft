package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.RevisionApprovalStatus;
import com.provesoft.resource.entity.Document.RevisionApprovalStatusKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisionApprovalStatusRepository extends JpaRepository<RevisionApprovalStatus, RevisionApprovalStatusKey> {

    RevisionApprovalStatus findByKeyCompanyNameAndKeyDocumentId(String companyName, String documentId);
}
