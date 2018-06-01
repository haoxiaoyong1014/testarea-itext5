package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

/**
 * <a href="http://stackoverflow.com/questions/32688572/how-do-i-extract-rows-from-a-pdf-file-into-a-csv-file">
 * How do I extract rows from a PDF file into a csv file?
 * </a>
 * <br/>
 * <a href="https://studyinthestates.dhs.gov/assets/certified-school-list-9-16-2015.pdf">
 * certified-school-list-9-16-2015.pdf
 * </a>
 * <p>
 * This test extracts the "certified-school-list-9-16-2015.pdf" data as tab-separated text lines.
 * </p>
 * 
 * @author mkl
 */
public class ExtractCertifiedSchoolList
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testCertifiedSchoolList_9_16_2015() throws IOException
    {
        try (   Writer data = new OutputStreamWriter(new FileOutputStream(new File(RESULT_FOLDER, "data.txt")), "UTF-8");
                Writer nonData = new OutputStreamWriter(new FileOutputStream(new File(RESULT_FOLDER, "non-data.txt")), "UTF-8");
                InputStream resource = getClass().getResourceAsStream("certified-school-list-9-16-2015.pdf")    )
        {
            CertifiedSchoolListExtractionStrategy strategy = new CertifiedSchoolListExtractionStrategy(data, nonData);
            PdfReader reader = new PdfReader(resource);

            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            for (int page = 1; page <= reader.getNumberOfPages(); page++)
                parser.processContent(page, strategy);
//            parser.processContent(28, strategy);
            strategy.close();
        }
    }
}
