package com.provesoft.resource.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.resource.entity.Document;
import com.provesoft.resource.entity.DocumentType;
import com.provesoft.resource.exceptions.InternalServerErrorException;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.DocumentService;
import com.provesoft.resource.utils.UserHelpers;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.transaction.TransactionRolledbackException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class DocumentController {

    @Autowired
    DocumentService documentService;


    /* -------------------------------------------------------- */
    /* ------------------------ GET --------------------------- */
    /* -------------------------------------------------------- */

    /* ------ Document ------ */

    // Find all documents by the user's company, and join the like list of Id's and Titles
    @RequestMapping(value = "/document/lookup",
            method = RequestMethod.GET
    )
    public List<Document> documentLookup (@RequestParam("searchString") String searchString,
                                          Authentication auth)
    {

// TODO: (MAYBE) ONLY RETRIEVE RESULTS WHICH THE USER HAS PERMISSIONS FOR
        String companyName = UserHelpers.getCompany(auth);

        // Add wildcard characters
        searchString = "%" + searchString + "%";

        // Join results
        List<Document> documentsByTitle = documentService.findByTitle(companyName, searchString);
        List<Document> documentsById = documentService.findById(companyName, searchString);

        documentsByTitle.addAll(documentsById);

        return documentsByTitle;
    }

    /* ------ DocumentType ------ */

    @RequestMapping(value = "/documentType",
            method = RequestMethod.GET
    )
    public List<DocumentType> getDocumentTypes (Authentication auth) {

        // Get all document types by the user's company
// TODO: ONLY RETRIEVE RESULTS WHICH THE USER HAS PERMISSIONS FOR
        String companyName = UserHelpers.getCompany(auth);

        return documentService.findByCompanyName(companyName);
    }


    /* --------------------------------------------------------- */
    /* ------------------------ POST --------------------------- */
    /* --------------------------------------------------------- */

    @RequestMapping(
            value = "/document",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Document createDocument(@RequestBody String json,
                                   Authentication auth) {

        Document newDocument;

        ObjectMapper mapper = new ObjectMapper();
        try {
            newDocument = mapper.readValue(json, Document.class);

            // Retrieve their company
            String company = UserHelpers.getCompany(auth);

            if (company != null) {

// TODO: Verify if document and organizations belong to user
// TODO: Verify if user has permissions to add document of this type to this organization

                newDocument.setCompanyName(company);

                // Retry if deadlock occurs until the resource becomes free or timeout occurs
                for (long stop=System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(30L); stop > System.currentTimeMillis();) {

                    try {
                        Long suffix = documentService.getAndGenerateDocumentId(newDocument.getDocumentType().getId()).getCurrentSuffixId();
                        Integer maxNumberOfDigits = newDocument.getDocumentType().getMaxNumberOfDigits();

                        if (String.valueOf(suffix).length() <= maxNumberOfDigits) {
                            String documentId = newDocument.getDocumentType().getDocumentPrefix();

                            for (int i = String.valueOf(suffix).length(); i < maxNumberOfDigits; i++) {
                                documentId = documentId + "0";
                            }

                            documentId = documentId + suffix;
                            newDocument.setId(documentId);

                            return documentService.addDocument(newDocument, suffix);
                        }

                        else {
// TODO: HANDLE ID OVERFLOW PAST MAX NUMBER OF DIGITS
                            throw new ResourceNotFoundException();
                        }

                    }
                    catch (CannotAcquireLockException | LockAcquisitionException | TransactionRolledbackException ex) {

                        // Sleep and try to get resource again
                        try {
                            Thread.sleep(5L);
                        }
                        catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        throw new InternalServerErrorException();
                    }
                }

            }

        }
        catch (IOException | NullPointerException ex) {
            throw new ResourceNotFoundException();
        }

        throw new ResourceNotFoundException();
    }
}
