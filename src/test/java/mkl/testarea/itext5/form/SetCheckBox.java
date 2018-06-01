package mkl.testarea.itext5.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class SetCheckBox
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/42819618/itext-fill-checkbox-with-value">
     * itext fill checkbox with value
     * </a>
     * <br/>
     * <a href="https://www.poreskaupravars.org/Documents/Doc/PD3100.pdf">
     * PD3100.pdf
     * </a>
     * <p>
     * This test uses a valid name and so at least creates
     * a PDF displayed with a somehow selected field. 
     * </p>
     * @see #testPd3100SetVrPr4ToNoAppend()
     */
    @Test
    public void testPd3100SetVrPr4ToNo() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("PD3100.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "PD3100-SetVrPr4ToNo.pdf"))  )
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, (char)0, false);
            AcroFields acroFields = pdfStamper.getAcroFields();
            System.out.println("Available values for VrPr4: " + Arrays.asList(acroFields.getAppearanceStates("VrPr4")));
            acroFields.setField("VrPr4", "No");
            pdfStamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/42819618/itext-fill-checkbox-with-value">
     * itext fill checkbox with value
     * </a>
     * <br/>
     * <a href="https://www.poreskaupravars.org/Documents/Doc/PD3100.pdf">
     * PD3100.pdf
     * </a>
     * <p>
     * This test uses a valid name in append mode and so creates
     * a PDF displayed in Adobe Reader with a selected field as
     * desired. 
     * </p>
     * @see #testPd3100SetVrPr4ToNoAppend()
     */
    @Test
    public void testPd3100SetVrPr4ToNoAppend() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("PD3100.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "PD3100-SetVrPr4ToNoAppend.pdf"))  )
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result, (char)0, true);
            AcroFields acroFields = pdfStamper.getAcroFields();
            System.out.println("Available values for VrPr4: " + Arrays.asList(acroFields.getAppearanceStates("VrPr4")));
            acroFields.setField("VrPr4", "No");
            pdfStamper.close();
        }
    }
}
