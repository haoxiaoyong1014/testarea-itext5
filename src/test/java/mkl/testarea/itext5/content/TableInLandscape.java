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
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class TableInLandscape {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/46140356/change-orientation-of-itext-pdfptable">
     * Change orientation of iText PdfPTable
     * </a>
     * <p>
     * In contrast to the claim of the OP, the table on the landscape page does make
     * use of the extra width.
     * </p>
     */
    @Test
    public void testSimpleTableInLandscape() throws IOException, DocumentException {
        try (   OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "tableInLandscape.pdf")) ) {
            Document pdfDocument = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(pdfDocument, result);

            pdfDocument.open();

            // PDF table
            PdfPTable pdfPTable = new PdfPTable(1);

            // Add column header cell
            PdfPCell dateCell = new PdfPCell(new Phrase("Date"));
            pdfPTable.addCell(dateCell);

            // Adds a cell to the table with "date" data
            for (int i = 0; i < 50; i++) {
                dateCell = new PdfPCell(new Phrase("2017-09-03"));
                pdfPTable.addCell(dateCell);
            }

            // Adds the table to the pdf document
            try {
                pdfDocument.add(pdfPTable);
            } catch (DocumentException e) {
                e.printStackTrace();
            }

            pdfDocument.close();
        }
    }

}
