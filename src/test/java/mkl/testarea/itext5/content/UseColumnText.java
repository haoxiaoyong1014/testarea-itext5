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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class UseColumnText
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/32162759/columntext-showtextaligned-vs-columntext-setsimplecolumn-top-alignment">
     * ColumnText.ShowTextAligned vs ColumnText.SetSimpleColumn Top Alignment
     * </a>
     * <p>
     * Indeed, the coordinates do not line up. The y coordinate of 
     * {@link ColumnText#showTextAligned(PdfContentByte, int, Phrase, float, float, float)}
     * denotes the baseline while {@link ColumnText#setSimpleColumn(Rectangle)} surrounds
     * the text to come.
     * </p>
     */
    @Test
    public void testShowTextAlignedVsSimpleColumnTopAlignment() throws DocumentException, IOException
    {
        Document document = new Document(PageSize.A4);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "ColumnTextTopAligned.pdf")));
        document.open();

        Font fontQouteItems = new Font(BaseFont.createFont(), 12);
        PdfContentByte canvas = writer.getDirectContent();

        // Item Number
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase("36222-0", fontQouteItems), 60, 450, 0);

        // Estimated Qty
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase("47", fontQouteItems), 143, 450, 0);

        // Item Description
        ColumnText ct = new ColumnText(canvas); // Uses a simple column box to provide proper text wrapping
        ct.setSimpleColumn(new Rectangle(193, 070, 390, 450));
        ct.setText(new Phrase("In-Situ : Poly Cable - 100'\nPoly vented rugged black gable 100ft\nThis is an additional description. It can wrap an extra line if it needs to so this text is long.", fontQouteItems));
        ct.go();

        document.close();
    }

}
