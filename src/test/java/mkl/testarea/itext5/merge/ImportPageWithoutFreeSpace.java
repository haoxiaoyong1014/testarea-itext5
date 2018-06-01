package mkl.testarea.itext5.merge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import mkl.testarea.itext5.content.MarginFinder;
import mkl.testarea.itext5.content.TestTrimPdfPage;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

/**
 * This class contains tests for importing a PDF page from an
 * existing document while ignoring the white space surrounding
 * the content.
 *  
 * @author mkl
 */
public class ImportPageWithoutFreeSpace
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/31980979/itext-importing-styled-text-and-informations-from-an-existing-pdf">
     * iText: Importing styled Text and informations from an existing PDF
     * </a>
     * <p>
     * This method demonstrates how to import merely the region of a PDF page with
     * actual content. The main necessity is to call {@link #cropPdf(PdfReader)}
     * for the reader in question which restricts the media boxes of the pages to
     * the bounding box of the existing content.
     * </p>
     */
    @Test
    public void testImportPages() throws DocumentException, IOException
    {
        byte[] docText = createSimpleTextPdf();
        Files.write(new File(RESULT_FOLDER, "textOnly.pdf").toPath(), docText);
        byte[] docGraphics = createSimpleCircleGraphicsPdf();
        Files.write(new File(RESULT_FOLDER, "graphicsOnly.pdf").toPath(), docGraphics);

        PdfReader readerText = new PdfReader(docText);
        cropPdf(readerText);
        PdfReader readerGraphics = new PdfReader(docGraphics);
        cropPdf(readerGraphics);
        try (   FileOutputStream fos = new FileOutputStream(new File(RESULT_FOLDER, "importPages.pdf")))
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();
            document.add(new Paragraph("Let's import 'textOnly.pdf'", new Font(FontFamily.HELVETICA, 12, Font.BOLD)));
            document.add(Image.getInstance(writer.getImportedPage(readerText, 1)));
            document.add(new Paragraph("and now 'graphicsOnly.pdf'", new Font(FontFamily.HELVETICA, 12, Font.BOLD)));
            document.add(Image.getInstance(writer.getImportedPage(readerGraphics, 1)));
            document.add(new Paragraph("That's all, folks!", new Font(FontFamily.HELVETICA, 12, Font.BOLD)));

            document.close();
        }
        finally
        {
            readerText.close();
            readerGraphics.close();
        }
    }

    /**
     * <p>
     * This method restricts the media boxes of the pages in the given {@link PdfReader}
     * to the actual content found by the {@link MarginFinder} extended render listener.
     * </p>
     * <p>
     * It essentially is copied from the {@link TestTrimPdfPage} methods
     * {@link TestTrimPdfPage#testWithStamperExtFinder()} and
     * {@link TestTrimPdfPage#getOutputPageSize4(Rectangle, PdfReader, int)}.
     * In contrast to the code there this method manipulates
     * the media box because this is the only box respected by
     * {@link PdfWriter#getImportedPage(PdfReader, int)}.
     * </p>
     */
    static void cropPdf(PdfReader reader) throws IOException
    {
        int n = reader.getNumberOfPages();
        for (int i = 1; i <= n; i++)
        {
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            MarginFinder finder = parser.processContent(i, new MarginFinder());
            Rectangle rect = new Rectangle(finder.getLlx(), finder.getLly(), finder.getUrx(), finder.getUry());

            PdfDictionary page = reader.getPageN(i);
            page.put(PdfName.MEDIABOX, new PdfArray(new float[]{rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop()}));
        }
    }

    /**
     * This method creates a PDF with a single styled paragraph.
     */
    static byte[] createSimpleTextPdf() throws DocumentException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        Paragraph paragraph = new Paragraph();
        paragraph.add(new Phrase("Beware: ", new Font(FontFamily.HELVETICA, 12, Font.BOLDITALIC)));
        paragraph.add(new Phrase("The implementation of ", new Font(FontFamily.HELVETICA, 12, Font.ITALIC)));
        paragraph.add(new Phrase("MarginFinder", new Font(FontFamily.COURIER, 12, Font.ITALIC)));
        paragraph.add(new Phrase(" is far from optimal. It is not even correct as it includes all curve control points which is too much. Furthermore it ignores stuff like line width or wedge types. It actually merely is a proof-of-concept.", new Font(FontFamily.HELVETICA, 12, Font.ITALIC)));
        document.add(paragraph);

        document.close();

        return baos.toByteArray();
    }

    /**
     * This method creates a PDF with a single styled paragraph.
     */
    static byte[] createSimpleCircleGraphicsPdf() throws DocumentException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        float y = writer.getPageSize().getTop(document.topMargin());
        float radius = 20;
        for (int i = 0; i < 3; i++)
        {
            Rectangle pageSize = writer.getPageSize();
            writer.getDirectContent().circle(
                    pageSize.getLeft(document.leftMargin()) + (pageSize.getWidth() - document.leftMargin() - document.rightMargin()) * Math.random(),
                    y-radius, radius);
            y-= 2*radius + 5;
        }

        writer.getDirectContent().fillStroke();
        document.close();

        return baos.toByteArray();
    }
}
