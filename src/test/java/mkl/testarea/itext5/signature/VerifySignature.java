// $Id$
package mkl.testarea.itext5.signature;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.EncryptionAlgorithms;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * @author mkl
 */
public class VerifySignature
{
    @BeforeClass
    public static void setUp() throws Exception
    {
        BouncyCastleProvider bcp = new BouncyCastleProvider();
        //Security.addProvider(bcp);
        Security.insertProviderAt(bcp, 1);
    }

    /**
     * <a href="http://stackoverflow.com/questions/35846427/pdfpkcs7-verify-return-false">
     * PdfPKCS7 .verify() return false
     * </a>
     * <br>
     * <a href="http://itext.2136553.n4.nabble.com/PdfPKCS7-verify-return-false-tt4661004.html">
     * PdfPKCS7 .verify() return false
     * </a>
     * <br>
     * <a href="http://itext.2136553.n4.nabble.com/file/n4661004/Test.pdf">
     * TestMGomez.pdf
     * </a>
     * <p>
     * Indeed, iText <code>PdfPKCS7.verify()</code> returns <code>false</code> while Adobe Reader
     * does not complain.
     * </p>
     * <p>
     * The reason for this is that the signature container in the OP's document has a zero-length
     * octet string in the encapsulated content optional eContent. iText assumes this octet string
     * to actually contain a hash (as if the signature were a adbe.pkcs7.sha1 subfilter type).
     * After resetting that value to <code>null</code>, iText also verifies positively.
     * </p>
     */
    @Test
    public void testVerifyTestMGomez() throws IOException, GeneralSecurityException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        System.out.println("\n\nTestMGomez.pdf\n==============");
    	
