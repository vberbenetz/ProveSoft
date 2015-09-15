package com.provesoft.resource.service;

import com.provesoft.resource.entity.Document.*;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.repository.*;
import com.provesoft.resource.utils.DocumentHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.transaction.TransactionRolledbackException;
import java.util.List;

/**
 * Service contains all methods related to Document
 */
@Service
public class DocumentService {

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    DocumentUploadRepository documentUploadRepository;

    @Autowired
    DocumentTypeRepository documentTypeRepository;

    @Autowired
    DocumentTypeIdRepository documentTypeIdRepository;

    @Autowired
    DocumentRevisionsRepository documentRevisionsRepository;

    @Autowired
    DocumentRevisionIdsRepository documentRevisionIdsRepository;

    @Autowired
    SignoffPathTemplateStepsRepository signoffPathTemplateStepsRepository;

    @Autowired
    DocumentCommentRepository documentCommentRepository;

    @Autowired
    DocumentCommentLikeRepository documentCommentLikeRepository;


    /* ------------------------ Document -------------------------- */

    /**
     * Find single document by Id
     * @param companyName
     * @param id
     * @return Document
     */
    public Document findDocumentById(String companyName, String id) {
        return documentRepository.findByCompanyNameAndId(companyName, id);
    }

    /**
     * Find documents by state
     * @param companyName
     * @param state
     * @return List of Document
     */
    public List<Document> findDocumentByState(String companyName, String state) {
        return documentRepository.findByCompanyNameAndState(companyName, state);
    }

    /**
     * Find by list of document ids
     * @param companyName
     * @param ids
     * @return List of Document
     */
    public List<Document> findDocumentByIdList(String companyName, String[] ids) {
        return documentRepository.findByCompanyNameAndIdIn(companyName, ids);
    }

    /**
     * Wildcard search based on search string
     * @param companyName
     * @param searchString
     * @param includeObsolete
     * @return List of Document
     */
    public List<Document> findDocumentBySearchString(String companyName, String searchString, Boolean includeObsolete) {
        if (includeObsolete) {
            return documentRepository.wildCardSearchWithObsolete(companyName, searchString);
        }
        else {
            return documentRepository.wildCardSearchNoObsolete(companyName, searchString);
        }

    }

    /**
     * Search for document by documentId
     * @param companyName
     * @param documentId
     * @return Document
     */
    public Document findDocumentByCompanyNameAndDocumentId(String companyName, String documentId) {
        return documentRepository.findByCompanyNameAndId(companyName, documentId);
    }

    /**
     * Get first 10 Documents by company. Remove Obsolete state documents by default.
     * @param companyName
     * @return List of Document
     */
    public List<Document> findFirst10DocumentsByCompanyName(String companyName) {
        return documentRepository.findFirst10ByCompanyNameAndStateNotOrderByIdAsc(companyName, "Obsolete");
    }

    /**
     * Add a document and perform a lazy id update for the DocumentType associated.
     * Does not have to be entirely accurate (concurrently modified) because the DocumentTypeId keeps track of this.
     *
     * 1) Update id in DocumentType
     * 2) Add initial RevisionId
     * 3) Add initial Revision
     * 4) Add document
     * @param document
     * @param suffix
     * @param user
     * @return Document
     */
    public Document addDocument(Document document, Long suffix, UserDetails user) {
        documentTypeRepository.updateCurrentSuffix(document.getDocumentType().getId(), suffix);

        DocumentRevisions initialRev = new DocumentRevisions(document.getCompanyName(),
                                                                document.getId(),
                                                                "A",
                                                                "Document Created",
                                                                user,
                                                                document.getDate(),
                false);

        DocumentRevisionIds initialRevId = new DocumentRevisionIds(document.getCompanyName(), document.getId(), "A");

        documentRevisionIdsRepository.save(initialRevId);
        documentRevisionsRepository.save(initialRev);

        return documentRepository.saveAndFlush(document);
    }

