package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <a href="http://stackoverflow.com/questions/34522776/java-pdf-creation-using-itext">
 * Java PDF creation using iTEXT
 * </a>
 * 
 * <p>
 * This class tests the page event listener {@link ParagraphBackground} which draws
 * uninterrupted paragraph backgrounds.
 * </p>
 * 
 * @author mkl
 */
public class ColorParagraphBackground
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testParagraphBackgroundEventListener() throws DocumentException, FileNotFoundException
    {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "document-with-paragraph-backgrounds.pdf")));
        ParagraphBackground border = new ParagraphBackground();
        writer.setPageEvent(border);
        document.open();
        document.add(new Paragraph("Hello,"));
        document.add(new Paragraph("In this document, we'll add several paragraphs that will trigger page events. As long as the event isn't activated, nothing special happens, but let's make the event active and see what happens:"));
        border.setActive(true);
        document.add(new Paragraph("This paragraph now has a background. Isn't that fantastic? By changing the event, we can even draw a border, change the line width of the border and many other things. Now let's deactivate the event."));
        border.setActive(false);
        document.add(new Paragraph("This paragraph no longer has a background."));
        document.close();
    }

}
