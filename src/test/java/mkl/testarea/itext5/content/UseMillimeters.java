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
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class UseMillimeters
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/30979732/itext-rectangle-from-milimeters">
     * Itext rectangle from milimeters
     * </a>
     * <p>
     * This test creates a sample PDF from the OP's code in {@link #createPDF(String)}
     * an {@link #createRectangle(PdfWriter, float, float, float, float, BaseColor)}.
     * Measuring the rectangle both on-screen (using Adobe's Measurement tool) and on
     * paper resulted in the correct 148,5mm x 210mm.
     * </p>
     */
    @Test
    public void testCreateDocWithMillimeters() throws FileNotFoundException, DocumentException
    {
        createPDF(new File(RESULT_FOLDER, "MillimeterRectangle.pdf").toString());
    }

    public static boolean createPDF(String pathPDF) throws FileNotFoundException, DocumentException {

        Document document = new Document(PageSize.A4);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pathPDF));
        writer.addViewerPreference(PdfName.PRINTSCALING, PdfName.NONE);
        document.open();

        createRectangle(writer, 30.75f, 11, 148.5f, 210, BaseColor.RED);
        document.close();

        return true;
    }

    private static void createRectangle(PdfWriter writer, float x, float y, float width, float height, BaseColor color)
    {
        float posX = Utilities.millimetersToPoints(x);
        float posY = Utilities.millimetersToPoints(y);

        float widthX = Utilities.millimetersToPoints(width + x);
        float heightY = Utilities.millimetersToPoints(height + y);

        Rectangle rectangle = new Rectangle(posX, posY, widthX, heightY);

        PdfContentByte canvas = writer.getDirectContent();
        rectangle.setBorder(Rectangle.BOX);
        rectangle.setBorderWidth(1);
        rectangle.setBorderColor(color);
        canvas.rectangle(rectangle);
    }
}
