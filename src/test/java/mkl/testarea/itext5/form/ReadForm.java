package mkl.testarea.itext5.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.PdfReader;

/**
 * The class tests form parsing functionality.
 * 
 * @author mkl
 */
public class ReadForm
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/32455178/itextsharp-acrofields-unable-to-cast-object-of-type-itextsharp-text-pdf-pdf">
     * iTextsharp : AcroFields - Unable to cast object of type 'iTextSharp.text.pdf.PdfDictionary' to type 'iTextSharp.text.pdf.PdfArray'
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B3W8aJry8ZMERnJubHpMdVk5SmM/view?usp=sharing">
     * Sample.PDF
     * </a>
     * <p>
     * Indeed, parsing the form definition results in a class cast error. The cause for
     * that exception is that the AcroForm PDF form description in your sample PDF is
     * invalid: In the AcroForm interactive form dictionary the value of the Fields key
     * is a dictionary object but that value must be an array.
     * </p>
     */
    @Test
    public void test() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Sample.PDF") )
        {
            PdfReader reader = new PdfReader(resource);
            reader.getAcroFields();
        }
    }

}
