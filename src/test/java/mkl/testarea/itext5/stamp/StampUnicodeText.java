package mkl.testarea.itext5.stamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class StampUnicodeText
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "stamp");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35082653/adobe-reader-cant-display-unicode-font-of-pdf-added-with-itext">
     * Adobe Reader can't display unicode font of pdf added with iText
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/erkv9wot9d460dg/sampleOriginal.pdf?dl=0">
     * sampleOriginal.pdf
     * </a>
     * <p>
     * Indeed, just like in the iTextSharp version of the code, the resulting file has
     * issues in Adobe Reader. With a different starting file, though, it doesn't, cf.
     * {@link #testAddUnicodeStampEg_01()}.
     * </p>
     * <p>
     * As it eventually turns out, Adobe Reader treats PDF files with composite fonts
     * differently if they claim to be PDF-1.2 like the OP's sample file.
     * </p>
     */
    @Test
    public void testAddUnicodeStampSampleOriginal() throws DocumentException, IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("sampleOriginal.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "sampleOriginal-unicodeStamp.pdf"))  )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);
            BaseFont bf = BaseFont.createFont("c:/windows/fonts/arialuni.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            PdfContentByte cb = stamper.getOverContent(1);

            Phrase p = new Phrase();
            p.setFont(new Font(bf, 25, Font.NORMAL, BaseColor.BLUE));
            p.add("Sample Text");

            ColumnText.showTextAligned(cb, PdfContentByte.ALIGN_LEFT, p, 200, 200, 0);
            
            stamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/35082653/adobe-reader-cant-display-unicode-font-of-pdf-added-with-itext">
     * Adobe Reader can't display unicode font of pdf added with iText
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/erkv9wot9d460dg/sampleOriginal.pdf?dl=0">
     * sampleOriginal.pdf
     * </a>
     * <p>
     * Indeed, just like in the iTextSharp version of the code, the resulting file has
     * issues in Adobe Reader, cf. {@link #testAddUnicodeStampSampleOriginal()}. With
     * a different starting file, though, it doesn't as this test shows.
     * </p>
     * <p>
     * As it eventually turns out, Adobe Reader treats PDF files with composite fonts
     * differently if they claim to be PDF-1.2 like the OP's sample file.
     * </p>
     */
    @Test
    public void testAddUnicodeStampEg_01() throws DocumentException, IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("eg_01.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "eg_01-unicodeStamp.pdf"))  )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);

            BaseFont bf = BaseFont.createFont("c:/windows/fonts/arialuni.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            PdfContentByte cb = stamper.getOverContent(1);

            Phrase p = new Phrase();
            p.setFont(new Font(bf, 25, Font.NORMAL, BaseColor.BLUE));
            p.add("Sample Text");

            ColumnText.showTextAligned(cb, PdfContentByte.ALIGN_LEFT, p, 200, 200, 0);
            
            stamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/35082653/adobe-reader-cant-display-unicode-font-of-pdf-added-with-itext">
     * Adobe Reader can't display unicode font of pdf added with iText
     * </a>
     * <p>
     * Indeed, just like in the iTextSharp version of the code, the resulting file has
     * issues in Adobe Reader, cf. {@link #testAddUnicodeStampSampleOriginal()}. With
     * a different starting file, though, it doesn't, cf.
     * {@link #testAddUnicodeStampEg_01()}. This test creates a new PDF with the same
     * font and chunk as stamped by the OP. Adobe Reader has no problem with it either.
     * </p>
     * <p>
     * As it eventually turns out, Adobe Reader treats PDF files with composite fonts
     * differently if they claim to be PDF-1.2 like the OP's sample file.
     * </p>
     */
    @Test
    public void testCreateUnicodePdf() throws DocumentException, IOException
    {
        Document document = new Document();
        try (   OutputStream result  = new FileOutputStream(new File(RESULT_FOLDER, "unicodePdf.pdf")) )
        {
            PdfWriter.getInstance(document, result);
            BaseFont bf = BaseFont.createFont("c:/windows/fonts/arialuni.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            document.open();
            
            Phrase p = new Phrase();
            p.setFont(new Font(bf, 25, Font.NORMAL, BaseColor.BLUE));
            p.add("Sample Text");

            document.add(p);
            
            document.close();
        }
    }
}
