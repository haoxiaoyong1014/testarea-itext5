package mkl.testarea.itext5.xmlworker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * @author mkl
 */
public class MemoryConsumption
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "xmlworker");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/38989235/itext-html-to-pdf-memory-leak">
     * IText HTML to PDF memory leak
     * </a>
     * <p>
     * The OP's memory problems could not be reproduced.
     * </p>
     */
    @Test
    public void testDevelofersScenarioOnce() throws IOException, DocumentException
    {
        testDevelofersScenario("develofersResult.pdf");
    }

    /**
     * <a href="http://stackoverflow.com/questions/38989235/itext-html-to-pdf-memory-leak">
     * IText HTML to PDF memory leak
     * </a>
     * <p>
     * The OP's memory problems could not be reproduced, not even by calling the OP's code
     * again and again.
     * </p>
     */
    @Test
    public void testDevelofersScenarioMultiple() throws IOException, DocumentException, InterruptedException
    {
        Thread.sleep(10000);
        for (int i = 0; i < 50; i++)
        {
            Thread.sleep(500);
            System.out.printf("Starting run %s\n", i);
            long start = System.currentTimeMillis();
            testDevelofersScenario(String.format("develofersResult%s.pdf", i));
            System.out.printf("Finished run %s after %s ms\n", i, System.currentTimeMillis() - start);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38989235/itext-html-to-pdf-memory-leak">
     * IText HTML to PDF memory leak
     * </a>
     * <p>
     * The OP's code plus a save-to-file.
     * </p>
     */
    public void testDevelofersScenario(String outputName) throws IOException, DocumentException
    {
        final String content = "<!--?xml version=\"1.0\" encoding=\"UTF-8\"?-->\n<html>\n <head>\n    <title>Title</title>\n    \n   \n </head>\n"
                + "\n    \n<body>  \n  \n      \nEXAMPLE\n\n</body>\n</html>";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);        
        document.open();
        InputStream is = new ByteArrayInputStream(content.getBytes());
        XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);

        document.close();
        
        baos.writeTo(new FileOutputStream(new File(RESULT_FOLDER, outputName)));
    }

}
