package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class DynamicTemplate
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35031373/how-to-edit-pdftemplate-width-in-java">
     * How to edit PdfTemplate width (in Java)
     * </a>
     * <p>
     * This test demonstrates that what the OP wants indeed works.
     * </p>
     */
    @Test
    public void testChangingTemplateWidths() throws IOException, DocumentException
    {
        try (   FileOutputStream stream = new FileOutputStream(new File(RESULT_FOLDER, "dynamicTemplateWidths.pdf"))    )
        {
            Document document = new Document(PageSize.A6);
            PdfWriter writer = PdfWriter.getInstance(document, stream);
            writer.setPageEvent(new PdfPageEventHelper()
            {
                PdfTemplate dynamicTemplate = null;
                Font font = new Font(BaseFont.createFont(), 12);
                String postfix = "0123456789";

                @Override
                public void onOpenDocument(PdfWriter writer, Document document)
                {
                    super.onOpenDocument(writer, document);
                    dynamicTemplate = writer.getDirectContent().createTemplate(10, 20);
                }

                @Override
                public void onEndPage(PdfWriter writer, Document document)
                {
                    writer.getDirectContent().addTemplate(dynamicTemplate, 100, 300);
                }

                @Override
                public void onCloseDocument(PdfWriter writer, Document document)
                {
                    float widthPoint = font.getBaseFont().getWidthPoint(postfix, font.getSize());
                    dynamicTemplate.setWidth(widthPoint / 2.0f);
                    ColumnText.showTextAligned(dynamicTemplate, Element.ALIGN_LEFT, new Phrase(String.valueOf(writer.getPageNumber()) + "-" + postfix), 0, 1, 0);
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
