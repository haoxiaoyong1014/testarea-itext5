package mkl.testarea.itext5.meta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class CreatePortableCollection
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "meta");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/46642994/how-to-create-pdf-package-using-pdfbox">
     * How to create pdf package using PdfBox?
     * </a>
     * <p>
     * This test executes the OP's code to determine the changes applied by
     * it to the PDF.
     * </p>
     */
    @Test
    public void test() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/test.pdf");
                OutputStream out = new FileOutputStream(new File(RESULT_FOLDER, "test-collection.pdf"))) {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(pdfReader, out);
            stamper.makePackage(PdfName.T);
            stamper.close();
        }
    }

}
