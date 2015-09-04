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

/**
 * Service contains all routes and methods involving SignoffPaths
 */
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

    /**
     * Method does an atomic get and increment for a new SignoffPathId for this company.
     * Isolation.SERIALIZABLE is used to lock the table globally and make this atomic.
     * A separate Id table is used to prevent locking more table data than necessary as this is a global lock.
     * @param companyName Company query parameter
     * @return SignoffPathId
     * @throws TransactionRolledbackException
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SignoffPathId getAndIncrementSignoffPathId (String companyName) throws TransactionRolledbackException {
        SignoffPathId newPathId = signoffPathIdRepository.findByKeyCompanyName(companyName);
        signoffPathIdRepository.incrementPathId(companyName);
        signoffPathIdRepository.flush();
        return newPathId;
    }


    /* ------------------------ SignoffPathTemplateSteps -------------------------- */

    /**
     * Retrieve all steps for SignoffPathTemplateSteps
     * @param companyName Company query parameter
     * @param pathId Path id query parameter
     * @return List of SignoffPathTemplateSteps
     */
    public List<SignoffPathTemplateSteps> getTemplateStepsForPath(String companyName, Long pathId) {
        return signoffPathTemplateStepsRepository.findByCompanyNameAndPathIdOrderByIdAsc(companyName, pathId);
    }

    /**
     * Method creates a single new SignoffPathTemplateStep
     * @param signoffPathStep New SignoffPathStep object
     * @return SignoffPathTemplateSteps
     */
    public SignoffPathTemplateSteps createNewTemplateStep(SignoffPathTemplateSteps signoffPathStep) {
        return signoffPathTemplateStepsRepository.saveAndFlush(signoffPathStep);
    }

    /**
     * Method creates multiple new SignoffPathTemplateStep
     * @param signoffPathTemplateSteps List of new SignoffPathTemplateSteps
     * @return List of SignoffPathTemplateSteps
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

    /**
     * Method retrieve group of Steps (steps which are in the same "OR" group).
     * @param companyName Company query parameter
     * @param documentId Document id parameter
     * @param stepId Step Id found in group being retrieved
     * @return List of SignoffPathSteps
     */
    public List<SignoffPathSteps> getGroupOfSteps(String companyName, String documentId, Long stepId) {
        List<SignoffPathSteps> stepsForDocument = getStepsForDocument(companyName, documentId);
        return SignoffPathHelpers.getStepsInGroup(stepsForDocument, stepId);
    }

    /**
     * Method retrieves the next set of steps from group which have not yet been approved
     * @param companyName Company query parameter
     * @param documentId Document to which steps correspond to
     * @return List of SignoffPathSteps
     */
    public List<SignoffPathSteps> getNextSetOfSteps(String companyName, String documentId) {
        List<SignoffPathSteps> nonApprovedSteps = signoffPathStepsRepository.findByCompanyNameAndDocumentIdAndApprovedOrderByIdAsc(companyName, documentId, false);

        // Reached end of set with no more steps left
        if (nonApprovedSteps.size() == 0) {
            return null;
        }

        return SignoffPathHelpers.getNextGroupOfSteps(nonApprovedSteps);
    }

    /**
     * Method sets all the steps in the group as approved and creates corresponding ApprovalHistory records.
     * @param stepsMarkedForApproval List of steps to mark as approved
     * @return List of SignoffPathSteps
     */
    public List<SignoffPathSteps> markStepsAsApproved(List<SignoffPathSteps> stepsMarkedForApproval) {
        for (SignoffPathSteps s : stepsMarkedForApproval) {
            s.setApproved(true);
            s.setApprovalDate(new Date());
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

    /**
     * Method creates new set of SignoffPathSteps for DocumentRevision
     * @param stepsToCreate New SignoffPathSteps list
     * @return List of SignoffPathSteps
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
