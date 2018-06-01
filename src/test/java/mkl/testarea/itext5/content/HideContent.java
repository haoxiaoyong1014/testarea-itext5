package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class HideContent
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/43870545/filling-a-pdf-with-itextsharp-and-then-hiding-the-base-layer">
     * Filling a PDF with iTextsharp and then hiding the base layer
     * </a>
     * <p>
     * This test shows how to cover all content using a white rectangle.
     * </p>
     */
    @Test
    public void testHideContenUnderRectangle() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("document.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "document-hiddenContent.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            for (int page = 1; page <= pdfReader.getNumberOfPages(); page++)
            {
                Rectangle pageSize = pdfReader.getPageSize(page);
                PdfContentByte canvas = pdfStamper.getOverContent(page);
                canvas.setColorFill(BaseColor.WHITE);
                canvas.rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(), pageSize.getHeight());
                canvas.fill();
            }
            pdfStamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/43870545/filling-a-pdf-with-itextsharp-and-then-hiding-the-base-layer">
     * Filling a PDF with iTextsharp and then hiding the base layer
     * </a>
     * <p>
     * This test shows how to remove all content.
     * </p>
     */
    @Test
    public void testRemoveContent() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("document.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "document-removedContent.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            for (int page = 1; page <= pdfReader.getNumberOfPages(); page++)
            {
                PdfDictionary pageDictionary = pdfReader.getPageN(page);
                pageDictionary.remove(PdfName.CONTENTS);
            }
            new PdfStamper(pdfReader, result).close();
        }
    }
}
