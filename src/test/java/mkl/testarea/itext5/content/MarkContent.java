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
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class MarkContent {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/50121297/missing-colored-area-on-pdf-using-itext-pdf">
     * Missing colored area on pdf using itext pdf
     * </a>
     * <p>
     * This test shows how to mark a whole table row without the
     * marking hiding the existing content or vice versa.
     * </p>
     */
    @Test
    public void test() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("document.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "document-marked.pdf"))) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(pdfReader, result);

            PdfContentByte canvas = stamper.getOverContent(1);
            canvas.saveState();
            PdfGState state = new PdfGState();
            state.setBlendMode(new PdfName("Multiply"));
            canvas.setGState(state);
            canvas.setColorFill(BaseColor.YELLOW);
            canvas.rectangle(60, 586, 477, 24);
            canvas.fill();
            canvas.restoreState();

            stamper.close();
        }
    }

}
