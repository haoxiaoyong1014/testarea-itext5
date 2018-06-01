package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author mkl
 */
public class CreateTable {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/49319833/split-part-of-the-pdf-horizontally">
     * Split part of the PDF horizontally
     * </a>
     * <p>
     * This test shows how to achieve the layout using one table
     * object and rowspan for the image cell.
     * </p>
     * @see #testSureshTwoTables()
     */
    @Test
    public void testSureshOneTableRowspan() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "sureshOneTableRowspan.pdf")));
        document.open();

        Image image = null;
        try (InputStream imageStream = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg "))
        {
            image = Image.getInstance(IOUtils.toByteArray(imageStream));
            image.scaleToFit(110,110);
        }

        PdfPTable table = new PdfPTable(new float[]{2,1,1});

        PdfPCell imageCell = new PdfPCell(image);
        imageCell.setRowspan(4);
        imageCell.setVerticalAlignment(PdfPTable.ALIGN_CENTER);
        table.addCell(imageCell);

        PdfPCell cell = new PdfPCell(new Phrase("Address1"));
        cell.setBorder(Rectangle.TOP | Rectangle.LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Contact Number"));
        cell.setBorder(Rectangle.TOP | Rectangle.RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Address2"));
        cell.setBorder(Rectangle.LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Fax"));
        cell.setBorder(Rectangle.RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Address3"));
        cell.setBorder(Rectangle.LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Pin Code"));
        cell.setBorder(Rectangle.RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Address4"));
        cell.setBorder(Rectangle.BOTTOM | Rectangle.LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT);
        table.addCell(cell);

        document.add(table);

        document.close();
    }

    /**
     * <a href="https://stackoverflow.com/questions/49319833/split-part-of-the-pdf-horizontally">
     * Split part of the PDF horizontally
     * </a>
     * <p>
     * This test shows how to achieve the layout using two table
     * objects.
     * </p>
     * @see #testSureshOneTableRowspan() 
     */
    @Test
    public void testSureshTwoTables() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER, "sureshTwoTables.pdf")));
        document.open();

        Image image = null;
        try (InputStream imageStream = getClass().getResourceAsStream("/mkl/testarea/itext5/layer/Willi-1.jpg "))
        {
            image = Image.getInstance(IOUtils.toByteArray(imageStream));
            image.scaleToFit(110,110);
        }

        PdfPTable innerTable = new PdfPTable(2);

        PdfPCell cell = new PdfPCell(new Phrase("Address1"));
        cell.setBorder(0);
        innerTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Contact Number"));
        cell.setBorder(0);
        innerTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Address2"));
        cell.setBorder(0);
        innerTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Fax"));
        cell.setBorder(0);
        innerTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Address3"));
        cell.setBorder(0);
        innerTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Pin Code"));
        cell.setBorder(0);
        innerTable.addCell(cell);
        cell = new PdfPCell(new Phrase("Address4"));
        cell.setBorder(0);
        innerTable.addCell(cell);
        cell = new PdfPCell(new Phrase(""));
        cell.setBorder(0);
        innerTable.addCell(cell);

        PdfPTable table = new PdfPTable(2);

        PdfPCell imageCell = new PdfPCell(image);
        imageCell.setVerticalAlignment(PdfPTable.ALIGN_CENTER);
        table.addCell(imageCell);

        table.addCell(innerTable);

        document.add(table);

        document.close();
    }
}
