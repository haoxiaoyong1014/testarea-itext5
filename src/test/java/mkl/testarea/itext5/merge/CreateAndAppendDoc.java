package mkl.testarea.itext5.merge;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This test focuses on combied PDF creation and merging.
 *
 * @author mkl
 */
public class CreateAndAppendDoc
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/29001852/how-to-create-a-pdf-and-you-then-merge-another-pdf-to-the-same-document-using-it">
     * how to create a PDF and you then merge another pdf to the same document using itext
     * </a>
     * <p>
     * Testing the OP's method with <code>paginate</code> set to <code>false</code>
     * </p>
     */
    @Test
    public void testAppendPDFs() throws IOException, DocumentException
    {
        try (
                InputStream testA4Stream = getClass().getResourceAsStream("testA4.pdf");
                InputStream fromStream = getClass().getResourceAsStream("from.pdf");
                InputStream prefaceStream = getClass().getResourceAsStream("preface.pdf");
                InputStream type3Stream = getClass().getResourceAsStream("Test_Type3_Problem.pdf");
                FileOutputStream output = new FileOutputStream(new File(RESULT_FOLDER, "appendPdfs.pdf"));
            )
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, output);
            document.open();
            document.add(new Paragraph("Some content to start with"));
            appendPDFs(Arrays.asList(testA4Stream, fromStream, prefaceStream, type3Stream), writer, document, null, false);
            document.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/29001852/how-to-create-a-pdf-and-you-then-merge-another-pdf-to-the-same-document-using-it">
     * how to create a PDF and you then merge another pdf to the same document using itext
     * </a>
     * <p>
     * Testing the OP's method with <code>paginate</code> set to <code>true</code>
     * </p>
     */
    @Test
    public void testAppendPDFsPaginate() throws IOException, DocumentException
    {
        try (
                InputStream testA4Stream = getClass().getResourceAsStream("testA4.pdf");
                InputStream fromStream = getClass().getResourceAsStream("from.pdf");
                InputStream prefaceStream = getClass().getResourceAsStream("preface.pdf");
                InputStream type3Stream = getClass().getResourceAsStream("Test_Type3_Problem.pdf");
                FileOutputStream output = new FileOutputStream(new File(RESULT_FOLDER, "appendPdfsPaginate.pdf"));
            )
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, output);
            document.open();
            document.add(new Paragraph("Some content to start with"));
            appendPDFs(Arrays.asList(testA4Stream, fromStream, prefaceStream, type3Stream), writer, document, null, true);
            document.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/29001852/how-to-create-a-pdf-and-you-then-merge-another-pdf-to-the-same-document-using-it">
     * how to create a PDF and you then merge another pdf to the same document using itext
     * </a>
     * <p>
     * The OP's original method.
     * </p>
     */
    public static void appendPDFs(List<InputStream> pdfs, PdfWriter writer, Document document, OutputStream opStream, boolean paginate)
    {
        try
        {
            List<PdfReader> readers = new ArrayList<PdfReader>();
            int totalPages = 0;
            Iterator<InputStream> iteratorPDFs = pdfs.iterator();
            // Create Readers for the pdfs.
            while (iteratorPDFs.hasNext())
            {
                InputStream pdf = iteratorPDFs.next();
                PdfReader pdfReader = new PdfReader(pdf);
                readers.add(pdfReader);
                totalPages += pdfReader.getNumberOfPages();
            }
            // Create a writer for the outputstream

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            PdfContentByte cb = writer.getDirectContent(); // Holds the PDF data

            PdfImportedPage page;
            int currentPageNumber = 0;
            int pageOfCurrentReaderPDF = 0;
            Iterator<PdfReader> iteratorPDFReader = readers.iterator();

            // Loop through the PDF files and add to the output.
            while (iteratorPDFReader.hasNext())
            {
                PdfReader pdfReader = iteratorPDFReader.next();

                // Create a new page in the target for each source page.
                while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages())
                {
                    document.newPage();
                    pageOfCurrentReaderPDF++;
                    currentPageNumber++;
                    page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
                    cb.addTemplate(page, 0, 0);

                    // Code for pagination.
                    if (paginate)
                    {
                        cb.beginText();
                        cb.setFontAndSize(bf, 9);
                        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "" + currentPageNumber + " of " + totalPages, 520, 5, 0);
                        cb.endText();
                    }
                }
                pageOfCurrentReaderPDF = 0;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
        }
    }
}
