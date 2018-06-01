package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class UseRowspan
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/44005834/changing-rowspans">
     * Changing rowspans
     * </a>
     * <p>
     * The original code used by the OP. This code adds the second cell
     * with rowspan 2 too early. Fixed in {@link #testUseRowspanLikeUser7968180Fixed()}.
     * </p>
     * @see #createPdf(String)
     * @see #addCellToTableCzech(PdfPTable, int, int, String, int, int, String, float)
     */
    @Test
    public void testUseRowspanLikeUser7968180() throws IOException, DocumentException
    {
        File file = new File(RESULT_FOLDER, "pdf1iTextZKOUSKA.pdf");
        file.getParentFile().mkdirs();
        createPdf(file.getAbsolutePath());
    }

    /**
     * <a href="http://stackoverflow.com/questions/44005834/changing-rowspans">
     * Changing rowspans
     * </a>
     * <p>
     * The fixed code. This code adds the second cell with rowspan 2
     * in twelfth place. Fixed of {@link #testUseRowspanLikeUser7968180()}.
     * </p>
     * @see #createPdfFixed(String)
     * @see #addCellToTableCzech(PdfPTable, int, int, String, int, int, String, float)
     */
    @Test
    public void testUseRowspanLikeUser7968180Fixed() throws IOException, DocumentException
    {
        File file = new File(RESULT_FOLDER, "pdf1iTextZKOUSKA-fixed.pdf");
        file.getParentFile().mkdirs();
        createPdfFixed(file.getAbsolutePath());
    }

    /**
     * <a href="http://stackoverflow.com/questions/44005834/changing-rowspans">
     * Changing rowspans
     * </a>
     * <p>
     * The original code used by the OP. This code adds the second cell
     * with rowspan 2 too early. Fixed in {@link #createPdfFixed(String)}.
     * </p>
     * @see #testUseRowspanLikeUser7968180()
     * @see #addCellToTableCzech(PdfPTable, int, int, String, int, int, String, float)
     */
    public void createPdf(String dest) throws IOException, DocumentException {
        int horizontalAlignmentCenter = Element.ALIGN_CENTER;
        int verticalAlignmentMiddle = Element.ALIGN_MIDDLE;
        String fontTypeRegular = "c:/Windows/Fonts/arial.ttf";
        float fontSizeRegular = 10f;

        float[] columns = { 100, 50, 100, 50, 50, 50, 50, 50, 75, 50, 50, 50 };
        int numberOfColumns = columns.length;
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();

        PdfPTable subTableZkouska = new PdfPTable(numberOfColumns);
        subTableZkouska.setTotalWidth(columns);
        subTableZkouska.setLockedWidth(true);

        addCellToTableCzech(subTableZkouska, horizontalAlignmentCenter,
                verticalAlignmentMiddle, "Brno Špitálka 8 Brno Hájecká 1068/14 CZ5159", 1,
                2, fontTypeRegular, fontSizeRegular);

        addCellToTableCzech(subTableZkouska, horizontalAlignmentCenter,
                verticalAlignmentMiddle, "38", 1, 2, fontTypeRegular, fontSizeRegular);

        for (int i = 0; i < 19; i++) {
            addCellToTableCzech(subTableZkouska, horizontalAlignmentCenter,
                    verticalAlignmentMiddle, "38", 1, 1, fontTypeRegular,
                    fontSizeRegular);
        }
        addCellToTableCzech(subTableZkouska, horizontalAlignmentCenter,
                verticalAlignmentMiddle, "38", 1, 1, fontTypeRegular, fontSizeRegular);

        document.add(subTableZkouska);
        document.close();
    }

    /**
     * <a href="http://stackoverflow.com/questions/44005834/changing-rowspans">
     * Changing rowspans
     * </a>
     * <p>
     * The fixed code. This code adds the second cell with rowspan 2
     * in twelfth place. Fixed of {@link #createPdf(String)}.
     * </p>
     * @see #testUseRowspanLikeUser7968180Fixed()
     * @see #addCellToTableCzech(PdfPTable, int, int, String, int, int, String, float)
     */
    public void createPdfFixed(String dest) throws IOException, DocumentException {
        int horizontalAlignmentCenter = Element.ALIGN_CENTER;
        int verticalAlignmentMiddle = Element.ALIGN_MIDDLE;
        String fontTypeRegular = "c:/Windows/Fonts/arial.ttf";
        float fontSizeRegular = 10f;

        float[] columns = { 100, 50, 100, 50, 50, 50, 50, 50, 75, 50, 50, 50 };
        int numberOfColumns = columns.length;
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();

        PdfPTable subTableZkouska = new PdfPTable(numberOfColumns);
        subTableZkouska.setTotalWidth(columns);
        subTableZkouska.setLockedWidth(true);

        addCellToTableCzech(subTableZkouska, horizontalAlignmentCenter,
                verticalAlignmentMiddle, "Brno Špitálka 8 Brno Hájecká 1068/14 CZ5159", 1,
                2, fontTypeRegular, fontSizeRegular);

        for (int i = 2; i < 12; i++) {
            addCellToTableCzech(subTableZkouska, horizontalAlignmentCenter,
                    verticalAlignmentMiddle, "38", 1, 1, fontTypeRegular,
                    fontSizeRegular);
        }

        addCellToTableCzech(subTableZkouska, horizontalAlignmentCenter,
                verticalAlignmentMiddle, "38", 1, 2, fontTypeRegular, fontSizeRegular);

        for (int i = 13; i < 23; i++) {
            addCellToTableCzech(subTableZkouska, horizontalAlignmentCenter,
                    verticalAlignmentMiddle, "38", 1, 1, fontTypeRegular,
                    fontSizeRegular);
        }

        document.add(subTableZkouska);
        document.close();
    }

    /**
     * <a href="http://stackoverflow.com/questions/44005834/changing-rowspans">
     * Changing rowspans
     * </a>
     * <p>
     * Helper method of the OP.
     * </p>
     * @see #testUseRowspanLikeUser7968180()
     * @see #testUseRowspanLikeUser7968180Fixed()
     * @see #createPdf(String)
     * @see #createPdfFixed(String)
     */
    private static void addCellToTableCzech(PdfPTable table, int horizontalAlignment,
            int verticalAlignment, String value, int colspan, int rowspan,
            String fontType, float fontSize) {
        BaseFont base = null;
        try {
            base = BaseFont.createFont(fontType, BaseFont.CP1250, BaseFont.EMBEDDED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Font font = new Font(base, fontSize);
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setColspan(colspan);
        cell.setRowspan(rowspan);
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setVerticalAlignment(verticalAlignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell);
    }
}
