package mkl.testarea.itext5.copy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.RectangleReadOnly;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfCopy.PageStamp;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class StampHeader
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "copy");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/29977927/table-header-in-pdf-getting-displayed-using-itextpdf5-1-1-but-not-in-itextpdf5-5">
     * table header in pdf getting displayed using itextpdf5.1.1 but not in itextpdf5.5.3
     * </a>
     * <p>
     * Indeed, the code as presented by the OP does not show the header table. This makes sense, though:
     * </p>
     * <p>
     * The OP has cells with default padding (i.e. 2) and height 10, and he tries to insert text at height 7.
     * But 2 (top margin) + 7 (text height) + 2 (bottom margin) = 11, i.e. more than fits into the cell height 10.
     * Thus, the text does not fit and is not displayed.
     * </p>
     * <p>
     * You can fix this by either
     * <ul>
     * <li>using a smaller font, e.g. 6, or
     * <li>using a higher cell, e.g. 11, or
     * <li>using a smaller padding, e.g. 1, see below-
     * </p>
     */
	@Test
	public void testSandeepSinghHeaderTable() throws DocumentException, IOException
	{
		byte[] strIntermediatePDFFile = createSampleDocument();
		String header1 = "Header 1";
		String header2 = "Header 2";
		String header3 = "Header 3";
		String header5 = "Header 5";
		

		Document document = new Document(PageSize.A4.rotate(), 20, 20, 75, 20);
		PdfCopy copy = new PdfCopy(document, new FileOutputStream(new File(RESULT_FOLDER, "stampTableHeader.pdf")));

		document.open();
		PdfReader pdfReaderIntermediate = new PdfReader(strIntermediatePDFFile);
		int numberOfPages = pdfReaderIntermediate.getNumberOfPages();
		Font ffont = new Font(Font.FontFamily.UNDEFINED, 7, Font.NORMAL);
		System.out.println("###### No. of Pages: " + numberOfPages);
		for (int j = 0; j < numberOfPages; )
		{
		    PdfImportedPage page = copy.getImportedPage(pdfReaderIntermediate, ++j);
		    PageStamp stamp = copy.createPageStamp(page);
		    Phrase footer = new Phrase(String.format("%d of %d", j, numberOfPages), ffont);
		    ColumnText.showTextAligned(stamp.getUnderContent(),
		                               Element.ALIGN_CENTER, footer,
		                               (document.right() - document.left()) /
		                               2 + document.leftMargin(),
		                               document.bottom() - 10, 0);
		    if (j != 1)
		    {
		    	PdfPTable headerTable = new PdfPTable(2);
		        headerTable.setTotalWidth(700);
		        headerTable.getDefaultCell().setFixedHeight(10);
		        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		        headerTable.getDefaultCell().setPadding(1); // Added!
		        headerTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
		        headerTable.addCell(new Phrase(String.format(header1), ffont));
		        headerTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
		        headerTable.addCell(new Phrase(String.format(header2), ffont));
		        headerTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
		        headerTable.addCell(new Phrase(String.format(header3), ffont));
		        headerTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
		        headerTable.addCell(new Phrase(String.format(header5, j), ffont));
		        headerTable.completeRow();
		        headerTable.writeSelectedRows(0, 5, 60.5f, 550, stamp.getUnderContent());
		    }

		    stamp.alterContents();
		    copy.addPage(page);
		}
		document.close();
	}

	byte[] createSampleDocument() throws IOException, DocumentException
	{
		try (	ByteArrayOutputStream baos = new ByteArrayOutputStream()	)
		{
			Document doc = new Document(new RectangleReadOnly(842,595));
			PdfWriter.getInstance(doc, baos);
			doc.open();
			doc.add(new Paragraph("Test Page 1"));
			doc.newPage();
			doc.add(new Paragraph("Test Page 2"));
			doc.close();
			return baos.toByteArray();
		}
	}
}
