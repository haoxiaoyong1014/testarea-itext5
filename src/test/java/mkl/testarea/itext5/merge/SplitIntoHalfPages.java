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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;

/**
 * <a href="http://stackoverflow.com/questions/32453489/itext-split-pdf-vertically">
 * itext Split PDF Vertically
 * </a>
 * <p>
 * The method {@link #splitIntoHalfPages(InputStream, File)} splits pages
 * in the source document along a vertical center line.
 * </p>
 * 
 * @author mkl
 */
public class SplitIntoHalfPages
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * This method splits the Body.pdf resource.
     */
    @Test
    public void testBody() throws IOException, DocumentException
    {
        try ( InputStream resource = getClass().getResourceAsStream("Body.pdf")    )
        {
            splitIntoHalfPages(resource, new File(RESULT_FOLDER, "Body-byHalf.pdf"));
        }
    }

    /**
     * This method splits the input.pdf resource. The effect introduced by the
     * typewriter annotations (containing the page numbers) is interesting.
     */
    @Test
    public void testInput() throws IOException, DocumentException
    {
        try ( InputStream resource = getClass().getResourceAsStream("input.pdf")    )
        {
            splitIntoHalfPages(resource, new File(RESULT_FOLDER, "input-byHalf.pdf"));
        }
    }

    /**
     * This methods creates a copy of the source document containing each page twice,
     * once with the cropbox limited to the left half page, once to the right one.
     */
    void splitIntoHalfPages(InputStream source, File target) throws IOException, DocumentException
    {
        final PdfReader reader = new PdfReader(source);
        
        try (   OutputStream targetStream = new FileOutputStream(target)    )
        {
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, targetStream);
            document.open();

            for (int page = 1; page <= reader.getNumberOfPages(); page++)
            {
                PdfDictionary pageN = reader.getPageN(page);
                Rectangle cropBox = reader.getCropBox(page);
                PdfArray leftBox = new PdfArray(new float[]{cropBox.getLeft(), cropBox.getBottom(), (cropBox.getLeft() + cropBox.getRight()) / 2.0f, cropBox.getTop()});
                PdfArray rightBox = new PdfArray(new float[]{(cropBox.getLeft() + cropBox.getRight()) / 2.0f, cropBox.getBottom(), cropBox.getRight(), cropBox.getTop()});

                PdfImportedPage importedPage = copy.getImportedPage(reader, page);
                pageN.put(PdfName.CROPBOX, leftBox);
                copy.addPage(importedPage);
                pageN.put(PdfName.CROPBOX, rightBox);
                copy.addPage(importedPage);
            }
            
            document.close();
        }
        finally
        {
            reader.close();
        }
    }
}
