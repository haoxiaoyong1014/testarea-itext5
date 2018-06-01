package mkl.testarea.itext5.merge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;

/**
 * @author mkl
 */
public class HeapOomDuringMerge
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35231801/pdf-merge-using-itext-throws-oom-error">
     * PDF merge using iText throws OOM error
     * </a>
     * <br/>
     * <a href="https://saibababojja.files.wordpress.com/2012/03/pro-linq-in-c-2010.pdf">
     * pro-linq-in-c-2010.pdf
     * </a>
     * <p>
     * Indeed, quite some memory is required to do this merger, but on Java 8 I actually merely
     * needed 725 MB (-Xmx725m) while for the OP the code failed even at 2 GB. Small improvements
     * are possible using <code>reader.freeReader(reader)</code>.
     * </p>
     */
    @Test
    public void testMergeLikeLe_Master() throws IOException, DocumentException
    {
//        String ifs2 = "C:\\Downloads\\02.pdf";
//        String result = "C:\\Merge\\final.pdf";
        String ifs2 = "src\\test\\resources\\mkl\\testarea\\itext5\\merge\\pro-linq-in-c-2010.pdf";
        String result = new File(RESULT_FOLDER, "finalLe_Master.pdf").toString();
        String[] stArray = new String[10]; 
        for(int i = 0; i<10; i++){
            stArray[i]=ifs2;
        }

        mergeFiles(stArray,result, false);
    }

    public static void mergeFiles(String[] files, String result, boolean smart) throws IOException, DocumentException {
        Document document = new Document();
        PdfCopy copy;
        if (smart)
            copy = new PdfSmartCopy(document, new FileOutputStream(result));
        else
            copy = new PdfCopy(document, new FileOutputStream(result));
        document.open();

        for (int i = 0; i < files.length; i++) {
            System.out.println(i);
            PdfReader reader = new PdfReader(files[i]);
            copy.addDocument(reader);
            reader.close();
        }
        document.close();
    }
}
