package mkl.testarea.itext5.stamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * <a href="http://stackoverflow.com/questions/29613776/content-byte-has-strange-colour-behaviour-with-5-5-5-jar">
 * content byte has strange colour behaviour with 5.5.5 jar
 * </a>
 * <br\>
 * <a href="https://www.dropbox.com/s/befn0eu6pe58paz/eg_01.pdf?dl=0">eg_01.pdf</a>
 * <br\>
 * <a href="https://www.dropbox.com/s/bfbtvh8t1sgoub1/eg_01B.pdf?dl=0">eg_01B.pdf</a>
 * <p>
 * Stamping text color sometimes is ignored.
 * </p>
 * <p>
 * As it turns out, it is ignored for tagged files. The cause has been explained in 
 * <a href="http://stackoverflow.com/a/29094269/1729265">this answer</a>. There also
 * is a work-around used here in {@link #stampTextChanged(InputStream, OutputStream)}.
 * </p>
 * 
 * @author mkl
 */
public class StampColoredText
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "stamp");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testOrigEg_01() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("eg_01.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "eg_01-orig_stamp.pdf")))
        {
            stampTextOriginal(resource, result);
        }
    }

    @Test
    public void testOrigEg_01B() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("eg_01B.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "eg_01B-orig_stamp.pdf")))
        {
            stampTextOriginal(resource, result);
        }
    }

    @Test
    public void testChangedEg_01() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("eg_01.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "eg_01-changed_stamp.pdf")))
        {
            stampTextChanged(resource, result);
        }
    }

    @Test
    public void testChangedEg_01B() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("eg_01B.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "eg_01B-changed_stamp.pdf")))
        {
            stampTextChanged(resource, result);
        }
    }

    /**
     * The OP's original code transformed into Java
     */
    void stampTextOriginal(InputStream source, OutputStream target) throws DocumentException, IOException
    {
        Date today = new Date();
        PdfReader reader = new PdfReader(source);
        PdfStamper stamper = new PdfStamper(reader, target);
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
        int tSize = 24;
        String mark = "DRAFT " + today;
        int angle = 45;
        float height = reader.getPageSizeWithRotation(1).getHeight()/2;
        float width = reader.getPageSizeWithRotation(1).getWidth()/2;
        PdfContentByte cb = stamper.getOverContent(1);
        cb.setColorFill(new BaseColor(255,200,200));
        cb.setFontAndSize(bf, tSize);
        cb.beginText();
        cb.showTextAligned(Element.ALIGN_CENTER, mark, width, height, angle);
        cb.endText();
        stamper.close();
        reader.close();
    }

    /**
     * The OP's code transformed into Java changed with the work-around.
     */
    void stampTextChanged(InputStream source, OutputStream target) throws DocumentException, IOException
    {
        Date today = new Date();
        PdfReader reader = new PdfReader(source);
        PdfStamper stamper = new PdfStamper(reader, target);
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
        int tSize = 24;
        String mark = "DRAFT " + today;
        int angle = 45;
        float height = reader.getPageSizeWithRotation(1).getHeight()/2;
        float width = reader.getPageSizeWithRotation(1).getWidth()/2;
        PdfContentByte cb = stamper.getOverContent(1);
        cb.setFontAndSize(bf, tSize);
        cb.beginText();
        cb.setColorFill(new BaseColor(255,200,200));
        cb.showTextAligned(Element.ALIGN_CENTER, mark, width, height, angle);
        cb.endText();
        stamper.close();
        reader.close();
    }
}
