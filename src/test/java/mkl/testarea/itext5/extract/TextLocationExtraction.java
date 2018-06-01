package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

import mkl.testarea.itext5.extract.SearchTextLocationExtractionStrategy.TextRectangle;

/**
 * @author mkl
 */
public class TextLocationExtraction {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/45823741/how-to-find-all-occurrences-of-specific-text-in-a-pdf-and-insert-a-page-break-ab">
     * How to find all occurrences of specific text in a PDF and insert a page break above?
     * </a>
     * <p>
     * This test shows how to <i>find all occurrences of specific text in a PDF</i>
     * using the {@link SearchTextLocationExtractionStrategy} and mark them.
     * </p>
     */
    @Test
    public void testPreface() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("preface.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "preface-marked-be.pdf"))) {
            mark(resource, result, Pattern.compile("be"));
        }
    }

    void mark(InputStream input, OutputStream output, Pattern pattern) throws DocumentException, IOException
    {
        PdfReader reader = new PdfReader(input);
        PdfStamper stamper = new PdfStamper(reader, output);
        try {
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            for (int pageNr = 1; pageNr <= reader.getNumberOfPages(); pageNr++)
            {
                SearchTextLocationExtractionStrategy strategy = new SearchTextLocationExtractionStrategy(pattern);
                parser.processContent(pageNr, strategy, Collections.emptyMap()).getResultantText();
                Collection<TextRectangle> locations = strategy.getLocations(null);
                if (locations.isEmpty())
                    continue;

                PdfContentByte canvas = stamper.getOverContent(pageNr);
                canvas.setRGBColorStroke(255, 255, 0);
                for (TextRectangle location : locations)
                {
                    canvas.rectangle(location.getMinX(), location.getMinY(), location.getWidth(), location.getHeight());
                }
                canvas.stroke();
            }
            stamper.close();
        } finally {
            reader.close();
        }
    }
}
