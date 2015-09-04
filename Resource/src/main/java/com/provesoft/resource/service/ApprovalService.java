package com.provesoft.resource.service;

import com.provesoft.resource.entity.Document.ApprovalHistory;
import com.provesoft.resource.entity.Document.ApprovalNotification;
import com.provesoft.resource.repository.ApprovalHistoryRepository;
import com.provesoft.resource.repository.ApprovalNotificationRepository;
import com.provesoft.resource.repository.SignoffPathStepsRepository;
import com.provesoft.resource.repository.SignoffPathTemplateStepsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service contains all aspects regarding approvals (notifications and history)
 */
@Service
public class ApprovalService {

    @Autowired
    SignoffPathTemplateStepsRepository signoffPathTemplateStepsRepository;

    @Autowired
    SignoffPathStepsRepository signoffPathStepsRepository;

    @Autowired
    ApprovalNotificationRepository approvalNotificationRepository;

    @Autowired
    ApprovalHistoryRepository approvalHistoryRepository;


    /* ------------------------ ApprovalNotification -------------------------- */

    /**
     * Method retrieves approval notifications by notification Id
     * @param notificationId Notification id query parameter
     * @return ApprovalNotification
     */
    public ApprovalNotification getApprovalNotification(Long notificationId) {
        return approvalNotificationRepository.findById(notificationId);
    }

    /**
     * Method retrieves List of ApprovalNotification by company and userId
     * @param companyName Company query parameter
     * @param userId User Id query parameter
     * @return List of ApprovalNotification
     */
    public List<ApprovalNotification> getApprovalNotifications(String companyName, Long userId) {
        return approvalNotificationRepository.findByCompanyNameAndUserId(companyName, userId);
    }

    /**
     * Checks whether there exists a notification for the given stepId.
     * This is used to see if an admin was approving steps in a group further down the path.
     * If no notification is present for this stepId, no action should be taken in creating new notifications.
     * @param companyName Company query parameter
     * @param documentId DocumentId corresponding to step
     * @param stepId Step id for notification
     * @return Boolean of whether notification exists for step
     */
    public Boolean checkIfNotificationExistsForStepId(String companyName, String documentId, Long stepId) {
        if (approvalNotificationRepository.countByCompanyNameAndDocumentIdAndStepId(companyName, documentId, stepId) == 0) {
            return false;
        }
        return true;
    }

    /**
     * Method creates a new notification
     * @param notifications List of ApprovalNotifications to create
     * @return List of ApprovalNotification
     */
    public List<ApprovalNotification> addApprovalNotifications(List<ApprovalNotification> notifications) {
        List<ApprovalNotification> addedNotifications = approvalNotificationRepository.save(notifications);
        approvalNotificationRepository.flush();
        return addedNotifications;
    }

    /**
     * Method removes notifications by stepId list, companyName, and documentId.
     * All notifications associated with this document can be removed.
     * @param companyName Company query parameter
     * @param documentId Document Id to remove notifications for
     */
    public void removeApprovalNotifications(String companyName, String documentId) {
        approvalNotificationRepository.deleteByCompanyNameAndDocumentId(companyName, documentId);
    }


    /* ---------------------------- ApprovalHistory ------------------------------ */

    /**
     * Method returns first 5 most recent approvals.
     * @param companyName Company query parameter
     * @param documentId Document id for which approvals were completed for
     * @return List of ApprovalHistory
     */
    public List<ApprovalHistory> getRecentApprovals(String companyName, String documentId) {
        return approvalHistoryRepository.findFirst5ByCompanyNameAndDocumentIdOrderByDateDesc(companyName, documentId);
    }

    /**
     * Method returns all ApprovalHistory for a specific DocumentRevision
     * @param companyName Company query parameter
     * @param documentId Document Id query parameter
     * @param revisionId Revision Id query parameter
     * @return
     */
    public List<ApprovalHistory> getApprovalHistoryByRevision(String companyName, String documentId, String revisionId) {
        return approvalHistoryRepository.findByCompanyNameAndDocumentIdAndRevisionIdOrderByDateAsc(companyName, documentId, revisionId);
    }

}