    /**
     * Update document after a revision or addition of a signoff path
     * @param document
     * @return Document
     */
    public Document updateDocument(Document document) {
        return documentRepository.saveAndFlush(document);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Document getAndSetDocumentState(String companyName, String documentId, String state) {
        Document d = documentRepository.findByCompanyNameAndId(companyName,documentId);
        if ( (d != null) && (d.getState() != null) && (d.getState().equals("Released")) ) {
            d.setState(state);
            return documentRepository.saveAndFlush(d);
        }
        else {
            return null;
        }
    }

    /* ------------------------ DocumentUpload -------------------------- */

    /**
     * Retrieve uploaded file
     * @param companyName
     * @param documentId
     * @param revision
     * @param redline
     * @return DocumentUpload
     */
    public DocumentUpload findUploadByCompanyNameAndDocumentIdAndRevisionAndRedline(String companyName, String documentId, String revision, Boolean redline) {
        return documentUploadRepository.findByKeyCompanyNameAndKeyDocumentIdAndKeyRevisionAndKeyRedline(companyName, documentId, revision, redline);
    }

    /**
     * Retrieve both regular and redline files
     * @param companyName
     * @param documentId
     * @param revision
     * @return
     */
    public List<DocumentUpload> findUploadByCompanyNameAndDocumentIdAndRevision(String companyName, String documentId, String revision) {
        return documentUploadRepository.findByKeyCompanyNameAndKeyDocumentIdAndKeyRevision(companyName, documentId, revision);
    }

    /**
     * Insert uploaded document file
     * @param documentUpload
     * @return DocumentUpload
     */
    public DocumentUpload addDocumentFile(DocumentUpload documentUpload) {
        return documentUploadRepository.saveAndFlush(documentUpload);
    }

    /**
     * Update the temporary revision id of the temporary documents after a commit
     * @param companyName
     * @param tempRevId
     * @param newRevId
     */
    public void updateRevisionId(String companyName, String tempRevId, String newRevId) {
        documentUploadRepository.updateRevisionId(companyName, tempRevId, newRevId);
    }

    /**
     * Delete temporary document uploads
     * @param companyName
     * @param documentId
     * @param tempRevId
     */
    public void deleteTempUploads(String companyName, String documentId, String tempRevId) {
        //documentUploadRepository.deleteByKeyCompanyNameAndKeyDocumentIdAndKeyRevision(companyName, documentId, tempRevId);
        documentUploadRepository.deleteTempUpload(companyName, documentId, tempRevId);
    }

    /**
     * Delete a failed document upload (Only used internally, not accessible by user)
     * @param du DocumentUpload object
     */
    public void deleteErrorUpload(DocumentUpload du) {
        documentUploadRepository.delete(du);
        documentUploadRepository.flush();
    }


    /* ------------------------ DocumentType -------------------------- */

    /**
     * Retrieve all DocumentType for company
     * @param companyName
     * @return List of DocumentType
     */
    public List<DocumentType> findDocumentTypeByCompanyName(String companyName) {
        return documentTypeRepository.findByCompanyName(companyName);
    }

    /**
     * Method retrieves one DocumentType
     * @param companyName
     * @param documentTypeId
     * @return
     */
    public DocumentType findDocumentTypeByCompanyNameAndId(String companyName, Long documentTypeId) {
        return documentTypeRepository.findByCompanyNameAndId(companyName, documentTypeId);
    }

    /**
     * Method retrieves a count of existing Documents for a DocumentType to see if any exist for that type.
     * @param companyName
     * @param documentType
     * @return
     */
    public Boolean documentsForTypeExist(String companyName, DocumentType documentType) {
        if (documentRepository.countByCompanyNameAndDocumentType(companyName, documentType) > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Add new DocumentType.
     * Insert new DocumentTypeId to maintain Id generation
     * @param documentType
     * @return DocumentType
     */
    public DocumentType addDocumentType(DocumentType documentType) {
        DocumentType newDocumentType = documentTypeRepository.saveAndFlush(documentType);
        DocumentTypeId newDocumentTypeId = new DocumentTypeId( newDocumentType.getCompanyName(), newDocumentType.getId(), newDocumentType.getCurrentSuffix() );
        documentTypeIdRepository.saveAndFlush(newDocumentTypeId);
        return newDocumentType;
    }

    /**
     * Method deletes a DocumentType if no Documents exist for that type
     * @param companyName
     * @param documentType
     * @return Boolean
     */
    public Boolean removeDocumentType(String companyName, DocumentType documentType) {
        if (!documentsForTypeExist(companyName, documentType)) {
            documentTypeRepository.delete(documentType);
            documentTypeRepository.flush();
            return true;
        }
        else {
            return false;
        }
    }


    /* ------------------------ DocumentTypeId -------------------------- */

    /**
     * Generate new unique id for document of documentType
     * @param companyName
     * @param documentTypeId
     * @return DocumentTypeId
     * @throws TransactionRolledbackException
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DocumentTypeId getAndGenerateDocumentId(String companyName, Long documentTypeId) throws TransactionRolledbackException {
        DocumentTypeId docTypeId = documentTypeIdRepository.findByKeyCompanyNameAndKeyDocumentTypeId(companyName, documentTypeId);
        documentTypeIdRepository.incrementSuffixId(companyName, documentTypeId);
        documentTypeIdRepository.flush();
        return docTypeId;
    }


    /* ------------------------ DocumentRevisions -------------------------- */

    /**
     * Add new DocumentRevision
     * @param documentRevision
     * @return DocumentRevisions
     */
    public DocumentRevisions addNewDocumentRevision(DocumentRevisions documentRevision) {
        return documentRevisionsRepository.saveAndFlush(documentRevision);
    }

    /**
     * Retrieve DocumentRevision by userId (and companyName)
     * @param companyName
     * @param documentId
     * @return DocumentRevisions
     */
    public List<DocumentRevisions> findDocRevByCompanyNameAndDocumentId (String companyName, String documentId) {
        return documentRevisionsRepository.findByKeyCompanyNameAndKeyDocumentIdOrderByKeyRevisionIdDesc(companyName, documentId);
    }

    /**
     * Retrieve a single DocumentRevision by companyname, documentId, and revisionId
     * @param companyName
     * @param documentId
     * @param revisionId
     * @return DocumentRevisions
     */
    public DocumentRevisions findDocRevByCompanyNameAndDocumentIdAndRevisionId (String companyName, String documentId, String revisionId) {
        return documentRevisionsRepository.findByKeyCompanyNameAndKeyDocumentIdAndKeyRevisionId(companyName, documentId, revisionId);
    }

    /**
     * Retrieve latest revision per documentId in documentId list
     * @param companyName
     * @param documentIds
     * @return List of DocumentRevisions
     */
    public List<DocumentRevisions> findLatestDocRevsByCompanyNameAndDocumentIds (String companyName, String[] documentIds) {
        return documentRevisionsRepository.findRevisionByKeyCompanyNameAndKeyDocumentIdIn(companyName, documentIds);
    }

    /**
     * Retrieve latest revisons by Company
     * @param companyName
     * @return List of DocumentRevisions
     */
    public List<DocumentRevisions> findLatestDocRevsByCompanyName (String companyName) {
        return documentRevisionsRepository.findFirst5ByKeyCompanyNameOrderByChangeDateDesc(companyName);
    }


    /* ------------------------ DocumentRevisionIds -------------------------- */

    /**
     * Generate a new revisionId for the document
     * @param companyName
     * @param documentId
     * @return String (DocumentRevision Id)
     * @throws TransactionRolledbackException
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String getAndGenerateDocumentRevisionId(String companyName, String documentId) throws TransactionRolledbackException {
        DocumentRevisionIds docRevId = documentRevisionIdsRepository.findByKeyCompanyNameAndKeyDocumentId(companyName, documentId);
        String nextRevId = DocumentHelpers.genNextRevId(docRevId.getRevisionId());
        documentRevisionIdsRepository.incrementRevId(companyName, documentId, nextRevId);
        documentRevisionIdsRepository.flush();
        return nextRevId;
    }


    /* ------------------------------ DocumentComments ------------------------------- */

    /**
     * Retrieve recent comments for particular document
     * @param companyName
     * @param documentId
     * @return List of DocumentComment
     */
    public List<DocumentComment> getRecentDocumentComments(String companyName, String documentId) {
        return documentCommentRepository.findFirst5ParentsByCompanyNameAndDocumentIdOrderByDateDesc(companyName, documentId);
    }

    /**
     * Retrieve latest comments by Company
     * @param companyName
     * @return List of DocumentComment
     */
    public List<DocumentComment> findLatestCommentsByCompanyName (String companyName) {
        return documentCommentRepository.findFirst5ParentsByCompanyNameOrderByDateDesc(companyName);
    }

    /**
     * Find children comments by Company and ParentId list
     * @param companyName
     * @param parentIds
     * @return List of DocumentComment
     */
    public List<DocumentComment> findChildrenCommentsByParentIds (String companyName, Long[] parentIds) {
        return documentCommentRepository.findChildrenByCompanyNameAndParentDocumentIdList(companyName, parentIds);
    }

    /**
     * Save comment for document
     * @param documentComment
     * @return DocumentComment
     */
    public DocumentComment createDocumentComment(DocumentComment documentComment) {
        return documentCommentRepository.saveAndFlush(documentComment);
    }


    /* ------------------------------ DocumentCommentLikes ------------------------------- */

    public List<DocumentCommentLike> findLikesByCommentList(String companyName, Long[] documentCommentIds) {
        return documentCommentLikeRepository.findByKeyCompanyNameAndKeyDocumentCommentIdIn(companyName, documentCommentIds);
    }

    /**
     * Create new comment like
     * @param newLike
     * @return DocumentCommentLike
     */
    public DocumentCommentLike createCommentLike(DocumentCommentLike newLike) {
        return documentCommentLikeRepository.saveAndFlush(newLike);
    }

}
