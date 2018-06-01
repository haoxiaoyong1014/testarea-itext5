// $Id$
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
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class FlattenForm
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/36268232/nullpointerexception-in-itextpdf-during-form-flattening-with-acrofields">
     * NullPointerException in itextpdf during Form flattening with Acrofields
     * </a>
     * <p>
     * The issue cannot be reproduced with the TwoPageForm.pdf resource which has two pages with
     * one field on each of them. Also tried to test {@link PdfStamper#setFormFlattening(boolean)}
     * instead of {@link PdfStamper#setFreeTextFlattening(boolean)}, no problem either.
     * </p>
     */
    @Test
    public void testFlattenAfterPageRemoval() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("TwoPageForm.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "TwoPagesForm-select1flat.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            pdfReader.selectPages(Arrays.asList(1));
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            pdfStamper.setFreeTextFlattening(true);
//            pdfStamper.setFormFlattening(true);
            pdfStamper.close();
        }
    }
}
