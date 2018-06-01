package mkl.testarea.itext5.copy;

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
 * @author mkl
 */
public class SubDocument
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "copy");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/41971359/itext-pdf-orientation">
     * iText pdf orientation
     * </a>
     * <p>
     * Extracting partial documents is most easily done using {@link PdfReader#selectPages(String)}.
     * </p>
     */
    @Test
    public void testExtractSubDocument() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("cjk-with-fonts.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "cjk-with-fonts-2-4.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            pdfReader.selectPages("2-4");
            new PdfStamper(pdfReader, result).close();
        }
    }

}
