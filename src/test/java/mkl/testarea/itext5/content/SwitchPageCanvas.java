package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class SwitchPageCanvas
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34394199/i-cant-rotate-my-page-from-existing-pdf">
     * I can't rotate my page from existing PDF
     * </a>
     * <p>
     * Switching between portrait and landscape like this obviously will cut off some parts of the page.
     * </p>
     */
    @Test
    public void testSwitchOrientation() throws DocumentException, IOException
    {
        try (InputStream resourceStream = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/n2013.00849449.pdf"))
        {
            PdfReader reader = new PdfReader(resourceStream);
            int n = reader.getNumberOfPages();
            PdfDictionary pageDict;
            for (int i = 1; i <= n; i++) {
                Rectangle rect = reader.getPageSize(i);
                Rectangle crop = reader.getCropBox(i);
                pageDict = reader.getPageN(i);
                pageDict.put(PdfName.MEDIABOX, new PdfArray(new float[] {rect.getBottom(), rect.getLeft(), rect.getTop(), rect.getRight()}));
                pageDict.put(PdfName.CROPBOX, new PdfArray(new float[] {crop.getBottom(), crop.getLeft(), crop.getTop(), crop.getRight()}));
            }
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "n2013.00849449-switch.pdf")));
            stamper.close();
            reader.close();
        }
    }
}
