package com.provesoft.resource.controller;

import com.provesoft.resource.exceptions.InternalServerErrorException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
public class UploadController {

    @RequestMapping(
            value = "/upload",
            method = RequestMethod.POST
    )
    @ResponseBody
    public String createDocument(MultipartHttpServletRequest request) {

        HttpHeaders h = request.getRequestHeaders();

        try {
            Iterator<String> itr = request.getFileNames();

            while (itr.hasNext()) {
                String uploadedFile = itr.next();
                MultipartFile file = request.getFile(uploadedFile);
                String filename = file.getOriginalFilename();
                byte[] bytes = file.getBytes();
            }
        }
        catch (IOException ioe) {
            throw new InternalServerErrorException();
        }
        catch (Exception e) {
            throw new InternalServerErrorException();
        }

        return "ASDF";
    }
}
