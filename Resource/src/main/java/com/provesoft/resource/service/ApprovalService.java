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

    /*
        Retrieve approval notifications by notification Id
     */
    public ApprovalNotification getApprovalNotification(Long notificationId) {
        return approvalNotificationRepository.findById(notificationId);
    }

    /*
        Retrieve approval notifications by user
     */
    public List<ApprovalNotification> getApprovalNotifications(String companyName, Long userId) {
        return approvalNotificationRepository.findByCompanyNameAndUserId(companyName, userId);
    }

    /*
        Checks whether there exists a notification for the given stepId.
        This is used to see if an admin was approving steps in a group further down the path.
        If no notification is present for this stepId, no action should be taken in creating new notifications.
     */
    public Boolean checkIfNotificationExistsForStepId(String companyName, String documentId, Long stepId) {
        if (approvalNotificationRepository.countByCompanyNameAndDocumentIdAndStepId(companyName, documentId, stepId) == 0) {
            return false;
        }
        return true;
    }

    /*
        Add approval notification
     */
    public List<ApprovalNotification> addApprovalNotifications(List<ApprovalNotification> notifications) {
        List<ApprovalNotification> addedNotifications = approvalNotificationRepository.save(notifications);
        approvalNotificationRepository.flush();
        return addedNotifications;
    }

    /*
        Remove notifications by stepId list, companyName, and documentId.
        All notifications associated with this document can be removed.
     */
    public void removeApprovalNotifications (String companyName, String documentId) {
        approvalNotificationRepository.deleteByCompanyNameAndDocumentId(companyName, documentId);
    }


    /* ---------------------------- ApprovalHistory ------------------------------ */

    public List<ApprovalHistory> getRecentApprovals(String companyName, String documentId) {
        return approvalHistoryRepository.findFirst5ByCompanyNameAndDocumentIdOrderByDateDesc(companyName, documentId);
    }

}
