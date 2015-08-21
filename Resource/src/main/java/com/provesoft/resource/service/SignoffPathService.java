package com.provesoft.resource.service;

import com.provesoft.resource.entity.Document.ApprovalHistory;
import com.provesoft.resource.entity.Document.Document;
import com.provesoft.resource.entity.Document.DocumentRevisions;
import com.provesoft.resource.entity.SignoffPath.*;
import com.provesoft.resource.repository.*;
import com.provesoft.resource.utils.SignoffPathHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.transaction.TransactionRolledbackException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SignoffPathService {

    @Autowired
    SignoffPathRepository signoffPathRepository;

    @Autowired
    SignoffPathIdRepository signoffPathIdRepository;

    @Autowired
    SignoffPathStepsRepository signoffPathStepsRepository;

    @Autowired
    ApprovalHistoryRepository approvalHistoryRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    DocumentRevisionsRepository documentRevisionsRepository;

    @Autowired
    SignoffPathTemplateStepsRepository signoffPathTemplateStepsRepository;


    /* ------------------------ SignoffPath -------------------------- */

    public SignoffPath findByCompanyNameAndPathId(String companyName, Long pathId) {
        return signoffPathRepository.findByKeyCompanyNameAndKeyPathId(companyName, pathId);
    }

    public List<SignoffPath> findFirst10ByCompanyNameAndPathId(String companyName, Long pathId) {
        return signoffPathRepository.findFirst10ByKeyCompanyNameAndKeyPathIdLikeOrderByKeyPathIdAsc(companyName, pathId);
    }

    public List<SignoffPath> findFirst10ByCompanyNameAndPathName(String companyName, String pathName) {
        return signoffPathRepository.findFirst10ByKeyCompanyNameAndNameLikeOrderByNameAsc(companyName, pathName);
    }

    public List<SignoffPath> findFirst10ByCompanyName(String companyName) {
        return signoffPathRepository.findFirst10ByKeyCompanyNameOrderByKeyPathIdAsc(companyName);
    }

    public List<SignoffPath> getPathsByOrganizationId(String companyName, Long organizationId) {
        return signoffPathRepository.getPathsByCompanyNameAndOrganizationId(companyName, organizationId);
    }

    // Create signoff path and create initial sequence object
    public SignoffPath createNewPath(SignoffPath signoffPath) {
        return signoffPathRepository.saveAndFlush(signoffPath);
    }


    /* ------------------------ SignoffPathId -------------------------- */

    /*
        Get and increment SignoffPath Id
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SignoffPathId getAndIncrementSignoffPathId (String companyName) throws TransactionRolledbackException {
        SignoffPathId newPathId = signoffPathIdRepository.findByKeyCompanyName(companyName);
        signoffPathIdRepository.incrementPathId(companyName);
        signoffPathIdRepository.flush();
        return newPathId;
    }


    /* ------------------------ SignoffPathTemplateSteps -------------------------- */

    /*
        Retrieve all steps for SignoffPathTemplateSteps
     */
    public List<SignoffPathTemplateSteps> getTemplateStepsForPath(String companyName, Long pathId) {
        return signoffPathTemplateStepsRepository.findByCompanyNameAndPathIdOrderByIdAsc(companyName, pathId);
    }

    /*
        Create new template step (single)
     */
    public SignoffPathTemplateSteps createNewTemplateStep(SignoffPathTemplateSteps signoffPathStep) {
        return signoffPathTemplateStepsRepository.saveAndFlush(signoffPathStep);
    }

    /*
        Create new template steps (multiple)
     */
    public List<SignoffPathTemplateSteps> createNewTemplateSteps(List<SignoffPathTemplateSteps> signoffPathTemplateSteps) {
        List<SignoffPathTemplateSteps> retList = signoffPathTemplateStepsRepository.save(signoffPathTemplateSteps);
        signoffPathTemplateStepsRepository.flush();
        return retList;
    }

    /*
        Delete a signoff template step
     */
    public void deleteTemplateSignoffSteps(List<SignoffPathTemplateSteps> stepsToDelete) {
        signoffPathTemplateStepsRepository.deleteInBatch(stepsToDelete);
        signoffPathTemplateStepsRepository.flush();
    }


    /* ------------------------ SignoffPathSteps -------------------------- */

    public List<SignoffPathSteps> getStepsForDocument(String companyName, String documentId) {
        return signoffPathStepsRepository.findByCompanyNameAndDocumentIdOrderByIdAsc(companyName, documentId);
    }

    /*
        Retrieve OR group of steps by stepId and documentId
     */
    public List<SignoffPathSteps> getGroupOfSteps(String companyName, String documentId, Long stepId) {
        List<SignoffPathSteps> stepsForDocument = getStepsForDocument(companyName, documentId);
        return SignoffPathHelpers.getStepsInGroup(stepsForDocument, stepId);
    }

    /*
        Retrieve next set of steps needed for approval
     */
    public List<SignoffPathSteps> getNextSetOfSteps(String companyName, String documentId) {
        List<SignoffPathSteps> nonApprovedSteps = signoffPathStepsRepository.findByCompanyNameAndDocumentIdAndApprovedOrderByIdAsc(companyName, documentId, false);

        // Reached end of set with no more steps left
        if (nonApprovedSteps.size() == 0) {
            return null;
        }

        return SignoffPathHelpers.getNextGroupOfSteps(nonApprovedSteps);
    }

    /*
        Mark steps as approved
     */
    public List<SignoffPathSteps> markStepsAsApproved(List<SignoffPathSteps> stepsMarkedForApproval) {
        for (SignoffPathSteps s : stepsMarkedForApproval) {
            s.setApproved(true);
        }
        List<SignoffPathSteps> approvedSteps = signoffPathStepsRepository.save(stepsMarkedForApproval);
        signoffPathRepository.flush();

        // Create new ApprovalHistorySteps
        Document doc = documentRepository.findByCompanyNameAndId(
                stepsMarkedForApproval.get(0).getCompanyName(),
                stepsMarkedForApproval.get(0).getDocumentId()
        );
        DocumentRevisions documentRevision = documentRevisionsRepository.findByKeyCompanyNameAndKeyDocumentIdAndKeyRevisionId(
                doc.getCompanyName(),
                doc.getId(),
                doc.getRevision()
        );
        List<ApprovalHistory> approvalHistorySteps = new ArrayList<>();
        for (SignoffPathSteps s : stepsMarkedForApproval) {

            ApprovalHistory ah = new ApprovalHistory(
                    doc.getCompanyName(),
                    doc.getId(),
                    doc.getRevision(),
                    documentRevision,
                    s.getAction(),
                    s.getUser(),
                    new Date());

            approvalHistorySteps.add(ah);
        }
        approvalHistoryRepository.save(approvalHistorySteps);
        approvalHistoryRepository.flush();

        return approvedSteps;
    }

    /*
        Create new set of steps for specific document revision
     */
    public List<SignoffPathSteps> createNewStepsForDocRev(List<SignoffPathSteps> stepsToCreate) {
        List<SignoffPathSteps> newSteps = signoffPathStepsRepository.save(stepsToCreate);
        signoffPathStepsRepository.flush();
        return newSteps;
    }

    /*
        Delete all SignoffPathSteps for this document
     */
    public void deleteSignoffPathStepsForDocument(String companyName, String documentId) {
        signoffPathStepsRepository.deleteSteps(companyName, documentId);
    }




}
