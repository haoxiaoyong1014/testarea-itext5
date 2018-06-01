package mkl.testarea.itext5.pdfcleanup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;

/**
 * @author mkl
 *
 */
public class RedactText
{
    final static File OUTPUTDIR = new File("target/test-outputs/pdfcleanup");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        OUTPUTDIR.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35374912/itext-cleaning-up-text-in-rectangle-without-cleaning-full-row">
     * iText - Cleaning Up Text in Rectangle without cleaning full row
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/3i7g4w85dvul6db/input.pdf?dl=0">
     * input.pdf
     * </a>
     * <p>
     * Cannot reproduce the OP's issue.
     * </p>
     */
    @Test
    public void testRedactJavishsInput() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("input.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "input-redactedJavish.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);

            List<Float> linkBounds = new ArrayList<Float>();
            linkBounds.add(0, (float) 200.7);
            linkBounds.add(1, (float) 547.3);
            linkBounds.add(2, (float) 263.3);
            linkBounds.add(3, (float) 558.4);

            Rectangle linkLocation1 = new Rectangle(linkBounds.get(0), linkBounds.get(1), linkBounds.get(2), linkBounds.get(3));
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
            cleanUpLocations.add(new PdfCleanUpLocation(1, linkLocation1, BaseColor.GRAY));

            PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
            cleaner.cleanUp();

            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/35374912/itext-cleaning-up-text-in-rectangle-without-cleaning-full-row">
     * iText - Cleaning Up Text in Rectangle without cleaning full row
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/zeljo2k8tly7yqi/Test1.pdf?dl=0">
     * Test1.pdf
     * </a>
     * <p>
     * With this new file the issue could be reproduced, indeed an iText issue.
     * </p>
     */
    @Test
    public void testRedactJavishsTest1() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Test1.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "Test1-redactedJavish.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);

            List<Float> linkBounds = new ArrayList<Float>();
            linkBounds.add(0, (float) 202.3);
            linkBounds.add(1, (float) 588.6);
            linkBounds.add(2, (float) 265.8);
            linkBounds.add(3, (float) 599.7);

            Rectangle linkLocation1 = new Rectangle(linkBounds.get(0), linkBounds.get(1), linkBounds.get(2), linkBounds.get(3));
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
            cleanUpLocations.add(new PdfCleanUpLocation(1, linkLocation1, BaseColor.GRAY));

            PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
            cleaner.cleanUp();

            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/37713112/attempt-to-apply-redactions-results-in-exception">
     * Attempt to apply redactions results in exception
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0Bz0Wye-k7GsZUXdKbWtMTUNpcXc/view?usp=sharing">
     * 20150325101924-2102000595-SessionReport.pdf
     * </a>
     * <p>
     * This corresponds to the iTextSharp/C# code by the OP having fixed a first issue, the
     * use of annots added in the same PdfStamper sesion.
     * </p>
     * <p>
     * In Java no more exception occurs but in C# one does. This is due to iText using a HashMap
     * and iTextSharp using a Dictionary as a member of the PdfCleanUpProcessor to which
     * annotation rectangles are added by their annotations index in their respective page
     * annotations array (which is a logical error on both sides). HashMaps allow overwriting
     * entries, Dictionaries don't. Thus, iTextSharp hickups and iText does not.
     * </p>
     */
    @Test
    public void testRedact20150325101924_2102000595_SessionReport() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("20150325101924-2102000595-SessionReport.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "20150325101924-2102000595-SessionReport-annotated.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);

