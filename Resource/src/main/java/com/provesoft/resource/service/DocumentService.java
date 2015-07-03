package com.provesoft.resource.service;

import com.provesoft.resource.entity.Document;
import com.provesoft.resource.entity.DocumentType;
import com.provesoft.resource.entity.DocumentTypeId;
import com.provesoft.resource.repository.DocumentRepository;
import com.provesoft.resource.repository.DocumentTypeIdRepository;
import com.provesoft.resource.repository.DocumentTypeRepository;
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
    DocumentTypeRepository documentTypeRepository;

    @Autowired
    DocumentTypeIdRepository documentTypeIdRepository;

    public List<DocumentType> findByCompanyName(String companyName) {
        return documentTypeRepository.findByCompanyName(companyName);
    }

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

    /*
        Add a document and perform a lazy id update for the DocumentType associated.
        Does not have to be entirely accurate (concurrently modified) because the DocumentTypeId keeps track of this
     */
    public Document addDocument(Document document, Long suffix) {
        documentTypeRepository.updateCurrentSuffix(document.getDocumentType().getId(), suffix);
        return documentRepository.saveAndFlush(document);
    }

    public DocumentType addDocumentType(DocumentType documentType) {
        return documentTypeRepository.saveAndFlush(documentType);
    }

    public void deleteDocumentTypeById(Long id, String companyName) {
        documentTypeRepository.deleteDocumentTypeById(id, companyName);
    }

}
