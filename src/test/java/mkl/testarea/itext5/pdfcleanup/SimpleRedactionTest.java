package mkl.testarea.itext5.pdfcleanup;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PatternColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPatternPainter;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;

/**
 * Miscellaneous tests of iText xtra pdfcleanup functions.
 * 
 * @author mkl
 */
public class SimpleRedactionTest
{
    final static File OUTPUTDIR = new File("target/test-outputs/pdfcleanup");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        OUTPUTDIR.mkdirs();
    }

    //
    // The tests
    //
    @Test
    public void testSimpleRedactionImmediate() throws DocumentException, IOException
    {
        byte[] simple = createSimpleTextPdf();
        Files.write(new File(OUTPUTDIR, "simpleImmediate.pdf").toPath(), simple);

        byte[] redacted = cleanUp(simple, Collections.singletonList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f, 445f), BaseColor.GRAY)));
        Files.write(new File(OUTPUTDIR, "simpleImmediateRedacted.pdf").toPath(), redacted);
    }

    @Test
    public void testSimpleRedactionIndirect() throws DocumentException, IOException
    {
        byte[] simple = createSimpleIndirectTextPdf();
        Files.write(new File(OUTPUTDIR, "simpleIndirect.pdf").toPath(), simple);

        byte[] redacted = cleanUp(simple, Collections.singletonList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f, 445f), BaseColor.GRAY)));
        Files.write(new File(OUTPUTDIR, "simpleIndirectRedacted.pdf").toPath(), redacted);
    }

    @Test
    public void testRotatedRedactionIndirect() throws DocumentException, IOException
    {
        byte[] simple = createRotatedIndirectTextPdf();
        Files.write(new File(OUTPUTDIR, "rotateIndirect.pdf").toPath(), simple);

        byte[] redacted = cleanUp(simple, Collections.singletonList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f, 445f), BaseColor.GRAY)));
        Files.write(new File(OUTPUTDIR, "rotateIndirectRedacted.pdf").toPath(), redacted);
    }

    @Test
    public void testSimpleRedactionPattern() throws DocumentException, IOException
    {
        byte[] simple = createSimplePatternPdf();
        Files.write(new File(OUTPUTDIR, "simplePattern.pdf").toPath(), simple);

        byte[] redacted = cleanUp(simple, Collections.singletonList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f, 445f), BaseColor.GRAY)));
        Files.write(new File(OUTPUTDIR, "simplePatternRedacted.pdf").toPath(), redacted);
    }

    @Test
    public void testSimpleRedactionClipText() throws DocumentException, IOException
    {
        byte[] simple = createClippingTextPdf();
        Files.write(new File(OUTPUTDIR, "simpleClipText.pdf").toPath(), simple);

        byte[] redacted = cleanUp(simple, Collections.singletonList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f, 445f), BaseColor.GRAY)));
        Files.write(new File(OUTPUTDIR, "simpleClipTextRedacted.pdf").toPath(), redacted);
    }

    @Test
    public void testMultiUseRedactionIndirect() throws DocumentException, IOException
    {
        byte[] simple = createMultiUseIndirectTextPdf();
        Files.write(new File(OUTPUTDIR, "multiUseIndirect.pdf").toPath(), simple);

        byte[] redacted = cleanUp(simple, Collections.singletonList(new PdfCleanUpLocation(1, new Rectangle(97f, 605f, 480f, 645f), BaseColor.GRAY)));
        Files.write(new File(OUTPUTDIR, "multiUseIndirectRedacted.pdf").toPath(), redacted);
    }

    @Test
    public void testSimpleRedactionImage() throws DocumentException, IOException
    {
        byte[] simple = createSimpleImagePdf();
        Files.write(new File(OUTPUTDIR, "simpleImage.pdf").toPath(), simple);

        byte[] redacted = cleanUp(simple, Collections.singletonList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f, 445f), BaseColor.GRAY)));
        Files.write(new File(OUTPUTDIR, "simpleImageRedacted.pdf").toPath(), redacted);
    }

    @Test
    public void testRotatedRedactionImage() throws DocumentException, IOException
    {
        byte[] simple = createRotatedImagePdf();
        Files.write(new File(OUTPUTDIR, "rotatedImage.pdf").toPath(), simple);

        byte[] redacted = cleanUp(simple, Collections.singletonList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f, 445f), BaseColor.GRAY)));
        Files.write(new File(OUTPUTDIR, "rotatedImageRedacted.pdf").toPath(), redacted);
    }

    @Test
    public void testMultiUseRedactionImage() throws DocumentException, IOException
    {
        byte[] simple = createMultiUseImagePdf();
        Files.write(new File(OUTPUTDIR, "multiUseImage.pdf").toPath(), simple);

        byte[] redacted = cleanUp(simple, Collections.singletonList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f, 445f), BaseColor.GRAY)));
        Files.write(new File(OUTPUTDIR, "multiUseImageRedacted.pdf").toPath(), redacted);
    }

    @Test
    public void testSMaskRedactionImage() throws DocumentException, IOException
    {
        byte[] simple = createSmaskImagePdf();
        Files.write(new File(OUTPUTDIR, "smaskImage.pdf").toPath(), simple);

        byte[] redacted = cleanUp(simple, Collections.singletonList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f, 445f), BaseColor.GRAY)));
        Files.write(new File(OUTPUTDIR, "smaskImageRedacted.pdf").toPath(), redacted);
    }

    //
    // helper methods creating test PDFs
    //
    static byte[] createSimpleTextPdf() throws DocumentException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();
        for (int i = 1; i < 20; i++)
        {
            Paragraph paragraph = new Paragraph();
            for (int j = 0; j < i; j++)
                paragraph.add("Hello World! ");
            document.add(paragraph);
        }
        document.close();

        return baos.toByteArray();
    }

    static byte[] createSimpleIndirectTextPdf() throws DocumentException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        PdfReader reader = new PdfReader(createSimpleTextPdf());
        writer.getDirectContent().addTemplate(writer.getImportedPage(reader, 1), 0, 0);
        document.close();

        return baos.toByteArray();
    }

    static byte[] createMultiUseIndirectTextPdf() throws DocumentException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        PdfReader reader = new PdfReader(createSimpleTextPdf());
        PdfImportedPage template = writer.getImportedPage(reader, 1);
        Rectangle pageSize = reader.getPageSize(1);
        writer.getDirectContent().addTemplate(template, 0, .7f, -.7f, 0, pageSize.getRight(), (pageSize.getTop() + pageSize.getBottom()) / 2);
        writer.getDirectContent().addTemplate(template, 0, .7f, -.7f, 0, pageSize.getRight(), pageSize.getBottom());
        document.newPage();
        writer.getDirectContent().addTemplate(template, pageSize.getLeft(), pageSize.getBottom());
        document.close();

        return baos.toByteArray();
    }

    static byte[] createRotatedIndirectTextPdf() throws DocumentException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        PdfReader reader = new PdfReader(createSimpleTextPdf());
        PdfImportedPage template = writer.getImportedPage(reader, 1);
        Rectangle pageSize = reader.getPageSize(1);
        writer.getDirectContent().addTemplate(template, .7f, .7f, -.7f, .7f, 400, -200);
        document.newPage();
        writer.getDirectContent().addTemplate(template, pageSize.getLeft(), pageSize.getBottom());
        document.close();

        return baos.toByteArray();
    }

    static byte[] createClippingTextPdf() throws DocumentException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        PdfContentByte directContent = writer.getDirectContent();
        directContent.beginText();
        directContent.setTextRenderingMode(PdfPatternPainter.TEXT_RENDER_MODE_CLIP);
        directContent.setTextMatrix(AffineTransform.getTranslateInstance(100, 400));
        directContent.setFontAndSize(BaseFont.createFont(), 100);
        directContent.showText("Test");
        directContent.endText();
        
        BufferedImage bim = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bim.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 500, 500);
        g2d.dispose();

        Image image = Image.getInstance(bim, null);
        directContent.addImage(image, 500, 0, 0, 599, 50, 50);
        document.close();

        return baos.toByteArray();
    }

    static byte[] createSimpleImagePdf() throws DocumentException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        BufferedImage bim = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bim.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 500, 500);
        g2d.dispose();

        Image image = Image.getInstance(bim, null);
        document.add(image);

        document.close();

        return baos.toByteArray();
    }

    static byte[] createRotatedImagePdf() throws DocumentException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        PdfContentByte directContent = writer.getDirectContent();

        BufferedImage bim = new BufferedImage(1000, 250, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bim.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 500, 500);
        g2d.dispose();

        Image image = Image.getInstance(bim, null);
        directContent.addImage(image, 0, 500, -500, 0, 550, 50);

        document.close();

        return baos.toByteArray();
    }

    static byte[] createMultiUseImagePdf() throws DocumentException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        BufferedImage bim = new BufferedImage(500, 250, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bim.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 250, 250);
        g2d.dispose();

        Image image = Image.getInstance(bim, null);
        document.add(image);
        document.add(image);
        document.add(image);

        document.close();

        return baos.toByteArray();
    }

    static byte[] createSmaskImagePdf() throws DocumentException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        BufferedImage bim = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bim.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 500, 500);
        g2d.dispose();

        BufferedImage bmask = new BufferedImage(500, 500, BufferedImage.TYPE_BYTE_GRAY);
        g2d = bmask.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 500, 500);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(200, 0, 100, 500);
        g2d.dispose();

        Image image = Image.getInstance(bim, null);
        Image mask = Image.getInstance(bmask, null, true);
        mask.makeMask();
        image.setImageMask(mask);
        document.add(image);

        document.close();

        return baos.toByteArray();
    }
    
    static byte[] createSimplePatternPdf() throws DocumentException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        PdfContentByte directContent = writer.getDirectContent();
        Rectangle pageSuze = document.getPageSize();
        
        PdfPatternPainter painter = directContent.createPattern(200, 150);
        painter.setColorStroke(BaseColor.GREEN);
        painter.beginText();
        painter.setTextRenderingMode(PdfPatternPainter.TEXT_RENDER_MODE_STROKE);
        painter.setTextMatrix(AffineTransform.getTranslateInstance(0, 50));
        painter.setFontAndSize(BaseFont.createFont(), 100);
        painter.showText("Test");
        painter.endText();

        directContent.setColorFill(new PatternColor(painter));
        directContent.rectangle(pageSuze.getLeft(), pageSuze.getBottom(), pageSuze.getWidth(), pageSuze.getHeight());
        directContent.fill();

        document.close();

        return baos.toByteArray();
    }

    //
    // other helper methods
    //
    static byte[] cleanUp(byte[] source, List<PdfCleanUpLocation> cleanUpLocations) throws IOException, DocumentException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfReader reader = new PdfReader(source);
        PdfStamper stamper = new PdfStamper(reader, baos);
        PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
        cleaner.cleanUp();
        stamper.close();
        reader.close();
        
        return baos.toByteArray();
    }
}
