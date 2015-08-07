package com.provesoft.resource.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.resource.entity.Document.Document;
import com.provesoft.resource.entity.Document.DocumentRevisions;
import com.provesoft.resource.entity.Document.DocumentType;
import com.provesoft.resource.entity.Document.RevisionApprovalStatus;
import com.provesoft.resource.entity.SignoffPath.SignoffPath;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSeq;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.entity.SystemSettings;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.exceptions.InternalServerErrorException;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.DocumentService;
import com.provesoft.resource.service.SignoffPathService;
import com.provesoft.resource.service.SystemSettingsService;
import com.provesoft.resource.service.UserDetailsService;
import com.provesoft.resource.utils.SignoffPathHelpers;
import com.provesoft.resource.utils.SystemHelpers;
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

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    SignoffPathService signoffPathService;

    @Autowired
    SystemSettingsService systemSettingsService;


    /* -------------------------------------------------------- */
    /* ------------------------ GET --------------------------- */
    /* -------------------------------------------------------- */

    /* ------ Document ------ */

    // Find document by Id
    @RequestMapping(value = "/document",
            method = RequestMethod.GET
    )
    public Document getDocumentById (@RequestParam("documentId") String documentId,
                                     Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        return documentService.findDocumentById(companyName, documentId);
    }

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

        return documentService.documentWildCardSearch(companyName, searchString);
    }

    /*
        Retrieve first 10 documents sorted alphabetically
     */
    @RequestMapping(
            value = "/document/first10",
            method = RequestMethod.GET
    )
    public List<Document> findFirst10ByCompanyName(Authentication auth) {

// TODO: (MAYBE) ONLY RETRIEVE RESULTS WHICH THE USER HAS PERMISSIONS FOR

        // Retrieve their company
        String company = UserHelpers.getCompany(auth);

        if (company != null) {
            return documentService.findFirst10ByCompanyName(company);
        }

        throw new ResourceNotFoundException();
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

    /* ------ DocumentRevision ------ */

    /*
        Retrieve all revisions by documentId.
     */
    @RequestMapping(
            value = "/document/revision",
            method = RequestMethod.GET
    )
    public List<DocumentRevisions> getRevisionsByDocumentId(@RequestParam String documentId,
                                                            Authentication auth) {

// TODO: CHECK IF USER HAS VIEW PERMISSIONS FOR THIS DOCUMENT

        String companyName = UserHelpers.getCompany(auth);

        return documentService.findDocRevByCompanyNameAndDocumentId(companyName, documentId);
    }


    /* --------------------------------------------------------- */
    /* ------------------------ POST --------------------------- */
    /* --------------------------------------------------------- */

    /* ------ Document ------ */

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
            String companyName = UserHelpers.getCompany(auth);

            if (companyName != null) {

// TODO: Verify if document and organizations belong to user
// TODO: Verify if user has permissions to add document of this type to this organization

                newDocument.setCompanyName(companyName);

                // Retry if deadlock occurs until the resource becomes free or timeout occurs
                for (long stop=System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(30L); stop > System.currentTimeMillis();) {

                    try {
                        Long suffix = documentService.getAndGenerateDocumentId(companyName, newDocument.getDocumentType().getId()).getCurrentSuffixId();
                        Integer maxNumberOfDigits = newDocument.getDocumentType().getMaxNumberOfDigits();

                        if (String.valueOf(suffix).length() <= maxNumberOfDigits) {
                            String documentId = newDocument.getDocumentType().getDocumentPrefix();

                            for (int i = String.valueOf(suffix).length(); i < maxNumberOfDigits; i++) {
                                documentId = documentId + "0";
                            }

                            // Get user for revision details
                            UserDetails user = userDetailsService.findByCompanyNameAndEmail(companyName, auth.getName());

                            String currentDate = SystemHelpers.getCurrentDate();

                            documentId = documentId + suffix;
                            newDocument.setId(documentId);
                            newDocument.setRevision("A");
                            newDocument.setState("Released");
                            newDocument.setDate(currentDate);

                            return documentService.addDocument(newDocument, suffix, user.getUserId());
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

    /* ------ DocumentRevision ------ */

    @RequestMapping(
            value = "/document/revision",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public DocumentRevisions reviseDocument(@RequestBody String json,
                                            Authentication auth) {

// TODO: CHECK IF USER HAS PERMISSIONS TO EDIT FOR THIS DOCUMENT BELONGING TO ORGANIZATION

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            String documentId = rootNode.get("documentId").textValue();
            String changeReason = rootNode.get("changeReason").textValue();
            String changeUserEmail = rootNode.get("changeUserEmail").textValue();
            Boolean redlineDocPresent = rootNode.get("redlineDocPresent").booleanValue();

            String companyName = UserHelpers.getCompany(auth);
            String currentDate = SystemHelpers.getCurrentDate();

            UserDetails changeUser = userDetailsService.findByCompanyNameAndEmail(companyName, changeUserEmail);

            if (changeUser == null) {
                throw new ResourceNotFoundException();
            }

            // Verify document belongs to company
            Document docToChange = documentService.findByCompanyNameAndDocumentId(companyName, documentId);
            if (docToChange == null) {
                throw new ResourceNotFoundException();
            }

            // Get system setting to see if signoffs are required
            SystemSettings signoffSetting = systemSettingsService.getSettingByCompanyNameAndSetting(companyName, "signoff");

            /* -------- Critical area starts here ---------- */
            // Multiple users may attempt to get new rev Id.
            // Retry until resource becomes free to generate new Id.
            for (long stop=System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(30L); stop > System.currentTimeMillis();) {

                try {
                    String documentRevisionId = documentService.getAndGenerateDocumentRevisionId(companyName, documentId);

                    DocumentRevisions newRevision = new DocumentRevisions(
                            companyName,
                            documentId,
                            documentRevisionId,
                            changeReason,
                            changeUser.getUserId(),
                            currentDate,
                            redlineDocPresent
                    );

                    newRevision = documentService.addNewRevision(newRevision);

                    docToChange.setRevision(documentRevisionId);
                    docToChange.setDate(currentDate);

                    // Set document status to changing if signoffs are required
                    if (signoffSetting.getValue().equals("on")) {
                        docToChange.setState("Changing");

                        // Create Approval Status Record
                        SignoffPathSeq seq = signoffPathService.getPathSeq(companyName, docToChange.getSignoffPathId());

                        // Get path steps
                        List<SignoffPathSteps> steps = signoffPathService.getStepsForPath(companyName, seq.getKey().getPathId());

                        String seqWithActions = SignoffPathHelpers.generateSeqWithActions(seq.getPathSequence(), steps);

                        RevisionApprovalStatus newApprovalStatus = new RevisionApprovalStatus(companyName, documentId, seqWithActions);
                        documentService.addApprovalStatus(newApprovalStatus);
                    }

                    documentService.updateDocument(docToChange);

                    return newRevision;

                } catch (CannotAcquireLockException | LockAcquisitionException | TransactionRolledbackException ex) {
                    // Sleep and try to get resource again
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }

        }
        catch (IOException ioe) {
            throw new ResourceNotFoundException();
        }

        throw new ResourceNotFoundException();
    }


    /* --------------------------------------------------------- */
    /* ------------------------ UPDATE ------------------------- */
    /* --------------------------------------------------------- */

    /* ------ Document SignoffPath ------ */
    @RequestMapping(
            value = "/document",
            method = RequestMethod.PUT
    )
    public Document addSignoffPath(@RequestParam("documentId") String documentId,
                                   @RequestParam("signoffPathId") Long signoffPathId,
                                   Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            Document doc = documentService.findByCompanyNameAndDocumentId(companyName, documentId);

            if (doc == null) {
                throw new ResourceNotFoundException();
            }

            // Check if signoff path belongs to company
            SignoffPath signoffPath = signoffPathService.findByCompanyNameAndPathId(companyName, signoffPathId);

            if (signoffPath == null) {
                throw new ResourceNotFoundException();
            }

            doc.setSignoffPathId( signoffPath.getKey().getPathId() );

            return documentService.updateDocument(doc);
        }

        throw new ForbiddenException();
    }

}




































