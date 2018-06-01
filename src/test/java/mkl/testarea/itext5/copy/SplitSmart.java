package mkl.testarea.itext5.copy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;

/**
 * @author mkl
 */
public class SplitSmart
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "copy");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/39348420/remain-only-used-font-subsets-while-splitting-pdf-in-java">
     * Remain only used font subsets while splitting PDF in Java
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/mqgozcbs7k4m40z/cjk-with-fonts.pdf?dl=0">
     * cjk-with-fonts.pdf
     * </a>
     * <p>
     * Indeed, splitting results in files the summed-up size of which is considerably larger
     * than the original size. The reason are no unnecessary unused entries in the resource
     * dictionaries, there simply are merely a handful of fonts in the file and each page
     * uses 3 or 4 of them. In the split PDFs, therefore, most fonts are copied to multiple
     * files which makes the sum of the sizes of the split files so much larger than the
     * size of the original file.
     * </p>
     * <p>
     * One would have to implement a preprocessor which would walk the pages, xobjects, etc.,
     * determine for each of them the actually used glyphs from each font, create subset fonts
     * and replace the currently associated fonts by those subset fonts. This is not easy,
     * most probably beyond the scope of a stackoverflow answer, but should be possible using
     * the building bricks iText offers.
     * </p>
     */
    @Test
    public void testSplitLikeNathanielDing() throws IOException, DocumentException
    {
        try ( InputStream resource = getClass().getResourceAsStream("cjk-with-fonts.pdf") )
        {
            byte[] resourceBytes = IOUtils.toByteArray(resource);
            List<byte[]> splitBytesList = split(resourceBytes);
            for (int i = 0; i < splitBytesList.size(); i++)
                Files.write(new File(RESULT_FOLDER, String.format("cjk-with-fonts-%s.pdf", i)).toPath(), splitBytesList.get(i));
        }
    }

    public List<byte[]> split(byte[] input) throws IOException, DocumentException {
        PdfReader pdfReader = new PdfReader(input);
        List<byte[]> pdfFiles = new ArrayList<>();
        int pageCount = pdfReader.getNumberOfPages();
        int pageIndex = 0;
        while (++pageIndex <= pageCount) {
            Document document = new Document(pdfReader.getPageSizeWithRotation(pageIndex));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfCopy pdfCopy = new PdfSmartCopy(document, byteArrayOutputStream);
            pdfCopy.setFullCompression();
            PdfImportedPage pdfImportedPage = pdfCopy.getImportedPage(pdfReader, pageIndex);
            document.open();
            pdfCopy.addPage(pdfImportedPage);
            document.close();
            pdfCopy.close();
            pdfFiles.add(byteArrayOutputStream.toByteArray());
        }
        return pdfFiles;
    }
}
