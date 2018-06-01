package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CreateTableDirectContent
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/43807931/creating-table-in-pdf-on-last-page-bottom-wrong-official-solution">
     * Creating table in pdf on last page bottom (wrong official solution)
     * </a>
     * <p>
     * Indeed, there is an error in the official sample which effectively
     * applies the margins twice.
     * </p>
     */
    @Test
    public void testCreateTableLikeUser7968180() throws FileNotFoundException, DocumentException
    {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(new File(RESULT_FOLDER, "calendarUser7968180.pdf")));
        document.open();

        PdfPTable datatable = null;//createHeaderTable();
        //document.add(datatable);
        datatable = createFooterTable();

        drawTableAtTheEndOfPage(document, writer, datatable);

        // Marking the border
        PdfContentByte canvas = writer.getDirectContentUnder();
        canvas.setColorStroke(BaseColor.RED);
        canvas.setColorFill(BaseColor.PINK);
        canvas.rectangle(document.left(), document.bottom(), document.right() - document.left(), document.top() - document.bottom());
        Rectangle pageSize = document.getPageSize(); 
        canvas.rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(), pageSize.getHeight());
        canvas.eoFillStroke();

        document.close();
        System.out.println("done");
    }

    /**
     * <a href="http://stackoverflow.com/questions/43807931/creating-table-in-pdf-on-last-page-bottom-wrong-official-solution">
     * Creating table in pdf on last page bottom (wrong official solution)
     * </a>
     * <p>
     * Helper method for {@link #testCreateTableLikeUser7968180()}. Here the error
     * is corrected.
     * </p>
     */
    private static void drawTableAtTheEndOfPage(Document document, PdfWriter writer, PdfPTable datatable)
    {
        datatable.setTotalWidth(document.right() - document.left());
//        datatable.setTotalWidth(document.right(document.rightMargin()) - document.left(document.leftMargin()));

        datatable.writeSelectedRows(0, -1, document.left(),
                datatable.getTotalHeight() + document.bottom(), writer.getDirectContent());
//        datatable.writeSelectedRows(0, -1, document.left(document.leftMargin()),
//                datatable.getTotalHeight() + document.bottom(document.bottomMargin()), writer.getDirectContent());
    }

    /**
     * <a href="http://stackoverflow.com/questions/43807931/creating-table-in-pdf-on-last-page-bottom-wrong-official-solution">
     * Creating table in pdf on last page bottom (wrong official solution)
     * </a>
     * <p>
     * Helper method for {@link #testCreateTableLikeUser7968180()}.
     * </p>
     */
    private static PdfPTable createFooterTable() throws DocumentException
    {
        int[] columnWidths = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        PdfPTable datatable = new PdfPTable(columnWidths.length);
        datatable.setKeepTogether(true);
        datatable.setWidthPercentage(100);
        datatable.setWidths(columnWidths);
        datatable.getDefaultCell().setPadding(5);

//        datatable.getDefaultCell().setHorizontalAlignment(horizontalAlignment);
//        datatable.getDefaultCell().setVerticalAlignment(verticalAlignment);

        for (int i = 0; i < 100; i++)
        {
            datatable.addCell("Přehledová tabulka");
//            addCellToTable(datatable, horizontalAlignmentLeft, verticalAlignmentMiddle, "Přehledová tabulka",
//                    columnWidths.length, 1, fontTypeBold, fontSizeRegular, cellLayout_Bottom);
        }

        return datatable;
    }
}
