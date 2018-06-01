package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.ArrayList;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.CrlClientOnline;
import com.itextpdf.text.pdf.security.LtvVerification;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * @author mkl
 */
public class AddLtvCrls
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

    /**
     * <a href="http://stackoverflow.com/questions/35134568/itext-ltv-enabled-how-to-add-more-crls">
     * iText LTV enabled - how to add more CRLs?
     * </a>
     * <br/>
     * <a href="http://www.quarim.cz/custom/itextLTV/source_signed.pdf">
     * source_signed.pdf
     * </a>
     * <p>
     * The original addLtv method of the OP.
     * </p>
     */
    @Test
    public void testAddLtvJanPokorny() throws IOException, DocumentException, GeneralSecurityException
    {
        try (   InputStream resource = getClass().getResourceAsStream("source_signed.pdf")  )
        {
            addLtvJanPokorny(resource, new File(RESULT_FOLDER, "source_signed-Ltv.pdf").toString());
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/35134568/itext-ltv-enabled-how-to-add-more-crls">
     * iText LTV enabled - how to add more CRLs?
     * </a>
     * <br/>
     * <a href="http://www.quarim.cz/custom/itextLTV/source_signed.pdf">
     * source_signed.pdf
     * </a>
     * <p>
     * The fixed addLtv method.
     * </p>
     */
    @Test
    public void testAddLtvJanPokornyFixed() throws IOException, DocumentException, GeneralSecurityException
    {
        try (   InputStream resource = getClass().getResourceAsStream("source_signed.pdf")  )
        {
            addLtvFixed(resource, new File(RESULT_FOLDER, "source_signed-Ltv-fixed.pdf").toString());
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/35134568/itext-ltv-enabled-how-to-add-more-crls">
     * iText LTV enabled - how to add more CRLs?
     * </a>
     * <p>
     * The original addLtv method of the OP modified merely to allow the
     * source PDF to be given as {@link InputStream} instead of {@link String}.
     * </p>
     */
    public void addLtvJanPokorny(InputStream src, String dest) throws IOException, DocumentException, GeneralSecurityException
    {
        PdfReader r = new PdfReader(src);
        FileOutputStream fos = new FileOutputStream(dest);
        PdfStamper stp = new PdfStamper(r, fos, '\0', true);
        LtvVerification v = stp.getLtvVerification();
        AcroFields fields = stp.getAcroFields();

        ArrayList<String> names = fields.getSignatureNames();
        String sigName = names.get(names.size() - 1);
        System.out.println("found signature: " + sigName);
        PdfPKCS7 pkcs7 = fields.verifySignature(sigName);

        //add LTV
        OcspClient ocsp = new OcspClientBouncyCastle();
        CrlClient crlClient1 = new CrlClientOnline("http://www.postsignum.cz/crl/psrootqca2.crl");
        ArrayList<CrlClient> crllist = new ArrayList<CrlClient>();
        crllist.add(crlClient1);
        CrlClient crlClient2 = new CrlClientOnline("http://www.postsignum.cz/crl/pspublicca2.crl");
        crllist.add(crlClient2);
        System.out.println("crllist.size=" + crllist.size());

        if (pkcs7.isTsp())
        {
            for (CrlClient crlclient : crllist)
            {
                if (v.addVerification(sigName, new OcspClientBouncyCastle(), crlclient,
                        LtvVerification.CertificateOption.SIGNING_CERTIFICATE,
                        LtvVerification.Level.CRL,
                        LtvVerification.CertificateInclusion.NO))
                {
                    System.out.println("crl " + crlclient.toString() + " added to timestamp");
                }
            }
        }
        else
        {
            for (String name : names)
            {
                for (int i = 0; i < crllist.size(); i++) {
                    if (v.addVerification(name, ocsp, crllist.get(i),
                            LtvVerification.CertificateOption.WHOLE_CHAIN,
                            LtvVerification.Level.CRL,
                            LtvVerification.CertificateInclusion.NO))
                    {
                        System.out.println("crl " + crllist.get(i).toString() + " added to " + name);
                    }
                    if (i > 0)
                    {
                        System.out.println("found verification, merge");
                        v.merge();
                    }
                }
            }
        }
        stp.close();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35134568/itext-ltv-enabled-how-to-add-more-crls">
     * iText LTV enabled - how to add more CRLs?
     * </a>
     * <p>
     * The original addLtv method of the OP modified to allow the source PDF
     * to be given as {@link InputStream} instead of {@link String} and fixed
     * to properly use multiple CRLs.
     * </p>
     */
    public void addLtvFixed(InputStream src, String dest) throws IOException, DocumentException, GeneralSecurityException
    {
        PdfReader r = new PdfReader(src);
        FileOutputStream fos = new FileOutputStream(dest);
        PdfStamper stp = new PdfStamper(r, fos, '\0', true);
        LtvVerification v = stp.getLtvVerification();
        AcroFields fields = stp.getAcroFields();

        ArrayList<String> names = fields.getSignatureNames();
        String sigName = names.get(names.size() - 1);
        System.out.println("found signature: " + sigName);
        PdfPKCS7 pkcs7 = fields.verifySignature(sigName);

        //add LTV
        OcspClient ocsp = new OcspClientBouncyCastle();
        CrlClient crlClient = new CrlClientOnline("http://www.postsignum.cz/crl/psrootqca2.crl", "http://www.postsignum.cz/crl/pspublicca2.crl");

        if (pkcs7.isTsp())
        {
            if (v.addVerification(sigName, new OcspClientBouncyCastle(), crlClient,
                    LtvVerification.CertificateOption.SIGNING_CERTIFICATE,
                    LtvVerification.Level.CRL,
                    LtvVerification.CertificateInclusion.NO))
            {
                System.out.println("crl " + crlClient.toString() + " added to timestamp");
            }
        }
        else
        {
            for (String name : names)
            {
                if (v.addVerification(name, ocsp, crlClient,
                        LtvVerification.CertificateOption.WHOLE_CHAIN,
                        LtvVerification.Level.CRL,
                        LtvVerification.CertificateInclusion.NO))
                {
                    System.out.println("crl " + crlClient.toString() + " added to " + name);
                }
            }
        }
        stp.close();
    }
}
