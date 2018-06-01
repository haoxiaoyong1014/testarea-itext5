package mkl.testarea.itext5.signature;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tsp.TimeStampToken;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * @author mkl
 */
public class VerifyTimestamp {
    @BeforeClass
    public static void setUp() throws Exception {
        BouncyCastleProvider bcp = new BouncyCastleProvider();
        //Security.addProvider(bcp);
        Security.insertProviderAt(bcp, 1);
    }

    /**
     * <a href="https://stackoverflow.com/questions/48211757/itext-pdf-timestamp-validation-returns-false-why">
     * iText pdf timestamp validation returns false, why?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1skI3NM9cqw2m6eW9jKXaJXKzvCjyQMib/view">
     * testpdf_timestamp.pdf
     * </a>
     * <p>
     * The code the OP used for inspiration was for retrieving information
     * from a signature which may include a signature time stamp. The PDF
     * of the OP, on the other hand, contains a document time stamp. The
     * call `pkcs7.verifyTimestampImprint()` checks the time stamp as a
     * signature time stamp and, therefore, fails.
     * </p>
     */
    @Test
    public void testDocumentTimestampLikeRadekKantor() throws IOException, GeneralSecurityException {
        try (   InputStream resource = getClass().getResourceAsStream("testpdf_timestamp.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields fields = reader.getAcroFields();
            ArrayList<String> names = fields.getSignatureNames();
            for (String name : names) {
                System.out.println("===== " + name + " =====");
                System.out.println("Signature covers whole document: " + fields.signatureCoversWholeDocument(name));
                System.out.println("Document revision: " + fields.getRevision(name) + " of " + fields.getTotalRevisions());
                PdfPKCS7 pkcs7 = fields.verifySignature(name);
                System.out.println("Integrity check OK? " + pkcs7.verify());
                SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
                System.out.println("Signed on: " + date_format.format(pkcs7.getSignDate().getTime()));
                if (pkcs7.getTimeStampDate() != null) {
                    System.out.println("TimeStamp: " + date_format.format(pkcs7.getTimeStampDate().getTime()));
                    TimeStampToken ts = pkcs7.getTimeStampToken();
                    System.out.println("TimeStamp service: " + ts.getTimeStampInfo().getTsa());
                    // Why pkcs7.verifyTimestampImprint() returns FLASE?
                    System.out.println("Timestamp verified? " + pkcs7.verifyTimestampImprint());
                }
            }
        }
    }
}
