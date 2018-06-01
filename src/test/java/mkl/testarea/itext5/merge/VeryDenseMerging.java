package mkl.testarea.itext5.merge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.RectangleReadOnly;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This test is about merging PDFs tighter than by page or source page.
 * 
 * @author mkl
 */
public class VeryDenseMerging
{
    final static File RESULT_FOLDER = new File("target/test-outputs/merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/28991291/how-to-remove-whitespace-on-merge">
     * How To Remove Whitespace on Merge
     * </a>
     * <p>
     * Testing {@link PdfVeryDenseMergeTool}.
     * </p>
     */
    @Test
    public void testMergeOnlyText() throws DocumentException, IOException
    {
        byte[] docA = createSimpleTextPdf("First document, paragraph %s.", 3);
        Files.write(new File(RESULT_FOLDER, "textOnlyA.pdf").toPath(), docA);
        byte[] docB = createSimpleTextPdf("Second document, paragraph %s.", 3);
        Files.write(new File(RESULT_FOLDER, "textOnlyB.pdf").toPath(), docB);
        byte[] docC = createSimpleTextPdf("Third document, paragraph %s, a bit longer lines.", 3);
        Files.write(new File(RESULT_FOLDER, "textOnlyC.pdf").toPath(), docC);
        byte[] docD = createSimpleTextPdf("Fourth document, paragraph %s, let us make this a much longer paragraph spanning more than one line.", 3);
        Files.write(new File(RESULT_FOLDER, "textOnlyD.pdf").toPath(), docD);

        PdfVeryDenseMergeTool tool = new PdfVeryDenseMergeTool(PageSize.A4, 18, 18, 5);
        PdfReader readerA = new PdfReader(docA);
        PdfReader readerB = new PdfReader(docB);
        PdfReader readerC = new PdfReader(docC);
        PdfReader readerD = new PdfReader(docD);
        try (FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "textOnlyMerge-veryDense.pdf")))
        {
            List<PdfReader> inputs = Arrays.asList(readerA, readerB, readerC, readerD);
            tool.merge(fos, inputs);
        }
        finally
        {
            readerA.close();
            readerB.close();
            readerC.close();
            readerD.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/28991291/how-to-remove-whitespace-on-merge">
     * How To Remove Whitespace on Merge
     * </a>
     * <p>
     * Testing {@link PdfVeryDenseMergeTool}.
     * </p>
     */
    @Test
    public void testMergeOnlyTextLong() throws DocumentException, IOException
    {
        byte[] docA = createSimpleTextPdf("First document, paragraph %s.", 20);
        Files.write(new File(RESULT_FOLDER, "textOnlyLongA.pdf").toPath(), docA);
        byte[] docB = createSimpleTextPdf("Second document, paragraph %s.", 20);
        Files.write(new File(RESULT_FOLDER, "textOnlyLongB.pdf").toPath(), docB);
        byte[] docC = createSimpleTextPdf("Third document, paragraph %s, a bit longer lines.", 20);
        Files.write(new File(RESULT_FOLDER, "textOnlyLongC.pdf").toPath(), docC);
        byte[] docD = createSimpleTextPdf("Fourth document, paragraph %s, let us make this a much longer paragraph spanning more than one line.", 20);
        Files.write(new File(RESULT_FOLDER, "textOnlyLongD.pdf").toPath(), docD);

        PdfVeryDenseMergeTool tool = new PdfVeryDenseMergeTool(PageSize.A4, 18, 18, 5);
        PdfReader readerA = new PdfReader(docA);
        PdfReader readerB = new PdfReader(docB);
        PdfReader readerC = new PdfReader(docC);
        PdfReader readerD = new PdfReader(docD);
        try (FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "longTextOnlyMerge-veryDense.pdf")))
        {
            List<PdfReader> inputs = Arrays.asList(readerA, readerB, readerC, readerD);
            tool.merge(fos, inputs);
        }
        finally
        {
            readerA.close();
            readerB.close();
            readerC.close();
            readerD.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/28991291/how-to-remove-whitespace-on-merge">
     * How To Remove Whitespace on Merge
     * </a>
     * <p>
     * Testing {@link PdfVeryDenseMergeTool}.
     * </p>
     */
    @Test
    public void testMergeOnlyGraphics() throws DocumentException, IOException
    {
        byte[] docA = createSimpleCircleGraphicsPdf(20, 20, 20);
        Files.write(new File(RESULT_FOLDER, "circlesOnlyA.pdf").toPath(), docA);
        byte[] docB = createSimpleCircleGraphicsPdf(50, 10, 2);
        Files.write(new File(RESULT_FOLDER, "circlesOnlyB.pdf").toPath(), docB);
        byte[] docC = createSimpleCircleGraphicsPdf(100, -20, 3);
        Files.write(new File(RESULT_FOLDER, "circlesOnlyC.pdf").toPath(), docC);
        byte[] docD = createSimpleCircleGraphicsPdf(20, 20, 20);
        Files.write(new File(RESULT_FOLDER, "circlesOnlyD.pdf").toPath(), docD);

        PdfVeryDenseMergeTool tool = new PdfVeryDenseMergeTool(PageSize.A4, 18, 18, 5);
        PdfReader readerA = new PdfReader(docA);
        PdfReader readerB = new PdfReader(docB);
        PdfReader readerC = new PdfReader(docC);
        PdfReader readerD = new PdfReader(docD);
        try (FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "circlesOnlyMerge-veryDense.pdf")))
        {
            List<PdfReader> inputs = Arrays.asList(readerA, readerB, readerC, readerD);
            tool.merge(fos, inputs);
        }
        finally
        {
            readerA.close();
            readerB.close();
            readerC.close();
            readerD.close();
        }
    }
    
    static byte[] createSimpleTextPdf(String paragraphFormat, int paragraphCount) throws DocumentException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();
        for (int i = 0; i < paragraphCount; i++)
        {
            Paragraph paragraph = new Paragraph();
            paragraph.add(String.format(paragraphFormat, i));
            document.add(paragraph);
        }
        document.close();

        return baos.toByteArray();
    }

    static byte[] createSimpleCircleGraphicsPdf(int radius, int gap, int count) throws DocumentException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        float y = writer.getPageSize().getTop();
        for (int i = 0; i < count; i++)
        {
            Rectangle pageSize = writer.getPageSize();
            if (y <= pageSize.getBottom() + 2*radius)
            {
                y = pageSize.getTop();
                writer.getDirectContent().fillStroke();
                document.newPage();
            }
            writer.getDirectContent().circle(pageSize.getLeft() + pageSize.getWidth() * Math.random(), y-radius, radius);
            y-= 2*radius + gap;
        }
        writer.getDirectContent().fillStroke();
        document.close();

        return baos.toByteArray();
    }

    /**
     * <a href="http://stackoverflow.com/questions/28991291/how-to-remove-whitespace-on-merge">
     * How To Remove Whitespace on Merge
     * </a>
     * <p>
     * Testing {@link PdfVeryDenseMergeTool} using the OP's files.
     * </p>
     */
    @Test
    public void testMergeGrandizerFiles() throws DocumentException, IOException
    {
        try (   InputStream docA = getClass().getResourceAsStream("Header.pdf");
                InputStream docB = getClass().getResourceAsStream("Body.pdf");
                InputStream docC = getClass().getResourceAsStream("Footer.pdf");    )
        {
            PdfVeryDenseMergeTool tool = new PdfVeryDenseMergeTool(PageSize.A4, 18, 18, 5);
            PdfReader readerA = new PdfReader(docA);
            PdfReader readerB = new PdfReader(docB);
            PdfReader readerC = new PdfReader(docC);
            try (FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "GrandizerMerge-veryDense.pdf")))
            {
                List<PdfReader> inputs = Arrays.asList(readerA, readerB, readerC);
                tool.merge(fos, inputs);
            }
            finally
            {
                readerA.close();
                readerB.close();
                readerC.close();
            }
        }
    }    
    
    /**
     * <a href="http://stackoverflow.com/questions/28991291/how-to-remove-whitespace-on-merge">
     * How To Remove Whitespace on Merge
     * </a>
     * <p>
     * Testing {@link PdfVeryDenseMergeTool} using the OP's files and a gap of 10. This was the
     * OP's gap value of choice resulting in lost lines. Cannot reproduce...
     * </p>
     */
    @Test
    public void testMergeGrandizerFilesGap10() throws DocumentException, IOException
    {
        try (   InputStream docA = getClass().getResourceAsStream("Header.pdf");
                InputStream docB = getClass().getResourceAsStream("Body.pdf");
                InputStream docC = getClass().getResourceAsStream("Footer.pdf");    )
        {
            PdfVeryDenseMergeTool tool = new PdfVeryDenseMergeTool(PageSize.A4, 18, 18, 10);
            PdfReader readerA = new PdfReader(docA);
            PdfReader readerB = new PdfReader(docB);
            PdfReader readerC = new PdfReader(docC);
            try (FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "GrandizerMerge-veryDense-gap10.pdf")))
            {
                List<PdfReader> inputs = Arrays.asList(readerA, readerB, readerC);
                tool.merge(fos, inputs);
            }
            finally
            {
                readerA.close();
                readerB.close();
                readerC.close();
            }
        }
    }    
    
    /**
     * <a href="http://stackoverflow.com/questions/28991291/how-to-remove-whitespace-on-merge">
     * How To Remove Whitespace on Merge
     * </a>
     * <p>
     * Testing {@link PdfVeryDenseMergeTool} using the OP's files on a even smaller page.
     * </p>
     */
    @Test
    public void testMergeGrandizerFilesA5() throws DocumentException, IOException
    {
        try (   InputStream docA = getClass().getResourceAsStream("Header.pdf");
                InputStream docB = getClass().getResourceAsStream("Body.pdf");
                InputStream docC = getClass().getResourceAsStream("Footer.pdf");    )
        {
            PdfVeryDenseMergeTool tool = new PdfVeryDenseMergeTool(new RectangleReadOnly(595,421), 18, 18, 5);
            PdfReader readerA = new PdfReader(docA);
            PdfReader readerB = new PdfReader(docB);
            PdfReader readerC = new PdfReader(docC);
            try (FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "GrandizerMerge-veryDense-A5.pdf")))
            {
                List<PdfReader> inputs = Arrays.asList(readerA, readerB, readerC);
                tool.merge(fos, inputs);
            }
            finally
            {
                readerA.close();
                readerB.close();
                readerC.close();
            }
        }
    }    
}
