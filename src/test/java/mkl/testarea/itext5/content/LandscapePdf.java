package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mklink
 */
public class LandscapePdf {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/47209312/i-have-a-trouble-with-lowagie-pdf-and-report-making-i-cant-include-headerfooter">
     * I have a trouble with lowagie pdf and Report Making. I cant include headerfooter on the first page
     * </a>
     * <p>
     * This example shows how to generate a PDF with page level
     * settings (page size, page margins) customized already on
     * the first page.
     * </p>
     */
    @Test
    public void testCreateLandscapeDocument() throws FileNotFoundException, DocumentException {
        Document document = new Document();

        PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "landscape.pdf")));

        document.setPageSize(PageSize.A4.rotate());
        document.setMargins(60, 30, 30, 30);
        document.open();
        document.add(new Paragraph("Test string for a landscape PDF."));
        document.close();
    }

}
