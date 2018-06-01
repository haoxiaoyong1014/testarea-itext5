package mkl.testarea.itext5.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * This test covers situations with hybrid AcroForm/XFA forms and
 * field name duplication..
 * 
 * @author mkl
 */
public class DuplicateHybridFormNames
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/28967737/itextsharp-setfield-for-fields-with-same-name-on-different-pages">
     * iTextSharp SetField for fields with same name on different pages
     * </a>
     * <p>
     * Reproducing the issue with iText
     * </p>
     */
    @Test
    public void testFillInForm() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("ING_bewindvoering_regelen_tcm162-49609.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "hybrid-form-fillin.pdf"))   )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);
            AcroFields fields = stamper.getAcroFields();
            fields.setField("topmostSubform[0].CheckBox2A[0]", "1");
//            fields.setField("topmostSubform[0].Page2[0].CheckBox2A[0]", "1");
            stamper.close();
        }
    }

}
