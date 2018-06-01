// $Id$
package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class DoubleSpace
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35699167/double-space-not-being-preserved-in-pdf">
     * Double space not being preserved in PDF
     * </a>
     * <p>
     * Indeed, the double space collapses into a single one when copying&pasting from the
     * generated PDF displayed in Adobe Reader. On the other hand the gap for the double
     * space is twice as wide as for the single space. So this essentially is a quirk of
     * copy&paste of Adobe Reader (and some other PDF viewers, too).
     * </p>
     */
    @Test
    public void testDoubleSpace() throws DocumentException, IOException
    {
        try (   OutputStream pdfStream = new FileOutputStream(new File(RESULT_FOLDER, "DoubleSpace.pdf")))
        {
            PdfPTable table = new PdfPTable(1);
            table.getDefaultCell().setBorderWidth(0.5f);
            table.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

            table.addCell(new Phrase("SINGLE SPACED", new Font(BaseFont.createFont(), 36)));
            table.addCell(new Phrase("DOUBLE  SPACED", new Font(BaseFont.createFont(), 36)));
            table.addCell(new Phrase("TRIPLE   SPACED", new Font(BaseFont.createFont(), 36)));

            Document pdfDocument = new Document(PageSize.A4.rotate(), 0, 0, 0, 0);
            PdfWriter.getInstance(pdfDocument, pdfStream);
            pdfDocument.open();
            pdfDocument.add(table);
            pdfDocument.close();
        }
    }

}
