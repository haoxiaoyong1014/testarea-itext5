package mkl.testarea.itext5.merge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

/**
 * @author mkl
 */
public class Merging {
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/46120706/when-i-open-a-pdf-in-adobe-acrobat-pro-dc-the-text-is-getting-messed-up-for-pdf">
     * when i open a pdf in adobe acrobat pro Dc the text is getting messed up for pdf creation i used itext5.5.12
     * </a>
     * <p>
     * Test files "1 Loyds.pdf", "2 CRPWLI.pdf", "3 SLC Dec.pdf", "4 Schedule.pdf",
     * and "5 Sched of INS.pdf" were received via e-mail from Kishore Rachakonda
     * (kishore.rachakonda@yash.com) on 2017-09-11 17:31.
     * </p>
     * <p>
     * This test is a port of the Ruby-on-Rails merge routine provided by the OP.
     * The result does not have the problem described by the OP.
     * </p>
     * <p>
     * Later it became clear that the OP's merge result was post-processed by at
     * least two other programs, and one of those post processors appears to have
     * optimized the use of embedded fonts. Unfortunately "5 Sched of INS.pdf" is
     * not a completely valid PDF: It uses an embedded subset of a font but does
     * not mark the font name accordingly. Thus, the optimizing post processor
     * added this mere font subset (assuming it to be the whole font program) to
     * font resources which require glyphs missing in the subset.
     * </p> 
     */
    @Test
    public void testMergeLikeKishoreSagar() throws IOException, DocumentException {
        try (   InputStream resource1 = getClass().getResourceAsStream("1 Loyds.pdf");
                InputStream resource2 = getClass().getResourceAsStream("2 CRPWLI.pdf");
                InputStream resource3 = getClass().getResourceAsStream("3 SLC Dec.pdf");
                InputStream resource4 = getClass().getResourceAsStream("4 Schedule.pdf");
                InputStream resource5 = getClass().getResourceAsStream("5 Sched of INS.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "mergeLikeKishoreSagar.pdf"))) {
            InputStream[] pdf_files = {resource1, resource2, resource3, resource4, resource5};

            Document doc = new Document();
            PdfCopy pdf_copy = new PdfCopy(doc, result);
            doc.open();
            
            for (InputStream pdf : pdf_files) {
                PdfReader reader = new PdfReader(pdf);
                int pages = reader.getNumberOfPages();
                for (int p = 1; p <= pages; p++)
                    pdf_copy.addPage(pdf_copy.getImportedPage(reader, p));
                reader.close();
            }

            doc.close();
        }

        /* ported from the original:
  doc =Document.new
  pdf_copy = PdfCopy.new(doc, FileStream.new(@output_filename))
  doc.open
  @pdf_files.each do |pdf|
    reader = PdfReader.new(pdf)
    pages = reader.getNumberOfPages()
    (1..pages).each do |p|
      pdf_copy.addPage(pdf_copy.getImportedPage(reader, p))
    end
    reader.close
  end
  doc.close
         */
    }

}
