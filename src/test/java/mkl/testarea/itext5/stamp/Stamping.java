package mkl.testarea.itext5.stamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * This test is for testing basic stamping functionality
 * 
 * @author mkl
 */
public class Stamping
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "stamp");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href = "http://stackoverflow.com/questions/28898636/itextpdf-stop-transform-pdf-correctly">
     * Itextpdf stop transform pdf correctly
     * </a>
     * <p>
     * <a href="https://drive.google.com/file/d/0B3-DPMN-iMOmNjItRVJ4MHRZX3M/view?usp=sharing">
     * template.pdf
     * </a>
     * <p>
     * ... as it turned out, the problem was not an iText issue at all; in the OP's web application
     * the template.pdf had already been mangled by maven resource filtering.
     * </p>
     */
    @Test
    public void testStampTemplate() throws DocumentException, IOException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("template.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "test.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            stamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41183349/pdf-file-size-is-largely-increased-when-copied-using-itext-java-library">
     * pdf file size is largely increased when copied using itext java library
     * </a>
     * <br/>
     * <a href="https://www.pdfill.com/download/AcroJS.pdf">
     * AcroJS.pdf
     * </a>
     * <p>
     * Indeed, using the OP's code the size explodes.
     * </p>
     */
    @Test
    public void testStampAcroJS() throws DocumentException, IOException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("AcroJS.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "AcroJS-stamped.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            stamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41183349/pdf-file-size-is-largely-increased-when-copied-using-itext-java-library">
     * pdf file size is largely increased when copied using itext java library
     * </a>
     * <br/>
     * <a href="https://www.pdfill.com/download/AcroJS.pdf">
     * AcroJS.pdf
     * </a>
     * <p>
     * Using append mode, things are pretty much like in the original file.
     * </p>
     */
    @Test
    public void testStampAcroJSAppended() throws DocumentException, IOException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("AcroJS.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "AcroJS-stamped-appended.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream, '\0', true);

            stamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41183349/pdf-file-size-is-largely-increased-when-copied-using-itext-java-library">
     * pdf file size is largely increased when copied using itext java library
     * </a>
     * <br/>
     * <a href="https://www.pdfill.com/download/AcroJS.pdf">
     * AcroJS.pdf
     * </a>
     * <p>
     * Using full compression is much more compressed than the original.
     * </p>
     */
    @Test
    public void testStampAcroJSCompressed() throws DocumentException, IOException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("AcroJS.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "AcroJS-stamped-compressed.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);
            stamper.setFullCompression();

            stamper.close();
        }
    }
    
}
