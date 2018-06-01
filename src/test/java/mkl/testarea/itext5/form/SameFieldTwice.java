package mkl.testarea.itext5.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This test covers situations with multiple widgets of the same field.
 * 
 * @author mkl
 */
public class SameFieldTwice
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/28943245/possible-to-ues-variables-in-a-pdf-doument">
     * Possible to ues variables in a PDF doument?
     * </a>
     * <p>
     * Generates a sample PDF containing two widgets of the same text field.
     * </p>
     */
    @Test
    public void testCreateFormWithSameFieldTwice() throws IOException, DocumentException
    {
        try (   OutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "aFieldTwice.pdf"))  )
        {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();
            document.add(new Paragraph("The same field twice"));
            
            PdfFormField field = PdfFormField.createTextField(writer, false, false, 0);
            field.setFieldName("fieldName");

            PdfFormField annot1 = PdfFormField.createEmpty(writer);
            annot1.setWidget(new Rectangle(30, 700, 200, 720), PdfAnnotation.HIGHLIGHT_INVERT);
            field.addKid(annot1);

            PdfFormField annot2 = PdfFormField.createEmpty(writer);
            annot2.setWidget(new Rectangle(230, 700, 400, 720), PdfAnnotation.HIGHLIGHT_INVERT);
            field.addKid(annot2);

            writer.addAnnotation(field);
            
            document.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/31402602/how-to-rename-only-the-first-found-duplicate-acrofield-in-pdf">
     * How to rename only the first found duplicate acrofield in pdf?
     * </a>
     * <br>
     * <a href="http://s000.tinyupload.com/index.php?file_id=34970992934525199618">
     * test_duplicate_field2.pdf
     * </a>
     * <p>
     * Demonstration of how to transform generate a new field for a widget.
     * </p> 
     */
    @Test
    public void testWidgetToField() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test_duplicate_field2.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test_duplicate_field2-widgetToField.pdf"))   )
        {
            PdfReader reader = new PdfReader(resource);

            PdfDictionary form = reader.getCatalog().getAsDict(PdfName.ACROFORM);
            PdfArray fields = form.getAsArray(PdfName.FIELDS);
            for (PdfObject object: fields)
            {
                PdfDictionary field = (PdfDictionary) PdfReader.getPdfObject(object);
                if ("Text1".equals(field.getAsString(PdfName.T).toString()))
                {
                    PdfDictionary newField = new PdfDictionary();
                    PRIndirectReference newFieldRef = reader.addPdfObject(newField);
                    fields.add(newFieldRef);
                    newField.putAll(field);
                    newField.put(PdfName.T, new PdfString("foobar"));
                    PdfArray newKids = new PdfArray();
                    newField.put(PdfName.KIDS, newKids);
                    PdfArray kids = field.getAsArray(PdfName.KIDS);
                    PdfObject widget = kids.remove(0);
                    newKids.add(widget);
                    PdfDictionary widgetDict = (PdfDictionary) PdfReader.getPdfObject(widget);
                    widgetDict.put(PdfName.PARENT, newFieldRef);
                    break;
                }
            }

            PdfStamper stamper = new PdfStamper(reader, result);
            stamper.close();
        }
    }
}
