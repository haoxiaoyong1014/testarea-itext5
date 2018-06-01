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
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfShading;
import com.itextpdf.text.pdf.PdfShadingPattern;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class DrawGradient
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/39072316/itext-gradient-issue-in-landscape">
     * iText gradient issue in landscape
     * </a>
     * <p>
     * The problem is that while itext content adding functionalities <b>do</b> take the page
     * rotation into account (they translate the given coordinates so that in the rotated page
     * <em>x</em> goes right and <em>y</em> goes up and the origin is in the lower left), the
     * shading pattern definitions (which are <em>not</em> part of the page content but
     * externally defined) <b>don't</b>.
     * </p>
     * <p>
     * Thus, you have to make the shading definition rotation aware, e.g. like this.
     * </p>
     */
    @Test
    public void testGradientOnRotatedPage() throws FileNotFoundException, DocumentException
    {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(new File(RESULT_FOLDER, "gradientProblem.pdf")));
        doc.open();
        drawSexyTriangle(writer, false);
        doc.setPageSize(PageSize.A4.rotate());
        doc.newPage();
        drawSexyTriangle(writer, true);
        doc.close();
    }

    private static void drawSexyTriangle(PdfWriter writer, boolean rotated)
    {
        PdfContentByte canvas = writer.getDirectContent();
        float x = 36;
        float y = 400;
        float side = 70;
        PdfShading axial = rotated ?
                PdfShading.simpleAxial(writer, PageSize.A4.getRight() - y, x, PageSize.A4.getRight() - y, x + side, BaseColor.PINK, BaseColor.BLUE)
                : PdfShading.simpleAxial(writer, x, y, x + side, y, BaseColor.PINK, BaseColor.BLUE);
        PdfShadingPattern shading = new PdfShadingPattern(axial);
        canvas.setShadingFill(shading);
        canvas.moveTo(x,y);
        canvas.lineTo(x + side, y);
        canvas.lineTo(x + (side / 2), (float)(y + (side * Math.sin(Math.PI / 3))));
        canvas.closePathFillStroke();
    }
}
