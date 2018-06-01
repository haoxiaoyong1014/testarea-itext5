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
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class TableKeepTogether
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/39529281/itext-table-inside-columntext-dont-keeptogether">
     * iText Table inside ColumnText don't keeptogether
     * </a>
     * <p>
     * The first two pages use code of the OP which does not work as desired.
     * The last page shows how the desired effect can be achieved.
     * </p>
     */
    @Test
    public void testKeepTogetherTableInColumnText() throws IOException, DocumentException
    {
        File file = new File(RESULT_FOLDER, "keepTogetherTableInColumnText.pdf");
        OutputStream os = new FileOutputStream(file);

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, os);

        document.open();

        PdfContentByte canvas = writer.getDirectContent();

        printPage1(document, canvas);

        document.newPage();

        printPage2(document, canvas);

        document.newPage();

        printPage3(document, canvas);

        document.close();
        os.close();
    }

    private void printPage1(Document document, PdfContentByte canvas) throws DocumentException {
        int cols = 3;
        int rows = 15;

        PdfPTable table = new PdfPTable(cols);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                table.addCell(new Phrase("Cell " + row + ", " + col));
            }
        }

        Paragraph paragraph = new Paragraph();
        paragraph.add(table);

        ColumnText columnText = new ColumnText(canvas);
        columnText.addElement(new Paragraph("This table should keep together!"));
        columnText.addElement(table);

        int status = ColumnText.START_COLUMN;

        Rectangle docBounds = document.getPageSize();

        Rectangle bounds = new Rectangle(docBounds.getLeft(20), docBounds.getTop(20) - 200, docBounds.getRight(20), docBounds.getTop(20));
        bounds.setBorder(Rectangle.BOX);
        bounds.setBorderColor(BaseColor.BLACK);
        bounds.setBorderWidth(1);
        bounds.setBackgroundColor(new BaseColor(23, 142, 255, 20));

        canvas.rectangle(bounds);

        columnText.setSimpleColumn(bounds);

        status = columnText.go();

        if (ColumnText.hasMoreText(status)) {
            bounds = new Rectangle(docBounds.getLeft(20), docBounds.getBottom(20), docBounds.getRight(20), docBounds.getBottom(20) + 600);
            bounds.setBorder(Rectangle.BOX);
            bounds.setBorderColor(BaseColor.BLACK);
            bounds.setBorderWidth(1);
            bounds.setBackgroundColor(new BaseColor(255, 142, 23, 20));

            canvas.rectangle(bounds);

            columnText.setSimpleColumn(bounds);

            status = columnText.go();
        }
    }

    private void printPage2(Document document, PdfContentByte canvas) throws DocumentException {
        int cols = 3;
        int rows = 15;

        PdfPTable table = new PdfPTable(cols);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                table.addCell(new Phrase("Cell " + row + ", " + col));
            }
        }

        PdfPTable tableWrapper = new PdfPTable(1);
        tableWrapper.addCell(table);
        tableWrapper.setSplitRows(false);

        Paragraph paragraph = new Paragraph();
        paragraph.add(tableWrapper);

        ColumnText columnText = new ColumnText(canvas);
        columnText.addElement(new Paragraph("This table should keep together!"));
        columnText.addElement(tableWrapper);

        int status = ColumnText.START_COLUMN;

        Rectangle docBounds = document.getPageSize();

        Rectangle bounds = new Rectangle(docBounds.getLeft(20), docBounds.getTop(20) - 200, docBounds.getRight(20), docBounds.getTop(20));
        bounds.setBorder(Rectangle.BOX);
        bounds.setBorderColor(BaseColor.BLACK);
        bounds.setBorderWidth(1);
        bounds.setBackgroundColor(new BaseColor(23, 142, 255, 20));

        canvas.rectangle(bounds);

        columnText.setSimpleColumn(bounds);

        status = columnText.go();

        if (ColumnText.hasMoreText(status)) {
            bounds = new Rectangle(docBounds.getLeft(20), docBounds.getBottom(20), docBounds.getRight(20), docBounds.getBottom(20) + 600);
            bounds.setBorder(Rectangle.BOX);
            bounds.setBorderColor(BaseColor.BLACK);
            bounds.setBorderWidth(1);
            bounds.setBackgroundColor(new BaseColor(255, 142, 23, 20));

            canvas.rectangle(bounds);

            columnText.setSimpleColumn(bounds);

            status = columnText.go();
        }
    }

    private void printPage3(Document document, PdfContentByte canvas) throws DocumentException {
        int cols = 3;
        int rows = 15;

        PdfPTable table = new PdfPTable(cols);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                table.addCell(new Phrase("Cell " + row + ", " + col));
            }
        }
        table.setSpacingBefore(5);

        Rectangle docBounds = document.getPageSize();
        Rectangle upper = new Rectangle(docBounds.getLeft(20), docBounds.getTop(20) - 200, docBounds.getRight(20), docBounds.getTop(20));
        upper.setBackgroundColor(new BaseColor(23, 142, 255, 20));
        Rectangle lower = new Rectangle(docBounds.getLeft(20), docBounds.getBottom(20), docBounds.getRight(20), docBounds.getBottom(20) + 600);
        lower.setBackgroundColor(new BaseColor(255, 142, 23, 20));
        Rectangle[] rectangles = new Rectangle[] { upper, lower };

        for (Rectangle bounds : rectangles)
        {
            bounds.setBorder(Rectangle.BOX);
            bounds.setBorderColor(BaseColor.BLACK);
            bounds.setBorderWidth(1);

            canvas.rectangle(bounds);
        }

        rectangles = drawKeepTogether(new Paragraph("This table should keep together!"), canvas, rectangles);
        rectangles = drawKeepTogether(table, canvas, rectangles);
    }

    Rectangle[] drawKeepTogether(Element element, PdfContentByte canvas, Rectangle... rectangles) throws DocumentException
    {
        int i = 0;
        for (; i < rectangles.length; i++)
        {
            ColumnText columnText = new ColumnText(canvas);
            columnText.addElement(element);
            columnText.setSimpleColumn(rectangles[i]);
            int status = columnText.go(true);
            if (!ColumnText.hasMoreText(status))
                break;
        }

        if (i < rectangles.length)
        {
            Rectangle rectangle = rectangles[i];
            ColumnText columnText = new ColumnText(canvas);
            columnText.addElement(element);
            columnText.setSimpleColumn(rectangle);
            columnText.go(false);

            Rectangle[] remaining = new Rectangle[rectangles.length-i];
            System.arraycopy(rectangles, i, remaining, 0, remaining.length);
            remaining[0] = new Rectangle(rectangle.getLeft(), rectangle.getBottom(), rectangle.getRight(), columnText.getYLine());
            return remaining;
        }

        return new Rectangle[0];
    }
}
