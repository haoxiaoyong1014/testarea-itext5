package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class AnnotationIcons {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/46204693/cant-get-itext-rectangle-to-work-correctly-with-annotations">
     * Can't get itext Rectangle to work correctly with annotations
     * </a>
     * <p>
     * This test looks at a <b>Text</b> annotation added via a {@link Chunk}
     * as done by the OP. As this way of adding annotations resets the
     * annotation <b>Rect</b> to the bounding box of the rendered {@link Chunk},
     * it is not really what the OP wants.
     * </p>
     */
    @Test
    public void testAnnotationIconForTYD() throws FileNotFoundException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "annotationIcons.pdf")));
        document.open();

        // Not "new Rectangle(164, 190, 164, 110)" which would be empty
        Rectangle rect = new Rectangle(164, 190, 328, 300);

        // Annotation added like the OP does
        Chunk chunk_text = new Chunk("Let's test a Text annotation...");
        chunk_text.setAnnotation(PdfAnnotation.createText(writer, rect, "Warning", "This is a Text annotation with Comment icon.", false, "Comment"));        

        document.add(chunk_text);

        // Annotation added to the document without Chunk
        writer.addAnnotation(PdfAnnotation.createText(writer, rect, "Warning 2", "This is another Text annotation with Comment icon.", false, "Comment"));

        document.close();
    }

}
