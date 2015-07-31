package com.provesoft.gateway.service;

import com.provesoft.gateway.entity.DocumentType;
import com.provesoft.gateway.entity.DocumentTypeId;
import com.provesoft.gateway.repository.DocumentTypeIdRepository;
import com.provesoft.gateway.repository.DocumentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    @Autowired
    DocumentTypeRepository documentTypeRepository;

    @Autowired
    DocumentTypeIdRepository documentTypeIdRepository;

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

}
