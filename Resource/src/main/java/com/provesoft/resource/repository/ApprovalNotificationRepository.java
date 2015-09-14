package com.provesoft.resource.repository;


import com.provesoft.resource.entity.Document.ApprovalNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ApprovalNotificationRepository extends JpaRepository<ApprovalNotification, Long> {

    ApprovalNotification findById(Long id);

    ApprovalNotification findByCompanyNameAndDocumentIdAndStepId(String companyName, String documentId, Long stepId);

    Long countByCompanyNameAndDocumentIdAndStepId(String companyName, String documentId, Long stepId);

    List<ApprovalNotification> findByCompanyNameAndUserId(String companyName, Long userId);

    @Query(
            "SELECT an.nextState " +
            "FROM ApprovalNotification an " +
            "WHERE an.companyName=:companyName " +
            "AND an.documentId=:documentId " +
            "AND an.stepId=:stepId"
    )
    String getNextState(@Param("companyName") String companyName,
                        @Param("documentId") String documentId,
                        @Param("stepId") Long stepId);

    @Query(
            "DELETE FROM ApprovalNotification an " +
            "WHERE an.companyName=:companyName " +
            "AND an.documentId=:documentId"
    )
    @Modifying
    @Transactional
    void deleteByCompanyNameAndDocumentId(@Param("companyName") String companyName,
                                          @Param("documentId") String documentId);
}
