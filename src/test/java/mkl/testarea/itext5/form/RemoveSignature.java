package mkl.testarea.itext5.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * @author mkl
 */
public class RemoveSignature
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://itext.2136553.n4.nabble.com/trying-to-remove-a-signature-from-pdf-file-tt4660983.html">
     * trying to remove a signature from pdf file
     * </a>
     * <br/>
     * <a href="http://itext.2136553.n4.nabble.com/attachment/4660983/0/PDFSignedFirmaCerta.pdf">
     * PDFSignedFirmaCerta.pdf
     * </a>
     * <p>
     * Indeed, this code fails with a {@link NullPointerException}. The cause is that a dubious construct
     * created by the signature software then is processed by iText code not sufficiently defensively programmed:
     * The signature claims to have an annotation on a page but that page does claim not to have any anotations
     * at all.
     * </p>
     */
    @Test
    public void testRemoveSignatureFromPDFSignedFirmaCerta() throws IOException, GeneralSecurityException, DocumentException
    {
        try (   InputStream inputStream = getClass().getResourceAsStream("PDFSignedFirmaCerta.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "PDFSignedFirmaCerta-withoutSig.pdf")))
        {
            Provider provider = new BouncyCastleProvider();
            Security.addProvider(provider);

            PdfReader reader = new PdfReader(inputStream, null);
            AcroFields af = reader.getAcroFields();
            ArrayList<String> names = af.getSignatureNames();
            for (String name : names) {
                System.out.println("Signature name: " + name);
                System.out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
                PdfPKCS7 pk = af.verifySignature(name, provider.getName());
                System.out.println("SignatureDate: " + pk.getSignDate());
                System.out.println("Certificate: " + pk.getSigningCertificate());
                System.out.println("Document modified: " + !pk.verify());
                af.removeField(name);
            }
            PdfStamper stamper = new PdfStamper(reader, outputStream, '\0');
            stamper.close();
        }
    }
}
