package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.awt.geom.Point2D;
import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ExtRenderListener;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;

/**
 * <a href="http://stackoverflow.com/questions/26464324/how-can-i-can-insert-an-image-or-stamp-on-a-pdf-where-there-is-free-space-availa">
 * How can I can insert an image or stamp on a pdf where there is free space available like a density scanner
 * </a>
 * 
 * This tests the {@link RenderListener} originally presented as an answer and also the {@link ExtRenderListener} improving that answer.
 */
public class FindFreeSpace
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    //
    // a series of tests finding the free space of some documents
    //
    @Test
    public void testZugferd_20x20() throws IOException, DocumentException
    {
        test("zugferd_add_xml_to_pdf.pdf", 20, 20);
        testExt("zugferd_add_xml_to_pdf.pdf", 20, 20);
    }

    @Test
    public void testZugferd_200x50() throws IOException, DocumentException
    {
        test("zugferd_add_xml_to_pdf.pdf", 200, 50);
        testExt("zugferd_add_xml_to_pdf.pdf", 200, 50);
    }

    @Test
    public void testZugferd_200x200() throws IOException, DocumentException
    {
        test("zugferd_add_xml_to_pdf.pdf", 200, 200);
        testExt("zugferd_add_xml_to_pdf.pdf", 200, 200);
    }

    @Test
    public void testN2013_20x20() throws IOException, DocumentException
    {
        test("n2013.00849449.pdf", 20, 20);
        testExt("n2013.00849449.pdf", 20, 20);
    }

    @Test
    public void testTest_20x20() throws IOException, DocumentException
    {
        test("test.pdf", 20, 20);
        testExt("test.pdf", 20, 20);
    }

    @Test
    public void testTest_200x100() throws IOException, DocumentException
    {
        test("test.pdf", 200, 100);
        testExt("test.pdf", 200, 100);
    }

    @Test
    public void testTest_200x200() throws IOException, DocumentException
    {
        test("test.pdf", 200, 200);
        testExt("test.pdf", 200, 200);
    }

    @Test
    public void testSample1_20x20() throws IOException, DocumentException
    {
        test("Sample_1.pdf", 20, 20);
        testExt("Sample_1.pdf", 20, 20);
    }

    @Test
    public void testSample1_200x200() throws IOException, DocumentException
    {
        test("Sample_1.pdf", 200, 200);
        testExt("Sample_1.pdf", 200, 200);
    }

    @Test
    public void testPreface_20x20() throws IOException, DocumentException
    {
        test("preface.pdf", 20, 20);
        testExt("preface.pdf", 20, 20);
    }

    @Test
    public void testPreface_200x200() throws IOException, DocumentException
    {
        test("preface.pdf", 200, 200);
        testExt("preface.pdf", 200, 200);
    }

    void test(String resource, float minWidth, float minHeight) throws IOException, DocumentException
    {
        String name = new File(resource).getName();
        String target = String.format("%s-freeSpace%.0fx%.0f.pdf", name, minWidth, minHeight);
        InputStream resourceStream = getClass().getResourceAsStream(resource);
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            System.out.printf("\nFree %.0fx%.0f regions in %s\n", minWidth, minHeight, name);

            Collection<Rectangle2D> rectangles = find(reader, minWidth, minHeight, 1);
            print(rectangles);

            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, target)));
            PdfContentByte over = stamper.getOverContent(1);

            enhance(over, rectangles);
            Point2D[] points = getPointsOfInterest(reader.getCropBox(1));
            for (int i = 0; i < points.length; i++)
                enhance(over, rectangles, points[i], colors[i]);

            stamper.close();
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    void testExt(String resource, float minWidth, float minHeight) throws IOException, DocumentException
    {
        String name = new File(resource).getName();
        String target = String.format("%s-freeSpaceExt%.0fx%.0f.pdf", name, minWidth, minHeight);
        InputStream resourceStream = getClass().getResourceAsStream(resource);
        try
        {
            PdfReader reader = new PdfReader(resourceStream);
            System.out.printf("\nFree %.0fx%.0f regions in %s\n", minWidth, minHeight, name);

            Collection<Rectangle2D> rectangles = findExt(reader, minWidth, minHeight, 1);
            print(rectangles);

            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(RESULT_FOLDER, target)));
            PdfContentByte over = stamper.getOverContent(1);

            enhance(over, rectangles);
            Point2D[] points = getPointsOfInterest(reader.getCropBox(1));
            for (int i = 0; i < points.length; i++)
                enhance(over, rectangles, points[i], colors[i]);

            stamper.close();
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    //
    // The code later-on provided by the OP
    //

    @Test
    public void testOP() throws IOException, DocumentException
    {
        System.out.println("\nTHE OP's CODE");

        InputStream resourceStream = getClass().getResourceAsStream("Final PADR Release.pdf");
        try
        {
            // The resulting PDF file
            String RESULT = "target/test-outputs/extract/Final PADR Release 1.pdf";

            // Create a reader
            PdfReader reader = new PdfReader(resourceStream);

            // Create a stamper
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(RESULT));

            // Loop over the pages and add a footer to each page
            int n = reader.getNumberOfPages();

            for (int i = 2; i <= n; i++)
            {
                Collection<Rectangle2D> rectangles = find(reader, 300, 100, i); // FIX: i was n before

                Iterator<Rectangle2D> itr = rectangles.iterator();
                while (itr.hasNext())
                {
                    System.out.println(itr.next());
                }

                if (!(rectangles.isEmpty()) && (rectangles.size() != 0))
                {
                    Rectangle2D best = null;
                    double bestDist = Double.MAX_VALUE;

                    Point2D.Double point = new Point2D.Double(200, 400);

                    float x = 0, y = 0;

                    for (Rectangle2D rectangle : rectangles)
                    {
                        double distance = distance(rectangle, point);

                        if (distance < bestDist)
                        {
                            best = rectangle;

                            bestDist = distance;

                            x = (float) best.getX();

                            y = (float) best.getMaxY(); // FIX: was getY

                            int left = (int) best.getMinX();

                            int right = (int) best.getMaxX();

                            int top = (int) best.getMaxY();

                            int bottom = (int) best.getMinY();

                            System.out.println("x : " + x);
                            System.out.println("y : " + y);
                            System.out.println("left : " + left);
                            System.out.println("right : " + right);
                            System.out.println("top : " + top);
                            System.out.println("bottom : " + bottom);

                        }
                    }

                    getFooterTable(i, n).writeSelectedRows(0, -1, x, y, stamper.getOverContent(i));
                }

                else
                {
                    System.err.println("No free space found here");
                    getFooterTable(i, n).writeSelectedRows(0, -1, 94, 140, stamper.getOverContent(i));
                }
            }

            // Close the stamper
            stamper.close();

            // Close the reader
            reader.close();
        }
        finally
        {
            if (resourceStream != null)
                resourceStream.close();
        }
    }

    // Create a table with page X of Y, @param x the page number, @param y the
    // total number of pages, @return a table that can be used as footer
    public static PdfPTable getFooterTable(int x, int y)
    {
        java.util.Date date = new java.util.Date();

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

        String month = sdf.format(date);
        System.out.println("Month : " + month);

        PdfPTable table = new PdfPTable(1);

        table.setTotalWidth(120);
        table.setLockedWidth(true);

        table.getDefaultCell().setFixedHeight(20);
        table.getDefaultCell().setBorder(Rectangle.TOP);
        table.getDefaultCell().setBorder(Rectangle.LEFT);
        table.getDefaultCell().setBorder(Rectangle.RIGHT);
        table.getDefaultCell().setBorderColorTop(BaseColor.BLUE);
        table.getDefaultCell().setBorderColorLeft(BaseColor.BLUE);
        table.getDefaultCell().setBorderColorRight(BaseColor.BLUE);
        table.getDefaultCell().setBorderWidthTop(1f);
        table.getDefaultCell().setBorderWidthLeft(1f);
        table.getDefaultCell().setBorderWidthRight(1f);

        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        Font font1 = new Font(FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLUE);

        table.addCell(new Phrase("CONTROLLED COPY", font1));

        table.getDefaultCell().setFixedHeight(20);
        table.getDefaultCell().setBorder(Rectangle.LEFT);
        table.getDefaultCell().setBorder(Rectangle.RIGHT);
        table.getDefaultCell().setBorderColorLeft(BaseColor.BLUE);
        table.getDefaultCell().setBorderColorRight(BaseColor.BLUE);
        table.getDefaultCell().setBorderWidthLeft(1f);
        table.getDefaultCell().setBorderWidthRight(1f);

        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        Font font = new Font(FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.RED);

        table.addCell(new Phrase(month, font));

        table.getDefaultCell().setFixedHeight(20);
        table.getDefaultCell().setBorder(Rectangle.LEFT);
        table.getDefaultCell().setBorder(Rectangle.RIGHT);
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.getDefaultCell().setBorderColorLeft(BaseColor.BLUE);
        table.getDefaultCell().setBorderColorRight(BaseColor.BLUE);
        table.getDefaultCell().setBorderColorBottom(BaseColor.BLUE);
        table.getDefaultCell().setBorderWidthLeft(1f);
        table.getDefaultCell().setBorderWidthRight(1f);
        table.getDefaultCell().setBorderWidthBottom(1f);

        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        table.addCell(new Phrase("BLR DESIGN DEPT.", font1));

        return table;
    }

    // helper methods
    public Collection<Rectangle2D> find(PdfReader reader, float minWidth, float minHeight, int page) throws IOException
    {
        Rectangle cropBox = reader.getCropBox(page);
        Rectangle2D crop = new Rectangle2D.Float(cropBox.getLeft(), cropBox.getBottom(), cropBox.getWidth(), cropBox.getHeight());
        FreeSpaceFinder finder = new FreeSpaceFinder(crop, minWidth, minHeight);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        parser.processContent(page, finder);
        return finder.freeSpaces;
    }

    public Collection<Rectangle2D> findExt(PdfReader reader, float minWidth, float minHeight, int page) throws IOException
    {
        Rectangle cropBox = reader.getCropBox(page);
        Rectangle2D crop = new Rectangle2D.Float(cropBox.getLeft(), cropBox.getBottom(), cropBox.getWidth(), cropBox.getHeight());
        FreeSpaceFinder finder = new FreeSpaceFinderExt(crop, minWidth, minHeight);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        parser.processContent(page, finder);
        return finder.freeSpaces;
    }

    void print(Collection<Rectangle2D> rectangles)
    {
        System.out.println("  x       y       w      h");
        for (Rectangle2D rectangle : rectangles)
        {
            System.out.printf("  %07.3f %07.3f %07.3f %07.3f\n", rectangle.getMinX(), rectangle.getMinY(), rectangle.getWidth(), rectangle.getHeight());
        }
    }

    void enhance(PdfContentByte page, Collection<Rectangle2D> rectangles)
    {
        for (Rectangle2D rectangle : rectangles)
        {
            page.setColorStroke(pickColor());
            page.rectangle((float) rectangle.getMinX(), (float) rectangle.getMinY(), (float) rectangle.getWidth(), (float) rectangle.getHeight());
            page.stroke();
        }
    }

    void enhance(PdfContentByte page, Collection<Rectangle2D> rectangles, Point2D point, BaseColor color)
    {
        Rectangle2D best = null;
        double bestDist = Double.MAX_VALUE;

        for (Rectangle2D rectangle : rectangles)
        {
            double distance = distance(rectangle, point);
            if (distance < bestDist)
            {
                best = rectangle;
                bestDist = distance;
            }
        }

        if (best != null)
        {
            page.setColorFill(color);
            page.rectangle((float) best.getMinX(), (float) best.getMinY(), (float) best.getWidth(), (float) best.getHeight());
            page.fill();
            System.out.printf("    Best rectangle for %7.3f, %7.3f is %7.3f, %7.3f, %7.3f, %7.3f\n", point.getX(), point.getY(), best.getMinX(),
                    best.getMinY(), best.getWidth(), best.getHeight());
        }
        else
        {
            System.err.printf("!!! No best rectangle for %7.3f, %7.3f\n", point.getX(), point.getY());
        }
    }

    double distance(Rectangle2D rectangle, Point2D point)
    {
        double x = point.getX();
        double y = point.getY();
        double left = rectangle.getMinX();
        double right = rectangle.getMaxX();
        double top = rectangle.getMaxY();
        double bottom = rectangle.getMinY();

        if (x < left) // point left of rect
        {
            if (y < bottom) // and below
                return Point2D.distance(x, y, left, bottom);
            if (y > top) // and top
                return Point2D.distance(x, y, left, top);
            return left - x;
        }
        if (x > right) // point right of rect
        {
            if (y < bottom) // and below
                return Point2D.distance(x, y, right, bottom);
            if (y > top) // and top
                return Point2D.distance(x, y, right, top);
            return x - right;
        }
        if (y < bottom) // and below
            return bottom - y;
        if (y > top) // and top
            return y - top;
        return 0;
    }

    Point2D[] getPointsOfInterest(Rectangle box)
    {
        Point2D[] result = new Point2D[6];
        result[0] = new Point2D.Float(box.getLeft(), box.getTop());
        result[1] = new Point2D.Float(box.getRight(), box.getTop());
        result[2] = new Point2D.Float(box.getRight(), (box.getTop() + box.getBottom()) / 2.0f);
        result[3] = new Point2D.Float(box.getRight(), box.getBottom());
        result[4] = new Point2D.Float(box.getLeft(), box.getBottom());
        result[5] = new Point2D.Float(box.getLeft(), (box.getTop() + box.getBottom()) / 2.0f);
        return result;
    }

    final static BaseColor[] colors = new BaseColor[] { BaseColor.RED, BaseColor.PINK, BaseColor.ORANGE, /*
                                                                                                          * BaseColor
                                                                                                          * .
                                                                                                          * YELLOW
                                                                                                          * ,
                                                                                                          */BaseColor.GREEN, BaseColor.MAGENTA, BaseColor.CYAN,
            BaseColor.BLUE };
    static int colorIndex = 0;

    static BaseColor pickColor()
    {
        colorIndex++;
        if (colorIndex >= colors.length)
            colorIndex = 0;
        return colors[colorIndex];
    }
}
