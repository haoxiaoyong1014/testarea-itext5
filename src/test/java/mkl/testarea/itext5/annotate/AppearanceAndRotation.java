package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AppearanceAndRotation {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/48137530/freetext-annotation-appearance-stream-in-landscape-pdf-using-itext">
     * FreeText Annotation Appearance Stream In Landscape PDF Using iText
     * </a>
     * <p>
     * This test is a first approximation to what the OP may want; based
     * on this some clarifications were requested.
     * </p>
     */
    @Test
    public void testCreateWithAppearanceAndRotation() throws DocumentException, IOException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "appearanceAndRotation.pdf")));
        document.open();

        Rectangle rect = new Rectangle(50, 630, 150, 660);
        String contents = "Test";
        PdfAnnotation annotation = createAnnotation(writer, rect, contents);

        document.add(new Paragraph("Test paragraph"));
        writer.addAnnotation(annotation);

        document.setPageSize(PageSize.A4.rotate());
        document.newPage();

        rect = new Rectangle(50, 380, 80, 480);
        contents = "NoRotate flag";
        annotation = createAnnotation(writer, rect, contents);
        annotation.setFlags(PdfAnnotation.FLAGS_NOROTATE);

        document.add(new Paragraph("Test paragraph"));
        writer.addAnnotation(annotation);

        document.close();
    }

    PdfAnnotation createAnnotation(PdfWriter writer, Rectangle rect, String contents) throws DocumentException, IOException {
        PdfContentByte cb = writer.getDirectContent();
        PdfAppearance cs = cb.createAppearance(rect.getWidth(), rect.getHeight());

        cs.rectangle(0 , 0, rect.getWidth(), rect.getHeight());
        cs.fill();

        cs.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED), 12);                        
        cs.beginText();
        cs.setLeading(12 + 1.75f);
        cs.moveText(.75f, rect.getHeight() - 12 + .75f);
        cs.showText(contents);
        cs.endText();

        return PdfAnnotation.createFreeText(writer, rect, contents, cs);
    }
}
