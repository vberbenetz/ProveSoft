package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {
}
