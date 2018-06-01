package mkl.testarea.itext5.stamp;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.xml.xmp.XmpWriter;

/**
 * @author mkl
 */
public class UpdateMetaData
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "stamp");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/43511558/how-to-set-attributes-for-existing-pdf-that-contains-only-images-using-java-itex">
     * how to set attributes for existing pdf that contains only images using java itext?
     * </a>
     * <p>
     * The OP indicated in a comment that he searches a solution without a second file.
     * This test shows how to work with a single file, by first loading the file into a byte array.
     * </p>
     */
    @Test
    public void testChangeTitleWithoutTempFile() throws IOException, DocumentException
    {
        File singleFile = new File(RESULT_FOLDER, "eg_01-singleFile.pdf");
        try (   InputStream resource = getClass().getResourceAsStream("eg_01.pdf")  )
        {
            Files.copy(resource, singleFile.toPath());
        }

        byte[] original = Files.readAllBytes(singleFile.toPath());

        PdfReader reader = new PdfReader(original);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(singleFile));
        Map<String, String> info = reader.getInfo();
        info.put("Title", "New title");
        info.put("CreationDate", new PdfDate().toString());
        stamper.setMoreInfo(info);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmpWriter xmp = new XmpWriter(baos, info);
        xmp.close();
        stamper.setXmpMetadata(baos.toByteArray());
        stamper.close();
        reader.close();
    }

}