            PdfAnnotation pdfAnot1 = new PdfAnnotation(stamper.getWriter(), new Rectangle(165f, 685f, 320f, 702f));
            pdfAnot1.setTitle("First Page");
            pdfAnot1.put(PdfName.SUBTYPE, PdfName.REDACT);
            pdfAnot1.put(PdfName.IC, new PdfArray(new float[] { 0f, 0f, 0f }));
            pdfAnot1.put(PdfName.OC, new PdfArray(new float[] { 1f, 0f, 0f })); // red outline
            pdfAnot1.put(PdfName.QUADPOINTS, new PdfArray());
            stamper.addAnnotation(pdfAnot1, 1);
            for (int i = 1; i <= reader.getNumberOfPages(); i++)
            {
                PdfAnnotation pdfAnot2 = new PdfAnnotation(stamper.getWriter(), new Rectangle(220f, 752f, 420f, 768f));
                pdfAnot2.setTitle("Header");
                pdfAnot2.put(PdfName.SUBTYPE, PdfName.REDACT);
                pdfAnot2.put(PdfName.IC, new PdfArray(new float[] { 0f, 0f, 0f }));
                pdfAnot2.put(PdfName.OC, new PdfArray(new float[] { 1f, 0f, 0f })); // red outline
                pdfAnot2.put(PdfName.QUADPOINTS, new PdfArray());
                stamper.addAnnotation(pdfAnot2, i);
            }

