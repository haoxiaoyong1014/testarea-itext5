package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfBorderDictionary;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class CreateEllipse
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/43205385/trying-to-draw-an-ellipse-annotation-and-the-border-on-the-edges-goes-thin-and-t">
     * Trying to draw an ellipse annotation and the border on the edges goes thin and thik when i try to roatate pdf itext5
     * </a>
     * <p>
     * This test creates an ellipse annotation without appearance on a page without rotation. Everything looks ok.
     * </p>
     * @see #testCreateEllipseAppearance()
     * @see #testCreateEllipseOnRotated()
     * @see #testCreateEllipseAppearanceOnRotated()
     * @see #testCreateCorrectEllipseAppearanceOnRotated()
     */
    @Test
    public void testCreateEllipse() throws IOException, DocumentException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("/mkl/testarea/itext5/merge/testA4.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "testA4-ellipse.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            Rectangle rect = new Rectangle(202 + 6f, 300, 200 + 100, 300 + 150);

            PdfAnnotation annotation = PdfAnnotation.createSquareCircle(stamper.getWriter(), rect, null, false);
            annotation.setFlags(PdfAnnotation.FLAGS_PRINT);
            annotation.setColor(BaseColor.RED);
            annotation.setBorderStyle(new PdfBorderDictionary(3.5f, PdfBorderDictionary.STYLE_SOLID));

            stamper.addAnnotation(annotation, 1);

            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/43205385/trying-to-draw-an-ellipse-annotation-and-the-border-on-the-edges-goes-thin-and-t">
     * Trying to draw an ellipse annotation and the border on the edges goes thin and thik when i try to roatate pdf itext5
     * </a>
     * <p>
     * This test creates an ellipse annotation with appearance on a page without rotation. Everything looks ok.
     * </p>
     * @see #testCreateEllipse()
     * @see #testCreateEllipseOnRotated()
     * @see #testCreateEllipseAppearanceOnRotated()
     * @see #testCreateCorrectEllipseAppearanceOnRotated()
     */
    @Test
    public void testCreateEllipseAppearance() throws IOException, DocumentException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("/mkl/testarea/itext5/merge/testA4.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "testA4-ellipse-appearance.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            Rectangle rect = new Rectangle(202 + 6f, 300, 200 + 100, 300 + 150);

            PdfAnnotation annotation = PdfAnnotation.createSquareCircle(stamper.getWriter(), rect, null, false);
            annotation.setFlags(PdfAnnotation.FLAGS_PRINT);
            annotation.setColor(BaseColor.RED);
            annotation.setBorderStyle(new PdfBorderDictionary(3.5f, PdfBorderDictionary.STYLE_SOLID));

            PdfContentByte cb = stamper.getOverContent(1);
            PdfAppearance app = cb.createAppearance(rect.getWidth(), rect.getHeight());
            app.setColorStroke(BaseColor.RED);
            app.setLineWidth(3.5);
            app.ellipse( 1.5,  1.5, rect.getWidth() - 1.5, rect.getHeight() - 1.5);
            app.stroke();
            annotation.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, app);

            stamper.addAnnotation(annotation, 1);

            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/43205385/trying-to-draw-an-ellipse-annotation-and-the-border-on-the-edges-goes-thin-and-t">
     * Trying to draw an ellipse annotation and the border on the edges goes thin and thik when i try to roatate pdf itext5
     * </a>
     * <p>
     * This test creates an ellipse annotation without appearance on a page with rotation.
     * The ellipse form looks ok but it is moved to the right of the actual appearance rectangle when viewed in Adobe Reader.
     * This is caused by iText creating a non-standard rectangle, the lower left not being the lower left etc.
     * </p>
     * @see #testCreateEllipse()
     * @see #testCreateEllipseAppearance()
     * @see #testCreateEllipseAppearanceOnRotated()
     * @see #testCreateCorrectEllipseAppearanceOnRotated()
     */
    @Test
    public void testCreateEllipseOnRotated() throws IOException, DocumentException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("/mkl/testarea/itext5/merge/testA4.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "testA4-rotated-ellipse.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            reader.getPageN(1).put(PdfName.ROTATE, new PdfNumber(90));

            PdfStamper stamper = new PdfStamper(reader, outputStream);

            Rectangle rect = new Rectangle(202 + 6f, 300, 200 + 100, 300 + 150);

            PdfAnnotation annotation = PdfAnnotation.createSquareCircle(stamper.getWriter(), rect, null, false);
            annotation.setFlags(PdfAnnotation.FLAGS_PRINT);
            annotation.setColor(BaseColor.RED);
            annotation.setBorderStyle(new PdfBorderDictionary(3.5f, PdfBorderDictionary.STYLE_SOLID));

            stamper.addAnnotation(annotation, 1);

            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/43205385/trying-to-draw-an-ellipse-annotation-and-the-border-on-the-edges-goes-thin-and-t">
     * Trying to draw an ellipse annotation and the border on the edges goes thin and thik when i try to roatate pdf itext5
     * </a>
     * <p>
     * This test creates an ellipse annotation with appearance on a page with rotation.
     * The ellipse position looks ok but it is deformed.
     * This is caused by iText rotating the annotation rectangle but not (how could it?) the appearance rectangle.
     * </p>
     * @see #testCreateEllipse()
     * @see #testCreateEllipseAppearance()
     * @see #testCreateEllipseOnRotated()
     * @see #testCreateCorrectEllipseAppearanceOnRotated()
     */
    @Test
    public void testCreateEllipseAppearanceOnRotated() throws IOException, DocumentException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("/mkl/testarea/itext5/merge/testA4.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "testA4-rotated-ellipse-appearance.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            reader.getPageN(1).put(PdfName.ROTATE, new PdfNumber(90));

            PdfStamper stamper = new PdfStamper(reader, outputStream);

            Rectangle rect = new Rectangle(202 + 6f, 300, 200 + 100, 300 + 150);

            PdfAnnotation annotation = PdfAnnotation.createSquareCircle(stamper.getWriter(), rect, null, false);
            annotation.setFlags(PdfAnnotation.FLAGS_PRINT);
            annotation.setColor(BaseColor.RED);
            annotation.setBorderStyle(new PdfBorderDictionary(3.5f, PdfBorderDictionary.STYLE_SOLID));

            PdfContentByte cb = stamper.getOverContent(1);
            PdfAppearance app = cb.createAppearance(rect.getWidth(), rect.getHeight());
            app.setColorStroke(BaseColor.RED);
            app.setLineWidth(3.5);
            app.ellipse( 1.5,  1.5, rect.getWidth() - 1.5, rect.getHeight() - 1.5);
            app.stroke();
            annotation.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, app);

            stamper.addAnnotation(annotation, 1);

            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/43205385/trying-to-draw-an-ellipse-annotation-and-the-border-on-the-edges-goes-thin-and-t">
     * Trying to draw an ellipse annotation and the border on the edges goes thin and thik when i try to roatate pdf itext5
     * </a>
     * <p>
     * This test creates an ellipse annotation with appearance with switched dimensions on a page with rotation.
     * Everything looks ok.
     * </p>
     * @see #testCreateEllipse()
     * @see #testCreateEllipseAppearance()
     * @see #testCreateEllipseOnRotated()
     * @see #testCreateEllipseAppearanceOnRotated()
     */
    @Test
    public void testCreateCorrectEllipseAppearanceOnRotated() throws IOException, DocumentException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("/mkl/testarea/itext5/merge/testA4.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "testA4-rotated-ellipse-appearance-correct.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            reader.getPageN(1).put(PdfName.ROTATE, new PdfNumber(90));

            PdfStamper stamper = new PdfStamper(reader, outputStream);

            Rectangle rect = new Rectangle(202 + 6f, 300, 200 + 100, 300 + 150);

            PdfAnnotation annotation = PdfAnnotation.createSquareCircle(stamper.getWriter(), rect, null, false);
            annotation.setFlags(PdfAnnotation.FLAGS_PRINT);
            annotation.setColor(BaseColor.RED);
            annotation.setBorderStyle(new PdfBorderDictionary(3.5f, PdfBorderDictionary.STYLE_SOLID));

            PdfContentByte cb = stamper.getOverContent(1);
            PdfAppearance app = cb.createAppearance(rect.getHeight(), rect.getWidth());
            app.setColorStroke(BaseColor.RED);
            app.setLineWidth(3.5);
            app.ellipse( 1.5,  1.5, rect.getHeight() - 1.5, rect.getWidth() - 1.5);
            app.stroke();
            annotation.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, app);

            stamper.addAnnotation(annotation, 1);

            stamper.close();
            reader.close();
        }
    }
}
