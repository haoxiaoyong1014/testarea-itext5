// $Id$
package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfLiteral;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;

/**
 * <a href="http://stackoverflow.com/questions/35526822/removing-watermark-from-pdf-itextsharp">
 * Removing Watermark from PDF iTextSharp
 * </a>
 * <br/>
 * <a href="https://www.dropbox.com/s/qvlo1v9uzgpu4nj/test3.pdf?dl=0">
 * test3.pdf
 * </a>
 * <p>
 * This class tests the {@link PdfContentStreamEditor} and {@link TransparentGraphicsRemover}
 * classes.
 * </p>
 * <p>
 * {@link PdfContentStreamEditor} is the base editor classes which by default acts as the
 * identity operation (or at least it produces an equivalent content stream).
 * </p>
 * <p>
 * {@link TransparentGraphicsRemover} on the other hand changes all vector graphics drawing
 * operations so that operations which would create somewhat transparent result are dropped.
 * This suffices to not draw the watermark in the OP's sample PDF
 * </p>
 * <p>
 * Beware: This latter sample editor is very simple:
 * </p>
 * <ul>
 * <li>It only considers transparency created by the ExtGState parameters ca and CA, it
 * in particular ignores masks.
 * <li>It does not look for operations saving or restoring the graphics state.
 * </ul>
 * <p>
 * These limitations can easily be lifted but require more code than appropriate for a
 * stackoverflow answer.
 * </p>
 * 
 * @author mkl
 */
public class EditPageContent
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testIdentityTest3() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test3.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test3-identity.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            PdfContentStreamEditor identityEditor = new PdfContentStreamEditor();

            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++)
            {
                identityEditor.editPage(pdfStamper, i);
            }
            
            pdfStamper.close();
        }
    }

    @Test
    public void testIdentity20150211600() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/itext5/extract/20150211600.PDF");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "20150211600-identity.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            PdfContentStreamEditor identityEditor = new PdfContentStreamEditor();

            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++)
            {
                identityEditor.editPage(pdfStamper, i);
            }
            
            pdfStamper.close();
        }
    }

    @Test
    public void testRemoveTransparentGraphicsTest3() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test3.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test3-noTransparency.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            PdfContentStreamEditor editor = new TransparentGraphicsRemover();

            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++)
            {
                editor.editPage(pdfStamper, i);
            }
            
            pdfStamper.close();
        }
    }

    @Test
    public void testRemoveTransparentGraphicsTransparency() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("transparency.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "transparency-noTransparency.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            PdfContentStreamEditor editor = new TransparentGraphicsRemover();

            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++)
            {
                editor.editPage(pdfStamper, i);
            }
            
            pdfStamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38498431/how-to-remove-filtered-content-from-a-pdf-with-itext">
     * How to remove filtered content from a PDF with iText
     * </a>
     * <br/>
     * <a href="https://1drv.ms/b/s!AmNST-TRoPSemi2k0UnGFsjQM1Yt">
     * document.pdf
     * </a>
     * <p>
     * This test shows how to remove text matching the filter condition given
     * by the OP, i.e. any text drawn using a font whose name ends with "BoldMT".
     * </p>
     * <p>
     * This works well, too good actually, as some table headers also use a
     * "BoldMT" font and, therefore, also vanish. As an alternative look at
     * {@link #testRemoveBigTextDocument()} which simply uses the font size
     * as filter condition.
     * </p>
     */
    @Test
    public void testRemoveBoldMTTextDocument() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("document.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "document-noBoldMTText.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            PdfContentStreamEditor editor = new PdfContentStreamEditor()
            {
                @Override
                protected void write(PdfContentStreamProcessor processor, PdfLiteral operator, List<PdfObject> operands) throws IOException
                {
                    String operatorString = operator.toString();

                    if (TEXT_SHOWING_OPERATORS.contains(operatorString))
                    {
                        if (gs().getFont().getPostscriptFontName().endsWith("BoldMT"))
                            return;
                    }
                    
                    super.write(processor, operator, operands);
                }

                final List<String> TEXT_SHOWING_OPERATORS = Arrays.asList("Tj", "'", "\"", "TJ");
            };

            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++)
            {
                editor.editPage(pdfStamper, i);
            }
            
            pdfStamper.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38498431/how-to-remove-filtered-content-from-a-pdf-with-itext">
     * How to remove filtered content from a PDF with iText
     * </a>
     * <br/>
     * <a href="https://1drv.ms/b/s!AmNST-TRoPSemi2k0UnGFsjQM1Yt">
     * document.pdf
     * </a>
     * <p>
     * This test shows how to remove text filtered by font size. The filter
     * condition given by the OP, i.e. any text drawn using a font whose
     * name ends with "BoldMT", had turned out to match more often than
     * desired, cf. {@link #testRemoveBoldMTTextDocument()}.
     * </p>
     */
    @Test
    public void testRemoveBigTextDocument() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("document.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "document-noBigText.pdf")))
        {
            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, result);
            PdfContentStreamEditor editor = new PdfContentStreamEditor()
            {
                @Override
                protected void write(PdfContentStreamProcessor processor, PdfLiteral operator, List<PdfObject> operands) throws IOException
                {
                    String operatorString = operator.toString();

                    if (TEXT_SHOWING_OPERATORS.contains(operatorString))
                    {
                        if (gs().getFontSize() > 100)
                            return;
                    }
                    
                    super.write(processor, operator, operands);
                }

                final List<String> TEXT_SHOWING_OPERATORS = Arrays.asList("Tj", "'", "\"", "TJ");
            };

            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++)
            {
                editor.editPage(pdfStamper, i);
            }
            
            pdfStamper.close();
        }
    }
}
