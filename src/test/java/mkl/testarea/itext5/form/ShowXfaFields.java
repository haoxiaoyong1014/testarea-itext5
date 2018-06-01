package mkl.testarea.itext5.form;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.Test;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.XfaForm;

/**
 * @author mkl
 */
public class ShowXfaFields {
    /**
     * <a href="https://stackoverflow.com/questions/46730760/no-fields-were-printed-on-console-after-verifying-if-form-is-using-acroform-or-x">
     * No fields were printed on console after verifying if form is using Acroform or XFA technology?
     * </a>
     * <br/>
     * <a href="http://blogs.adobe.com/formfeed/files/formfeed/Samples/multiview.pdf">
     * multiview.pdf
     * </a>
     * from
     * <a href="http://blogs.adobe.com/formfeed/2011/02/multiple-top-level-subforms.html">
     * Multiple Top Level Subforms
     * </a>
     * <p>
     * The OP's observation can be reproduced using this sample PDF.
     * </p>
     */
    @Test
    public void testReadFieldsFromMultiview() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("multiview.pdf")  ) {
            PdfReader reader = new PdfReader(resource);
            AcroFields form = reader.getAcroFields();
            XfaForm xfa = form.getXfa();
            System.out.println(xfa.isXfaPresent() ? "XFA form" : "AcroForm");
            Set<String> fields = form.getFields().keySet();
            for (String key : fields) {
                System.out.println(key);
            }
            System.out.flush();
            System.out.close();
            reader.close();
        }
    }

}
