// $Id$
package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mklink
 *
 */
public class MarkAnnotationReadOnly
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/37275267/how-to-make-pdf-annotation-as-read-only-using-itext">
     * how to make pdf annotation as read only using itext?
     * </a>
     * <br/>
     * test-annotated.pdf <i>simple PDF with sticky note</i>
     * 
     * <p>
     * This test shows how to set the read-only flags of all annotations of a document.
     * </p>
     */
    @Test
    public void testMarkAnnotationsReadOnly() throws IOException, DocumentException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("test-annotated.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "test-annotated-ro.pdf"))    )
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            for (int page = 1; page <= reader.getNumberOfPages(); page++)
            {
                PdfDictionary pageDictionary = reader.getPageN(page);
                PdfArray annotationArray = pageDictionary.getAsArray(PdfName.ANNOTS);
                if (annotationArray == null)
                    continue;
                for (PdfObject object : annotationArray)
                {
                    PdfObject directObject = PdfReader.getPdfObject(object);
                    if (directObject instanceof PdfDictionary)
                    {
                        PdfDictionary annotationDictionary = (PdfDictionary) directObject;
                        PdfNumber flagsNumber = annotationDictionary.getAsNumber(PdfName.F);
                        int flags = flagsNumber != null ? flagsNumber.intValue() : 0;
                        flags |= PdfAnnotation.FLAGS_READONLY;
                        annotationDictionary.put(PdfName.F, new PdfNumber(flags));
                    }
                }
            }

            stamper.close();
        }
    }
}
