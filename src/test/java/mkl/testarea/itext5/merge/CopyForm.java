package mkl.testarea.itext5.merge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

/**
 * This test deals with copying forms.
 * 
 * @author mkl
 */
public class CopyForm
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/32710839/itextsharp-pdfcopy-makes-read-only-fields-editable">
     * iTextSharp PdfCopy makes read-only fields editable
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/nhy7av9b37uwowl/in1.pdf?dl=0">
     * in1.pdf
     * </a>
     * <p>
     * Indeed, the issue can be reproduced. A possible explanation in a SO answer.
     * </p> 
     */
    @Test
    public void testCopyReadOnlyFields() throws IOException, DocumentException
    {
        Document document = new Document();
        
        try (   OutputStream fileStream = new FileOutputStream(new File(RESULT_FOLDER, "in1Copy.pdf"));
                InputStream resource = getClass().getResourceAsStream("in1.pdf")    )
        {
            PdfCopy copier = new PdfCopy(document, fileStream);
            PdfReader reader = new PdfReader(resource);

            copier.setMergeFields();
            document.open();
            copier.addDocument(reader);
            copier.addJavaScript(reader.getJavaScript());
            document.close();
        }
    }
}
