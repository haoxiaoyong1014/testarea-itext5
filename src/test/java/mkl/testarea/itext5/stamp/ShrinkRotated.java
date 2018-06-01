package mkl.testarea.itext5.stamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class ShrinkRotated {
    final static File RESULT_FOLDER = new File("target/test-outputs", "stamp");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/48601115/pdf-shrink-causing-change-in-orientation">
     * PDF Shrink causing change in Orientation
     * </a>
     * <p>
     * Sample.PDF - retrieved via e-mail
     * </p>
     * <p>
     * This issue is just another result of iText adding rotations to the
     * transformation matrices at the start of undercontent and overcontent
     * of rotated pages: Due to the nature of the literal the OP adds to
     * the undercontent, this rotation bleeds through and also affects the
     * original content.
     * </p>
     */
    @Test
    public void testShrinkSample() throws IOException, DocumentException {
        float xPercentage = 0.3F;
        float yPercentage = 0.6F;
        try (   InputStream resource = getClass().getResourceAsStream("Sample.PDF");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "Sample-shrunken.PDF"))) {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);
            //vvv
            stamper.setRotateContents(false);
            //^^^
            for (int p = 1; p <= 1; p++) {
                float offsetX = (reader.getPageSize(p).getWidth() * (1 - xPercentage)) / 2;
                float offsetY = (reader.getPageSize(p).getHeight() * (1 - yPercentage)) / 2;
                PdfDictionary page;
                PdfArray crop;
                PdfArray media;
                page = reader.getPageN(p);
                System.out.println("reader.getPateRoatation-->"+reader.getPageRotation(p));
                media = page.getAsArray(PdfName.CROPBOX);
                if (media == null) {
                    media = page.getAsArray(PdfName.MEDIABOX);
                }
                crop = new PdfArray();
                crop.add(new PdfNumber(0));
                crop.add(new PdfNumber(0));
                crop.add(new PdfNumber(media.getAsNumber(2).floatValue()));
                crop.add(new PdfNumber(media.getAsNumber(3).floatValue()));
                page.put(PdfName.MEDIABOX, crop);
                page.put(PdfName.CROPBOX, crop);
                Rectangle mediabox = reader.getPageSize(p);
                stamper.getUnderContent(p).setLiteral(
                        String.format("\nq %s %s %s %s %s %s cm\nq\n",
                        xPercentage, mediabox.getLeft(),mediabox.getBottom(), yPercentage,  offsetX, offsetY));
                stamper.getOverContent(p).setLiteral("\nQ\nQ\n");           
            }
            stamper.close();
            reader.close();
        }
    }

}