            stamper.close();
        }

        try (   InputStream resource = new FileInputStream(new File(OUTPUTDIR, "20150325101924-2102000595-SessionReport-annotated.pdf"));
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "20150325101924-2102000595-SessionReport-redacted.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);

            PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(stamper);
            cleaner.cleanUp();
            
            stamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38053804/itextsharp-cropping-pdf-with-images-throws-exception">
     * iTextSharp - Cropping PDF with images throws exception
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/i76q8j1n5b0kdtv/1.pdf?dl=0">
     * 1.pdf
     * </a>, as Narek-1.pdf here
     * <p>
     * Indeed, there is an exception reading this image from PDF.
     * </p>
     */
    @Test
    public void testRedactNarek_1() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Narek-1.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "Narek-1-redacted.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);
            redactLikeNarek(stamper);
            stamper.close();
        }
    }
    
    /**
     * <a href="http://stackoverflow.com/questions/38053804/itextsharp-cropping-pdf-with-images-throws-exception">
     * iTextSharp - Cropping PDF with images throws exception
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/i8bg76033oj5bug/2.pdf?dl=0">
     * 2.pdf
     * </a>, as Narek-2.pdf here
     * <p>
     * The issue cannot be reproduced with Java. It can be reproduced with C#, though.
     * </p>
     * <p>
     * Nonetheless, even here the image is not properly redacted, merely covered.
     * </p>
     */
    @Test
    public void testRedactNarek_2() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Narek-2.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "Narek-2-redacted.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);
            redactLikeNarek(stamper);
            stamper.close();
        }
    }
    
    /**
     * <a href="http://stackoverflow.com/questions/38053804/itextsharp-cropping-pdf-with-images-throws-exception">
     * iTextSharp - Cropping PDF with images throws exception
     * </a>
     * <p>
     * The OP's redaction code ported to Java.
     * </p>
     */
    void redactLikeNarek(PdfStamper stamper) throws IOException, DocumentException
    {
        Rectangle redactionRectangle = new Rectangle(74, 503, 385, 761);
        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, redactionRectangle, BaseColor.WHITE));
        PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
        cleaner.cleanUp();
    }

    /**
     * <a href="http://stackoverflow.com/questions/38278816/remove-header-of-a-pdf-using-itext-pdfcleanupprocessor-does-not-work">
     * Remove header of a pdf using iText PdfCleanUpProcessor does not work
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/4u8vupjqc4st3ib/love.pdf?dl=0">
     * love.pdf
     * </a>
     * <p>
     * Cannot reproduce, I get a <code>org.apache.commons.imaging.ImageReadException: Invalid marker found in entropy data</code>.
     * </p>
     */
    @Test
    public void testRedactLikeShiranSEkanayake() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("love.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "love-redacted.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

            for(int i=1; i<=reader.getNumberOfPages(); i++)
            {
                    //System.out.println(i);
                    Rectangle mediabox = reader.getPageSize(i); 
                    cleanUpLocations.add(new PdfCleanUpLocation(i, new Rectangle(0,800,1000,1000)));
            }
            PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
            cleaner.cleanUp();
            stamper.close();
            reader.close(); 
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38605538/itextpdf-redaction-partly-redacted-text-string-is-fully-removed">
     * itextpdf Redaction :Partly redacted text string is fully removed
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B42NqA5UnXMVMDc4MnE5VmU5YVk/view">
     * Document.pdf
     * </a>
     * <p>
     * This indeed is a case which shows that glyphs are completely removed even if their
     * bounding box merely minutely intersects the redaction area. While not desired by
     * the OP, this is how <code>PdfCleanUp</code> works.
     * </p>
     * 
     * @see #testRedactStrictForMayankPandey()
     * @see #testRedactStrictForMayankPandeyLarge()
     */
    @Test
    public void testRedactLikeMayankPandey() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Document.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "Document-redacted.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfCleanUpProcessor cleaner= null;
            PdfStamper stamper = new PdfStamper(reader, result);
            stamper.setRotateContents(false);
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
            Rectangle rectangle = new Rectangle(380, 640, 430, 665);
            cleanUpLocations.add(new PdfCleanUpLocation(1, rectangle, BaseColor.BLACK));
            cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);   
            cleaner.cleanUp();
            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38605538/itextpdf-redaction-partly-redacted-text-string-is-fully-removed">
     * itextpdf Redaction :Partly redacted text string is fully removed
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B42NqA5UnXMVMDc4MnE5VmU5YVk/view">
     * Document.pdf
     * </a>
     * <p>
     * This test applies the redaction using the {@link StrictPdfCleanUpProcessor}
     * which in contrast to the {@link PdfCleanUpProcessor} causes only text to be
     * removed if it is <b>completely</b> inside the redaction zone . The original
     * removes also text located merely <b>partially</b> inside the redaction zone.
     * </p>
     * <p>
     * This might more correspond to what the OP desires.
     * </p>
     * @see #testRedactLikeMayankPandey()
     * @see #testRedactStrictForMayankPandeyLarge()
     */
    @Test
    public void testRedactStrictForMayankPandey() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Document.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "Document-redacted-strict.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            StrictPdfCleanUpProcessor cleaner= null;
            PdfStamper stamper = new PdfStamper(reader, result);
            stamper.setRotateContents(false);
            List<mkl.testarea.itext5.pdfcleanup.PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();
            Rectangle rectangle = new Rectangle(380, 640, 430, 665);
            cleanUpLocations.add(new mkl.testarea.itext5.pdfcleanup.PdfCleanUpLocation(1, rectangle, BaseColor.BLACK));
            cleaner = new StrictPdfCleanUpProcessor(cleanUpLocations, stamper);   
            cleaner.cleanUp();
            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38605538/itextpdf-redaction-partly-redacted-text-string-is-fully-removed">
     * itextpdf Redaction :Partly redacted text string is fully removed
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B42NqA5UnXMVMDc4MnE5VmU5YVk/view">
     * Document.pdf
     * </a>
     * <p>
     * This test applies the redaction using the {@link StrictPdfCleanUpProcessor}
     * which in contrast to the {@link PdfCleanUpProcessor} causes only text to be
     * removed if it is <b>completely</b> inside the redaction zone . The original
     * removes also text located merely <b>partially</b> inside the redaction zone.
     * Furthermore this test uses a larger redaction zone to check whether text
     * completely contained in the redaction zone really is removed.
     * </p>
     * <p>
     * This might more correspond to what the OP desires.
     * </p>
     * @see #testRedactLikeMayankPandey()
     * @see #testRedactStrictForMayankPandey()
     */
    @Test
    public void testRedactStrictForMayankPandeyLarge() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Document.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "Document-redacted-strict-large.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            StrictPdfCleanUpProcessor cleaner= null;
            PdfStamper stamper = new PdfStamper(reader, result);
            stamper.setRotateContents(false);
            List<mkl.testarea.itext5.pdfcleanup.PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();
            Rectangle rectangle = new Rectangle(380, 640, 430, 680);
            cleanUpLocations.add(new mkl.testarea.itext5.pdfcleanup.PdfCleanUpLocation(1, rectangle, BaseColor.BLACK));
            cleaner = new StrictPdfCleanUpProcessor(cleanUpLocations, stamper);   
            cleaner.cleanUp();
            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/43211367/getting-exception-while-redacting-pdf-using-itext">
     * getting exception while redacting pdf using itext
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B-zalNTEeIOwM1JJVWctcW8ydU0/view?usp=drivesdk">
     * edited_120192824_5 (1).pdf
     * </a>
     * <p>
     * Indeed, the PdfClean classes throw a {@link NullPointerException} on page
     * 1 of this PDF. As it turns out, the cause is that the PDF makes use of a
     * construct which according to the PDF specification is obsolete and iText,
     * therefore, chose not to support. 
     * </p>
     */
    @Test
    public void testRedactLikeDevAvitesh() throws DocumentException, IOException
    {
//      InputStream resource = new FileInputStream("D:/itext/edited_120192824_5 (1).pdf");
//      OutputStream result = new FileOutputStream(new File(OUTPUTDIR,
//              "aviteshs.pdf"));

        try (   InputStream resource = getClass().getResourceAsStream("edited_120192824_5 (1).pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "edited_120192824_5 (1)-redacted.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);
            int pageCount = reader.getNumberOfPages();
            Rectangle linkLocation1 = new Rectangle(440f, 700f, 470f, 710f);
            Rectangle linkLocation2 = new Rectangle(308f, 205f, 338f, 215f);
            Rectangle linkLocation3 = new Rectangle(90f, 155f, 130f, 165f);
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
            for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                if (currentPage == 1) {
                    cleanUpLocations.add(new PdfCleanUpLocation(currentPage,
                            linkLocation1, BaseColor.BLACK));
                    cleanUpLocations.add(new PdfCleanUpLocation(currentPage,
                            linkLocation2, BaseColor.BLACK));
                    cleanUpLocations.add(new PdfCleanUpLocation(currentPage,
                            linkLocation3, BaseColor.BLACK));
                } else {
                    cleanUpLocations.add(new PdfCleanUpLocation(currentPage,
                            linkLocation1, BaseColor.BLACK));
                }
            }
            PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations,
                    stamper);
            try {
                cleaner.cleanUp();
            } catch (Exception e) {
                e.printStackTrace();
            }
            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/44304695/itext-5-5-11-bold-text-looks-blurry-after-using-pdfcleanupprocessor">
     * iText 5.5.11 - bold text looks blurry after using PdfCleanUpProcessor
     * </a>
     * <br/>
     * <a href="http://s000.tinyupload.com/index.php?file_id=52420782334200922303">
     * before.pdf
     * </a>
     * <p>
     * Indeed, the observation by the OP can be reproduced. The issue has been introduced
     * into iText in commits d5abd23 and 9967627, both dated May 4th, 2015.
     * </p>
     */
    @Test
    public void testRedactLikeTieco() throws DocumentException, IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("before.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "before-redacted.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, result);
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

            cleanUpLocations.add(new PdfCleanUpLocation(1, new Rectangle(0f, 0f, 595f, 680f)));

            PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
            cleaner.cleanUp();

            stamper.close();
            reader.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/38240692/error-in-redaction-with-itext-5-the-color-depth-1-is-not-supported-exception">
     * Error in redaction with iText 5: “The color depth 1 is not supported.” exception when apply redaction on pdf which contain image also
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B42NqA5UnXMVbkhQQk9tR2hpSUE/view?pref=2&pli=1">
     * Pages from Miscellaneous_corrupt.pdf
     * </a>
     * <p>
     * In iText 5.5.11 a work-around for this issue has been added to iText, images in
     * formats not explicitly supported by itext are now removed as a whole if they
     * intersect a redaction area.
     * </p>
     */
    @Test
    public void testRedactLikeMayankPandeyPagesfromMiscellaneous_corrupt() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Pages from Miscellaneous_corrupt.pdf");
                OutputStream result = new FileOutputStream(new File(OUTPUTDIR, "Pages from Miscellaneous_corrupt-redacted.pdf")) )
        {
            PdfReader reader = new PdfReader(resource);
            PdfCleanUpProcessor cleaner= null;
            PdfStamper stamper = new PdfStamper(reader, result);
            stamper.setRotateContents(false);
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
            Rectangle rectangle = new Rectangle(190, 320, 430, 665);
            cleanUpLocations.add(new PdfCleanUpLocation(1, rectangle, BaseColor.BLACK));
            cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);   
            cleaner.cleanUp();
            stamper.close();
            reader.close();
        }
    }
}
