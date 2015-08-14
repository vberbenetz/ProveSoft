package com.provesoft.resource.controller;

import com.provesoft.resource.entity.Document.ApprovalNotification;
import com.provesoft.resource.entity.Document.Document;
import com.provesoft.resource.entity.Document.RevisionApprovalStatus;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.exceptions.InternalServerErrorException;
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

    /*
        Get list of user Ids who already approved for the document
     */
    @RequestMapping(
            value = "signoffPath/approvals",
            method = RequestMethod.GET
    )
    public List<Long> getApprovedStepIds(@RequestParam("documentId") String documentId,
                                         Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        return approvalService.getApprovedStepIds(companyName, documentId);
    }


    /*
        Approve request in notification.
        1a) Check if this notification approval belongs to user
        1b) User is super admin and will fetch notification for this step. DocumentId is already provided
        2) Update RevisionApprovalStatus
        3) Remove notification from queue
        4) Call method to clean up other "OR" notifications if they exist
        5) Create next iteration of notifications
        OR) If no more left to do, mark document as released. Delete RevisionApprovalStatus
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

        // Update RevisionApprovalStatus ------------------------------------------------------------------
        RevisionApprovalStatus revApproval = approvalService.getApprovalStatusByCompanyAndDocumentId(companyName, documentId);

        // Retrieve group of other steps id's, part of OR group. Mark as approved
        List<Long> notificationStepIdsForRemoval = approvalService.getCurrentGroupOfStepIds(companyName, documentId, stepId);

        // Next iteration of approvals if applicable
        List<SignoffPathSteps> nextApprovalSteps = null;

        // Case where admin approves a step in a group down the line from the current one
        if (notificationStepIdsForRemoval != null) {

            // Append all id's which were part of this "OR" group
            String updatedApprovedSeq = revApproval.getApprovedSeq();
            for (Long s : notificationStepIdsForRemoval) {
                updatedApprovedSeq = updatedApprovedSeq + "&" + s;
            }
            revApproval.setApprovedSeq(updatedApprovedSeq);

            // Remove all notifications part of this notification's "OR" group ---------------------------------------------
            approvalService.removeApprovalNotifications(companyName, documentId);

            // Generate next iteration of notifications or mark document as released ---------------------------------------
            nextApprovalSteps = approvalService.getNextApprovalSteps(companyName, documentId);

            // No more approvers needed.
            // Mark document as released.
            // Delete RevisionApprovalStatus for this document
            if (nextApprovalSteps == null) {
                Document doc = documentService.findDocumentById(companyName, documentId);
                doc.setState("Released");

                approvalService.removeRevisionApprovalStatusByCompanyNameAndDocumentId(companyName, documentId);
            }
            // Add next set of notifications
            else {

                List<ApprovalNotification> newNotifications = new ArrayList<>();

                // Create a new notification for each approver in the next group
                for (SignoffPathSteps step : nextApprovalSteps) {
                    ApprovalNotification newNotification = new ApprovalNotification(companyName, step.getUser().getUserId(), step.getId(), documentId);
                    newNotifications.add(newNotification);
                }

                approvalService.addApprovalNotifications(newNotifications);
            }
        }

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

}


























