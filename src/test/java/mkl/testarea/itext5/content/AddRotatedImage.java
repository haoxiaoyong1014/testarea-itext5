package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AddRotatedImage
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/39364197/how-to-rotate-around-the-image-center-by-itext">
     * How to rotate around the image center by itext?
     * </a>
     * <p>
     * This test draws an image at given coordinates without rotation and then again
     * as if that image was rotated around its center by some angle.
     * </p>
     */
    @Test
    public void testAddRotatedImage() throws IOException, DocumentException
    {
        try (   FileOutputStream stream = new FileOutputStream(new File(RESULT_FOLDER, "rotatedImage.pdf"))    )
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, stream);
            document.open();

            PdfContentByte contentByte = writer.getDirectContent();

            int x = 200;
            int y = 300;
            float rotate = (float) Math.PI / 3;

            try (InputStream imageStream = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg"))
            {
                Image image = Image.getInstance(IOUtils.toByteArray(imageStream));

                // Draw image at x,y without rotation
                contentByte.addImage(image, image.getWidth(), 0, 0, image.getHeight(), x, y);

                // Draw image as if the previous image was rotated around its center
                // Image starts out being 1x1 with origin in lower left
                // Move origin to center of image
                AffineTransform A = AffineTransform.getTranslateInstance(-0.5, -0.5);
                // Stretch it to its dimensions
                AffineTransform B = AffineTransform.getScaleInstance(image.getWidth(), image.getHeight());
                // Rotate it
                AffineTransform C = AffineTransform.getRotateInstance(rotate);
                // Move it to have the same center as above
                AffineTransform D = AffineTransform.getTranslateInstance(x + image.getWidth()/2, y + image.getHeight()/2);
                // Concatenate
                AffineTransform M = (AffineTransform) A.clone();
                M.preConcatenate(B);
                M.preConcatenate(C);
                M.preConcatenate(D);
                //Draw
                contentByte.addImage(image, M);
            }

            document.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/39364197/how-to-rotate-around-the-image-center-by-itext">
     * How to rotate around the image center by itext?
     * </a>
     * <p>
     * This test draws an image at given coordinates without rotation and then again
     * as if that image was flipped and rotated around its center by some angle.
     * </p>
     */
    @Test
    public void testAddRotatedFlippedImage() throws IOException, DocumentException
    {
        try (   FileOutputStream stream = new FileOutputStream(new File(RESULT_FOLDER, "rotatedFlippedImage.pdf"))    )
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, stream);
            document.open();

            PdfContentByte contentByte = writer.getDirectContent();

            int x = 200;
            int y = 300;
            float rotate = (float) Math.PI / 3;

            try (InputStream imageStream = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg"))
            {
                Image image = Image.getInstance(IOUtils.toByteArray(imageStream));

                // Draw image at x,y without rotation
                contentByte.addImage(image, image.getWidth(), 0, 0, image.getHeight(), x, y);

                // Draw image as if the previous image was flipped and rotated around its center
                // Image starts out being 1x1 with origin in lower left
                // Move origin to center of image
                AffineTransform A = AffineTransform.getTranslateInstance(-0.5, -0.5);
                // Flip it horizontally
                AffineTransform B = new AffineTransform(-1, 0, 0, 1, 0, 0);
                // Stretch it to its dimensions
                AffineTransform C = AffineTransform.getScaleInstance(image.getWidth(), image.getHeight());
                // Rotate it
                AffineTransform D = AffineTransform.getRotateInstance(rotate);
                // Move it to have the same center as above
                AffineTransform E = AffineTransform.getTranslateInstance(x + image.getWidth()/2, y + image.getHeight()/2);
                // Concatenate
                AffineTransform M = (AffineTransform) A.clone();
                M.preConcatenate(B);
                M.preConcatenate(C);
                M.preConcatenate(D);
                M.preConcatenate(E);
                //Draw
                contentByte.addImage(image, M);
            }

            document.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/39364197/how-to-rotate-around-the-image-center-by-itext">
     * How to rotate around the image center by itext?
     * </a>
     * <p>
     * This test draws an image at given coordinates without rotation and then again
     * as if that image was rotated around its center by some angle. This is first done
     * without interpolation and then with interpolation.
     * </p>
     */
    @Test
    public void testAddRotatedInterpolatedImage() throws IOException, DocumentException
    {
        try (   FileOutputStream stream = new FileOutputStream(new File(RESULT_FOLDER, "rotatedInterpolatedImage.pdf"))    )
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, stream);
            document.open();

            PdfContentByte contentByte = writer.getDirectContent();

            int width = 200;
            int height = 200;
            float rotate = (float) Math.PI / 3;

            try (InputStream imageStream = getClass().getResourceAsStream("2x2colored.png"))
            {
                Image image = Image.getInstance(IOUtils.toByteArray(imageStream));

                addRotatedImage(contentByte, image, 200, 500, width, height, rotate);
            }

            try (InputStream imageStream = getClass().getResourceAsStream("2x2colored.png"))
            {
                Image image = Image.getInstance(IOUtils.toByteArray(imageStream));
                image.setInterpolation(true);

                addRotatedImage(contentByte, image, 200, 100, width, height, rotate);
            }

            document.close();
        }
    }

    void addRotatedImage(PdfContentByte contentByte, Image image, float x, float y, float width, float height, float rotation) throws DocumentException
    {
        // Draw image at x,y without rotation
        contentByte.addImage(image, width, 0, 0, height, x, y);

        // Draw image as if the previous image was rotated around its center
        // Image starts out being 1x1 with origin in lower left
        // Move origin to center of image
        AffineTransform A = AffineTransform.getTranslateInstance(-0.5, -0.5);
        // Stretch it to its dimensions
        AffineTransform B = AffineTransform.getScaleInstance(width, height);
        // Rotate it
        AffineTransform C = AffineTransform.getRotateInstance(rotation);
        // Move it to have the same center as above
        AffineTransform D = AffineTransform.getTranslateInstance(x + width/2, y + height/2);
        // Concatenate
        AffineTransform M = (AffineTransform) A.clone();
        M.preConcatenate(B);
        M.preConcatenate(C);
        M.preConcatenate(D);
        //Draw
        contentByte.addImage(image, M);
    }
}
