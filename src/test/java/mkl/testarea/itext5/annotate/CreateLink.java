package mkl.testarea.itext5.annotate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfIndirectReference;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CreateLink
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34408764/create-local-link-in-rotated-pdfpcell-in-itextsharp">
     * Create local link in rotated PdfPCell in iTextSharp
     * </a>
     * <p>
     * This is the equivalent Java code for the C# code in the question. Indeed, this code
     * also gives rise to the broken result. The cause is simple: Normally iText does not
     * touch the current transformation matrix. So the chunk link creation code assumes the
     * current user coordinate system to be the same as used for positioning annotations.
     * But in case of rotated cells iText does change the transformation matrix and
     * consequently the chunk link creation code positions the annotation at the wrong
     * location.
     * </p>
     */
    @Test
    public void testCreateLocalLinkInRotatedCell() throws IOException, DocumentException
    {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(new File(RESULT_FOLDER, "local-link.pdf")));
        doc.open();

        PdfPTable linkTable = new PdfPTable(2);
        PdfPCell linkCell = new PdfPCell();

        linkCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        linkCell.setRotation(90);
        linkCell.setFixedHeight(70);

        Anchor linkAnchor = new Anchor("Click here");
        linkAnchor.setReference("#target");
        Paragraph linkPara = new Paragraph();
        linkPara.add(linkAnchor);
        linkCell.addElement(linkPara);
        linkTable.addCell(linkCell);

        PdfPCell linkCell2 = new PdfPCell();
        Anchor linkAnchor2 = new Anchor("Click here 2");
        linkAnchor2.setReference("#target");
        Paragraph linkPara2 = new Paragraph();
        linkPara2.add(linkAnchor2);
        linkCell2.addElement(linkPara2);
        linkTable.addCell(linkCell2);

        linkTable.addCell(new PdfPCell(new Phrase("cell 3")));
        linkTable.addCell(new PdfPCell(new Phrase("cell 4")));
        doc.add(linkTable);

        doc.newPage();

        Anchor destAnchor = new Anchor("top");
        destAnchor.setName("target");
        PdfPTable destTable = new PdfPTable(1);
        PdfPCell destCell = new PdfPCell();
        Paragraph destPara = new Paragraph();
        destPara.add(destAnchor);
        destCell.addElement(destPara);
        destTable.addCell(destCell);
        destTable.addCell(new PdfPCell(new Phrase("cell 2")));
        destTable.addCell(new PdfPCell(new Phrase("cell 3")));
        destTable.addCell(new PdfPCell(new Phrase("cell 4")));
        doc.add(destTable);

        doc.close();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34734669/define-background-color-and-transparency-of-link-annotation-in-pdf">
     * Define background color and transparency of link annotation in PDF
     * </a>
     * <p>
     * This test creates a link annotation with custom appearance. Adobe Reader chooses
     * to ignore it but other viewers use it. Interestingly Adobe Acrobat export-as-image
     * does use the custom appearance...
     * </p>
     */
    @Test
    public void testCreateLinkWithAppearance() throws IOException, DocumentException
    {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(new File(RESULT_FOLDER, "custom-link.appearance.pdf")));
        writer.setCompressionLevel(0);
        doc.open();

        BaseFont baseFont = BaseFont.createFont();
        int fontSize = 15;
        doc.add(new Paragraph("Hello", new Font(baseFont, fontSize)));
        
        PdfContentByte content = writer.getDirectContent();
        
        String text = "Test";
        content.setFontAndSize(baseFont, fontSize);
        content.beginText();
        content.moveText(100, 500);
        content.showText(text);
        content.endText();
        
        Rectangle linkLocation = new Rectangle(95, 495 + baseFont.getDescentPoint(text, fontSize),
                105 + baseFont.getWidthPoint(text, fontSize), 505 + baseFont.getAscentPoint(text, fontSize));

        PdfAnnotation linkGreen = PdfAnnotation.createLink(writer, linkLocation, PdfName.HIGHLIGHT, "green" );
        PdfTemplate appearance = PdfTemplate.createTemplate(writer, linkLocation.getWidth(), linkLocation.getHeight());
        PdfGState state = new PdfGState();
        //state.FillOpacity = .3f;
        // IMPROVEMENT: Use blend mode Darken instead of transparency; you may also want to try Multiply.
        state.setBlendMode(new PdfName("Darken"));
        appearance.setGState(state);

        appearance.setColorFill(BaseColor.GREEN);
        appearance.rectangle(0, 0, linkLocation.getWidth(), linkLocation.getHeight());
        appearance.fill();
        linkGreen.setAppearance(PdfName.N, appearance);
        writer.addAnnotation(linkGreen);

        doc.open();
        doc.close();
    }
}
