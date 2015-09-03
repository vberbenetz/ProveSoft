package com.provesoft.resource.controller;

import com.provesoft.resource.entity.Document.ApprovalHistory;
import com.provesoft.resource.entity.Document.ApprovalNotification;
import com.provesoft.resource.entity.Document.Document;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.ApprovalService;
import com.provesoft.resource.service.DocumentService;
import com.provesoft.resource.service.SignoffPathService;
import com.provesoft.resource.service.UserDetailsService;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller encompasses all routes pertaining to approvals and notifications
 */
@RestController
public class ApprovalController {

    @Autowired
    ApprovalService approvalService;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    SignoffPathService signoffPathService;

    @Autowired
    DocumentService documentService;


    /**
     * Retrieve all approval notifications for this user.
     * @param auth
     * @return List of ApprovalNotifications
     */
    @RequestMapping(
            value = "/notifications/approvals",
            method = RequestMethod.GET
    )
    public List<ApprovalNotification> getMyApprovalNotifications(Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);
        String email = auth.getName();

        UserDetails me = userDetailsService.findByCompanyNameAndEmail(companyName, email);

        return approvalService.getApprovalNotifications(companyName, me.getUserId());
    }

    /**
     * Approve request in notification.
     * 1) Check if correct parameters are passed in.
     * 2) Get the current group of steps by a stepId.
     * 3) Mark all the steps in that group as approved.
     * 4) Check if notifications exist for this group of steps.
     * - If not then it indicates that an admin approved a set of steps further down the path. Return from here.
     * 5) Remove all notifications for this document.
     * 6) Get next group of steps.
     * 7a) If no steps are returned, end of document has been reached. Mark as released and delete all steps.
     * 7b) Create new set of notifications for this group.
     * @param notificationId
     * @param documentId
     * @param stepId
     * @param auth
     * @return ResponseEntity
     */
    @RequestMapping(
            value = "/notifications/approvals",
            method = RequestMethod.PUT
    )
    public ResponseEntity<?> approveRevision(@RequestParam(value = "notificationId", required = false) Long notificationId,
                                             @RequestParam(value = "documentId", required = false) String documentId,
                                             @RequestParam(value = "stepId", required = false) Long stepId,
                                             Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);
        ApprovalNotification notification;

        // Check if parameters are missing
        if (notificationId != null) {

            String myEmail = auth.getName();

            // Check if notification belongs to this user ----------------------------------------------
            notification = approvalService.getApprovalNotification(notificationId);

            if (notification == null) {
                throw new ResourceNotFoundException();
            }

            documentId = notification.getDocumentId();
            stepId = notification.getStepId();

            UserDetails notificationUser = userDetailsService.findByCompanyNameAndUserId(companyName, notification.getUserId());

            if (!notificationUser.getEmail().equals(myEmail) || !notificationUser.getCompanyName().equals(companyName)) {
                throw new ForbiddenException();
            }
        }
        // Used by admin.
        // Fetch notification to update below
        else if (documentId != null && stepId != null) {
            if (!UserHelpers.isSuperAdmin(auth)) {
                throw new ForbiddenException();
            }
        }
        else {
            throw new ResourceNotFoundException();
        }

        // Get group of steps and mark as approved
        List<SignoffPathSteps> stepsMarkedApproved = signoffPathService.getGroupOfSteps(companyName, documentId, stepId);
        signoffPathService.markStepsAsApproved(stepsMarkedApproved);

        // Notifications need to be removed and new ones need to be created.
        // If notifications do not exist, this indicates that an admin approved a group of steps further down the path chain.
        if (approvalService.checkIfNotificationExistsForStepId(companyName, documentId, stepId)) {

            // Delete all notifications for this document revision
            approvalService.removeApprovalNotifications(companyName, documentId);

            // Get next set of steps
            List<SignoffPathSteps> nextSetOfSteps = signoffPathService.getNextSetOfSteps(companyName, documentId);

            // No next steps.
            // Mark document as released.
            // Delete all signoff path steps for this document
            if (nextSetOfSteps == null) {
                Document doc = documentService.findDocumentById(companyName, documentId);
                doc.setState("Released");
                documentService.updateDocument(doc);

                signoffPathService.deleteSignoffPathStepsForDocument(companyName, documentId);
            }

            // Create notifications for next group of steps
            else {
                List<ApprovalNotification> newNotifications = new ArrayList<>();

                for (SignoffPathSteps s : nextSetOfSteps) {
                    ApprovalNotification newNotification = new ApprovalNotification(companyName, s.getUser().getUserId(), s.getId(), documentId);
                    newNotifications.add(newNotification);
                }
                approvalService.addApprovalNotifications(newNotifications);
            }
        }

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }


    /* --------------------------------------- Approval History ------------------------------------------ */

    /**
     * Retrieve ApprovalHistory for documentId and revisionId
     * @param documentId
     * @param revisionId
     * @param auth
     * @return List of ApprovalHistory
     */
    @RequestMapping(
            value = "/approvalHistory",
            method = RequestMethod.GET
    )
    public List<ApprovalHistory> getApprovalHistory(@RequestParam(value="documentId", required=false) String documentId,
                                                    @RequestParam(value="revisionId", required=false) String revisionId,
                                                    Authentication auth) {

        if ( (documentId != null) && (revisionId != null) ) {
            String companyName = UserHelpers.getCompany(auth);
            return approvalService.getApprovalHistoryByRevision(companyName, documentId, revisionId);
        }

        throw new ResourceNotFoundException();
    }

    /**
     * Retrieve recent ApprovalHistory for document
     * @param documentId
     * @param auth
     * @return List of ApprovalHistory
     */
    @RequestMapping(
            value = "/approvalHistory/recent",
            method = RequestMethod.GET
    )
    public List<ApprovalHistory> getRecentApprovals(@RequestParam("documentId") String documentId,
                                                    Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);
        return approvalService.getRecentApprovals(companyName, documentId);
    }
}


























