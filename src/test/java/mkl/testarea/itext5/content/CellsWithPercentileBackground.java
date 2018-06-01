package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CellsWithPercentileBackground {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/46869670/itext-5-create-pdfpcell-containing-2-background-colors-with-text-overlap">
     * iText 5: create PdfPcell containing 2 background colors with text overlap
     * </a>
     * <p>
     * This test creates a table with cells that have backgrounds
     * which represent percentile values. To do so it makes use of the
     * {@link PercentileCellBackground} cell event listener.
     * </p>
     * 
     * @author mkl
     */
    @Test
    public void testCreateTableWithCellsWithPercentileBackground() throws DocumentException, IOException {
        Document document = new Document();
        try (OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "TableWithCellsWithPercentileBackground.pdf"))) {
            PdfWriter.getInstance(document, os);
            document.open();

            Font font = new Font(FontFamily.UNDEFINED, Font.UNDEFINED, Font.UNDEFINED, BaseColor.WHITE);
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(40);
            PdfPCell cell = new PdfPCell(new Phrase("Group A"));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(new Chunk("60 Pass, 40 Fail", font)));
            cell.setCellEvent(new PercentileCellBackground(60, BaseColor.GREEN, BaseColor.RED));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Group B"));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(new Chunk("70 Pass, 30 Fail", font)));
            cell.setCellEvent(new PercentileCellBackground(70, BaseColor.GREEN, BaseColor.RED));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Group C"));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(new Chunk("50 Pass, 50 Fail", font)));
            cell.setCellEvent(new PercentileCellBackground(50, BaseColor.GREEN, BaseColor.RED));
            table.addCell(cell);
            document.add(table);

            document.close();
        }
    }

}
