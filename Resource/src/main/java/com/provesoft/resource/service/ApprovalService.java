package com.provesoft.resource.service;

import com.provesoft.resource.entity.Document.ApprovalNotification;
import com.provesoft.resource.entity.Document.RevisionApprovalStatus;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.repository.ApprovalNotificationRepository;
import com.provesoft.resource.repository.RevisionApprovalStatusRepository;
import com.provesoft.resource.repository.SignoffPathStepsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ApprovalService {

    @Autowired
    RevisionApprovalStatusRepository revisionApprovalStatusRepository;

    @Autowired
    SignoffPathStepsRepository signoffPathStepsRepository;

    @Autowired
    ApprovalNotificationRepository approvalNotificationRepository;


    /* ------------------------ RevisionStatusApproval -------------------------- */

    /*
        Retrieve list of userIds which already approved
     */
    public List<Long> getApprovedStepIds(String companyName, String documentId) {
        RevisionApprovalStatus r = revisionApprovalStatusRepository.findByKeyCompanyNameAndKeyDocumentId(companyName, documentId);

        // Break path sequence into groups separated on & (THEN step)
        String[] approvedStepIds = r.getApprovedSeq().split("&");

        List<Long> retApprovedStepIds = new ArrayList<>();
        for (String stepId : approvedStepIds) {
            if (!stepId.equals("")) {
                retApprovedStepIds.add( Long.parseLong(stepId) );
            }
        }

        List<SignoffPathSteps> approvedSteps = signoffPathStepsRepository.findByIdIn( retApprovedStepIds.toArray(new Long[retApprovedStepIds.size()]) );

        return retApprovedStepIds;
    }

    /*
        Retrieve next approver for revision
     */
    public List<SignoffPathSteps> getNextApprovalSteps(String companyName, String documentId) {
        RevisionApprovalStatus r = revisionApprovalStatusRepository.findByKeyCompanyNameAndKeyDocumentId(companyName, documentId);

        // Break path sequence into groups separated on & (THEN step)
        String[] requiredGroups = r.getSignoffPathSeq().split("&");
        String[] approvedSteps = r.getApprovedSeq().split("&");

        // Traverse approved steps and compare against requiredGroup
        for (String group : requiredGroups) {
            String[] stepIds = group.split("\\|");

            boolean stepInGroup = false;

            for (String stepId : stepIds) {

                // Traverse approved steps and compare if any required steps in this group have been approved
                for (String approvedStepId : approvedSteps) {
                    if (stepId.equals(approvedStepId)) {
                        stepInGroup = true;
                        break;
                    }
                }

                if (stepInGroup) {
                    break;
                }
            }

            // No required steps in this group have been approved yet. This group needs approvals sent out
            if (!stepInGroup) {

                // Convert string ids to long
                List<Long> longStepIds = new ArrayList<>();
                for (String stepId : stepIds) {
                    longStepIds.add(Long.parseLong(stepId));
                }

                // Get next steps for approvals
                return signoffPathStepsRepository.findByIdIn( longStepIds.toArray(new Long[longStepIds.size()]) );
            }
        }

        return null;
    }

    /*
        Retrieve list of stepIds in the current group of approving users.
        - currentStepId is one of the Ids in this group

        Returns null if not in chain
     */
    public List<Long> getCurrentApproversIds(String companyName, String documentId, Long currentStepId) {
        RevisionApprovalStatus r = revisionApprovalStatusRepository.findByKeyCompanyNameAndKeyDocumentId(companyName, documentId);

        // Break sequence into groups
        String[] requiredGroups = r.getSignoffPathSeq().split("&");

        // Traverse each group to see which on contains the currentStepId
        for (String group : requiredGroups) {
            String[] stepIds = group.split("\\|");

            for (String stepId : stepIds) {
                if (Long.parseLong(stepId) == currentStepId) {

                    List<Long> retStepIds = new ArrayList<>();

                    // Convert String values to Long ids
                    for (String s : stepIds) {
                        retStepIds.add(Long.parseLong(s));
                    }

                    return retStepIds;
                }
            }
        }

        return null;
    }

    /*
        Retrieve revision approval status
     */
    public RevisionApprovalStatus getApprovalStatusByCompanyAndDocumentId(String companyName, String documentId) {
        return revisionApprovalStatusRepository.findByKeyCompanyNameAndKeyDocumentId(companyName, documentId);
    }

    /*
        Create a new Approval Status record.
     */
    public RevisionApprovalStatus addApprovalStatus(RevisionApprovalStatus revisionApprovalStatus) {
        return revisionApprovalStatusRepository.saveAndFlush(revisionApprovalStatus);
    }

    /*
        Remove RevisionApprovalStatus when revision advances to "Released"
     */
    public void removeRevisionApprovalStatus(RevisionApprovalStatus revisionApprovalStatus) {
        revisionApprovalStatusRepository.delete(revisionApprovalStatus);
        revisionApprovalStatusRepository.flush();
    }

    /*
        Remove RevisionApprovalStatus by companyName and documentId when revision advances to "Released"
     */
    public void removeRevisionApprovalStatusByCompanyNameAndDocumentId(String companyName, String documentId) {
        RevisionApprovalStatus revToDelete = getApprovalStatusByCompanyAndDocumentId(companyName, documentId);
        revisionApprovalStatusRepository.delete(revToDelete);
        revisionApprovalStatusRepository.flush();
    }


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
        Add approval notification
     */
    public List<ApprovalNotification> addApprovalNotifications(List<ApprovalNotification> notifications) {
        List<ApprovalNotification> addedNotifications = approvalNotificationRepository.save(notifications);
        approvalNotificationRepository.flush();
        return addedNotifications;
    }

    /*
        Remove notification
     */
    public void removeApprovalNotification (ApprovalNotification notification) {
        approvalNotificationRepository.delete(notification);
        approvalNotificationRepository.flush();
    }

    /*
        Remove notifications by stepId list, companyName, and documentId.
        Used to remove other notifications for approvers in and "OR" group after one has approved
     */
    public void removeApprovalNotifications (String companyName, String documentId, List<Long> stepIds) {
        approvalNotificationRepository.deleteByCompanyNameAndDocumentIdAndStepIds(companyName, documentId, stepIds);
    }
}
