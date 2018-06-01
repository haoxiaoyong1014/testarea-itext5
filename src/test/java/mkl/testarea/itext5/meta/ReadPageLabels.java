package mkl.testarea.itext5.meta;

import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfReader;

/**
 * @author mkl
 */
public class ReadPageLabels
{
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    /**
     * <a href="http://stackoverflow.com/questions/32393858/why-result-of-getpagelabels-is-different-from-the-adobe-acrobat">
     * Why result of GetPageLabels is different from the Adobe Acrobat
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0Bxb0Du7de8igNmVPSUc3VzdPSjg/view?usp=sharing">
     * testHuangMeizai.pdf
     * </a>
     * <p>
     * Indeed, the labels are wrong. There is a small bug in {@link PdfPageLabels#getPageLabelFormats(PdfReader)}.
     * When encountering a new page label dictionary without a P (prefix) entry, it does not reset the current
     * prefix value.
     * </p>
     */
    @Test
    public void testTestHuangMeizai() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("testHuangMeizai.pdf"))
        {
            final PdfReader reader = new PdfReader(resource);
            
            String[] objLabels = PdfPageLabels.getPageLabels(reader);
            System.out.println("page number:");
            if (objLabels != null)
            {
                for (int i = 0; i <= objLabels.length - 1; i++)
                {
                    System.out.printf("%2d - %s\n", i, objLabels[i]);
                }
            }
        }
    }

}