        try (   InputStream resource = getClass().getResourceAsStream("TestMGomez.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();

        Field rsaDataField = PdfPKCS7.class.getDeclaredField("RSAdata");
        rsaDataField.setAccessible(true);
        
        try (   InputStream resource = getClass().getResourceAsStream("TestMGomez.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));

               Object rsaDataFieldContent = rsaDataField.get(pk);
               if (rsaDataFieldContent != null && ((byte[])rsaDataFieldContent).length == 0)
               {
                   System.out.println("Found zero-length encapsulated content: ignoring");
                   rsaDataField.set(pk, null);
               }
               System.out.println("Document verifies: " + pk.verify());
            }
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/37726215/why-does-my-signature-revision-number-increment-by-2-in-itext-after-detached-s">
     * Why does my signature revision number increment by 2 (in itext) after detached signing?
     * </a>
     * <br/>
     * <a href="https://onedrive.live.com/redir?resid=2F03BFDA84B77A41!113&authkey=!ABPGZ7pxuxoE8A0&ithint=file%2Cpdf">
     * signedoutput.pdf
     * </a>
     * <p>
     * The issue cannot be reproduced. In particular the PDF contains only a single revision.
     * </p>
     */
    @Test
    public void testVerifySignedOutput() throws IOException, GeneralSecurityException
    {
        System.out.println("\n\nsignedoutput.pdf\n================");
    	
        try (   InputStream resource = getClass().getResourceAsStream("signedoutput.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();
    }

    /**
     * <a href="http://stackoverflow.com/questions/42824577/itext-can-not-verify-signed-pdf-docs-edited-by-nitro-pro-10-11">
     * itext can not verify signed pdf docs edited by nitro pro 10/11
     * </a>
     * <br/>
     * <a href="https://alimail.fadada.com/signed.pdf">
     * babylove_signed.pdf
     * </a>
     * <p>
     * Validation correctly shows verification success for a single
     * signature that does cover the whole document.
     * </p>
     */
    @Test
    public void testVerifyBabyloveSigned() throws IOException, GeneralSecurityException
    {
        System.out.println("\n\nbabylove_signed.pdf\n===================");
    	
        try (   InputStream resource = getClass().getResourceAsStream("babylove_signed.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();
    }
    
    /**
     * <a href="http://stackoverflow.com/questions/42824577/itext-can-not-verify-signed-pdf-docs-edited-by-nitro-pro-10-11">
     * itext can not verify signed pdf docs edited by nitro pro 10/11
     * </a>
     * <br/>
     * <a href="https://alimail.fadada.com/signed&modify_by_nitro.pdf">
     * babylove_signed&modify_by_nitro.pdf
     * </a>
     * <p>
     * Validation correctly shows verification success for a single
     * signature that does <b>not</b> cover the whole document.
     * </p>
     */
    @Test
    public void testVerifyBabyloveSignedAndModifyByNitro() throws IOException, GeneralSecurityException
    {
        System.out.println("\n\nbabylove_signed&modify_by_nitro.pdf\n===================");
    	
        try (   InputStream resource = getClass().getResourceAsStream("babylove_signed&modify_by_nitro.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();
    }
    
    /**
     * <a href="https://stackoverflow.com/questions/45027712/invalid-signature-when-signing-an-existing-sigrature-field-with-cosign-sapi">
     * Invalid signature when signing an existing sigrature field with CoSign SAPI
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/j6eme53lleaok13/test_signed.pdf?dl=0">
     * test_signed-1.pdf
     * </a>
     * <p>
     * Validation shows verification success while both Adobe and SD DSS fail.
     * Embedded certificates have issues (emailAddress RDN is typed PrintableString
     * which is wrong - specified is IA5String - and does not even make sense as
     * there is no '@' in PrintableString), but does this explain it?
     * </p>
     */
    @Test
    public void testVerifyTestSigned1() throws IOException, GeneralSecurityException
    {
        System.out.println("\n\ntest_signed-1.pdf\n===================");
        
        try (   InputStream resource = getClass().getResourceAsStream("test_signed-1.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();
    }
    
    /**
     * <a href="https://stackoverflow.com/questions/46346144/digital-signature-verification-with-itext-not-working">
     * Digital Signature Verification with itext not working
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=0B1XKjvoeoyPZWnk5bzc5T3VSQUk">
     * test_dsp.pdf
     * </a>
     * <p>
     * The issue is that the signature uses ECDSA and iText 5 does not (yet)
     * support ECDSA. "Support" here actually means that iText cannot find
     * the name ECDSA for the OID 1.2.840.10045.4.3.2 (SHA256withECDSA) to
     * build a proper algorithm name to use for verification.
     * </p>
     * <p>
     * Adding a mapping "1.2.840.10045.4.3.2" to "ECDSA" resolves the issue.
     * </p>
     * @see #testVerify20180115an_signed_original()
     */
    @Test
    public void testVerifyTestDsp() throws IOException, GeneralSecurityException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        Field algorithmNamesField = EncryptionAlgorithms.class.getDeclaredField("algorithmNames");
        algorithmNamesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<String, String> algorithmNames = (HashMap<String, String>) algorithmNamesField.get(null);
        algorithmNames.put("1.2.840.10045.4.3.2", "ECDSA");

        System.out.println("\n\ntest_dsp.pdf\n===================");
        
        try (   InputStream resource = getClass().getResourceAsStream("test_dsp.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();
    }

    /**
     * <a href="https://stackoverflow.com/questions/48285453/verifying-certificate-of-signed-and-secured-pdf-in-itext-pdf-java">
     * Verifying certificate of signed and secured PDF in iText PDF Java
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/1KAqHUh-Iij0I4WXJUCx-rMd8FQFq5tCe?usp=sharing">
     * pdf-sample-signed.pdf
     * </a>
     * <p>
     * The PDF is both signed and encrypted. Apparently iText "decrypts" the
     * values of the <b>Contents</b> key in signature dictionaries even though
     * this is an explicit exception. The parsing of this "decrypted" signature
     * container obviously fails.
     * </p>
     */
    @Test
    public void testVerifyPdfSampleSigned() throws IOException, GeneralSecurityException
    {
        System.out.println("\n\npdf-sample-signed.pdf\n===================");
        
        try (   InputStream resource = getClass().getResourceAsStream("pdf-sample-signed.pdf") )
        {
            PdfReader reader = new PdfReader(resource, "password".getBytes());
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();
    }

    /**
     * <a href="https://github.com/itext/itextpdf/pull/36">
     * Adding Support for OID 1.2.840.113549.1.1.10 #36
     * </a>
     * <br/>
     * <a href="https://github.com/itext/itextpdf/files/1641593/2018.01.15.an_signed_original.pdf">
     * 2018.01.15.an_signed_original.pdf
     * </a>
     * <p>
     * Support for RSASSA-PSS can also be injected using reflection as done here. 
     * </p>
     * @see #testVerifyTestDsp()
     */
    @Test
    public void testVerify20180115an_signed_original() throws IOException, GeneralSecurityException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        Field algorithmNamesField = EncryptionAlgorithms.class.getDeclaredField("algorithmNames");
        algorithmNamesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<String, String> algorithmNames = (HashMap<String, String>) algorithmNamesField.get(null);
        algorithmNames.put("1.2.840.113549.1.1.10", "RSAandMGF1");

        System.out.println("\n\n2018.01.15.an_signed_original.pdf\n===================");
        
        try (   InputStream resource = getClass().getResourceAsStream("2018.01.15.an_signed_original.pdf") )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields acroFields = reader.getAcroFields();

            List<String> names = acroFields.getSignatureNames();
            for (String name : names) {
               System.out.println("Signature name: " + name);
               System.out.println("Signature covers whole document: " + acroFields.signatureCoversWholeDocument(name));
               System.out.println("Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
               PdfPKCS7 pk = acroFields.verifySignature(name);
               System.out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
               System.out.println("Document verifies: " + pk.verify());
            }
        }

        System.out.println();
    }
}
