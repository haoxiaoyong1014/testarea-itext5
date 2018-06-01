/**
 * 
 */
package mkl.testarea.itext5.merge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;

/**
 * Test of {@link PageVerticalAnalyzer} {@link RenderListener}
 * 
 * @author mkl
 */
public class PageVerticalAnalysis
{
    final static File RESULT_FOLDER = new File("target/test-outputs/merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void test() throws IOException
    {
        String[] pdfs = new String[]{"Header.pdf", "Body.pdf", "Footer.pdf", "from.pdf", "preface.pdf", "Test_Type3_Problem.pdf", "testA4.pdf"};
        for (String pdf: pdfs)
        {
            try (   InputStream resource = getClass().getResourceAsStream(pdf))
            {
                File target = new File(RESULT_FOLDER, pdf + "-sections.txt");
                analyzeVertically(resource, target);
            }
        }
    }

    void analyzeVertically(InputStream pdf, File target) throws IOException
    {
        final PdfReader reader = new PdfReader(pdf);

        try 
        {
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            StringBuilder builder = new StringBuilder();
            for (int page=1; page <= reader.getNumberOfPages(); page++)
            {
                PageVerticalAnalyzer analyzer = parser.processContent(page, new PageVerticalAnalyzer());
                builder.append("Page ").append(page).append('\n');
                if (analyzer.verticalFlips.size() > 0)
                {
                    for (int i = 0; i < analyzer.verticalFlips.size() - 1; i+=2)
                    {
                        builder.append(String.format("%3.3f - %3.3f\n", analyzer.verticalFlips.get(i), analyzer.verticalFlips.get(i+1)));
                    }
                    builder.append('\n');
                }
                else
                {
                    builder.append("No content\n\n");
                }
            }
            String sections = builder.toString();
            System.out.print(sections);
            Files.write(target.toPath(), sections.getBytes());
        }
        finally
        {
            reader.close();
        }
    }
}
