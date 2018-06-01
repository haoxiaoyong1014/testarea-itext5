package mkl.testarea.itext5.xmlworker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * @author mkl
 */
public class CreatePdf
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "xmlworker");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/41743574/itextpdf-creates-unvalid-pdf-document">
     * Itextpdf creates unvalid pdf document
     * </a>
     * <p>
     * CasperSlynge.html
     * </p>
     * <p>
     * Works for me. Admittedly, I replaced the {@link ByteArrayInputStream} by a
     * resource {@link InputStream} and the {@link ByteArrayOutputStream} by a
     * {@link FileOutputStream}.
     * </p>
     * <p>
     * I also added a `Charset` but the test created a valid file without, too.
     * </p>
     */
    @Test
    public void testCreatePdfLikeCasperSlynge() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("CasperSlynge.html");
                FileOutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "CasperSlynge.pdf")))
        {
            // step 1
            Document document = new Document();
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, result);
            // step 3
            document.open();
            // step 4
            XMLWorkerHelper.getInstance().parseXHtml(writer, document, resource, Charset.forName("UTF8"));
            // step 5
            document.close();
        }
    }
}
