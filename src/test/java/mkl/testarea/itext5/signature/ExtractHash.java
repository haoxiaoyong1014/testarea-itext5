/**
 * 
 */
package mkl.testarea.itext5.signature;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.HexEncoder;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.io.RASInputStream;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * <a href="http://stackoverflow.com/questions/29939831/obtaining-the-hash-digest-from-a-pcks7-signed-pdf-file-with-itext">
 * Obtaining the hash/digest from a PCKS7 signed PDF file with iText
 * </a>
 * <p>
 * {@link #extractHashes(PdfReader, String)} implements a sample routine extracting the message digest.
 * </p>
 * 
 * @author mkl
 */
public class ExtractHash
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    @BeforeClass
    public static void setUp() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        //Security.addProvider(bcp);
        Security.insertProviderAt(bcp, 1);
    }

    @Test
    public void testFirstPage11P0022AD_20150202164018_307494() throws IOException, GeneralSecurityException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        try (   InputStream resource = getClass().getResourceAsStream("FirstPage11P0022AD_20150202164018_307494.pdf")   )
        {
            System.out.println("FirstPage11P0022AD_20150202164018_307494.pdf");
            PdfReader reader = new PdfReader(resource);
            extractHashes(reader, "FirstPage11P0022AD_20150202164018_307494-%s%s.hash");
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/44196316/pdf-signing-generated-pdf-document-certification-is-invalid-using-external-si">
     * PDF Signing, generated PDF Document certification is invalid? (using external signing, web-eid, HSM)
     * </a>
     * <br/>
     * <a href="https://www.mediafire.com/?fqvnf9mg50pfzjh">
     * signed_file.pdf
     * </a>
     * <p>
     * The calculated hash is not the same as the hash in the signature.
     * </p>
     */
    @Test
    public void testPareshSignedFile() throws IOException, GeneralSecurityException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        try (   InputStream resource = getClass().getResourceAsStream("signed_file.pdf")   )
        {
            System.out.println("signed_file.pdf");
            PdfReader reader = new PdfReader(resource);
            extractHashes(reader, "signed_file-%s%s.hash");
        }
    }

    void extractHashes(PdfReader reader, String format) throws NoSuchFieldException, SecurityException, GeneralSecurityException, IllegalArgumentException, IllegalAccessException, IOException
    {
        AcroFields acroFields = reader.getAcroFields();
        List<String> names = acroFields.getSignatureNames();

        for (String name: names)
        {
            System.out.printf("  %s\n", name);
            PdfPKCS7 pdfPkcs7 = acroFields.verifySignature(name);
            System.out.printf("    Digest algorithm: %s\n", pdfPkcs7.getHashAlgorithm());
            pdfPkcs7.verify();

            Field digestAttrField = PdfPKCS7.class.getDeclaredField("digestAttr");
            digestAttrField.setAccessible(true);
            byte[] digestAttr = (byte[]) digestAttrField.get(pdfPkcs7);

            if (digestAttr != null)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new HexEncoder().encode(digestAttr, 0, digestAttr.length, baos);
                byte[] digestAttrHex = baos.toByteArray();
                System.out.printf("    Hash: %s\n", new String(digestAttrHex));

                Files.write(new File(RESULT_FOLDER, String.format(format, name, "-attr")).toPath(), digestAttr);
                Files.write(new File(RESULT_FOLDER, String.format(format, name, "-attr") + ".hex").toPath(), digestAttrHex);
            }
            else
            {
                System.out.printf("    Hash: N/A\n");
            }

            PdfDictionary v = acroFields.getSignatureDictionary(name);
            MessageDigest md = MessageDigest.getInstance(pdfPkcs7.getHashAlgorithm());
            PdfArray b = v.getAsArray(PdfName.BYTERANGE);
            RandomAccessFileOrArray rf = reader.getSafeFile();
            try (   InputStream rg = new RASInputStream(new RandomAccessSourceFactory().createRanged(rf.createSourceView(), b.asLongArray()))   )
            {
                byte buf[] = new byte[8192];
                int rd;
                while ((rd = rg.read(buf, 0, buf.length)) > 0) {
                    md.update(buf, 0, rd);
                }
            }
            byte[] digestValue = md.digest();
            if (digestValue != null)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new HexEncoder().encode(digestValue, 0, digestValue.length, baos);
                byte[] digestValueHex = baos.toByteArray();
                System.out.printf("    Hash of doc: %s\n", new String(digestValueHex));

                Files.write(new File(RESULT_FOLDER, String.format(format, name, "-doc")).toPath(), digestValue);
                Files.write(new File(RESULT_FOLDER, String.format(format, name, "-doc") + ".hex").toPath(), digestValueHex);

                byte[] digestedDigestValue = MessageDigest.getInstance(pdfPkcs7.getHashAlgorithm()).digest(digestValue);
                baos.reset();
                new HexEncoder().encode(digestedDigestValue, 0, digestedDigestValue.length, baos);
                byte[] digestedDigestValueHex = baos.toByteArray();
                System.out.printf("    Hash of doc, hashed: %s\n", new String(digestedDigestValueHex));

                Files.write(new File(RESULT_FOLDER, String.format(format, name, "-doc-hash")).toPath(), digestedDigestValue);
                Files.write(new File(RESULT_FOLDER, String.format(format, name, "-doc-hash") + ".hex").toPath(), digestedDigestValueHex);
            }
            else
            {
                System.out.printf("    Hash of doc: N/A\n");
            }
        }
    }
}
