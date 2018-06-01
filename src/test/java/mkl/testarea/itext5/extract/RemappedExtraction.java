package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * <a href="http://stackoverflow.com/questions/33500819/itextsharp-pdfbox-text-extract-fails-for-certain-pdfs">
 * ITextSharp / PDFBox text extract fails for certain pdfs
 * </a>
 * <br/>
 * <a href="https://www.dropbox.com/s/8x5lnvmw6mv8ko8/Vol16_2.pdf?dl=0">
 * Vol16_2.pdf
 * </a>
 * <p>
 * This test tests the evil {@link TextExtractionStrategy} wrapper
 * {@link RemappingExtractionFilter} which replaces the text in a
 * {@link TextRenderInfo} instance by mapping it using the Differences
 * of the Encoding of the font assuming the differences to contain a starting
 * 1 only followed by names all of which are built as /Gxx, xx being the
 * hexadecimal representation of the ASCII code (as Unicode subset) of the
 * glyph rendered.
 * </p>
 * <p>
 * It is only useful for documents like the one presented by the OP.
 * </p>
 * 
 * @author mkl
 */
public class RemappedExtraction
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testVol16_2() throws IOException, DocumentException, NoSuchFieldException, SecurityException
    {
        InputStream resourceStream = getClass().getResourceAsStream("Vol16_2.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStoreRemapped(reader, new File(RESULT_FOLDER, "Vol16_2.%s.txt").toString());

            System.out.println("\nText Vol16_2.pdf\n************************");
            System.out.println(content);
            System.out.println("************************");
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    String extractAndStoreRemapped(PdfReader reader, String format) throws IOException, NoSuchFieldException, SecurityException
    {
        StringBuilder builder = new StringBuilder();

        for (int page = 1; page <= reader.getNumberOfPages(); page++)
        {
            String pageText = extractRemapped(reader, page);
            Files.write(Paths.get(String.format(format, page)), pageText.getBytes("UTF8"));

            if (page > 1)
                builder.append("\n\n");
            builder.append(pageText);
        }

        return builder.toString();
    }

    String extractRemapped(PdfReader reader, int pageNo) throws IOException, NoSuchFieldException, SecurityException
    {
        TextExtractionStrategy strategy = new RemappingExtractionFilter(new LocationTextExtractionStrategy());
        return PdfTextExtractor.getTextFromPage(reader, pageNo, strategy);
    }
}
