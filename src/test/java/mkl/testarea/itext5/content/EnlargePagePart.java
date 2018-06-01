// $Id$
package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class EnlargePagePart
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35374110/how-do-i-use-itext-to-have-a-landscaped-pdf-on-half-of-a-a4-back-to-portrait-and">
     * How do i use iText to have a landscaped PDF on half of a A4 back to portrait and full size on A4
     * </a>
     * <p>
     * This sample shows how to rotate and enlarge the upper half of an A4 page to fit into a new A4 page.
     * </p>
     */
    @Test
    public void testRotateAndZoomUpperHalfPage() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test-upperHalf.pdf"))   )
        {
            PdfReader reader = new PdfReader(resource);
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, result);
            document.open();

            double sqrt2 = Math.sqrt(2);
            Rectangle pageSize = reader.getPageSize(1);
            PdfImportedPage importedPage = writer.getImportedPage(reader, 1);
            writer.getDirectContent().addTemplate(importedPage, 0, sqrt2, -sqrt2, 0, pageSize.getTop() * sqrt2, -pageSize.getLeft() * sqrt2);
            
            document.close();
        }
    }
}
