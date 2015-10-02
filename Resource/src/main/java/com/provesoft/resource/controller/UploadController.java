package com.provesoft.resource.controller;

import com.provesoft.resource.ExternalConfiguration;
import com.provesoft.resource.entity.Document.Document;
import com.provesoft.resource.entity.Document.DocumentRevisions;
import com.provesoft.resource.entity.Document.DocumentUpload;
import com.provesoft.resource.entity.ProfilePicture;
import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.exceptions.InternalServerErrorException;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.DocumentService;
import com.provesoft.resource.service.UserDetailsService;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@RestController
public class UploadController {

    @Autowired
    DocumentService documentService;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    ExternalConfiguration externalConfiguration;

    /**
     * Method retrieves a file based on the parameters, formats the headers with the file metadata, and sends a
     * ResponseEntity with the payload consisting of the file.
     * @param documentId Document Id
     * @param revisionId Revision Id
     * @param isRedline Redline flag
     * @param auth Authentication object
     * @return ResponseEntity
     */
    @RequestMapping(
            value = "/download",
            method = RequestMethod.GET
    )
    public ResponseEntity<byte[]> downloadFile(@RequestParam("documentId") String documentId,
                                               @RequestParam("revisionId") String revisionId,
                                               @RequestParam(value = "isRedline", required= false) Boolean isRedline,
                                               Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        DocumentUpload documentUpload;

        if (isRedline == null) {
            documentUpload = documentService.findUploadByCompanyNameAndDocumentIdAndRevisionAndRedline(companyName, documentId, revisionId, false);
        }
        else {
            documentUpload = documentService.findUploadByCompanyNameAndDocumentIdAndRevisionAndRedline(companyName, documentId, revisionId, isRedline);
        }

        if (documentUpload == null) {
            throw new ResourceNotFoundException();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add( "content-disposition", "attachment; filename=\"" + documentUpload.getFilename() + "\"" );

        // Split the mimeType into primary and sub types
        String primaryType, subType;
        try {
            primaryType = documentUpload.getMimeType().split("/")[0];
            subType = documentUpload.getMimeType().split("/")[1];
        }
        catch (IndexOutOfBoundsException | NullPointerException ex) {
            throw new ResourceNotFoundException();
        }

        headers.setContentType( new MediaType(primaryType, subType) );

        // Return the file data
        Path p = Paths.get(externalConfiguration.getFileUploadDirectory() + documentUpload.getFileId());

        try {
            byte[] fileData = Files.readAllBytes(p);
            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        }
        catch (Exception e) {
            throw new InternalServerErrorException();
        }

    }

    /**
     * Method accepts a MultipartHttpServletRequest for the file data.
     * A DocumentUpload object is created for holding the file meta-data.
     * The file data is uploaded to the filesystem and is assigned a UUID to refer back to the meta-data.
     * @param documentId Document Id
     * @param isRedline Redline flag
     * @param tempUpload Temporary upload flag
     * @param tempRevId UUID assigned to upload because revision Id is not assigned until after user completed revision
     *                  process
     * @param request File payload
     * @param auth Authentication object
     * @return ResponseEntity with a temporary revision Id (UUID)
     */
    @RequestMapping(
            value = "/upload",
            method = RequestMethod.POST
    )
    public ResponseEntity uploadFile(@RequestParam("filename") String filename,
                                     @RequestParam("documentId") String documentId,
                                     @RequestParam("isRedline") Boolean isRedline,
                                     @RequestParam("tempUpload") Boolean tempUpload,
                                     @RequestParam(value = "tempRevId", required = false) String tempRevId,
                                     MultipartHttpServletRequest request,
                                     Authentication auth) {

        DocumentUpload newUpload = null;
        File newFileUpload = null;
        BufferedOutputStream stream = null;

        try {
            Iterator<String> itr = request.getFileNames();

            String companyName = UserHelpers.getCompany(auth);

            Document document = documentService.findDocumentByCompanyNameAndDocumentId(companyName, documentId);

            if (document == null) {
                throw new ResourceNotFoundException();
            }

            while (itr.hasNext()) {
                String uploadedFile = itr.next();
                MultipartFile file = request.getFile(uploadedFile);
                String mimeType = file.getContentType();
                String fileId = UUID.randomUUID().toString();
                byte[] bytes = file.getBytes();

                // Sanitize filename
                filename = filename.replaceAll("(?:\\n|\\r|\"|\')", " ");

                DocumentUpload newUploadedFile;

                if (tempUpload) {
                    if (tempRevId.equals("null")) {
                        tempRevId = UUID.randomUUID().toString();
                    }
                    newUploadedFile = new DocumentUpload(companyName, documentId, fileId, filename, mimeType, tempRevId, isRedline);
                }
                else {
                    newUploadedFile = new DocumentUpload(companyName, documentId, fileId, filename, mimeType, document.getRevision(), isRedline);
                }

                // Add document meta-data
                newUpload = documentService.addDocumentFile(newUploadedFile);

                // Upload file
                newFileUpload = new File(externalConfiguration.getFileUploadDirectory() + fileId);
                stream = new BufferedOutputStream(new FileOutputStream(newFileUpload));
                stream.write(bytes);
            }
        }
        catch (IOException ioe) {
            // Delete upload if it exists
            documentService.deleteErrorUpload(newUpload);

            // Delete document from file system if it was uploaded
            try {
                newFileUpload.delete();
            }
            catch (NullPointerException npe) {
                throw new InternalServerErrorException();
            }

            throw new InternalServerErrorException();
        }
        catch (Exception e) {
            throw new InternalServerErrorException();
        }
        finally {
            try {
                stream.close();
            }
            catch (Exception e) {
                assert true;
            }
        }

        return new ResponseEntity<>("{\"tempRevId\":\"" + tempRevId + "\"}", HttpStatus.OK);
    }





//=======================================================
/*
    TEMPORARY METHOD FOR UPLOADING PROFILE PICTURE
 */
//=======================================================

    /**
     * Method is here temporarily to allow profile picture uploading for testing purposes
     * @param request Picutre data
     * @param auth Authentication object
     * @return ReponseEntity
     */
    @RequestMapping(
            value = "/upload/profilePicture",
            method = RequestMethod.POST
    )
    public ResponseEntity uploadProfilePicture(MultipartHttpServletRequest request,
                                               Authentication auth) {

        try {
            Iterator<String> itr = request.getFileNames();

            String companyName = UserHelpers.getCompany(auth);

            while (itr.hasNext()) {
                String uploadedFile = itr.next();
                MultipartFile file = request.getFile(uploadedFile);
                byte[] bytes = file.getBytes();

                Long userId = userDetailsService.findUserIdByCompanyNameAndEmail(companyName, auth.getName());

                ProfilePicture newPic = new ProfilePicture(companyName, userId, bytes);
                userDetailsService.uploadProfilePicture(newPic);
            }
        }
        catch (IOException ioe) {
            throw new InternalServerErrorException();
        }
        catch (Exception e) {
            throw new InternalServerErrorException();
        }

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }


    /**
     * Method updates the revisionId of a document once the revision process if finalized. UUID replaces with rev Id
     * @param documentId Document Id
     * @param tempRevId UUID assigned to temporary document upload
     * @param newRevId Real generated revision Id upon finalization of revision process
     * @param auth Authentication object
     * @return ResponseEntity (HTTP Status)
     */
    @RequestMapping(
            value = "/upload/updateRevId",
            method = RequestMethod.PUT
    )
    public ResponseEntity updateUploadRevId(@RequestParam("documentId") String documentId,
                                            @RequestParam("tempRevId") String tempRevId,
                                            @RequestParam("newRevId") String newRevId,
                                            Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        DocumentRevisions docRev = documentService.findDocRevByCompanyNameAndDocumentIdAndRevisionId(companyName, documentId, newRevId);
        if (docRev == null) {
            throw new ResourceNotFoundException();
        }

        documentService.updateRevisionId(companyName, tempRevId, newRevId);

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }


    /**
     * Method deletes a temporary uploaded revision document should the user cancel the revision process before
     * completing it
     * @param documentId Document Id
     * @param tempRevId UUID assigned to temporary revision upload
     * @param auth Authentication object
     * @return Response Entity (HTTP Status)
     */
    @RequestMapping(
            value = "/upload",
            method = RequestMethod.DELETE
    )
    public ResponseEntity deleteDocument(@RequestParam("documentId") String documentId,
                                         @RequestParam("tempRevId") String tempRevId,
                                         Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        // Verify tempRevId is a UUID string and not a legitimate revision
        if (!tempRevId.contains("-")) {
            throw new ForbiddenException();
        }

        List<DocumentUpload> tempUploads = documentService.findUploadByCompanyNameAndDocumentIdAndRevision(companyName, documentId, tempRevId);
        try {
            for (DocumentUpload du : tempUploads) {
                File f = new File(externalConfiguration.getFileUploadDirectory() + du.getFileId());
                f.delete();
            }

            documentService.deleteUploads(companyName, documentId, tempRevId);
        }
        catch (Exception e) {
            throw new InternalServerErrorException();
        }

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

}




























