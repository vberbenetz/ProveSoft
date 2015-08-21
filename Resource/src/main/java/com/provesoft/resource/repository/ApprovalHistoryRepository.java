package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {

    List<ApprovalHistory> findFirst10ByCompanyNameAndDocumentIdOrderByDateDesc(String companyName, String documentId);
}
