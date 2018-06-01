package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfShading;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfTransparencyGroup;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <a href="http://stackoverflow.com/questions/28484590/how-to-set-fill-alpha-in-pdf">
 * How to set fill alpha in PDF
 * </a>
 * <br/>
 * <a href="http://stackoverflow.com/questions/35316421/shading-with-transparency-with-itextdpf">
 * shading with transparency with itextdpf
 * </a>
 * <p>
 * Demonstrating transparency effects with iText.
 * </p>
 * 
 * @author mkl
 */
public class TestTransparency
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testSimple() throws FileNotFoundException, DocumentException
    {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "transparency.pdf")));
        writer.setCompressionLevel(0);
        document.open();
        PdfContentByte content = writer.getDirectContent();

        content.setRGBColorStroke(0, 255, 0);
        for (int y = 0; y <= 400; y+= 10)
        {
            content.moveTo(0, y);
            content.lineTo(500, y);
        }
        for (int x = 0; x <= 500; x+= 10)
        {
            content.moveTo(x, 0);
            content.lineTo(x, 400);
        }
        content.stroke();

        
        content.saveState();
        PdfGState state = new PdfGState();
        state.setFillOpacity(0.5f);
        content.setGState(state);
        content.setRGBColorFill(255, 0, 0);
        content.moveTo(162, 86);
        content.lineTo(162, 286);
        content.lineTo(362, 286);
        content.lineTo(362, 86);
        content.closePath();
        //content.fillStroke();
        content.fill();
        
        content.restoreState();

        document.close();
    }

    @Test
    public void testComplex() throws FileNotFoundException, DocumentException
    {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "transparencyComplex.pdf")));
        writer.setCompressionLevel(0);
        document.open();
        PdfContentByte content = writer.getDirectContent();

        content.setRGBColorStroke(0, 255, 0);
        for (int y = 0; y <= 400; y+= 10)
        {
            content.moveTo(0, y);
            content.lineTo(500, y);
        }
        for (int x = 0; x <= 500; x+= 10)
        {
            content.moveTo(x, 0);
            content.lineTo(x, 400);
        }
        content.stroke();

        PdfTemplate template = content.createTemplate(500, 400);
        PdfTransparencyGroup group = new PdfTransparencyGroup();
        group.put(PdfName.CS, PdfName.DEVICEGRAY);
        group.setIsolated(false);
        group.setKnockout(false);
        template.setGroup(group);
        PdfShading radial = PdfShading.simpleRadial(writer, 262, 186, 10, 262, 186, 190, BaseColor.WHITE, BaseColor.BLACK, true, true);
        template.paintShading(radial);

        PdfDictionary mask = new PdfDictionary();
        mask.put(PdfName.TYPE, PdfName.MASK);
        mask.put(PdfName.S, new PdfName("Luminosity"));
        mask.put(new PdfName("G"), template.getIndirectReference());

        content.saveState();
        PdfGState state = new PdfGState();
        state.put(PdfName.SMASK, mask);
        content.setGState(state);
        content.setRGBColorFill(255, 0, 0);
        content.moveTo(162, 86);
        content.lineTo(162, 286);
        content.lineTo(362, 286);
        content.lineTo(362, 86);
        content.closePath();
        //content.fillStroke();
        content.fill();
        
        content.restoreState();

        document.close();
    }
}
