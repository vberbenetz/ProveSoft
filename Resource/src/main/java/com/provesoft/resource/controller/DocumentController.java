package com.provesoft.resource.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.resource.entity.Document.*;
import com.provesoft.resource.entity.SignoffPath.SignoffPath;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.entity.SignoffPath.SignoffPathTemplateSteps;
import com.provesoft.resource.entity.SystemSettings;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.exceptions.InternalServerErrorException;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

    @Autowired
    ApprovalService approvalService;


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

    // Find by document Id list
    @RequestMapping(value = "/document/multiple",
            method = RequestMethod.GET
    )
    public List<Document> getDocumentByIdList (@RequestParam("documentIds") String[] documentIds,
                                               Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        return documentService.findDocumentByIdList(companyName, Arrays.asList(documentIds));
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

    /*
        Get latest revisions by list of documentIds
     */
    @RequestMapping(
            value = "/document/revisions",
            method = RequestMethod.GET
    )
    public List<DocumentRevisions> getRevisionsByDocumentIds(@RequestParam("documentIds") String[] documentIds,
                                                                   Authentication auth) {
        String companyName = UserHelpers.getCompany(auth);

        return documentService.findLatestDocRevsByCompanyNameAndDocumentIds(companyName, documentIds);
    }

    /* ---------- DocumentComment ---------- */

    /*
        Retrieve recent comments for document
     */
    @RequestMapping(
            value = "/document/comments",
            method = RequestMethod.GET
    )
    public List<DocumentComment> getRecentDocumentComments(@RequestParam String documentId,
                                                           @RequestParam(value = "recent", required = false) Boolean recent,
                                                           Authentication auth) {
        String companyName = UserHelpers.getCompany(auth);

        if ( (recent != null) && (recent) ) {
            return documentService.getRecentDocumentComments(companyName, documentId);
        }

// TODO: IMPLEMENT METHOD IF RECENT IS NOT TRUE

        throw new ResourceNotFoundException();
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

                            // Get system setting to see if signoffs are required
                            SystemSettings signoffSetting = systemSettingsService.getSettingByCompanyNameAndSetting(companyName, "signoff");

                            // Signoffs required. Make new document signoff compliant
                            if (signoffSetting.getValue().equals("on")) {
                                newDocument.setState("Changing");

                                // Get template path steps
                                Long pathId = newDocument.getSignoffPathId();
                                List<SignoffPathTemplateSteps> templateSteps = signoffPathService.getTemplateStepsForPath(companyName, pathId);

                                // Create path steps from template and apply to this document
                                List<SignoffPathSteps> newSteps = new ArrayList<>();
                                for (SignoffPathTemplateSteps s : templateSteps) {
                                    SignoffPathSteps newStep = new SignoffPathSteps(companyName, documentId, pathId, s.getId(), s.getAction(), s.getUser());
                                    newSteps.add(newStep);
                                }
                                newSteps = signoffPathService.createNewStepsForDocRev(newSteps);

                                // Create notifications
                                // Extract initial sequence Ids
                                List<SignoffPathSteps> firstSetOfSteps = SignoffPathHelpers.extractNextSetOfSteps(newSteps);
                                List<ApprovalNotification> notifications = new ArrayList<>();

                                for (SignoffPathSteps s : firstSetOfSteps) {
                                    ApprovalNotification notification = new ApprovalNotification(companyName, s.getUser().getUserId(), s.getId(), documentId);
                                    notifications.add(notification);
                                }
                                approvalService.addApprovalNotifications(notifications);

                            }

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

            // Check if document is currently under revision
            if (docToChange.getState().equals("Changing")) {
                throw new ForbiddenException();
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

                        // Get template path steps
                        Long pathId = docToChange.getSignoffPathId();
                        List<SignoffPathTemplateSteps> templateSteps = signoffPathService.getTemplateStepsForPath(companyName, pathId);

                        // Create path steps from template and apply to this document
                        List<SignoffPathSteps> newSteps = new ArrayList<>();
                        for (SignoffPathTemplateSteps s : templateSteps) {
                            SignoffPathSteps newStep = new SignoffPathSteps(companyName, documentId, pathId, s.getId(), s.getAction(), s.getUser());
                            newSteps.add(newStep);
                        }
                        newSteps = signoffPathService.createNewStepsForDocRev(newSteps);

                        // Create notifications
                        // Extract initial sequence Ids
                        List<SignoffPathSteps> firstSetOfSteps = SignoffPathHelpers.extractNextSetOfSteps(newSteps);
                        List<ApprovalNotification> notifications = new ArrayList<>();

                        for (SignoffPathSteps s : firstSetOfSteps) {
                            ApprovalNotification notification = new ApprovalNotification(companyName, s.getUser().getUserId(), s.getId(), documentId);
                            notifications.add(notification);
                        }
                        approvalService.addApprovalNotifications(notifications);

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

    /* -------------------------- DocumentComment ----------------------------- */

    @RequestMapping(
            value = "/document/comment",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public DocumentComment createDocumentComment(@RequestBody String json,
                                                 Authentication auth) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            DocumentComment documentComment = mapper.readValue(json, DocumentComment.class);

            String companyName = UserHelpers.getCompany(auth);

            // Check if document belongs to user's company OR exists
            Document doc = documentService.findByCompanyNameAndDocumentId(companyName, documentComment.getDocumentId());
            if (doc == null) {
                throw new ResourceNotFoundException();
            }

            UserDetails me = userDetailsService.findByCompanyNameAndEmail(companyName, auth.getName());

            documentComment.setCompanyName(companyName);
            documentComment.setUser(me);
            documentComment.setDate(new Date());

            return documentService.createDocumentComment(documentComment);
        }
        catch (IOException | NullPointerException ex) {
            throw new ResourceNotFoundException();
        }

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




































