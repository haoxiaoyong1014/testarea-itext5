package mkl.testarea.itext5.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class SetRichTextFields
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34633300/rich-text-box-on-a-pdf-with-acrofields-itextsharp">
     * Rich text box on a pdf with AcroFields ITextSharp
     * </a>
     * <p>
     * Indeed, the OP's sample rich text is not displayed.
     * </p>
     */
    @Test
    public void testCarndacierValue() throws IOException, DocumentException
    {
        File result = new File(RESULT_FOLDER, "RichTextDoc-carndacier.pdf");
        
        String value = "<p><em>No rece</em>nt c<s>hanges in marital st</s>atus or dependen<strong>ts test</strong></p>"
                + "<p>&nbsp;</p>"
                + "<h2><s><em><strong>Hello !</strong></em></s></h2>";
        
        setFields(result, value);
    }

    /**
     * <a href="http://stackoverflow.com/questions/34633300/rich-text-box-on-a-pdf-with-acrofields-itextsharp">
     * Rich text box on a pdf with AcroFields ITextSharp
     * </a>
     * <p>
     * Not only the OP's sample rich text is not displayed, cf. {@link #testCarndacierValue()},
     * the sample from the specification is not displayed, either.
     * </p>
     */
    @Test
    public void testSpecificationValue() throws IOException, DocumentException
    {
        File result = new File(RESULT_FOLDER, "RichTextDoc-specification.pdf");
        
/*  Sample from the configuration
        String value = "<?xml version=\"1.0\"?><body xmlns=\"http://www.w3.org/1999/xhtml\""
                + "      xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\""
                + "      xfa:contentType=\"text/html\" xfa:APIVersion=\"Acrobat:8.0.0\" xfa:spec=\"2.4\">"
                + "  <p style=\"text-align:left\">"
                + "    <b>"
                + "      <i>"
                + "        Here is some bold italic text"
                + "      </i>"
                + "    </b>"
                + "  </p>"
                + "  <p style= \"font-size:16pt\">"
                + "    This text uses default text state parameters but changes the font size to 16."
                + "  </p>"
                + "</body>";
*/
/*  Sample copied from a field filled by Acrobat, pretty-printed
        String value = "<?xml version=\"1.0\"?>"
                + "<body xfa:APIVersion=\"Acroform:2.7.0.0\""
                + "     xfa:spec=\"2.1\""
                + "     xmlns=\"http://www.w3.org/1999/xhtml\""
                + "     xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\">"
                + "  <p dir=\"ltr\" style=\"margin-top:0pt;margin-bottom:0pt;font-family:Helvetica;font-size:12pt;font-weight:bold;font-style:italic\">"
                + "    Here is some bold italic text"
                + "    <span style=\"font-weight:normal;font-style:normal\"><span style=\"xfa-spacerun:yes\">"
                + "      &#160;"
                + "    </span></span>"
                + "  </p>"
                + "  <p dir=\"ltr\" style=\"margin-top:0pt;margin-bottom:0pt;font-family:Helvetica;font-size:12pt;font-weight:normal;font-style:normal\">"
                + "    This text uses default text state parameters but changes the font size to 16."
                + "    <span style=\"xfa-spacerun:yes\">"
                + "      &#160;"
                + "    </span>"
                + "  </p>"
                + "</body>";
*/
/*  Sample copied from a field filled by Acrobat, original */
        String value = "<?xml version=\"1.0\"?><body xfa:APIVersion=\"Acroform:2.7.0.0\" xfa:spec=\"2.1\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\"><p dir=\"ltr\" style=\"margin-top:0pt;margin-bottom:0pt;font-family:Helvetica;font-size:12pt;font-weight:bold;font-style:italic\">Here is some bold italic text<span style=\"font-weight:normal;font-style:normal\"><span style=\"xfa-spacerun:yes\">&#160;</span></span></p><p dir=\"ltr\" style=\"margin-top:0pt;margin-bottom:0pt;font-family:Helvetica;font-size:12pt;font-weight:normal;font-style:normal\">This text uses default text state parameters but changes the font size to 16.<span style=\"xfa-spacerun:yes\">&#160;</span></p></body>";
        
        setFields(result, value);
    }

    void setFields(File result, String value) throws IOException, DocumentException
    {
        try ( InputStream resource = getClass().getResourceAsStream("RichTextDoc.pdf")  )
        {
            PdfReader reader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(result));
            
            AcroFields fields = stamper.getAcroFields();
            fields.setGenerateAppearances(false);
            Assert.assertFalse("Setting rich text to normal field should fail", fields.setFieldRichValue("NormalText", value));
            Assert.assertTrue("Setting rich text to rich text field should succeed", fields.setFieldRichValue("RichText", value));
            
            stamper.close();
        }
    }
}
