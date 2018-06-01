package mkl.testarea.itext5.signature;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.FieldPosition;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class CreateEmptyField
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
     * <a href="http://stackoverflow.com/questions/32332490/new-signature-field-rotated-90-degrees">
     * New Signature Field Rotated 90 Degrees
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=0B3pKBz-WDrXnM3hYeDFtXzhldnM">
     * DA3161-Template.pdf
     * </a>
     * <p>
     * The page in your document is rotated using the Rotate page dictionary entry.
     * When creating a field to be filled-in by others later, you have to add a hint
     * to the field indicating a counter-rotation if you want the field content to
     * effectively appear upright.
     * </p>
     * <p>
     * You do this by setting the MKRotation attribute of the field. This creates a
     * rotation entry R with value 90 in the appearance characteristics dictionary
     * MK of the field.
     * </p>
     */
    @Test
    public void testDA3161_Template() throws IOException, GeneralSecurityException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("DA3161-Template.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "DA3161-Template_Field.pdf"))    )
        {
            System.out.println("DA3161-Template.pdf");
            PdfReader reader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(reader, result);
            AcroFields fields = pdfStamper.getAcroFields();
            int itemno = 3;

          //Get location to the field where we will place the signature field
            FieldPosition NewPosition = fields.getFieldPositions("DESC_0_" + (itemno + 1)).get(0);
            float l1 = NewPosition.position.getLeft();
            float r1 = NewPosition.position.getRight();
            float t1 = NewPosition.position.getTop();
            float b1 = NewPosition.position.getBottom();

            PdfFormField field = PdfFormField.createSignature(pdfStamper.getWriter());
            field.setFieldName("G4_SignatureX");

            // Set the widget properties
            field.setWidget(new Rectangle(r1, t1, l1, b1), PdfAnnotation.HIGHLIGHT_NONE);
            field.setFlags(PdfAnnotation.FLAGS_PRINT);

            // !!!!!!!!!!!!!!!!!!!
            field.setMKRotation(90);

            // Add the annotation
            pdfStamper.addAnnotation(field, 1);

            pdfStamper.close();
        }
    }
}
