package mkl.testarea.itext5.xmlworker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * @author mkl
 */
public class AddAnnotation
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "xmlworker");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/41949253/how-to-add-columntext-as-an-annotation-in-itext-pdf">
     * How to add columnText as an annotation in itext pdf
     * </a>
     * <p>
     * This test demonstrates how to use a columntext in combination with an annotation.
     * </p>
     */
    @Test
    public void testAddAnnotationLikeJasonY() throws IOException, DocumentException
    {
        String html ="<html><h1>Header</h1><p>A paragraph</p><p>Another Paragraph</p></html>";
        String css = "h1 {color: red;}";
        ElementList elementsList = XMLWorkerHelper.parseToElementList(html, css);

        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "JasonY.pdf"))   )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);

            Rectangle cropBox = reader.getCropBox(1);

            PdfAnnotation annotation = stamper.getWriter().createAnnotation(cropBox, PdfName.FREETEXT);
            PdfAppearance appearance = PdfAppearance.createAppearance(stamper.getWriter(), cropBox.getWidth(), cropBox.getHeight());

            ColumnText ct = new ColumnText(appearance);
            ct.setSimpleColumn(new Rectangle(cropBox.getWidth(), cropBox.getHeight()));
            elementsList.forEach(element -> ct.addElement(element));
            ct.go();

            annotation.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, appearance);
            stamper.addAnnotation(annotation, 1);

            stamper.close();
            reader.close();
        }
    }
}
