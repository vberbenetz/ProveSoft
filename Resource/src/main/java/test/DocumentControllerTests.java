package test;

import com.provesoft.resource.ResourceApplication;
import com.provesoft.resource.entity.Document;
import com.provesoft.resource.entity.DocumentType;
import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.DocumentService;
import com.provesoft.resource.service.OrganizationsService;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.TransactionRolledbackException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ResourceApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class DocumentControllerTests {

    @Autowired
    DocumentService documentService;

    @Autowired
    OrganizationsService organizationsService;

    public class DocumentIdTestRunnable implements Runnable {

        @Override
        public void run() {

            DocumentType documentType = new DocumentType("Company", "name", "desc", "SU", new Integer(6), new Long(1));
            documentType.setId(1L);

            Organizations organization = organizationsService.findByOrganizationIdAndCompanyName(1L, "Company");

            Document newDocument = new Document("Company", "Title", documentType, organization);

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

                        documentService.addDocument(newDocument, suffix);

                        return;
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
                    return;
                }
            }
        }
    }

    @Test
    public void documentIdGenerationTest() {

        ExecutorService executor = Executors.newFixedThreadPool(400);
        for (int i = 0; i < 400; i++) {
            Runnable worker = new DocumentIdTestRunnable();
            executor.execute(worker);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch(InterruptedException ie) {
            ie.printStackTrace();
        }
    }

}
