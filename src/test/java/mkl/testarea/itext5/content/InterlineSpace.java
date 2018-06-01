package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class InterlineSpace
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34681893/itextsharp-extra-space-between-lines">
     * iTextSharp: Extra space between lines
     * </a>
     * <p>
     * Indeed, the OP's {@link Phrase#setLeading(float, float)} calls are ignored.
     * The reason is that the op is working in text mode. Thus, he has to use
     * {@link ColumnText#setLeading(float, float)} instead, cf.
     * {@link #testLikeUser3208131Fixed()}.
     * </p>
     */
    @Test
    public void testLikeUser3208131() throws DocumentException, FileNotFoundException
    {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "interline-user3208131.pdf")));
        document.open();

        Font font = new Font(FontFamily.UNDEFINED, 4, Font.UNDEFINED, null);
        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        float gutter = 15;
        float colwidth = (document.getPageSize().getRight() - document.getPageSize().getLeft() - gutter) / 2;

        float[] left = { document.getPageSize().getLeft() + 133, document.getPageSize().getTop() - 35,
                document.getPageSize().getLeft() + 133, document.getPageSize().getBottom() };
        float[] right = { document.getPageSize().getLeft() + colwidth, document.getPageSize().getTop() - 35,
                document.getPageSize().getLeft() + colwidth, document.getPageSize().getBottom() };

        for (int i = 0; i < 3; i++)
        {
            Phrase Ps = new Phrase("Test " + i + "\n", font);
            Ps.setLeading(0.0f, 0.6f);
            ct.addText(Ps);
            ct.addText(Chunk.NEWLINE);
        }
        ct.setColumns(left, right);
        ct.go();

        document.close();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34681893/itextsharp-extra-space-between-lines">
     * iTextSharp: Extra space between lines
     * </a>
     * <p>
     * Indeed, the OP's {@link Phrase#setLeading(float, float)} calls are ignored,
     * cf. {@link #testLikeUser3208131()}. The reason is that the op is working in
     * text mode. Thus, he has to use {@link ColumnText#setLeading(float, float)}
     * instead.
     * </p>
     */
    @Test
    public void testLikeUser3208131Fixed() throws DocumentException, FileNotFoundException
    {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "interline-user3208131-fixed.pdf")));
        document.open();

        Font font = new Font(FontFamily.UNDEFINED, 4, Font.UNDEFINED, null);
        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        float gutter = 15;
        float colwidth = (document.getPageSize().getRight() - document.getPageSize().getLeft() - gutter) / 2;

        float[] left = { document.getPageSize().getLeft() + 133, document.getPageSize().getTop() - 35,
                document.getPageSize().getLeft() + 133, document.getPageSize().getBottom() };
        float[] right = { document.getPageSize().getLeft() + colwidth, document.getPageSize().getTop() - 35,
                document.getPageSize().getLeft() + colwidth, document.getPageSize().getBottom() };

        ct.setLeading(0.0f, 0.3f);
        for (int i = 0; i < 3; i++)
        {
            Phrase Ps = new Phrase("Test " + i + "\n", font);
            ct.addText(Ps);
            ct.addText(Chunk.NEWLINE);
        }
        ct.setColumns(left, right);
        ct.go();

        document.close();
    }

}
