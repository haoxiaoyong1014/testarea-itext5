package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;

/**
 * <a href="http://stackoverflow.com/questions/29303345/java-itext-rotate-a-link-rectangle">
 * Java iText Rotate a Link Rectangle
 * </a>
 * 
 * Essentially the OP's original code {@link #testOPCode()} merely rotates the lower left
 * and the upper right of the link.
 * 
 * An own test {@link #testOwnAppearances()} attempts to supply a rotated appearance, but
 * obviously Adobe Reader insists on providing its own. 
 * 
 * But testing with QUADPOINTS {@link #testQuadPoints()} shows how to do it.
 * 
 * @author mkl
 */
public class RotateLink
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testOPCode() throws IOException, DocumentException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("/mkl/testarea/itext5/merge/testA4.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "testA4-annotate.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            Rectangle linkLocation = new Rectangle( 100, 700, 100 + 200, 700 + 25 );
            PdfName highlight = PdfAnnotation.HIGHLIGHT_INVERT;
            PdfAnnotation linkRed  = PdfAnnotation.createLink( stamper.getWriter(), linkLocation, highlight, "red" );
            PdfAnnotation linkGreen = PdfAnnotation.createLink( stamper.getWriter(), linkLocation, highlight, "green" );
            BaseColor baseColorRed = new BaseColor(255,0,0);
            BaseColor baseColorGreen = new BaseColor(0,255,0);
            linkRed.setColor(baseColorRed);
            linkGreen.setColor(baseColorGreen);
            double angleDegrees = 10;
            double angleRadians = Math.PI*angleDegrees/180;
            stamper.addAnnotation(linkRed, 1);
            linkGreen.applyCTM(AffineTransform.getRotateInstance(angleRadians));
            stamper.addAnnotation(linkGreen, 1);
            stamper.close();
        }
    }

    @Test
    public void testOwnAppearances() throws IOException, DocumentException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("/mkl/testarea/itext5/merge/testA4.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "testA4-annotate-app.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            BaseColor baseColorRed = new BaseColor(255,0,0);
            BaseColor baseColorGreen = new BaseColor(0,255,0);
            Rectangle linkLocation = new Rectangle( 100, 700, 100 + 200, 700 + 25 );
            PdfName highlight = PdfAnnotation.HIGHLIGHT_INVERT;

            PdfAnnotation linkGreen = PdfAnnotation.createLink( stamper.getWriter(), linkLocation, highlight, "green" );
            PdfTemplate appearance = PdfTemplate.createTemplate(stamper.getWriter(), linkLocation.getWidth(), linkLocation.getHeight());
            appearance.setColorFill(baseColorGreen);
            appearance.rectangle(0, 0, linkLocation.getWidth(), linkLocation.getHeight());
            appearance.fill();
            double angleDegrees = 35;
            double angleRadians = Math.PI*angleDegrees/180;
            AffineTransform at = AffineTransform.getRotateInstance(angleRadians);
            appearance.setMatrix((float)at.getScaleX(), (float)at.getShearY(),(float) at.getShearX(), (float)at.getScaleY(),(float) at.getTranslateX(), (float)at.getTranslateY());
            linkGreen.setAppearance(PdfName.N, appearance);
            linkGreen.setColor(baseColorGreen);
            stamper.addAnnotation(linkGreen, 1);

            PdfAnnotation linkRed  = PdfAnnotation.createLink( stamper.getWriter(), linkLocation, highlight, "red" );
            linkRed.setColor(baseColorRed);
            stamper.addAnnotation(linkRed, 1);

            stamper.close();
        }
    }

    @Test
    public void testQuadPoints() throws IOException, DocumentException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("/mkl/testarea/itext5/merge/testA4.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "testA4-annotate-quad.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            Rectangle linkLocation = new Rectangle( 100, 700, 100 + 200, 700 + 25 );
            PdfName highlight = PdfAnnotation.HIGHLIGHT_INVERT;

            PdfAnnotation linkRed  = PdfAnnotation.createLink( stamper.getWriter(), linkLocation, highlight, "red" );
            BaseColor baseColorRed = new BaseColor(255,0,0);
            linkRed.setColor(baseColorRed);
            //stamper.addAnnotation(linkRed, 1);

            linkLocation = new Rectangle( 100, 700, 100 + 200, 700 + 25 );
            PdfAnnotation linkGreen = PdfAnnotation.createLink( stamper.getWriter(), linkLocation, highlight, "green" );
            BaseColor baseColorGreen = new BaseColor(0,255,0);
            linkGreen.setColor(baseColorGreen);
//            double angleDegrees = 10;
//            double angleRadians = Math.PI*angleDegrees/180;
//            AffineTransform affine = AffineTransform.getRotateInstance(angleRadians);
//            float[] corners = new float[]{
//                    linkLocation.getLeft(), linkLocation.getBottom(),
//                    linkLocation.getRight(), linkLocation.getBottom(),
//                    linkLocation.getRight(), linkLocation.getTop(),
//                    linkLocation.getLeft(), linkLocation.getTop()
//            };
//            affine.transform(corners, 0, corners, 0, 4);
            float[] corners = new float[]{100, 700, 200, 710, 300, 700, 200, 725};
            PdfArray array = new PdfArray(corners);
            linkGreen.put(PdfName.QUADPOINTS, array);
            stamper.addAnnotation(linkGreen, 1);

            stamper.close();
        }
    }
}
