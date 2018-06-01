package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.ExtRenderListener;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextMarginFinder;

/**
 * <a href="http://stackoverflow.com/questions/20129775/using-itextpdf-to-trim-a-pages-whitespace">
 * Using iTextPDF to trim a page's whitespace
 * </a>
 * <p>
 * {@link #testWithWriter()} with {@link #getOutputPageSize(Rectangle, PdfReader, int)} represents the code
 * presented by the OP while {@link #testWithStamper()}, {@link #testWithStamperTopBottom()}, 
 * {@link #testWithStamperCentered()}, and {@link #testWithStamperExtFinder()} with
 * {@link #getOutputPageSize(Rectangle, PdfReader, int)}, {@link #getOutputPageSize2(Rectangle, PdfReader, int)},
 * {@link #getOutputPageSize3(Rectangle, PdfReader, int)}, or {@link #getOutputPageSize4(Rectangle, PdfReader, int)}
 * respectively represent improving approaches at the solution.
 * </p>
 * <p>
 * The final one, {@link #testWithStamperExtFinder()} with {@link #getOutputPageSize4(Rectangle, PdfReader, int)},
 * makes use of {@link MarginFinder} which is an extended version of {@link TextMarginFinder} implementing the
 * extended {@link ExtRenderListener} interface new in iText version 5.5.6.
 * </p>
 * 
 * @author mkl
 */
