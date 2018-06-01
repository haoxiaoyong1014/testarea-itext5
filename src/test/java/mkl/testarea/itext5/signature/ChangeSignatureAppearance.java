// $Id$
package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.security.cert.X509Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.Item;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * @author mkl
 */
public class ChangeSignatureAppearance
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "signature");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
    }

    /**
     * <a href="http://stackoverflow.com/questions/37027579/how-to-associate-a-previous-signature-in-a-new-signature-field">
     * How to associate a previous signature in a new signature field
     * </a>
     * <br/>
     * <span>BLANK-signed.pdf, <em>a blank file from elsewhere with an invisible signature.</em></span>
     * <p>
     * Quite surprisingly it turns out that changing the signature appearance is possible without
     * breaking the signature, merely a warning appears which can be hidden by simply signing again.
     * </p>
     */
    @Test
    public void testChangeAppearances() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("BLANK-signed.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "BLANK-signed-app.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, '\0', true);

            AcroFields acroFields = pdfStamper.getAcroFields();
            for (String signatureName : acroFields.getSignatureNames())
            {
                Item field = acroFields.getFieldItem(signatureName);
                field.writeToAll(PdfName.RECT, new PdfArray(new int[]{100,100,200,200}), Item.WRITE_WIDGET);
                field.markUsed(acroFields, Item.WRITE_WIDGET);
                
                PdfAppearance appearance = PdfAppearance.createAppearance(pdfStamper.getWriter(), 100, 100);
                appearance.setColorStroke(BaseColor.RED);
                appearance.moveTo(0, 0);
                appearance.lineTo(99, 99);
                appearance.moveTo(0, 99);
                appearance.lineTo(99, 0);
                appearance.stroke();
                
                PdfDictionary appDict = new PdfDictionary();
                appDict.put(PdfName.N, appearance.getIndirectReference());
                field.writeToAll(PdfName.AP, appDict, Item.WRITE_WIDGET);
            }

            pdfStamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/37027579/how-to-associate-a-previous-signature-in-a-new-signature-field">
     * How to associate a previous signature in a new signature field
     * </a>
     * <br/>
     * <span>BLANK-signed.pdf, <em>a blank file from elsewhere with an invisible signature.</em></span>
     * <p>
     * Similarly to {@link #testChangeAppearances()}, this test adds a signature appearance
     * not breaking signature validity, but this time it contains the signer certificate
     * subject common name.
     * </p>
     */
    @Test
    public void testChangeAppearancesWithName() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("BLANK-signed.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "BLANK-signed-app-name.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, '\0', true);

            AcroFields acroFields = pdfStamper.getAcroFields();
            for (String signatureName : acroFields.getSignatureNames())
            {
                PdfPKCS7 pkcs7 = acroFields.verifySignature(signatureName);
                X509Certificate signerCert = (X509Certificate) pkcs7.getSigningCertificate();
                String signerName = CertificateInfo.getSubjectFields(signerCert).getField("CN");

                Item field = acroFields.getFieldItem(signatureName);
                field.writeToAll(PdfName.RECT, new PdfArray(new int[]{100,100,200,200}), Item.WRITE_WIDGET);
                field.markUsed(acroFields, Item.WRITE_WIDGET);
                
                PdfAppearance appearance = PdfAppearance.createAppearance(pdfStamper.getWriter(), 100, 100);
                ColumnText columnText = new ColumnText(appearance);
                Chunk chunk = new Chunk();
                chunk.setSkew(0, 12);
                chunk.append("Signed by:");
                columnText.addElement(new Paragraph(chunk));
                chunk = new Chunk();
                chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 1, BaseColor.BLACK);
                chunk.append(signerName);
                columnText.addElement(new Paragraph(chunk));
                columnText.setSimpleColumn(0, 0, 100, 100);
                columnText.go();

                PdfDictionary appDict = new PdfDictionary();
                appDict.put(PdfName.N, appearance.getIndirectReference());
                field.writeToAll(PdfName.AP, appDict, Item.WRITE_WIDGET);
            }

            pdfStamper.close();
        }
    }
}
