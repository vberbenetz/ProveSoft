package com.provesoft.resource.service;

import com.provesoft.resource.entity.Document.ApprovalNotification;
import com.provesoft.resource.entity.Document.RevisionApprovalStatus;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.repository.ApprovalNotificationRepository;
import com.provesoft.resource.repository.RevisionApprovalStatusRepository;
import com.provesoft.resource.repository.SignoffPathStepsRepository;
import com.provesoft.resource.utils.SignoffPathHelpers;
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
        Retrieve list of stepIds which already approved
     */
    public List<Long> getApprovedStepIds(String companyName, String documentId) {
        RevisionApprovalStatus r = revisionApprovalStatusRepository.findByKeyCompanyNameAndKeyDocumentId(companyName, documentId);

        // Break path sequence into groups separated on & (THEN step)
        String[] approvedStringStepIds = r.getApprovedSeq().split("&");

        List<Long> approvedStepIds = new ArrayList<>();
        for (String stepId : approvedStringStepIds) {
            if (!stepId.equals("")) {
                approvedStepIds.add( Long.parseLong(stepId) );
            }
        }

        return approvedStepIds;
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
    public List<Long> getCurrentGroupOfStepIds(String companyName, String documentId, Long currentStepId) {
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
        Append steps to Rev approval sequence. (Used by admin to add extra approval steps)
     */
    public void appendStepsToApprovalStatus(String companyName, String documentId, List<SignoffPathSteps> stepsToAppend) {
        RevisionApprovalStatus rev = revisionApprovalStatusRepository.findByKeyCompanyNameAndKeyDocumentId(companyName, documentId);
        String updatedSignoffPathSeq = rev.getSignoffPathSeq() + SignoffPathHelpers.generateSeqWithActions(stepsToAppend);
        rev.setSignoffPathSeq(updatedSignoffPathSeq);
        revisionApprovalStatusRepository.saveAndFlush(rev);
    }

    /*
        Remove RevisionApprovalStatus when revision advances to "Released"
     */
    public void removeRevisionApprovalStatus(RevisionApprovalStatus revisionApprovalStatus) {
        revisionApprovalStatusRepository.delete(revisionApprovalStatus);
        revisionApprovalStatusRepository.flush();
    }

    /*
        Remove RevisionApprovalStatus by companyName and documentId when revision advances to "Released".
        Remove any temporary steps created for this document.
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
        Retrieve approval notification by stepId and documentId
     */
    public ApprovalNotification getApprovalNotification(String companyName, String documentId, Long stepId) {
        return approvalNotificationRepository.findByCompanyNameAndDocumentIdAndStepId(companyName, documentId, stepId);
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
        All notifications associated with this document can be removed.
     */
    public void removeApprovalNotifications (String companyName, String documentId) {
        approvalNotificationRepository.deleteByCompanyNameAndDocumentId(companyName, documentId);
    }
}
