package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfIndirectReference;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;

/**
 * @author mkl
 */
public class ProcessLink {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/49370352/how-do-i-get-a-get-destination-page-of-a-link-in-pdf-file">
     * How do I get a get destination page of a link in PDF file?
     * </a>
     * <br/>
     * local-link.pdf - output of the test {@link CreateLink}.
     * <p>
     * This test shows how to access data of the target page, once by
     * directly reading from the page dictionary referenced from the
     * link destination, once by first determining the page number and
     * then using {@link PdfReader} helper methods.
     * </p>
     */
    @Test
    public void testDetermineTargetPage() throws IOException {
        try (   InputStream src = getClass().getResourceAsStream("local-link.pdf")  ) {
            PdfReader reader = new PdfReader(src);
            PdfDictionary page = reader.getPageN(1);
            PdfArray annots = page.getAsArray(PdfName.ANNOTS); 
            for (int i = 0; i < annots.size(); i++) {
                PdfDictionary annotation = annots.getAsDict(i);
                if (PdfName.LINK.equals(annotation.getAsName(PdfName.SUBTYPE))) {
                    PdfArray d = annotation.getAsArray(PdfName.DEST);
                    if (d == null) {
                        PdfDictionary action = annotation.getAsDict(PdfName.A);
                        if (action != null)
                            d = action.getAsArray(PdfName.D);
                    }
                        
                    if (d != null && d.size() > 0) {
                        System.out.println("Next destination -");
                        PdfIndirectReference pageReference = d.getAsIndirectObject(0);

                        // Work with target dictionary directly
                        PdfDictionary pageDict = d.getAsDict(0);
                        PdfArray boxArray = pageDict.getAsArray(PdfName.CROPBOX);
                        if (boxArray == null) {
                            boxArray = pageDict.getAsArray(PdfName.MEDIABOX);
                        }
                        Rectangle box = PdfReader.getNormalizedRectangle(boxArray);
                        System.out.printf("* Target page object %s has cropbox %s\n", pageReference, box);

                        // Work via page number
                        for (int pageNr = 1; pageNr <= reader.getNumberOfPages(); pageNr++) {
                            PRIndirectReference pp = reader.getPageOrigRef(pageNr);
                            if (pp.getGeneration() == pageReference.getGeneration() && pp.getNumber() == pageReference.getNumber()) {
                                System.out.printf("* Target page %s has cropbox %s\n", pageNr, reader.getCropBox(pageNr));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

}
