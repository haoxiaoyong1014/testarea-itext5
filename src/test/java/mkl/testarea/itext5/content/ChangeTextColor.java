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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfLiteral;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;

/**
 * @author mkl
 */
public class ChangeTextColor
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/40401800/traverse-whole-pdf-and-change-some-attribute-with-some-object-in-it-using-itext">
     * Traverse whole PDF and change some attribute with some object in it using iText
     * </a>
     * <p>
     * This test shows how to change the color of text of a given color. In this case,
     * black text is changed to green.
     * </p>
     * <p>
     * Beware, this is a proof-of-concept, not a final and complete solution. In particular
     * </p>
     * <ul>
     * <li>Text is considered to be black if for its <code>color</code> the expression
     * <code>BaseColor.BLACK.equals(color)</code> is <code>true</code>; as equality among
     * <code>BaseColor</code> and its descendant classes is not completely well-defined,
     * this might lead to some false positives.
     * <li><code>PdfContentStreamEditor</code> only inspects and edits the content
     * stream of the page itself, not the content streams of displayed form xobjects
     * or patterns; thus, some text may not be found.
     * </ul>
     * <p>
     * Improving the class to properly detect black color and to recursively traverse and
     * edit the content streams of used patterns and xobjects remains as an exercise for
     * the reader.
     * </p> 
     */
    @Test
    public void testChangeBlackTextToGreenDocument() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("document.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "document-blackTextToGreen.pdf")))
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
                        if (currentlyReplacedBlack == null)
                        {
                            BaseColor currentFillColor = gs().getFillColor();
                            if (BaseColor.BLACK.equals(currentFillColor))
                            {
                                currentlyReplacedBlack = currentFillColor;
                                super.write(processor, new PdfLiteral("rg"), Arrays.asList(new PdfNumber(0), new PdfNumber(1), new PdfNumber(0), new PdfLiteral("rg")));
                            }
                        }
                    }
                    else if (currentlyReplacedBlack != null)
                    {
                        if (currentlyReplacedBlack instanceof CMYKColor)
                        {
                            super.write(processor, new PdfLiteral("k"), Arrays.asList(new PdfNumber(0), new PdfNumber(0), new PdfNumber(0), new PdfNumber(1), new PdfLiteral("k")));
                        }
                        else if (currentlyReplacedBlack instanceof GrayColor)
                        {
                            super.write(processor, new PdfLiteral("g"), Arrays.asList(new PdfNumber(0), new PdfLiteral("g")));
                        }
                        else
                        {
                            super.write(processor, new PdfLiteral("rg"), Arrays.asList(new PdfNumber(0), new PdfNumber(0), new PdfNumber(0), new PdfLiteral("rg")));
                        }
                        currentlyReplacedBlack = null;
                    }
                    
                    super.write(processor, operator, operands);
                }

                BaseColor currentlyReplacedBlack = null;

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
