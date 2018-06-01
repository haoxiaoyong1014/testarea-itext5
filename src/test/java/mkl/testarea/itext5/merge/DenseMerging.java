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
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This test is about merging PDFs tighter than by page.
 * 
 * @author mkl
 */
public class DenseMerging
{
    final static File RESULT_FOLDER = new File("target/test-outputs/merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/27988503/how-can-i-combine-multiple-pdf-files-excluding-page-breaks-using-itextsharp">
     * How can I combine multiple PDF files excluding page breaks using iTextSharp?
     * </a>
     * <p>
     * Testing {@link PdfDenseMergeTool}.
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

        PdfDenseMergeTool tool = new PdfDenseMergeTool(PageSize.A4, 18, 18, 5);
        PdfReader readerA = new PdfReader(docA);
        PdfReader readerB = new PdfReader(docB);
        PdfReader readerC = new PdfReader(docC);
        PdfReader readerD = new PdfReader(docD);
        try (FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "textOnlyMerge.pdf")))
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
     * <a href="http://stackoverflow.com/questions/27988503/how-can-i-combine-multiple-pdf-files-excluding-page-breaks-using-itextsharp">
     * How can I combine multiple PDF files excluding page breaks using iTextSharp?
     * </a>
     * <p>
     * Testing {@link PdfDenseMergeTool}.
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

        PdfDenseMergeTool tool = new PdfDenseMergeTool(PageSize.A4, 18, 18, 5);
        PdfReader readerA = new PdfReader(docA);
        PdfReader readerB = new PdfReader(docB);
        PdfReader readerC = new PdfReader(docC);
        PdfReader readerD = new PdfReader(docD);
        try (FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "longTextOnlyMerge.pdf")))
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

    /**
     * <a href="http://stackoverflow.com/questions/28991291/how-to-remove-whitespace-on-merge">
     * How To Remove Whitespace on Merge
     * </a>
     * <p>
     * Testing {@link PdfDenseMergeTool} using the OP's files.
     * </p>
     */
    @Test
    public void testMergeGrandizerFiles() throws DocumentException, IOException
    {
        try (   InputStream docA = getClass().getResourceAsStream("Header.pdf");
                InputStream docB = getClass().getResourceAsStream("Body.pdf");
                InputStream docC = getClass().getResourceAsStream("Footer.pdf");    )
        {
            PdfDenseMergeTool tool = new PdfDenseMergeTool(PageSize.A4, 18, 18, 5);
            PdfReader readerA = new PdfReader(docA);
            PdfReader readerB = new PdfReader(docB);
            PdfReader readerC = new PdfReader(docC);
            try (FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "GrandizerMerge.pdf")))
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