public class TestTrimPdfPage
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    // the OP's code
    @Test
    public void testWithWriter() throws DocumentException, IOException
    {
        InputStream resourceStream = getClass().getResourceAsStream("test.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            Rectangle pageSize = reader.getPageSize(1);

            Rectangle rect = getOutputPageSize(pageSize, reader, 1);

            Document document = new Document(rect, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "test-trimmed-writer.pdf")));

            document.open();
            PdfImportedPage page;

            // Go through all pages
            int n = reader.getNumberOfPages();
            for (int i = 1; i <= n; i++)
            {
                document.newPage();
                page = writer.getImportedPage(reader, i);
                System.out.println("BBox:  "+ page.getBoundingBox().toString());
                Image instance = Image.getInstance(page);
                document.add(instance);
                Rectangle outputPageSize = document.getPageSize();
                System.out.println(outputPageSize.toString());
            }
            document.close();
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    // testWithWriter revised to use a PdfStamper instead of a PdfWriter
    @Test
    public void testWithStamper() throws DocumentException, IOException
    {
        InputStream resourceStream = getClass().getResourceAsStream("test.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "test-trimmed-stamper.pdf")));
            
            // Go through all pages
            int n = reader.getNumberOfPages();
            for (int i = 1; i <= n; i++)
            {
                Rectangle pageSize = reader.getPageSize(i);
                Rectangle rect = getOutputPageSize(pageSize, reader, i);

                PdfDictionary page = reader.getPageN(i);
                page.put(PdfName.CROPBOX, new PdfArray(new float[]{rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop()}));
                stamper.markUsed(page);
            }
            stamper.close();
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    // testWithStamper revised to use the whole page width
    @Test
    public void testWithStamperTopBottom() throws DocumentException, IOException
    {
        InputStream resourceStream = getClass().getResourceAsStream("test.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "test-trimmed-stamper-top-bottom.pdf")));
            
            // Go through all pages
            int n = reader.getNumberOfPages();
            for (int i = 1; i <= n; i++)
            {
                Rectangle pageSize = reader.getPageSize(i);
                Rectangle rect = getOutputPageSize2(pageSize, reader, i);

                PdfDictionary page = reader.getPageN(i);
                page.put(PdfName.CROPBOX, new PdfArray(new float[]{rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop()}));
                stamper.markUsed(page);
            }
            stamper.close();
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    // testWithStamper revised to use a width with equal margins left and right
    @Test
    public void testWithStamperCentered() throws DocumentException, IOException
    {
        InputStream resourceStream = getClass().getResourceAsStream("test.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "test-trimmed-stamper-centered.pdf")));
            
            // Go through all pages
            int n = reader.getNumberOfPages();
            for (int i = 1; i <= n; i++)
            {
                Rectangle pageSize = reader.getPageSize(i);
                Rectangle rect = getOutputPageSize3(pageSize, reader, i);

                PdfDictionary page = reader.getPageN(i);
                page.put(PdfName.CROPBOX, new PdfArray(new float[]{rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop()}));
                stamper.markUsed(page);
            }
            stamper.close();
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    // testWithStamper revised to use MarginFinder
    @Test
    public void testWithStamperExtFinder() throws DocumentException, IOException
    {
        InputStream resourceStream = getClass().getResourceAsStream("test.pdf");
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, "test-trimmed-stamper-ext.pdf")));
            
            // Go through all pages
            int n = reader.getNumberOfPages();
            for (int i = 1; i <= n; i++)
            {
                Rectangle pageSize = reader.getPageSize(i);
                Rectangle rect = getOutputPageSize4(pageSize, reader, i);

                PdfDictionary page = reader.getPageN(i);
                page.put(PdfName.CROPBOX, new PdfArray(new float[]{rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop()}));
                stamper.markUsed(page);
            }
            stamper.close();
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    /**
     * Need to get the size of the page excluding whitespace......
     * <p>
     * The OP's code
     * 
     * @param pageSize the original page size
     * @param reader the pdf reader
     * @return a new page size which cuts out the whitespace
     * @throws IOException 
     */
    private Rectangle getOutputPageSize(Rectangle pageSize, PdfReader reader, int page) throws IOException
    {
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        TextMarginFinder finder = parser.processContent(page, new TextMarginFinder());
        Rectangle result = new Rectangle(finder.getLlx(), finder.getLly(), finder.getUrx(), finder.getUry());
        System.out.printf("Actual boundary: (%f;%f) to (%f;%f)\n", finder.getLlx(), finder.getLly(), finder.getUrx(), finder.getUry());
        return result;
    }

    /**
     * Need to get the size of the page excluding whitespace......
     * <p>
     * The OP's code revised to use the whole page width
     * 
     * @param pageSize the original page size
     * @param reader the pdf reader
     * @return a new page size which cuts out the whitespace
     * @throws IOException 
     */
    private Rectangle getOutputPageSize2(Rectangle pageSize, PdfReader reader, int page) throws IOException
    {
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        TextMarginFinder finder = parser.processContent(page, new TextMarginFinder());
        Rectangle result = new Rectangle(pageSize.getLeft(), finder.getLly(), pageSize.getRight(), finder.getUry());
        System.out.printf("Actual boundary: (%f;%f) to (%f;%f)\n", finder.getLlx(), finder.getLly(), finder.getUrx(), finder.getUry());
        return result;
    }

    /**
     * Need to get the size of the page excluding whitespace......
     * <p>
     * The OP's code revised to use a width with equal margins left and right
     * 
     * @param pageSize the original page size
     * @param reader the pdf reader
     * @return a new page size which cuts out the whitespace
     * @throws IOException 
     */
    private Rectangle getOutputPageSize3(Rectangle pageSize, PdfReader reader, int page) throws IOException
    {
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        TextMarginFinder finder = parser.processContent(page, new TextMarginFinder());
        float right = 2 * finder.getUrx() - finder.getLlx();
        Rectangle result = new Rectangle(finder.getLlx(), finder.getLly(), right, finder.getUry());
        System.out.printf("Actual boundary: (%f;%f) to (%f;%f)\n", finder.getLlx(), finder.getLly(), finder.getUrx(), finder.getUry());
        return result;
    }

    /**
     * Need to get the size of the page excluding whitespace......
     * <p>
     * The OP's code revised to use MarginFinder
     * 
     * @param pageSize the original page size
     * @param reader the pdf reader
     * @return a new page size which cuts out the whitespace
     * @throws IOException 
     */
    private Rectangle getOutputPageSize4(Rectangle pageSize, PdfReader reader, int page) throws IOException
    {
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        MarginFinder finder = parser.processContent(page, new MarginFinder());
        Rectangle result = new Rectangle(finder.getLlx(), finder.getLly(), finder.getUrx(), finder.getUry());
        System.out.printf("Actual boundary: (%f;%f) to (%f;%f)\n", finder.getLlx(), finder.getLly(), finder.getUrx(), finder.getUry());
        return result;
    }
}
