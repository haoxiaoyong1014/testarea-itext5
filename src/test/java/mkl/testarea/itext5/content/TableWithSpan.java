package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class TableWithSpan
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/40947306/strange-setrowspan-error-not-working">
     * Strange setRowspan error/not working
     * </a>
     * <p>
     * Selecting 1 header row and having a cell in the first row which spans 2 rows
     * does not match. iText ignores the row span resulting in the weird appearance.
     * </p>
     */
    @Test
    public void testRowspanWithHeaderRows() throws IOException, DocumentException
    {
        File file = new File(RESULT_FOLDER, "rowspanWithHeaderRows.pdf");
        OutputStream os = new FileOutputStream(file);

        Document document = new Document();
        /*PdfWriter writer =*/ PdfWriter.getInstance(document, os);
        document.open();

        document.add(createHeaderContent());
        document.newPage();
        document.add(createHeaderContent(new int[] {5,5,5,5,5}));

        document.close();
    }

    public PdfPTable createHeaderContent()
    {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        PdfPCell dobicell = new PdfPCell();
        dobicell.setColspan(2);
        dobicell.addElement(new Phrase(/*docType*/"Letter", DOBIFONTADR));
        dobicell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.TOP);
        table.addCell(dobicell);

        dobicell = new PdfPCell();
        dobicell.setColspan(2);
        dobicell.addElement(new Phrase("Ing. Mario J. Schwaiger", DOBIFONTADR));
        dobicell.setBorder(Rectangle.TOP);
        table.addCell(dobicell);

        dobicell = /*Dobilogo.getPiccell(92, 104)*/ getPiccell(92,104);
        dobicell.setBorder(Rectangle.TOP | Rectangle.RIGHT);
        dobicell.setColspan(3);
        dobicell.setRowspan(2);
        table.addCell(dobicell);

        dobicell = /*getKundenCol(kunde)*/new PdfPCell(new Phrase("Mr. John Smith", DOBIFONTADR));
        dobicell.setColspan(2);
        dobicell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        table.addCell(dobicell);

        dobicell = /*getUserCell(user)*/new PdfPCell(new Phrase("Address in Austria", DOBIFONTADR));
        dobicell.setColspan(2);
        table.addCell(dobicell);

        table.setHeaderRows(1);
        return table;
    }

    public PdfPTable createHeaderContent(int[] coldist) {
        PdfPTable table = new PdfPTable(coldist[0] + coldist[1] + coldist[2]); //createHeaderContent(new int[]{4, 7, 4, 4, 7});
        table.setWidthPercentage(100);

        PdfPCell dobicell = new PdfPCell();
        dobicell.setColspan(coldist[0]); //used to be 2, now 4
        dobicell.addElement(new Phrase(/*doctype*/"Letter", DOBIFONTADR));        
        dobicell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.TOP);
        table.addCell(dobicell);

        dobicell = new PdfPCell();
        dobicell.setColspan(coldist[1]); //used to be 2, now 7
        dobicell.addElement(new Phrase("Ing. Mario J. Schwaiger", /*DOBIFONTTITEL*/DOBIFONTADR));
        dobicell.setBorder(Rectangle.TOP);
        table.addCell(dobicell);

        dobicell = /*Dobilogo.getPiccell(92, 104)*/getPiccell(92,104);
        dobicell.setBorder(Rectangle.TOP | Rectangle.RIGHT);
        dobicell.setColspan(coldist[2]); //used to be 3, now 4
        dobicell.setRowspan(2);  // <--- This is fishy, but why?
        table.addCell(dobicell);

        dobicell = /*getKundenCol(kunde)*/new PdfPCell(new Phrase("Mr. John Smith", DOBIFONTADR));
        dobicell.setColspan(coldist[3]); //used to be 2, now 4
        dobicell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        table.addCell(dobicell);

        dobicell = /*getUserCell(user)*/new PdfPCell(new Phrase("Address in Austria", DOBIFONTADR));
        dobicell.setColspan(coldist[4]); //used to be 2, now 7
        table.addCell(dobicell);

        table.setHeaderRows(1);
        return table;
    }

    PdfPCell getPiccell(int w, int h)
    {
        try
        {
            Image image = Image.getInstance("src/test/resources/mkl/testarea/itext5/content/2x2colored.png");
            image.scaleAbsolute(w, h);
            return new PdfPCell(image);
        }
        catch (BadElementException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    Font DOBIFONTADR = new Font();
}
