package test;

import com.provesoft.resource.ResourceApplication;
import com.provesoft.resource.entity.Document.Document;
import com.provesoft.resource.entity.Document.DocumentType;
import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import com.provesoft.resource.service.DocumentService;
import com.provesoft.resource.service.OrganizationsService;
import com.provesoft.resource.utils.DocumentHelpers;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.Assert;
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

            String companyName = "Company";

            DocumentType documentType = new DocumentType("Company", "name", "desc", "SU", new Integer(6), new Long(1));
            documentType.setId(1L);

            Organizations organization = organizationsService.findByOrganizationIdAndCompanyName(1L, "Company");

            Document newDocument = new Document(companyName, "Title", documentType, organization);

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

                        documentId = documentId + suffix;
                        newDocument.setId(documentId);

                        //documentService.addDocument(newDocument, suffix, new UserDetails("TEST", "test", "test", "test@test.com", "test", ));

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

    public class DocumentStateTestRunnable implements Runnable {

        public String documentId = "TEST0001";
        String companyName = "Company";

        @Override
        public void run() {

            Document d = documentService.getAndSetDocumentState(this.companyName, this.documentId, "Changing");
            try {
                if (d != null) {
                    System.out.println(Thread.currentThread().getId() + ": " + d.getState());
                }
            }
            catch (Exception e) {
                assert true;
            }
        }
    }

    //@Test
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

    //@Test
    public void documentRevisionIncrementTest() {
        String id1 = "ABC";
        String id4 = "ABZ";
        String id2 = "AZ";
        String id3 = "ZZZZ";

        String nextId1 = DocumentHelpers.genNextRevId(id1);
        String nextId4 = DocumentHelpers.genNextRevId(id4);
        String nextId2 = DocumentHelpers.genNextRevId(id2);
        String nextId3 = DocumentHelpers.genNextRevId(id3);

        System.out.println(nextId1);
        System.out.println(nextId4);
        System.out.println(nextId2);
        System.out.println(nextId3);

        Assert.assertEquals("ABD", nextId1);
        Assert.assertEquals("ACA", nextId4);
        Assert.assertEquals("BA", nextId2);
        Assert.assertEquals("AAAAA", nextId3);
    }

    @Test
    public void documentRevisionRollBackTest() {
        String i1 = "A";
        String i2 = "AAA";
        String i3 = "ABC";
        String i4 = "ABA";

        String prevId1 = DocumentHelpers.rollBackRevId(i1);
        String prevId2 = DocumentHelpers.rollBackRevId(i2);
        String prevId3 = DocumentHelpers.rollBackRevId(i3);
        String prevId4 = DocumentHelpers.rollBackRevId(i4);

        System.out.println(prevId1);
        System.out.println(prevId2);
        System.out.println(prevId3);
        System.out.println(prevId4);

        Assert.assertEquals("A", prevId1);
        Assert.assertEquals("ZZ", prevId2);
        Assert.assertEquals("ABB", prevId3);
        Assert.assertEquals("AAZ", prevId4);
    }

    //@Test
    public void getNextSetOfApproversTest() {}

    @Test
    public void documentStateChangeTest() {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            Runnable worker = new DocumentStateTestRunnable();
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
