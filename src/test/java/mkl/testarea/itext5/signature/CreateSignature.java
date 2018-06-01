package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

/**
 * Miscellaneous signing tests.
 *
 * @author mkl
 */
public class CreateSignature
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

    /**
     * <a href="http://stackoverflow.com/questions/30449348/signing-pdf-memory-consumption">
     * Signing PDF - memory consumption
     * </a>
     * <br>
     * <a href="http://50mpdf.tk/50m.pdf">50m.pdf</a>
     * <p>
     * {@link #sign50MNaive()} tests the naive approach,
     * {@link #sign50MBruno()} tests Bruno's original approach,
     * {@link #sign50MBrunoPartial()} tests Bruno's approach with partial reading,
     * {@link #sign50MBrunoAppend()} tests Bruno's approach with append mode, and
     * {@link #sign50MBrunoPartialAppend()} tests Bruno's approach with partial reading and append mode.
     * </p>
     */
    // runs with -Xmx240m, fails with -Xmx230m
    @Test
    public void sign50MNaive() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/50m-signedNaive.pdf";//50m.pdf
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "53m-signedNaive.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0',RESULT_FOLDER,true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(56, 648, 124, 780), 1, "sig2");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    //runs with -Xmx81m, fails with -Xmx80m
    @Test
    public void sign50MBruno() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/50m.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "50m-signedBruno.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, false);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    //runs with -Xmx81m, fails with -Xmx80m
    @Test
    public void sign50MBrunoPartial() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/50m.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "50m-signedBrunoPartial.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, false);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    //runs with -Xmx7m, fails with -Xmx6m
    @Test
    public void sign50MBrunoAppend() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/50m.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "50m-signedBrunoAppend.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    //runs with -Xmx7m, fails with -Xmx6m
    @Test
    public void sign50MBrunoPartialAppend() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/50m.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "50m-signedBrunoPartialAppend.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    /**
     * <a href="http://stackoverflow.com/questions/30526254/sign-concatenated-pdf-in-append-mode-with-certified-no-changes-allowed">
     * Sign concatenated PDF in append mode with CERTIFIED_NO_CHANGES_ALLOWED
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/s/lea6r9fup6th44c/test_pdf.zip?dl=0">test_pdf.zip</a>
     *
     * {@link #signCertifyG()} certifies g.pdf, OK
     * {@link #sign2g()} merely signs 2g.pdf, OK
     * {@link #signCertify2gNoAppend()} certifies 2g.pdf but not in append mode, OK
     * {@link #tidySignCertify2g()} first tidies, then certifies 2g.pdf, OK
     * {@link #signCertify2g()} certifies 2g.pdf, Adobe says invalid
     * {@link #signCertify2gFix()} certifies 2g-fix.pdf, OK!
     *
     * 2g-fix.pdf is a patched version of 2g.pdf with a valid /Size trailer entry
     * and a valid, single-sectioned cross reference table
     */
    @Test
    public void signCertifyG() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/g.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "g-certified.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    @Test
    public void sign2g() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2g.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2g-signed.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        //appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    @Test
    public void signCertify2g() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2g.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2g-certified.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    @Test
    public void signCertify2gNoAppend() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2g.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2g-certified-noAppend.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    @Test
    public void signCertify2gFix() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2g-fix.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2g-fix-certified.pdf"));
        PdfStamper stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    @Test
    public void tidySignCertify2g() throws IOException, DocumentException, GeneralSecurityException
    {
        String filepath = "src/test/resources/mkl/testarea/itext5/signature/2g.pdf";
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        // Tidying
        PdfReader reader = new PdfReader(filepath, null, true);
        FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "2g-tidied.pdf"));
        PdfStamper stamper = new  PdfStamper(reader, os);
        stamper.close();

        // Creating the reader and the stamper
        reader = new PdfReader(new File(RESULT_FOLDER, "2g-tidied.pdf").toString(), null, true);
        os = new FileOutputStream(new File(RESULT_FOLDER, "2g-tidied-certified.pdf"));
        stamper =
            PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setReason("reason");
        appearance.setLocation("location");
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain,
            null, null, null, 0, subfilter);
    }

    /**
     * <a href="http://stackoverflow.com/questions/36589698/error-while-digitally-signing-a-pdf">
     * Error while digitally signing a PDF
     * </a>
     * <p>
     * Tried to reproduce the OP's issue with my own test PDF and key. But it worked alright.
     * </p>
     */
    @Test
    public void signLikeJackSparrow() throws GeneralSecurityException, IOException, DocumentException
    {
        final String SRC      = "src/test/resources/mkl/testarea/itext5/extract/test.pdf";
        final String DEST     = new File(RESULT_FOLDER, "test-JackSparrow-%s.pdf").getPath();

        C2_01_SignHelloWorld_sign(SRC, String.format(DEST, 1), chain, pk, DigestAlgorithms.SHA256, "BC", CryptoStandard.CMS, "Signed for Testing", "Universe");
        C2_01_SignHelloWorld_sign(SRC, String.format(DEST, 2), chain, pk, DigestAlgorithms.SHA512, "BC", CryptoStandard.CMS, "Test 2", "Universe");
        C2_01_SignHelloWorld_sign(SRC, String.format(DEST, 3), chain, pk, DigestAlgorithms.SHA256, "BC", CryptoStandard.CADES, "Test 3", "Universe");
    }

    public void C2_01_SignHelloWorld_sign(String src, String dest, Certificate[] chain, PrivateKey pk, String digestAlgorithm, String provider, CryptoStandard subfilter, String reason, String location)
            throws GeneralSecurityException, IOException, DocumentException {
        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        // Creating the signature
        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new PrivateKeySignature(pk, digestAlgorithm, provider);
        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, subfilter);
    }

    /**
     * <a href="https://stackoverflow.com/questions/45062602/itext-pdfappearence-issue">
     * Text - PDFAppearence issue
     * </a>
     * <p>
     * This test shows how one can create a custom signature layer 2.
     * As the OP of the question at hand mainly wants to generate a
     * pure DESCRIPTION appearance that uses the whole area, we here
     * essentially copy the PdfSignatureAppearance.getAppearance code
     * for generating layer 2 in pure DESCRIPTION mode and apply it
     * to a plain pre-fetched layer 2.
     * </p>
     */
    @Test
    public void signWithCustomLayer2() throws IOException, DocumentException, GeneralSecurityException
    {
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf")  )
        {
            PdfReader reader = new PdfReader(resource);
            FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "test-customLayer2.pdf"));
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");

            // This essentially is the PdfSignatureAppearance.getAppearance code
            // for generating layer 2 in pure DESCRIPTION mode applied to a plain
            // pre-fetched layer 2.
            // vvvvv
            PdfTemplate layer2 = appearance.getLayer(2);
            String text = "We're using iText to put a text inside a signature placeholder in a PDF. "
                    + "We use a code snippet similar to this to define the Signature Appearence.\n"
                    + "Everything works fine, but the signature text does not fill all the signature "
                    + "placeholder area as expected by us, but the area filled seems to have an height "
                    + "that is approximately the 70% of the available space.\n"
                    + "As a result, sometimes especially if the length of the signature text is quite "
                    + "big, the signature text does not fit in the placeholder and the text is striped "
                    + "away.";
            Font font = new Font();
            float size = font.getSize();
            final float MARGIN = 2;
            Rectangle dataRect = new Rectangle(
                    MARGIN,
                    MARGIN,
                    appearance.getRect().getWidth() - MARGIN,
                    appearance.getRect().getHeight() - MARGIN);
            if (size <= 0) {
                Rectangle sr = new Rectangle(dataRect.getWidth(), dataRect.getHeight());
                size = ColumnText.fitText(font, text, sr, 12, appearance.getRunDirection());
            }
            ColumnText ct = new ColumnText(layer2);
            ct.setRunDirection(appearance.getRunDirection());
            ct.setSimpleColumn(new Phrase(text, font), dataRect.getLeft(), dataRect.getBottom(), dataRect.getRight(), dataRect.getTop(), size, Element.ALIGN_LEFT);
            ct.go();
            // ^^^^^

            ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, subfilter);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49861696/delete-padding-of-rectangle-in-itext-pdf-signature">
     * Delete padding of Rectangle in iText PDF signature
     * </a>
     * <p>
     * The overlapping-rectangles issue can be resolved by choosing
     * rectangle coordinates in a non-overlapping manner, cf.
     * {@link #tuneAppearanceLikeJoseJavierHernándezBenítez(PdfSignatureAppearance, int, String)}.
     * The other issue, the free space at the top, can be resolved
     * as shown in the test {@link #signWithCustomLayer2()} above.
     * </p>
     */
    @Test
    public void signInSmallRectangles() throws IOException, DocumentException, GeneralSecurityException {
        String digestAlgorithm = "SHA512";
        CryptoStandard subfilter = CryptoStandard.CMS;

        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "smallRectangles1.pdf"))) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            tuneAppearanceLikeJoseJavierHernándezBenítez(appearance, 1, "Sig1");

            ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, subfilter);
        }


        try (   InputStream is = new FileInputStream(new File(RESULT_FOLDER, "smallRectangles1.pdf"));
                OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "smallRectangles2.pdf"))) {
            PdfReader reader = new PdfReader(is);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', RESULT_FOLDER, true);

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("reason");
            appearance.setLocation("location");
            tuneAppearanceLikeJoseJavierHernándezBenítez(appearance, 2, "Sig2");

            ExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, "BC");
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, subfilter);
        }
    }

    // @see #signInSmallRectangles()
    void tuneAppearanceLikeJoseJavierHernándezBenítez(PdfSignatureAppearance signatureAppearance, int next, String contact) {
        signatureAppearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.DESCRIPTION);
        Rectangle rectangle = new Rectangle(
                        36,
                        760 - 20 * (next - 1) , // this is one possible correction of the original: 748 - 20 * (next - 1) ,
                        144,
                        780 - 20 * (next - 1)
            );
        rectangle.normalize();
        signatureAppearance.setVisibleSignature(
                rectangle,
                1, contact);
    }
}
