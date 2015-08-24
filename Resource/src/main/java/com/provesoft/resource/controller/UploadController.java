package com.provesoft.resource.controller;

import com.provesoft.resource.entity.Document.Document;
import com.provesoft.resource.entity.Document.DocumentRevisions;
import com.provesoft.resource.entity.Document.DocumentUpload;
import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.exceptions.InternalServerErrorException;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.DocumentService;
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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@RestController
public class UploadController {

    @Autowired
    DocumentService documentService;

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
        headers.add( "content-disposition", "attachement; filename=\"" + documentUpload.getFilename() + "\"" );

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

        return new ResponseEntity<>(documentUpload.getFile(), headers, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/upload",
            method = RequestMethod.POST
    )
    public ResponseEntity uploadFile(@RequestParam("documentId") String documentId,
                                     @RequestParam("isRedline") Boolean isRedline,
                                     @RequestParam("tempUpload") Boolean tempUpload,
                                     @RequestParam(value = "tempRevId", required = false) String tempRevId,
                                     MultipartHttpServletRequest request,
                                     Authentication auth) {

        try {
            Iterator<String> itr = request.getFileNames();

            String companyName = UserHelpers.getCompany(auth);

            Document document = documentService.findByCompanyNameAndDocumentId(companyName, documentId);

            if (document == null) {
                throw new ResourceNotFoundException();
            }

            while (itr.hasNext()) {
                String uploadedFile = itr.next();
                MultipartFile file = request.getFile(uploadedFile);
                String mimeType = file.getContentType();
                String filename = file.getOriginalFilename();
                byte[] bytes = file.getBytes();

                DocumentUpload newUploadedFile;

                if (tempUpload) {
                    if (tempRevId.equals("null")) {
                        tempRevId = UUID.randomUUID().toString();
                    }
                    newUploadedFile = new DocumentUpload(companyName, documentId, bytes, filename, mimeType, tempRevId, isRedline);
                }
                else {
                    newUploadedFile = new DocumentUpload(companyName, documentId, bytes, filename, mimeType, document.getRevision(), isRedline);
                }

                documentService.addDocumentFile(newUploadedFile);
            }
        }
        catch (IOException ioe) {
            throw new InternalServerErrorException();
        }
        catch (Exception e) {
            throw new InternalServerErrorException();
        }

        return new ResponseEntity<>("{\"tempRevId\":\"" + tempRevId + "\"}", HttpStatus.OK);
    }


    /*
        Update the revision Id after the document revision has been created (Id has been generated)
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


    /*
        Delete Uploaded Document (Only Used For Temporary Docs. Called upon cancelled revision)
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

        documentService.deleteTempUploads(companyName, documentId, tempRevId);

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

}




























