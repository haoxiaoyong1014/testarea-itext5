package mkl.testarea.itext5.annotate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.PdfString;

/**
 * @author mkl
 */
public class SearchActionJavaScript
{
    /**
     * <a href="http://stackoverflow.com/questions/41090131/searching-pdf-for-a-specific-string-in-javascript-action-in-itext">
     * Searching PDF for a specific string in JavaScript action in iText
     * </a>
     * <br/>
     * <a href="http://www21.zippyshare.com/v/RDdOJI97/file.html">
     * file.pdf
     * </a>
     * <p>
     * This test shows how to process the immediate JavaScript code in annotation actions.
     * </p> 
     */
    @Test
    public void testSearchJsActionInFile() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("file.pdf")   )
        {
            System.out.println("file.pdf - Looking for special JavaScript actions.");
            // Reads and parses a PDF document
            PdfReader reader = new PdfReader(resource);

            // For each PDF page
            for (int i = 1; i <= reader.getNumberOfPages(); i++)
            {
                System.out.printf("\nPage %d\n", i);
                // Get a page a PDF page
                PdfDictionary page = reader.getPageN(i);
                // Get all the annotations of page i
                PdfArray annotsArray = page.getAsArray(PdfName.ANNOTS);

                // If page does not have annotations
                if (annotsArray == null)
                {
                    System.out.printf("No annotations.\n", i);
                    continue;
                }

                // For each annotation
                for (int j = 0; j < annotsArray.size(); ++j)
                {
                    System.out.printf("Annotation %d - ", j);

                    // For current annotation
                    PdfDictionary curAnnot = annotsArray.getAsDict(j);

                    // check if has JS as described below
                    PdfDictionary annotationAction = curAnnot.getAsDict(PdfName.A);
                    if (annotationAction == null)
                    {
                        System.out.print("no action");
                    }
                    // test if it is a JavaScript action
                    else if (PdfName.JAVASCRIPT.equals(annotationAction.get(PdfName.S)))
                    {
                        PdfObject scriptObject = annotationAction.getDirectObject(PdfName.JS);
                        if (scriptObject == null)
                        {
                            System.out.print("missing JS entry");
                            continue;
                        }
                        final String script;
                        if (scriptObject.isString())
                            script = ((PdfString)scriptObject).toUnicodeString();
                        else if (scriptObject.isStream())
                        {
                            try (   ByteArrayOutputStream baos = new ByteArrayOutputStream()    )
                            {
                                ((PdfStream)scriptObject).writeContent(baos);
                                script = baos.toString("ISO-8859-1");
                            }
                        }
                        else
                        {
                            System.out.println("malformed JS entry");
                            continue;
                        }

                        if (script.contains("if (this.hostContainer) { try {"))
                            System.out.print("contains test string - ");

                        System.out.printf("\n---\n%s\n---", script);
                        // what here?
                    }
                    else
                    {
                        System.out.print("no JavaScript action");
                    }
                    System.out.println();
                }
            }
        }
    }
}
