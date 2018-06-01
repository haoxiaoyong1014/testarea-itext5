package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

/**
 * @author mkl
 */
public class ComplexSignatureFields
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    public static final String KEYSTORE = "keystores/demo-rsa2048.ks"; 
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

    @BeforeClass
    public static void setUp() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        //Security.addProvider(bcp);
        Security.insertProviderAt(bcp, 1);

        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    @Test
    public void sign2274_2007_H_PROVISIONAL_multifield() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2274_2007_H_PROVISIONAL - multifield.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2274_2007_H_PROVISIONAL - multifield-signed.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        //appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature("IntSig1");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    /**
     * <a href="http://stackoverflow.com/questions/32818522/itextsharp-setvisiblesignature-not-working-as-expected">
     * ITextSharp SetVisibleSignature not working as expected
     * </a>
     * <p>
     * The issue observed by the OP (user2699460) occurs since iText(Sharp) 5.5.7
     * both of iText and iTextSharp.
     * </p>
     * <p>
     * The file signed in this sample, test-2-user2699460-signed.pdf, has been created
     * as intermediary result using a simplified version of the OP's c# code. 
     * </p>
     */
    @Test
    public void signTest_2_user2699460() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/test-2-user2699460.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-2-user2699460-signed.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        //appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature("Bunker");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }
}
