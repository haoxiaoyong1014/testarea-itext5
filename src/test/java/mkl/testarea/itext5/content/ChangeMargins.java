package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class ChangeMargins
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/38057241/itextpdf-different-margin-on-specific-page">
     * itextpdf different margin on specific page
     * </a>
     * <p>
     * This test shows how to set different margins to separate pages.
     * </p> 
     */
    @Test
    public void testChangingMargins() throws IOException, DocumentException
    {
        StringBuilder builder = new StringBuilder("test");
        for (int i = 0; i < 100; i++)
            builder.append(" test");
        String test = builder.toString();
        
        try (   OutputStream pdfStream = new FileOutputStream(new File(RESULT_FOLDER, "ChangingMargins.pdf")))
        {
            Document pdfDocument = new Document(PageSize.A4.rotate(), 0, 0, 0, 0);
            PdfWriter.getInstance(pdfDocument, pdfStream);
            pdfDocument.open();

            for (int m = 0; m < pdfDocument.getPageSize().getWidth() / 2 && m < pdfDocument.getPageSize().getHeight() / 2; m += 100)
            {
                // pdfDocument.setMargins(m, m, 100, 100);
                pdfDocument.setMargins(m, m, m, m);
                pdfDocument.newPage();
                pdfDocument.add(new Paragraph(test));
            }

            pdfDocument.close();
        }
    }
}
