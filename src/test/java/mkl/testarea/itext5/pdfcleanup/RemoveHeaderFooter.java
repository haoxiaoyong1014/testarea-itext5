package mkl.testarea.itext5.pdfcleanup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;

/**
 * Specific tests of iText xtra pdfcleanup functions in use cases cleaning header
 * and footer areas..
 * 
 * @author mkl
 */
public class RemoveHeaderFooter
{
    final static File OUTPUTDIR = new File("target/test-outputs/pdfcleanup");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        OUTPUTDIR.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/32448118/issue-in-removing-header-and-footer-in-pdf-using-itext-pdf">
     * Issue in Removing Header and Footer in PDF using iText PDF
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/xznwx4ogemsgd42/spec.pdf?dl=0">
     * spec.pdf
     * </a>
     * <p>
     * This test applies the OP's method {@link #cleanUpContent(PdfReader, String, float, float, boolean)}
     * to his test file removing 65 units wide header and footer section. According to the OP this results
     * in empty pages and exceptions.
     * </p>
     * <p>
     * Using iText 5.5.6 there indeed is an exception, and the page during the cleanup of which the exception
     * occured, is blank. Using the current 5.5.7-SNAPSHOT, though, no exception is thrown and no page is
     * blanked. Issues can be observed, though, on landscape pages of the test file. These can be prevented
     * by using <code>stamper.setRotateContents(false)</code> in {@link #cleanUpContent(PdfReader, String, float, float, boolean)}.
     * </p>
     */
    @Test
    public void testSpec65_65() throws Exception
    {
        try (   InputStream resource = getClass().getResourceAsStream("spec.pdf")   )
        {
            PdfReader reader = new PdfReader(resource);
            cleanUpContent(reader, new File(OUTPUTDIR, "spec65_65.pdf").getAbsolutePath(), 65, 65, true);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/32448118/issue-in-removing-header-and-footer-in-pdf-using-itext-pdf">
     * Issue in Removing Header and Footer in PDF using iText PDF
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/xznwx4ogemsgd42/spec.pdf?dl=0">
     * spec.pdf
     * </a>
     * <p>
     * This test applies the OP's method {@link #cleanUpContent(PdfReader, String, float, float, boolean)}
     * to his test file removing 45 units wide header and footer section. According to the OP this works.
     * </p>
     * <p>
     * Using iText 5.5.6, though there is an exception, and the page during the cleanup of which the exception
     * occured, is blank. Using the current 5.5.7-SNAPSHOT, on the other hand, no exception is thrown and no
     * page is blanked. Issues can be observed, though, on landscape pages of the test file. These can be prevented
     * by using <code>stamper.setRotateContents(false)</code> in {@link #cleanUpContent(PdfReader, String, float, float, boolean)}.
     * </p>
     */
    @Test
    public void testSpec45_45() throws Exception
    {
        try (   InputStream resource = getClass().getResourceAsStream("spec.pdf")   )
        {
            PdfReader reader = new PdfReader(resource);
            cleanUpContent(reader, new File(OUTPUTDIR, "spec45_45.pdf").getAbsolutePath(), 45, 45, true);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/32448118/issue-in-removing-header-and-footer-in-pdf-using-itext-pdf">
     * Issue in Removing Header and Footer in PDF using iText PDF
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/xznwx4ogemsgd42/spec.pdf?dl=0">
     * spec.pdf
     * </a>
     * <p>
     * removes header and footer based on the configuration
     * </p>
     * <p>
     * This is the original code by the OP whith minor changes to render it runnable
     * in this testarea project.
     * </p>
     * <p>
     * Issues observed on landscape pages of the test file can be prevented by using
     * <code>stamper.setRotateContents(false)</code>, see below.
     * </p>
     */
    public static void cleanUpContent(PdfReader reader,String targetPDFFile, float upperY, float lowerY, boolean highLightColor) throws Exception
    {
        OutputStream outputStream = new FileOutputStream(targetPDFFile);
        PdfStamper stamper = new PdfStamper(reader, outputStream);
        //stamper.setRotateContents(false);
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        for (int i = 1; i <= reader.getNumberOfPages(); i++)
        {
            Rectangle pageRect = reader.getCropBox(i);  
            Rectangle headerRect= new Rectangle(pageRect);
            headerRect.setBottom(headerRect.getTop()-upperY);               
            Rectangle footerRect= new Rectangle(pageRect);
            footerRect.setTop(footerRect.getBottom()+lowerY);   

            if(highLightColor)
            {
                cleanUpLocations.add(new PdfCleanUpLocation(i, headerRect,BaseColor.GREEN));
                cleanUpLocations.add(new PdfCleanUpLocation(i, footerRect,BaseColor.GREEN));
            }
            else
            {
                cleanUpLocations.add(new PdfCleanUpLocation(i, headerRect));
                cleanUpLocations.add(new PdfCleanUpLocation(i, footerRect));
            }
        }   
        PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
        try
        {
            cleaner.cleanUp();
        }
        catch(Exception e)
        {
             e.printStackTrace();
        }

        stamper.close();
        reader.close();
        outputStream.flush();
        outputStream.close();
    }
}
