package mkl.testarea.itext5.form;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;

/**
 * @author mkl
 */
public class ShowStates
{
    /**
     * <a href="http://stackoverflow.com/questions/39450688/itext-pdf-checkboxon-off-not-appearing-for-some-pdf">
     * IText Pdf - Checkbox(On/Off) not appearing for some pdf
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/pdfexample_1">
     * PDF example.pdf
     * </a>
     * <p>
     * The observations of the OP cannot be reproduced.
     * </p>
     */
    @Test
    public void testShowPdfExampleStates() throws IOException
    {
        String resourceName = "PDF example.pdf";
        try (   InputStream resource = getClass().getResourceAsStream(resourceName)    )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields form = reader.getAcroFields();
            String[] values = form.getAppearanceStates("claimsType");
            System.out.printf("\n%s\nThe appearance states of claimsType are %s.\n", resourceName, Arrays.asList(values));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/39450688/itext-pdf-checkboxon-off-not-appearing-for-some-pdf">
     * IText Pdf - Checkbox(On/Off) not appearing for some pdf
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/pdftest2">
     * PDF test 2.pdf
     * </a>
     * <p>
     * The observations of the OP cannot be reproduced.
     * </p>
     */
    @Test
    public void testShowPdfTest2States() throws IOException
    {
        String resourceName = "PDF test 2.pdf";
        try (   InputStream resource = getClass().getResourceAsStream(resourceName)    )
        {
            PdfReader reader = new PdfReader(resource);
            AcroFields form = reader.getAcroFields();
            String[] values = form.getAppearanceStates("claimsType");
            System.out.printf("\n%s\nThe appearance states of claimsType are %s.\n", resourceName, Arrays.asList(values));
        }
    }
}
