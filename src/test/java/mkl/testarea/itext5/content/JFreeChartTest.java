package mkl.testarea.itext5.content;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <a href="http://stackoverflow.com/questions/35343527/how-to-reduce-table-cell-size-with-image-in-it-using-itext">
 * How to reduce table cell size with image in it using iText
 * </a>
 * <p>
 * Indeed, the image makes the cell grow so its width uses all the available width. 
 * </p>
 * <p>
 * It can be kept smaller by either fixing the cell height or wrapping the image into a {@link Chunk}.
 * Neither option always has a good result. After experimenting the OP chose the latter one.
 * </p>
 * 
 * @author Rasika Kulkarni
 */
public class JFreeChartTest
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void test()
    {
        writeChartToPDF(generatePieChart(), 50, 50, new File(RESULT_FOLDER, "piechart5.pdf").toString());
    }

    public static void main(String[] args)
    {
        writeChartToPDF(generatePieChart(), 50, 50, "D://piechart5.pdf");
    }

    public static JFreeChart generatePieChart()
    {
        DefaultPieDataset dataSet = new DefaultPieDataset();
        dataSet.setValue("China", 30);
        dataSet.setValue("India", 30);
        dataSet.setValue("United States", 40);

        JFreeChart chart = ChartFactory.createPieChart("", dataSet, false, true, false);
        PiePlot piePlot = (PiePlot) chart.getPlot();
        piePlot.setBackgroundPaint(Color.WHITE); // set background color white
        piePlot.setOutlineVisible(false); // remove background border
        piePlot.setLabelGenerator(null); // remove pie section labels
        piePlot.setSectionPaint("China", Color.GRAY);
        piePlot.setSectionPaint("India", Color.GREEN);
        piePlot.setSectionPaint("United States", Color.BLUE);
        piePlot.setShadowPaint(Color.WHITE);

        return chart;
    }

    public static void writeChartToPDF(JFreeChart chart, int width, int height, String fileName)
    {
        PdfWriter writer = null;
        Document document = new Document();

        try
        {
            writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            PdfContentByte pdfContentByte = writer.getDirectContent();
            PdfTemplate pdfTemplateChartHolder = pdfContentByte.createTemplate(50, 50);
            Graphics2D graphics2d = new PdfGraphics2D(pdfTemplateChartHolder, 50, 50);
            Rectangle2D chartRegion = new Rectangle2D.Double(0, 0, 50, 50);
            chart.draw(graphics2d, chartRegion);
            graphics2d.dispose();

            Image chartImage = Image.getInstance(pdfTemplateChartHolder);
            document.add(chartImage);

            PdfPTable table = new PdfPTable(5);
            // the cell object
            // we add a cell with colspan 3

            PdfPCell cellX = new PdfPCell(new Phrase("A"));
            cellX.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            cellX.setRowspan(6);
            table.addCell(cellX);

            PdfPCell cellA = new PdfPCell(new Phrase("A"));
            cellA.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            cellA.setColspan(4);
            table.addCell(cellA);

            PdfPCell cellB = new PdfPCell(new Phrase("B"));
            table.addCell(cellB);
            PdfPCell cellC = new PdfPCell(new Phrase("C"));
            table.addCell(cellC);
            PdfPCell cellD = new PdfPCell(new Phrase("D"));
            table.addCell(cellD);
            PdfPCell cellE = new PdfPCell(new Phrase("E"));
            table.addCell(cellE);
            PdfPCell cellF = new PdfPCell(new Phrase("F"));
            table.addCell(cellF);
            PdfPCell cellG = new PdfPCell(new Phrase("G"));
            table.addCell(cellG);
            PdfPCell cellH = new PdfPCell(new Phrase("H"));
            table.addCell(cellH);
            PdfPCell cellI = new PdfPCell(new Phrase("I"));
            table.addCell(cellI);

            PdfPCell cellJ = new PdfPCell(new Phrase("J"));
            cellJ.setColspan(2);
            cellJ.setRowspan(3);
            //instead of
            //  cellJ.setImage(chartImage);
            //the OP now uses
            Chunk chunk = new Chunk(chartImage, 20, -50);
            cellJ.addElement(chunk);
            //presumably with different contents of the other cells at hand
            table.addCell(cellJ);

            PdfPCell cellK = new PdfPCell(new Phrase("K"));
            cellK.setColspan(2);
            table.addCell(cellK);
            PdfPCell cellL = new PdfPCell(new Phrase("L"));
            cellL.setColspan(2);
            table.addCell(cellL);
            PdfPCell cellM = new PdfPCell(new Phrase("M"));
            cellM.setColspan(2);
            table.addCell(cellM);

            document.add(table);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        document.close();
    }
}
