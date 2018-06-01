package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

/**
 * <a href="http://stackoverflow.com/questions/33492792/how-can-i-extract-subscript-superscript-properly-from-a-pdf-using-itextsharp">
 * How can I extract subscript / superscript properly from a PDF using iTextSharp?
 * </a>
 * <br/>
 * <a href="http://www.mass.gov/courts/docs/lawlib/300-399cmr/310cmr7.pdf">310cmr7.pdf</a>
 * <p>
 * This test tests the {@link TextLineFinder} and {@link HorizontalTextExtractionStrategy} /
 * {@link HorizontalTextExtractionStrategy2} with the beginning of the OP's sample document.
 * </p>
 * 
 * @author mkl
 */
public class ExtractSuperAndSubInLine
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * Test with {@link HorizontalTextExtractionStrategy}, works for iText before 5.5.9-SNAPSHOT
     * Commit 53526e4854fcb80c86cbc2e113f7a07401dc9a67 ("Refactor LocationTextExtractionStrategy...").
     */
    @Test
    public void testExtract310cmr7() throws IOException, DocumentException, NoSuchFieldException, SecurityException
    {
        extract("310cmr7.pdf", 1, 21, false);
    }

    /**
     * Test with {@link HorizontalTextExtractionStrategy}, works for iText since 5.5.9-SNAPSHOT
     * Commit 1ab350beae148be2a4bef5e663b3d67a004ff9f8 ("Make TextChunkLocation a Comparable<> class...").
     */
    @Test
    public void testExtract310cmr7V2() throws IOException, DocumentException, NoSuchFieldException, SecurityException
    {
        extract("310cmr7.pdf", 1, 21, true);
    }

    @Test
    public void testDrawLineBoundaries310cmr7() throws IOException, DocumentException
    {
        markLineBoundaries("310cmr7.pdf", 1, 21);
    }

    void markLineBoundaries(String resource, int startPage, int endPage) throws IOException, DocumentException
    {
        String name = new File(resource).getName();
        String target = String.format("%s-lines-%s-%s.pdf", name, startPage, endPage);
        InputStream resourceStream = getClass().getResourceAsStream(resource);
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            System.out.printf("\nLine boundaries in %s\n", name);

            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, target)));
            
            for (int page = startPage; page < endPage; page++)
            {
                System.out.printf("\n   Page %s\n   ", page);
                
                TextLineFinder finder = new TextLineFinder();
                parser.processContent(page, finder);

                PdfContentByte over = stamper.getOverContent(page);
                Rectangle mediaBox = reader.getPageSize(page);
                
                for (float flip: finder.verticalFlips)
                {
                    System.out.printf(" %s", flip);
                    over.moveTo(mediaBox.getLeft(), flip);
                    over.lineTo(mediaBox.getRight(), flip);
                }

                System.out.println();
                over.stroke();
            }

            stamper.close();
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }
    
    void extract(String resource, int startPage, int endPage, boolean isV2) throws IOException, DocumentException, NoSuchFieldException, SecurityException
    {
        String name = new File(resource).getName();
        String target = String.format(isV2 ? "%s-lines-v2-%%s.txt" : "%s-lines-%%s.txt", name);
        InputStream resourceStream = getClass().getResourceAsStream(resource);
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            System.out.printf("\nText by line in %s\n", name);

            for (int page = startPage; page < endPage; page++)
            {
                System.out.printf("\n   Page %s\n", page);

                String pageText = isV2 ? extractV2(reader, page) : extract(reader, page);
                Files.write(new File(RESULT_FOLDER, String.format(target, page)).toPath(), pageText.getBytes("UTF8"));
                System.out.println(pageText);
            }
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    /**
     * Test with {@link HorizontalTextExtractionStrategy}, works for iText before 5.5.9-SNAPSHOT
     * Commit 53526e4854fcb80c86cbc2e113f7a07401dc9a67 ("Refactor LocationTextExtractionStrategy...").
     */
    @SuppressWarnings("deprecation")
    String extract(PdfReader reader, int pageNo) throws IOException, NoSuchFieldException, SecurityException
    {
        return PdfTextExtractor.getTextFromPage(reader, pageNo, new HorizontalTextExtractionStrategy());
    }

    /**
     * Test with {@link HorizontalTextExtractionStrategy}, works for iText since 5.5.9-SNAPSHOT
     * Commit 1ab350beae148be2a4bef5e663b3d67a004ff9f8 ("Make TextChunkLocation a Comparable<> class...").
     */
    String extractV2(PdfReader reader, int pageNo) throws IOException, NoSuchFieldException, SecurityException
    {
        return PdfTextExtractor.getTextFromPage(reader, pageNo, new HorizontalTextExtractionStrategy2());
    }
}
