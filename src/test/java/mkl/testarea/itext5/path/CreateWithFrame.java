package mkl.testarea.itext5.path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <a href="http://stackoverflow.com/questions/30017620/how-to-add-background-color-for-border-of-itextsharp-pdf-page">
 * how to add background color for border of itextsharp pdf page
 * </a>
 * <p>
 * One can color the "outside" of some path by adding a page-sized rectangle around it and using the appropriate `fill`
 * method, cf {@link #testCreateFramedDocumentEoFill()} and {@link #testCreateFramedDocumentFill()}. 
 * </p<
 * 
 * @author mkl
 */
public class CreateWithFrame
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "path");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testCreateFramedDocumentEoFill() throws FileNotFoundException, DocumentException
    {
        Document document = new Document();
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "framedEoFill.pdf")));
        pdfWriter.setPageEvent(new PdfPageEventHelper()
        {
            public void onEndPage(PdfWriter writer, Document document)
            {
                super.onEndPage(writer, document);
                PdfContentByte content = writer.getDirectContent();
                content.setColorFill(BaseColor.BLACK);
                content.rectangle(writer.getPageSize().getLeft(), writer.getPageSize().getBottom(), writer.getPageSize().getWidth(), writer.getPageSize().getHeight());
                content.roundRectangle(35f,55f, 520f, 750f ,20f);
                content.eoFill();        
            }    
        });
        document.open();

        document.add(new Paragraph("Some page content goes in here..."));
        document.close();
    }

    @Test
    public void testCreateFramedDocumentFill() throws FileNotFoundException, DocumentException
    {
        Document document = new Document();
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "framedFill.pdf")));
        pdfWriter.setPageEvent(new PdfPageEventHelper()
        {
            public void onEndPage(PdfWriter writer, Document document)
            {
                super.onEndPage(writer, document);
                PdfContentByte content = writer.getDirectContent();
                content.setColorFill(BaseColor.BLACK);
                content.rectangle(writer.getPageSize().getRight(), writer.getPageSize().getBottom(), -writer.getPageSize().getWidth(), writer.getPageSize().getHeight());
                content.roundRectangle(35f,55f, 520f, 750f ,20f);
                content.fill();
            }    
        });
        document.open();

        document.add(new Paragraph("Some page content goes in here..."));
        document.close();
    }

}
