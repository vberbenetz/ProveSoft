package com.provesoft.resource.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provesoft.resource.entity.Document.*;
import com.provesoft.resource.entity.SignoffPath.SignoffPath;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.entity.SignoffPath.SignoffPathTemplateSteps;
import com.provesoft.resource.entity.SystemSettings;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.exceptions.*;
import com.provesoft.resource.service.*;
import com.provesoft.resource.utils.SignoffPathHelpers;
import com.provesoft.resource.utils.UserHelpers;
import com.provesoft.resource.validators.DocumentFormValidation;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.transaction.TransactionRolledbackException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The DocumentController encompasses all routes tied to Document, DocumentType, DocumentRevision, and DocumentComment
 * objects.
 */
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

    /**
     * Method retrieves documents based on which parameters are passed in.
     * 1) By documentId: returns 1 document based on Id and company
     * 2) By documentIds: returns list of documents based on list of doc Ids and company
     * 3) By organizationId: returns list of documents associated with an organization
     * 4) By searchString: returns list of documents based on wildcard search string
     * @param documentId Single document Id
     * @param documentIds Array of document Ids
     * @param organizationId Single organization Id
     * @param searchString String used to perform wildcard search
     * @param includeObsolete Flag complements search search string to include obsolete documents
     * @param auth Authentication object
     * @return ReponseEntity with a Document or List of Documents as its payload
     */
    @RequestMapping(value = "/document",
            method = RequestMethod.GET
    )
    public ResponseEntity<?> getDocument (@RequestParam(value = "documentId", required = false) String documentId,
                                          @RequestParam(value = "documentIds", required = false) String[] documentIds,
                                          @RequestParam(value = "organizationId", required = false) Long organizationId,
                                          @RequestParam(value = "searchString", required = false) String searchString,
                                          @RequestParam(value = "includeObsolete", required = false) Boolean includeObsolete,
                                          @RequestParam(value = "all", required = false) Boolean all,
                                          Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        if (documentId != null) {
            Document d = documentService.findDocumentById(companyName, documentId);
            return new ResponseEntity<Object>(d, HttpStatus.OK);
        }

        if ( (documentIds != null) && (documentIds.length > 0) ) {
            List<Document> dList = documentService.findDocumentByIdList(companyName, documentIds);
            return new ResponseEntity<Object>(dList, HttpStatus.OK);
        }

        if (organizationId != null) {
            List<Document> dList = documentService.findDocumentByCompanyNameAndOrganizationId(companyName, organizationId);
            return new ResponseEntity<Object>(dList, HttpStatus.OK);
        }

        if (searchString != null) {

            if (includeObsolete == null) {
                includeObsolete = false;
            }

            // Add wildcard characters
            searchString = "%" + searchString + "%";
            List<Document> dList = documentService.findDocumentBySearchString(companyName, searchString, includeObsolete);
            return new ResponseEntity<Object>(dList, HttpStatus.OK);
        }

        if (all != null) {
            List<Document> dList = documentService.findNonObsoleteDocumentByCompanyName(companyName);
            return new ResponseEntity<Object>(dList, HttpStatus.OK);
        }

        // If all params are non-existant, return first 10 by company
        List<Document> dList = documentService.findFirst10DocumentsByCompanyName(companyName);
        return new ResponseEntity<Object>(dList, HttpStatus.OK);
    }

    /**
     * Method checks if document with this title exists for company
     * @param title Title of document to be created
     * @param auth Authentication object
     * @return Map
     */
    @RequestMapping(
            value = "/document/exist",
            method = RequestMethod.GET
    )
    public Map<String, Boolean> doesDocumentWithTitleExist(@RequestParam("title") String title,
                                                           Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        Map<String, Boolean> retMap = new HashMap<>();
        retMap.put("exists", documentService.isDocumentTitleTaken(companyName, title));
        return retMap;
    }

    /* ------ DocumentType ------ */

    /**
     * Method retrieves all DocumentTypes for this company
     * @param auth Authentication Object
     * @return List of DocumentType
     */
    @RequestMapping(value = "/document/type",
            method = RequestMethod.GET
    )
    public List<DocumentType> getDocumentTypes (Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);
        return documentService.findDocumentTypeByCompanyName(companyName);
    }


    /* ------ DocumentRevision ------ */

    /**
     * Method returns DocumentRevisions List depeneding on which parameters are passed in
     * 1) Return DocumentRevisions by documentId
     * 2) Return DocumentRevisions by documentId array
     * 3) No parameters - return lastest DocumentRevisions for company
     * @param documentId Single documentId
     * @param documentIds Array of documentIds
     * @param auth Authentication object
     * @return DocumentRevisions List
     */
    @RequestMapping(
            value = "/document/revision",
            method = RequestMethod.GET
    )
    public List<DocumentRevisions> getDocumentRevision(@RequestParam(value = "documentId", required = false) String documentId,
                                                       @RequestParam(value = "documentIds", required = false) String[] documentIds,
                                                       Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);
        List<DocumentRevisions> drList;

        if (documentId != null) {
            drList = documentService.findDocRevByCompanyNameAndDocumentId(companyName, documentId);
            return drList;
        }

        if ( (documentIds != null) && (documentIds.length > 0) ) {
            drList = documentService.findLatestDocRevsByCompanyNameAndDocumentIds(companyName, documentIds);
            return drList;
        }

        return documentService.findLatestDocRevsByCompanyName(companyName);
    }


    /* ------ DocumentRevisionLike ------ */

    /**
     * Method retrieves DocumentRevisionLikes based on revision query of RevisionLikeQueryPkgs
     * @param queryParamCodes Triple pipe separated documentId/revisionId params
     * @param auth Authentication Object
     * @return List of DocumentRevisionLikes
     */
    @RequestMapping(
            value = "/document/revision/like",
            method = RequestMethod.GET
    )
    public List<DocumentRevisionLike> getDocumentRevisionLikes (@RequestParam("queryParamCodes") String[] queryParamCodes,
                                                                Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        List<DocumentRevisionLike> retList = new ArrayList<>();

        try {
            for (String qpc : queryParamCodes) {

                String documentId = qpc.split("\\|\\|\\|")[0];
                String revisionId = qpc.split("\\|\\|\\|")[1];

                retList.addAll( documentService.findRevisionLikes(companyName, documentId, revisionId) );
            }
        }
        catch (Exception ex) {
            throw new BadRequestException();
        }

        return retList;
    }


    /* ---------- DocumentComment ---------- */

    /**
     * Method retrieves List of DocumentComments based on parameters passed in:
     * 1) By documentId: Get list of comments by documentId
     * 2) No parameters: Get list of comments by company
     * @param documentId DocumentId used to lookup comments
     * @param recent Flag indicating that most recent comments are needed for Document (false version not implemented yet)
     * @param auth Authentication object
     * @return List of DocumentComment
     */
    @RequestMapping(
            value = "/document/comment",
            method = RequestMethod.GET
    )
    public List<DocumentComment> getDocumentComments(@RequestParam(value = "documentId", required = false) String documentId,
                                                     @RequestParam(value = "recent", required = false) Boolean recent,
                                                     Authentication auth) {
// TODO: IMPLEMENT METHOD IF RECENT IS NOT TRUE

        String companyName = UserHelpers.getCompany(auth);

        if (documentId != null) {

            if ( (recent != null) && (recent) ) {
                return documentService.getRecentDocumentComments(companyName, documentId);
            }
        }

        // Return recent documents otherwise
        return documentService.findLatestCommentsByCompanyName(companyName);
    }

    /**
     * Method retrieves all child comments for the list of parent commentIds passed in
     * @param parentCommentIds Array of parent commentId for which children will be fetched for
     * @param auth Authentication object
     * @return List of DocumentComment
     */
    @RequestMapping(
            value = "/document/comment/children",
            method = RequestMethod.GET
    )
    public List<DocumentComment> getChildrenCommentsByParentIds(@RequestParam("parentCommentIds") Long[] parentCommentIds,
                                                                Authentication auth) {
        String companyName = UserHelpers.getCompany(auth);
        return documentService.findChildrenCommentsByParentIds(companyName, parentCommentIds);
    }

     /* ------------------- DocumentCommentLike ------------------- */

    /**
     * Method retrieves DocumentCommentLikes for specific DocumentComment
     * @param documentCommentIds Array of DocumentComment Ids to retrieve likes for
     * @param auth Authentication object
     * @return List of DocumentCommentLike
     */
    @RequestMapping(
            value = "/document/comment/like",
            method = RequestMethod.GET
    )
    public List<DocumentCommentLike> getLikesForCommentList (@RequestParam("documentCommentIds") Long[] documentCommentIds,
                                                             Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        return documentService.findLikesByCommentList(companyName, documentCommentIds);
    }

    /* ------------------- FavouriteDocuments ------------------- */

    /**
     * Method retrieves all favourite documents for this user
     * @param auth Authentication object
     * @return List of FavouriteDocuments
     */
    @RequestMapping(
            value = "/document/favourite",
            method = RequestMethod.GET
    )
    public List<FavouriteDocuments> getFavouriteDocumentsForSelf (Authentication auth) {
        String companyName = UserHelpers.getCompany(auth);
        return documentService.findAllFavouriteDocumentsByUser(companyName, auth.getName());
    }


    /* --------------------------------------------------------- */
    /* ------------------------ POST --------------------------- */
    /* --------------------------------------------------------- */

    /* ------ Document ------ */

    /**
     * Method creates a new Document:
     * 1) Converts POST payload to base Document object
     * 2) Get and generate a new unique document Id
     * 3) Save document object to the database
     * 4) If signoffs required, retrieve template steps and send notifications to approvers
     * @param json POST payload containing basic Document info
     * @param auth Authentication object
     * @return Document Object
     */
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

            // Get system setting to see if signoffs are required
            SystemSettings signoffSetting = systemSettingsService.getSettingByCompanyNameAndSetting(companyName, "signoff");

            if (companyName != null) {

                // Verify Document
                if (!DocumentFormValidation.validateNewDocument(newDocument, signoffSetting.getValue())) {
                    throw new BadRequestException("Document validation failed");
                }

                // Verify if document with this title already exists
                if (documentService.isDocumentTitleTaken(companyName, newDocument.getTitle())) {
                    throw new BadRequestException("Document title already taken");
                }

                newDocument.setCompanyName(companyName);

                // Retry if deadlock occurs until the resource becomes free or timeout occurs
                for (long stop=System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(30L); stop > System.currentTimeMillis();) {

                    try {
                        Long suffix = documentService.getAndGenerateDocumentId(companyName, newDocument.getDocumentType().getId()).getCurrentSuffixId();
                        Integer maxNumberOfDigits = newDocument.getDocumentType().getMaxNumberOfDigits();

                        // Automatically increment digit length to prevent overflow
                        if (String.valueOf(suffix).length() > maxNumberOfDigits) {
                            DocumentType dt = newDocument.getDocumentType();
                            dt.setMaxNumberOfDigits(dt.getMaxNumberOfDigits()+1);
                            newDocument.setDocumentType(documentService.updateDocumentType(dt));
                        }

                        String documentId = newDocument.getDocumentType().getDocumentPrefix();

                        for (int i = String.valueOf(suffix).length(); i < maxNumberOfDigits; i++) {
                            documentId = documentId + "0";
                        }

                        // Get user for revision details
                        UserDetails user = userDetailsService.findByCompanyNameAndEmail(companyName, auth.getName());

                        Date currentDate = new Date();

                        documentId = documentId + suffix;
                        newDocument.setId(documentId);
                        newDocument.setRevision("A");
                        newDocument.setState("Released");
                        newDocument.setDate(currentDate);

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
                                ApprovalNotification notification = new ApprovalNotification(companyName, s.getUser().getUserId(), s.getId(), documentId, "Released");
                                notifications.add(notification);
                            }
                            approvalService.addApprovalNotifications(notifications);

                        }

                        return documentService.addDocument(newDocument, suffix, user);

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

    /**
     * Method creates a new DocumentRevision
     * 1) Create basic DocumentRevision object from POST payload
     * 2) Get and increment a new unique RevisionId for this object
     * 3a) Persist full Revision object to database
     * 3b) If marking as obsolete, do not create a new revision
     * 4) If signoffs required, copy template steps and send out notifications to required approvers
     * @param json POST payload for basic DocumentRevision object
     * @param auth Authentication object
     * @return DocumentRevision
     */
    @RequestMapping(
            value = "/document/revision",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public DocumentRevisions createDocumentRevision(@RequestBody String json,
                                                    Authentication auth) {

// TODO: CHECK IF USER HAS PERMISSIONS TO EDIT FOR THIS DOCUMENT BELONGING TO ORGANIZATION

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            String documentId = rootNode.get("documentId").textValue();
            String changeReason = rootNode.get("changeReason").textValue();
            String changeUserEmail = rootNode.get("changeUserEmail").textValue();
            Boolean redlineDocPresent = rootNode.get("redlineDocPresent").booleanValue();
            Boolean changeToObsolete = rootNode.get("changeToObsolete").booleanValue();

            String companyName = UserHelpers.getCompany(auth);
            Date currentDate = new Date();
            String nextState = "Released";

            if (changeToObsolete) {
                nextState = "Obsolete";
            }

            UserDetails changeUser = userDetailsService.findByCompanyNameAndEmail(companyName, changeUserEmail);

            if (changeUser == null) {
                throw new ResourceNotFoundException();
            }

            // Verify document belongs to company
            Document docToChange = documentService.findDocumentByCompanyNameAndDocumentId(companyName, documentId);
            if (docToChange == null) {
                throw new ResourceNotFoundException();
            }

            // Check if document is obsolete
            if (docToChange.getState().equals("Obsolete")) {
                throw new BadRequestException();
            }

            // Mark document as changing. If state change fails cancel revision
            docToChange = documentService.getAndSetDocumentState(companyName, documentId, "Changing");
            if (docToChange == null) {
                throw new ConflictException();
            }

            // Validate changeReason
            if ( (changeReason == null) || (changeReason.length() < 1) || (changeReason.length() > 1000) ) {
                throw new BadRequestException("Revision validation error");
            }

            // Strip newlines from changeReason
            changeReason = changeReason.replaceAll("(?:\\n|\\r)", " ");

            // Get system setting to see if signoffs are required
            SystemSettings signoffSetting = systemSettingsService.getSettingByCompanyNameAndSetting(companyName, "signoff");

            /* -------- Critical area starts here ---------- */
            // Multiple users may attempt to get new rev Id.
            // Retry until resource becomes free to generate new Id.
            for (long stop=System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(30L); stop > System.currentTimeMillis();) {

                try {
                    String documentRevisionId;

                    if (!changeToObsolete) {
                        documentRevisionId = documentService.getAndGenerateDocumentRevisionId(companyName, documentId);
                    }
                    else {
                        documentRevisionId = "Obsolete";
                    }

                    DocumentRevisions newRevision = new DocumentRevisions(
                            companyName,
                            documentId,
                            documentRevisionId,
                            changeReason,
                            changeUser,
                            currentDate,
                            redlineDocPresent
                    );

                    newRevision = documentService.addNewDocumentRevision(newRevision);

                    docToChange.setRevision(documentRevisionId);
                    docToChange.setDate(currentDate);

                    // Set document status to changing if signoffs are required
                    if ( (signoffSetting.getValue().equals("on")) && (docToChange.getSignoffPathId() != null) ) {

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
                            ApprovalNotification notification = new ApprovalNotification(companyName, s.getUser().getUserId(), s.getId(), documentId, nextState);
                            notifications.add(notification);
                        }
                        approvalService.addApprovalNotifications(notifications);

                    }
                    else {
                        // Change state to Released because Signoffs are not required
                        docToChange.setState("Released");
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

            // Revert document state because return block was never triggered (Exception was thrown in transaction attempt loop
            docToChange.setState("Released");
            documentService.updateDocument(docToChange);

        }
        catch (IOException ioe) {
            throw new ResourceNotFoundException();
        }

        throw new ResourceNotFoundException();
    }

    /* -------------------------- DocumentRevisionLike ----------------------------- */

    @RequestMapping(
            value = "/document/revision/like",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public DocumentRevisionLike addDocumentRevisionLike (@RequestBody String json,
                                                         Authentication auth) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            DocumentRevisionLike documentRevisionLike = mapper.readValue(json, DocumentRevisionLike.class);

            String companyName = UserHelpers.getCompany(auth);

            Long myUserId = userDetailsService.findUserIdByCompanyNameAndEmail(companyName, auth.getName());

            // Check if user has access to document
            if (documentService.findDocRevByCompanyNameAndDocumentIdAndRevisionId(
                    companyName,
                    documentRevisionLike.getKey().getDocumentId(),
                    documentRevisionLike.getKey().getRevisionId() ) == null) {

                throw new BadRequestException();
            }

            DocumentRevisionLikeKey dKey = documentRevisionLike.getKey();
            dKey.setCompanyName(companyName);
            dKey.setUserId(myUserId);
            documentRevisionLike.setKey(dKey);

            return documentService.createRevisionLike(documentRevisionLike);
        }
        catch (IOException | NullPointerException ex) {
            throw new BadRequestException();
        }
    }

    /* -------------------------- DocumentComment ----------------------------- */

    /**
     * Method creates a new DocumentComment based on POST payload
     * @param json POST payload
     * @param auth Authentication object
     * @return DocumentComment
     */
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
            Document doc = documentService.findDocumentByCompanyNameAndDocumentId(companyName, documentComment.getDocumentId());
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


    /* ------------------- DocumentCommentLike ------------------- */

    /**
     * Method adds a DocumentCommentLike to respective DocumentComment
     * @param documentCommentId DocumentComment for which like will be associated with
     * @param auth Authentication object
     * @return DocumentCommentLike
     */
    @RequestMapping(
            value = "/document/comment/like",
            method = RequestMethod.POST
    )
    public DocumentCommentLike createCommentLike(@RequestParam("documentCommentId") Long documentCommentId,
                                                 Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);
        Long userId = userDetailsService.findUserIdByCompanyNameAndEmail(companyName, auth.getName());

        DocumentCommentLike dcl = new DocumentCommentLike(companyName, documentCommentId, userId);

        return documentService.createCommentLike(dcl);
    }


    /* ------------------- FavouriteDocuments ------------------- */

    /**
     * Method adds a FavouriteDocument for this user
     * @param json Payload containing a Document to add to favourites
     * @param auth Authentication object
     * @return FavouriteDocument
     */
    @RequestMapping(
            value = "/document/favourite",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public FavouriteDocuments addNewFavouriteDocument(@RequestBody String json,
                                                      Authentication auth) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            Document document = mapper.readValue(json, Document.class);

            String companyName = UserHelpers.getCompany(auth);

            // Check if document exists and belongs to this company
            Document docCheck = documentService.findDocumentByCompanyNameAndDocumentId(companyName, document.getId());
            if (docCheck != null) {
                FavouriteDocuments newFav = new FavouriteDocuments(companyName, auth.getName(), docCheck);
                return documentService.addFavouriteDocument(newFav);
            }
            else {
                throw new BadRequestException();
            }

        }
        catch (IOException | NullPointerException ex) {
            throw new BadRequestException();
        }
        catch (Exception ex) {
            throw new InternalServerErrorException();
        }
    }


    /* --------------------------------------------------------- */
    /* ------------------------ UPDATE ------------------------- */
    /* --------------------------------------------------------- */

    /**
     * Method edits document fields. Currently it only updates the signoff path for the document.
     * In the future this can be expanded to update other aspects of the document
     * @param documentId Document Id of the document that will be edited
     * @param signoffPathId SignoffPath Id to append document with. In the future this will be optional
     * @param auth Authentication object with current user information
     * @return Updated Document object
     */
    @RequestMapping(
            value = "/document",
            method = RequestMethod.PUT
    )
    public Document changeDocumentSignoffPath(@RequestParam("documentId") String documentId,
                                              @RequestParam("signoffPathId") Long signoffPathId,
                                              Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            Document doc = documentService.findDocumentByCompanyNameAndDocumentId(companyName, documentId);

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

    /**
     * Method marks the document as obsolete (Admin only function. Reserved for future use)
     * @param documentId Document id of the document to be marked obsolete
     * @param auth Authentication object
     * @return Updated Document
     */
    @RequestMapping(
            value = "/document/markObsolete",
            method = RequestMethod.PUT
    )
    public Document markDocumentObsolete(@RequestParam("documentId") String documentId,
                                         Authentication auth) {

        if (UserHelpers.isSuperAdmin(auth)) {

            String companyName = UserHelpers.getCompany(auth);

            // Verify document belongs to company
            Document docToChange = documentService.findDocumentByCompanyNameAndDocumentId(companyName, documentId);
            if (docToChange == null) {
                throw new ResourceNotFoundException();
            }

            // Mark document as changing. If state change fails cancel revision
            docToChange = documentService.getAndSetDocumentState(companyName, documentId, "Changing");
            if (docToChange == null) {
                throw new ConflictException();
            }

            // Get system setting to see if signoffs are required
            SystemSettings signoffSetting = systemSettingsService.getSettingByCompanyNameAndSetting(companyName, "signoff");

            docToChange.setDate(new Date());

            // Set document status to changing if signoffs are required
            if (signoffSetting.getValue().equals("on")) {

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
                    ApprovalNotification notification = new ApprovalNotification(companyName, s.getUser().getUserId(), s.getId(), documentId, "Obsolete");
                    notifications.add(notification);
                }
                approvalService.addApprovalNotifications(notifications);

            }
            else {
                docToChange.setState("Obsolete");
            }

            return documentService.updateDocument(docToChange);

        }

        throw new ForbiddenException();
    }


    /* --------------------------------------------------------- */
    /* ------------------------ DELETE ------------------------- */
    /* --------------------------------------------------------- */

    /* ------------------------- FavouriteDocument -------------------------- */

    /**
     * Method removes a favourite document from this user
     * @param documentId DocumentId to remove from this user
     * @param auth Authentication object
     */
    @RequestMapping(
            value = "/document/favourite",
            method = RequestMethod.DELETE
    )
    public ResponseEntity removeFavouriteDocument(@RequestParam("documentId") String documentId,
                                                  Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);
        documentService.removeFavouriteDocument(companyName, auth.getName(), documentId);
        return new ResponseEntity(HttpStatus.OK);
    }
}
