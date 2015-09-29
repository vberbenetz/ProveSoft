package com.provesoft.resource.validators;

import com.provesoft.resource.entity.Document.Document;
import com.provesoft.resource.entity.Document.DocumentType;
import com.provesoft.resource.entity.Organizations;

public final class DocumentFormValidation {

    private DocumentFormValidation() {}

    public static Boolean validateNewDocument(Document document) {

        String title = document.getTitle();
        DocumentType type = document.getDocumentType();
        Organizations organization = document.getOrganization();
        Long signoffPathId = document.getSignoffPathId();

        if (title == null) {
            return false;
        }
        else if ( (title.length() == 0) || (title.length() > 254) || (title.equals("")) ) {
            return false;
        }

        if (type == null) {
            return false;
        }

        if (organization == null) {
            return false;
        }

        if (signoffPathId == null) {
            return false;
        }

        return true;
    }
}
