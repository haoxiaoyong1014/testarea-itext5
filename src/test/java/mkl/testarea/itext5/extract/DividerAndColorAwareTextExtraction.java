package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import mkl.testarea.itext5.extract.DividerAwareTextExtrationStrategy.Section;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

/**
 * @author mkl
 */
public class DividerAndColorAwareTextExtraction {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/31730278/extract-text-from-pdf-between-two-dividers-with-itextsharp">
     * Extract text from PDF between two dividers with ITextSharp
     * </a>
     * <br>
     * <a href="http://www.tjsc.jus.br/institucional/diario/a2015/20150211600.PDF">
     * 20150211600.PDF
     * </a>
     * <p>
     * This test applies the {@link DividerAndColorAwareTextExtractionStrategy} to the OP's sample
     * document, page 346, to demonstrate its use.
     * </p>
     */
    @Test
    public void test20150211600_346() throws IOException, DocumentException {
        InputStream resourceStream = getClass().getResourceAsStream("20150211600.PDF");
        BaseColor headerColor = new CMYKColor(0.58f, 0.17f, 0, 0.56f);
        try {
            PdfReader reader = new PdfReader(resourceStream);
            String content = extractAndStore(reader, new File(RESULT_FOLDER, "20150211600.%s.%s.txt").toString(), 346, 346, headerColor);

            System.out.println("\nText 20150211600.PDF\n************************");
            System.out.println(content);
            System.out.println("************************");
        } finally {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    String extractAndStore(PdfReader reader, String format, int from, int to, BaseColor headerColor) throws IOException {
        StringBuilder builder = new StringBuilder();

        for (int page = from; page <= to; page++) {
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            DividerAwareTextExtrationStrategy strategy = parser.processContent(page, new DividerAndColorAwareTextExtractionStrategy(810, 30, 20, 575, headerColor));

            List<Section> sections = strategy.getSections();
            int i = 0;
            for (Section section : sections) {
                String sectionText = strategy.getResultantText(section);
                Files.write(Paths.get(String.format(format, page, i)), sectionText.getBytes("UTF8"));

                builder.append("--\n")
                        .append(sectionText)
                        .append('\n');
                i++;
            }
            builder.append("\n\n");
        }

        return builder.toString();
    }
}
