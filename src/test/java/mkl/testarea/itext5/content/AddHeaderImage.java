package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AddHeaderImage
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35035356/itext-add-an-image-on-a-header-with-an-absolute-position">
     * IText : Add an image on a header with an absolute position
     * </a>
     * <p>
     * This test demonstrates how to add an image a a fixed position on a page.
     * </p>
     */
    @Test
    public void testAddHeaderImageFixed() throws IOException, DocumentException
    {
        try (   FileOutputStream stream = new FileOutputStream(new File(RESULT_FOLDER, "headerImage.pdf"))    )
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, stream);
            writer.setPageEvent(new PdfPageEventHelper()
            {
                Image imgSoc = null;

                @Override
                public void onOpenDocument(PdfWriter writer, Document document)
                {
                    try (InputStream imageStream = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg "))
                    {
                        imgSoc = Image.getInstance(IOUtils.toByteArray(imageStream));
                        imgSoc.scaleToFit(110,110);
                        imgSoc.setAbsolutePosition(390, 720);
                    }
                    catch (BadElementException | IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    super.onOpenDocument(writer, document);
                }

                @Override
                public void onEndPage(PdfWriter writer, Document document)
                {
                    try
                    {
                        writer.getDirectContent().addImage(imgSoc);
                    }
                    catch (DocumentException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            document.open();
            
            document.add(new Paragraph("PAGE 1"));
            document.newPage();
            document.add(new Paragraph("PAGE 2"));
            
            document.close();
        }
    }
}
