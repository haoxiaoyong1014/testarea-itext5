/**
 * 
 */
package mkl.testarea.itext5.content;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class BinaryTransparency
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");
    private static Image bkgnd;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        bkgnd = Image.getInstance(new URL("http://gitlab.itextsupport.com/itext/sandbox/raw/master/resources/images/berlin2013.jpg"));
        bkgnd.scaleAbsolute(PageSize.A4);
        bkgnd.setAbsolutePosition(0, 0);
    }

    /**
     * <a href="http://stackoverflow.com/questions/39119776/itext-binary-transparency-bug">
     * iText binary transparency bug
     * </a>
     * <p>
     * Indeed, there is a bug in {@link Image#getInstance(Image, Color, boolean)},
     * the loop which determines whether to use a transparency array or a softmask
     * is erroneous and here falsely indicates a transparency array suffices.
     * </p>
     */
    @Test
    public void testBinaryTransparencyBug() throws IOException, DocumentException
    {
        Document document = new Document();
        File file = new File(RESULT_FOLDER, "binary_transparency_bug.pdf");
        FileOutputStream outputStream = new FileOutputStream(file);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();

        addBackground(writer);
        document.add(new Paragraph("Binary transparency bug test case"));
        document.add(new Paragraph("OK: Visible image (opaque pixels are red, non opaque pixels are black)"));
        document.add(com.itextpdf.text.Image.getInstance(createBinaryTransparentAWTImage(Color.red,false,null), null));
        document.newPage();

        addBackground(writer);
        document.add(new Paragraph("Suspected bug: invisible image (both opaque an non opaque pixels have the same color)"));
        document.add(com.itextpdf.text.Image.getInstance(createBinaryTransparentAWTImage(Color.black,false,null), null));
        document.newPage();

        addBackground(writer);
        document.add(new Paragraph("Analysis: Aliasing makes the problem disappear, because this way the image is not binary transparent any more"));
        document.add(com.itextpdf.text.Image.getInstance(createBinaryTransparentAWTImage(Color.black,true,null), null));
        document.newPage();

        addBackground(writer);
        document.add(new Paragraph("Analysis: Setting the color of the transparent pixels to anything but black makes the problem go away, too"));
        document.add(com.itextpdf.text.Image.getInstance(createBinaryTransparentAWTImage(Color.black,false,Color.red), null));

        document.close();
    }

    private static void addBackground(PdfWriter writer)
            throws BadElementException, MalformedURLException, IOException, DocumentException {
        PdfContentByte canvas = writer.getDirectContentUnder();
        canvas.saveState();
        canvas.addImage(bkgnd);
        canvas.restoreState();
    }

    // Create an ARGB AWT Image that has only 100% transparent and 0%
    // transparent pixels.
    // All 100% opaque pixels have the provided color "color"
    // All transparent pixels have the Color "backgroundColor"
    public static BufferedImage createBinaryTransparentAWTImage(Color color, boolean alias, Color backgroundColor) {
        Dimension size = new Dimension(200, 200);
        BufferedImage awtimg = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = awtimg.createGraphics();

        if (backgroundColor!=null)
        {
            //Usually it doen't make much sense to set the color of transparent pixels...
            //but in this case it changes the behavior of com.itextpdf.text.Image.getInstance fundamentally!
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0f));       
            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, size.width, size.height);
        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2d.setColor(color);
        if (alias)
        {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }

        BasicStroke bs = new BasicStroke(2);
        g2d.setStroke(bs);
        for (int i = 0; i < 5; i++) {
            g2d.drawLine((size.width + 2) / 4 * i, 0, (size.width + 2) / 4 * i, size.height - 1);
            g2d.drawLine(0, (size.height + 2) / 4 * i, size.width - 1, (size.height + 2) / 4 * i);
        }
        return awtimg;
    }
}
