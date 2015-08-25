package com.provesoft.resource.service;

import com.provesoft.resource.entity.Document.*;
import com.provesoft.resource.repository.*;
import com.provesoft.resource.utils.DocumentHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.transaction.TransactionRolledbackException;
import java.util.List;

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


    /* ------------------------ Document -------------------------- */

    // Search method will use wildcards for the title
    public List<Document> findByTitle(String companyName, String title) {
        return documentRepository.searchByTitle(companyName, title);
    }

    // Find single document by Id
    public Document findDocumentById(String companyName, String id) {
        return documentRepository.findByCompanyNameAndId(companyName, id);
    }

    // Find documents by state
    public List<Document> findDocumentByState(String companyName, String state) {
        return documentRepository.findByCompanyNameAndState(companyName, state);
    }

    /*
        Find by list of document ids
     */
    public List<Document> findDocumentByIdList(String companyName, List<String> ids) {
        return documentRepository.findByCompanyNameAndIdIn(companyName, ids);
    }


    /*
        Complete wildcard search
     */
    public List<Document> documentWildCardSearch(String companyName, String searchString) {
        return documentRepository.wildCardSearch(companyName, searchString);
    }

    /*
        Search for document by documentId
     */
    public Document findByCompanyNameAndDocumentId(String companyName, String documentId) {
        return documentRepository.findByCompanyNameAndId(companyName, documentId);
    }

    /*
        Get first 10 documents for document preview in lookup screen
     */
    public List<Document> findFirst10ByCompanyName(String companyName) {
        return documentRepository.findFirst10ByCompanyNameOrderByIdAsc(companyName);
    }

    /*
        Add a document and perform a lazy id update for the DocumentType associated.
        Does not have to be entirely accurate (concurrently modified) because the DocumentTypeId keeps track of this.

        1) Update id in DocumentType
        2) Add initial RevisionId
        3) Add initial Revision
        4) Add document
     */
    public Document addDocument(Document document, Long suffix, Long userId) {
        documentTypeRepository.updateCurrentSuffix(document.getDocumentType().getId(), suffix);

        DocumentRevisions initialRev = new DocumentRevisions(document.getCompanyName(),
                                                                document.getId(),
                                                                "A",
                                                                "Document Created",
                                                                userId,
                                                                document.getDate(),
                                                                false);

        DocumentRevisionIds initialRevId = new DocumentRevisionIds(document.getCompanyName(), document.getId(), "A");

        documentRevisionIdsRepository.save(initialRevId);
        documentRevisionsRepository.save(initialRev);

        return documentRepository.saveAndFlush(document);
    }

    /*
        Update document after a revision or addition of a signoff path
     */
    public Document updateDocument(Document document) {
        return documentRepository.saveAndFlush(document);
    }


    /* ------------------------ DocumentUpload -------------------------- */

    /*
        Retrieve uploaded file
     */
    public DocumentUpload findUploadByCompanyNameAndDocumentIdAndRevisionAndRedline(String companyName, String documentId, String revision, Boolean redline) {
        return documentUploadRepository.findByKeyCompanyNameAndKeyDocumentIdAndKeyRevisionAndKeyRedline(companyName, documentId, revision, redline);
    }

    /*
        Insert uploaded document file
     */
    public void addDocumentFile(DocumentUpload documentUpload) {
        documentUploadRepository.saveAndFlush(documentUpload);
    }

    /*
        Update the temporary revision id of the temporary documents after a commit
     */
    public void updateRevisionId(String companyName, String tempRevId, String newRevId) {
        documentUploadRepository.updateRevisionId(companyName, tempRevId, newRevId);
    }

    /*
        Delete temporary document uploads
     */
    public void deleteTempUploads(String companyName, String documentId, String tempRevId) {
        //documentUploadRepository.deleteByKeyCompanyNameAndKeyDocumentIdAndKeyRevision(companyName, documentId, tempRevId);
        documentUploadRepository.deleteTempUpload(companyName, documentId, tempRevId);
    }


    /* ------------------------ DocumentType -------------------------- */

    public List<DocumentType> findByCompanyName(String companyName) {
        return documentTypeRepository.findByCompanyName(companyName);
    }

    /*
        Add new DocumentType.
        Insert new DocumentTypeId to maintain Id generation
     */
    public DocumentType addDocumentType(DocumentType documentType) {
        DocumentType newDocumentType = documentTypeRepository.saveAndFlush(documentType);
        DocumentTypeId newDocumentTypeId = new DocumentTypeId( newDocumentType.getCompanyName(), newDocumentType.getId(), newDocumentType.getCurrentSuffix() );
        documentTypeIdRepository.saveAndFlush(newDocumentTypeId);
        return newDocumentType;
    }

    public void deleteDocumentTypeById(Long id, String companyName) {
        documentTypeRepository.deleteDocumentTypeById(id, companyName);
    }


    /* ------------------------ DocumentTypeId -------------------------- */

    /*
        Generate new unique id for document of documentType
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DocumentTypeId getAndGenerateDocumentId(String companyName, Long documentTypeId) throws TransactionRolledbackException {
        DocumentTypeId docTypeId = documentTypeIdRepository.findByKeyCompanyNameAndKeyDocumentTypeId(companyName, documentTypeId);
        documentTypeIdRepository.incrementSuffixId(companyName, documentTypeId);
        documentTypeIdRepository.flush();
        return docTypeId;
    }


    /* ------------------------ DocumentRevisions -------------------------- */

    /*
        Add new DocumentRevision
     */
    public DocumentRevisions addNewRevision (DocumentRevisions documentRevision) {
        return documentRevisionsRepository.saveAndFlush(documentRevision);
    }

    /*
        Retrieve DocumentRevision by userId (and companyName)
     */
    public List<DocumentRevisions> findDocRevByCompanyNameAndDocumentId (String companyName, String documentId) {
        return documentRevisionsRepository.findByKeyCompanyNameAndKeyDocumentIdOrderByKeyRevisionIdDesc(companyName, documentId);
    }

    /*
        Retrieve a single DocumentRevision by companyname, documentId, and revisionId
     */
    public DocumentRevisions findDocRevByCompanyNameAndDocumentIdAndRevisionId (String companyName, String documentId, String revisionId) {
        return documentRevisionsRepository.findByKeyCompanyNameAndKeyDocumentIdAndKeyRevisionId(companyName, documentId, revisionId);
    }

    /*
        Retrieve latest revision per documentId in documentId list
     */
    public List<DocumentRevisions> findLatestDocRevsByCompanyNameAndDocumentIds (String companyName, String[] documentIds) {
        return documentRevisionsRepository.findRevisionByKeyCompanyNameAndKeyDocumentIdIn(companyName, documentIds);
    }

    /*
        Retrieve latest revisons by Company
     */
    public List<DocumentRevisions> findLatestDocRevsByCompanyName (String companyName) {
        return documentRevisionsRepository.findFirst5ByKeyCompanyNameOrderByChangeDateDesc(companyName);
    }


    /* ------------------------ DocumentRevisionIds -------------------------- */

    /*
        Generate a new revisionId for the document
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

    /*
        Retrieve recent comments for particular document
     */
    public List<DocumentComment> getRecentDocumentComments(String companyName, String documentId) {
        return documentCommentRepository.findFirst5ByCompanyNameAndDocumentIdOrderByDateDesc(companyName, documentId);
    }

    /*
        Retrieve latest comments by Company
     */
    public List<DocumentComment> findLatestCommentsByCompanyName (String companyName) {
        return documentCommentRepository.findFirst5ByCompanyNameOrderByDateDesc(companyName);
    }

    /*
        Save comment for document
     */
    public DocumentComment createDocumentComment(DocumentComment documentComment) {
        return documentCommentRepository.saveAndFlush(documentComment);
    }

}
