/**
 * 
 */
package mkl.testarea.itext5.layer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfLayer;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * Stamping layered files.
 * 
 * @author mkl
 */
public class StampInLayer
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "layer");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/31507132/itextsharp-form-gets-flattened-even-when-the-formflattening-property-is-false">
     * Itextsharp form gets flattened even when the FormFlattening property is false
     * </a>
     * <p>
     * Reproducing the issue without work-around.
     * </p>
     */
    @Test
    public void testStampInLayerOnLayeredBug() throws IOException, DocumentException
    {
        try (   InputStream source = getClass().getResourceAsStream("House_Plan_Final.pdf");
                InputStream image = getClass().getResourceAsStream("Willi-1.jpg"))
        {
            Image iImage = Image.getInstance(StreamUtil.inputStreamToArray(image));
            byte[] result = stampLayer(source, iImage, 100, 100, "Logos", false);
            Files.write(new File(RESULT_FOLDER, "House_Plan_Final-stamped-bug.pdf").toPath(), result);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/31507132/itextsharp-form-gets-flattened-even-when-the-formflattening-property-is-false">
     * Itextsharp form gets flattened even when the FormFlattening property is false
     * </a>
     * <p>
     * Reproducing the issue with work-around.
     * </p>
     */
    @Test
    public void testStampInLayerOnLayeredWorkAround() throws IOException, DocumentException
    {
        try (   InputStream source = getClass().getResourceAsStream("House_Plan_Final.pdf");
                InputStream image = getClass().getResourceAsStream("Willi-1.jpg"))
        {
            Image iImage = Image.getInstance(StreamUtil.inputStreamToArray(image));
            byte[] result = stampLayer(source, iImage, 100, 100, "Logos", true);
            Files.write(new File(RESULT_FOLDER, "House_Plan_Final-stamped-workAround.pdf").toPath(), result);
        }
    }

    public static byte[] stampLayer(InputStream _pdfFile, Image iImage, int x, int y, String layername, boolean readLayers) throws IOException, DocumentException
    {
        PdfReader reader = new PdfReader(_pdfFile);

        try (   ByteArrayOutputStream ms = new ByteArrayOutputStream()  )
        {
            PdfStamper stamper = new PdfStamper(reader, ms);
            //Don't delete otherwise the stamper flattens the layers
            if (readLayers)
                stamper.getPdfLayers();

            PdfLayer logoLayer = new PdfLayer(layername, stamper.getWriter());
            PdfContentByte cb = stamper.getUnderContent(1);
            cb.beginLayer(logoLayer);

            //300dpi
            iImage.scalePercent(24f);
            iImage.setAbsolutePosition(x, y);
            cb.addImage(iImage);

            cb.endLayer();
            stamper.close();

            return (ms.toByteArray());
        }
    }
}
