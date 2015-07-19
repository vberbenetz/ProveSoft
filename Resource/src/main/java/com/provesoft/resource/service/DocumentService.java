package com.provesoft.resource.service;

import com.provesoft.resource.entity.*;
import com.provesoft.resource.repository.*;
import com.provesoft.resource.utils.DocumentHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.transaction.TransactionRolledbackException;
import java.io.File;
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


    /* ------------------------ Document -------------------------- */

    // Search method will use wildcards for the title
    public List<Document> findByTitle(String companyName, String title) {
        return documentRepository.searchByTitle(companyName, title);
    }

    // Search method will use wildcards for the title
    public List<Document> findById(String companyName, String id) {
        return documentRepository.searchById(companyName, id);
    }

    /*
        Search for document by documentId
     */
    public Document findByCompanyNameAndDocumentId(String companyName, String documentId) {
        return documentRepository.findByCompanyNameAndId(companyName, documentId);
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
                                                                document.getDate());

        DocumentRevisionIds initialRevId = new DocumentRevisionIds(document.getCompanyName(), document.getId(), "A");

        documentRevisionIdsRepository.save(initialRevId);
        documentRevisionsRepository.save(initialRev);

        return documentRepository.saveAndFlush(document);
    }

    /*
        Update document after a revision
     */
    public Document updateDocument(Document document) {
        return documentRepository.saveAndFlush(document);
    }


    /* ------------------------ DocumentUpload -------------------------- */

    /*
        Retrieve uploaded file
     */
    public DocumentUpload findUploadByCompanyNameAndDocumentIdAndRedline(String companyName, String documentId, Boolean redline) {
        return documentUploadRepository.findByKeyCompanyNameAndKeyDocumentIdAndKeyRedline(companyName, documentId, redline);
    }

    /*
        Insert uploaded document file
     */
    public void addDocumentFile(DocumentUpload documentUpload) {
        documentUploadRepository.saveAndFlush(documentUpload);
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
        DocumentTypeId newDocumentTypeId = new DocumentTypeId( newDocumentType.getId(), newDocumentType.getCurrentSuffix() );
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
    public DocumentTypeId getAndGenerateDocumentId(Long documentTypeId) throws TransactionRolledbackException {
        DocumentTypeId docTypeId = documentTypeIdRepository.findByDocumentTypeId(documentTypeId);
        documentTypeIdRepository.incrementSuffixId(documentTypeId);
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


    /* ------------------------ DocumentRevisionIds -------------------------- */

    /*
        Retrieve DocumentRevision by userId (and companyName)
     */
    public List<DocumentRevisions> findDocRevByCompanyNameAndDocumentId (String companyName, String documentId) {
        return documentRevisionsRepository.findByKeyCompanyNameAndKeyDocumentId(companyName, documentId);
    }

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

}
