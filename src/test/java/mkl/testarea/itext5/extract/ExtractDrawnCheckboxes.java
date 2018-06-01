package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.Vector;

import mkl.testarea.itext5.extract.CheckBoxExtractionStrategy.Box;

/**
 * @author mkl
 */
public class ExtractDrawnCheckboxes
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/40549977/reading-legacy-word-forms-checkboxes-converted-to-pdf">
     * Reading legacy Word forms checkboxes converted to PDF
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/s/4z7ky3yy2yaj53i/Doc1.pdf?dl=0">
     * Doc1.pdf
     * </a>
     * <p>
     * This test shows how one can extract the sample drawn "checkboxes" from the
     * sample PDF provided by the OP.
     * </p>
     */
    @Test
    public void testExtractDoc1() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Doc1.pdf"))
        {
            PdfReader pdfReader = new PdfReader(resource);

            for (int page = 1; page <= pdfReader.getNumberOfPages(); page++)
            {
                System.out.printf("\nPage %s\n====\n", page);

                CheckBoxExtractionStrategy strategy = new CheckBoxExtractionStrategy();
                PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
                parser.processContent(page, strategy);

                for (Box box : strategy.getBoxes())
                {
                    Vector basePoint = box.getDiagonal().getStartPoint();
                    System.out.printf("at %s, %s - %s\n", basePoint.get(Vector.I1), basePoint.get(Vector.I2),
                            box.isChecked() ? "checked" : "unchecked");
                }
            }
        }
    }

}
