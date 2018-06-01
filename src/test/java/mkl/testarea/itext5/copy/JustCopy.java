package mkl.testarea.itext5.copy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class JustCopy {
    final static File RESULT_FOLDER = new File("target/test-outputs", "copy");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/48586750/itext-messes-up-pdf-while-joining-multiple-pdfs">
     * iText messes up PDF while joining multiple PDFs
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1nH_21_f1bmnxeOn5ul8db3eC2CggZiRN">
     * page_444.pdf
     * </a>
     * <p>
     * I cannot reproduce the issue.
     * </p>
     */
    @Test
    public void testPage444() throws IOException, DocumentException {
        Rectangle pageSize = PageSize.A4;
        File outPut = new File(RESULT_FOLDER, "page_444-copied.pdf");

        final Document document = new Document(pageSize );
        final FileOutputStream fos = FileUtils.openOutputStream(outPut);
        final PdfWriter pdfWriter = new PdfSmartCopy(document, fos);

        pdfWriter.setViewerPreferences(PdfWriter.PageLayoutTwoColumnRight);
        pdfWriter.setFullCompression();
        pdfWriter.setPdfVersion(PdfWriter.VERSION_1_6);
//        pdfWriter.setXmpMetadata(getPdfMetaData());

        document.open();
        document.addAuthor("Author");

        final PdfReader reader = new PdfReader(getClass().getResourceAsStream("page_444.pdf"));
        PdfSmartCopy pdfSmartCopy = (PdfSmartCopy) pdfWriter;
        pdfSmartCopy.addPage(pdfSmartCopy.getImportedPage(reader, 1));
        pdfSmartCopy.freeReader(reader);

        // After all files are merged
        document.close();
    }

}
