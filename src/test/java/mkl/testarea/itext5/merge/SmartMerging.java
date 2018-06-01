package mkl.testarea.itext5.merge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;

/**
 * This test focuses on {@link PdfSmartCopy} issues.
 * 
 * @author mkl
 */
public class SmartMerging
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/32267542/pdf-merge-issue-in-itextsharp-pdf-looks-distorted-after-merge">
     * Pdf Merge issue in itextsharp - PDF looks distorted after Merge
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/s/l479kvar16p9p57/input.pdf?dl=0">
     * input.pdf
     * </a>
     * <p>
     * This test method together with the static methods {@link #ExtractPages(String, int, int)}
     * and {@link #Merge(File[])} constitute a fairly 1:1 translation of the OP's C#/iTextSharp
     * code. Unfortunately, though, it does not reproduce the error reproducibly occurring in the
     * iTextSharp version.
     * </p>
     */
    @Test
    public void testSplitAndRemerge() throws IOException, DocumentException
    {
        String inputDocPath = "input.pdf";

        byte[] part1 = ExtractPages(inputDocPath, 1, 2);
        File outputPath1 = new File(RESULT_FOLDER, "part1.pdf");
        Files.write(outputPath1.toPath(), part1);

        byte[] part2 = ExtractPages(inputDocPath, 3, 0);
        File outputPath2 = new File(RESULT_FOLDER, "part2.pdf");
        Files.write(outputPath2.toPath(), part2);

        byte[] merged = Merge(new File[] { outputPath1, outputPath2 });

        File mergedPath = new File(RESULT_FOLDER, "output.pdf");
        Files.write(mergedPath.toPath(), merged);
    }

    public static byte[] Merge(File[] documentPaths) throws IOException, DocumentException
    {
        byte[] mergedDocument;

        try (ByteArrayOutputStream memoryStream = new ByteArrayOutputStream())
        {
            Document document = new Document();
            PdfSmartCopy pdfSmartCopy = new PdfSmartCopy(document, memoryStream);
            document.open();

            for (File docPath : documentPaths)
            {
                PdfReader reader = new PdfReader(docPath.toString());
                try
                {
                    reader.consolidateNamedDestinations();
                    int numberOfPages = reader.getNumberOfPages();
                    for (int page = 0; page < numberOfPages;)
                    {
                        PdfImportedPage pdfImportedPage = pdfSmartCopy.getImportedPage(reader, ++page);
                        pdfSmartCopy.addPage(pdfImportedPage);
                    }
                }
                finally
                {
                    reader.close();
                }
            }

            document.close();
            mergedDocument = memoryStream.toByteArray();
        }

        return mergedDocument;
    }

    public static byte[] ExtractPages(String pdfDocument, int startPage, int endPage) throws IOException, DocumentException
    {
        try (InputStream pdfDocumentStream = SmartMerging.class.getResourceAsStream(pdfDocument))
        {
            PdfReader reader = new PdfReader(pdfDocumentStream);
            int numberOfPages = reader.getNumberOfPages();
            int endPageResolved = endPage > 0 ? endPage : numberOfPages;
            if (startPage > numberOfPages || endPageResolved > numberOfPages)
                System.err.printf("Error: page indices (%s, %s) out of bounds. Document has {2} pages.", startPage, endPageResolved, numberOfPages);

            byte[] outputDocument;
            try (ByteArrayOutputStream msOut = new ByteArrayOutputStream())
            {
                Document doc = new Document();
                PdfCopy pdfCopyProvider = new PdfCopy(doc, msOut);
                doc.open();
                for (int i = startPage; i <= endPageResolved; i++)
                {
                    PdfImportedPage page = pdfCopyProvider.getImportedPage(reader, i);
                    pdfCopyProvider.addPage(page);
                }
                doc.close();
                reader.close();
                outputDocument = msOut.toByteArray();
            }

            return outputDocument;
        }
    }
}
