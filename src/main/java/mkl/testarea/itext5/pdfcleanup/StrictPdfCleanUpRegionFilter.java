package mkl.testarea.itext5.pdfcleanup;

import java.util.List;

import com.itextpdf.awt.geom.Point2D;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * In contrast to the base class {@link PdfCleanUpRegionFilter}, this filter
 * only rejects text <b>completely</b> inside the redaction zone. The original
 * also rejects text located merely <b>partially</b> inside the redaction zone.
 */
public class StrictPdfCleanUpRegionFilter extends PdfCleanUpRegionFilter
{
    public StrictPdfCleanUpRegionFilter(List<Rectangle> rectangles)
    {
        super(rectangles);
        this.rectangles = rectangles;
    }

    /**
     * Checks if the text is COMPLETELY inside render filter region.
     */
    @Override
    public boolean allowText(TextRenderInfo renderInfo) {
        LineSegment ascent = renderInfo.getAscentLine();
        LineSegment descent = renderInfo.getDescentLine();

        Point2D[] glyphRect = new Point2D[] {
                new Point2D.Float(ascent.getStartPoint().get(0), ascent.getStartPoint().get(1)),
                new Point2D.Float(ascent.getEndPoint().get(0), ascent.getEndPoint().get(1)),
                new Point2D.Float(descent.getEndPoint().get(0), descent.getEndPoint().get(1)),
                new Point2D.Float(descent.getStartPoint().get(0), descent.getStartPoint().get(1)),
        };

        for (Rectangle rectangle : rectangles)
        {
            boolean glyphInRectangle = true;
            for (Point2D point2d : glyphRect)
            {
                glyphInRectangle &= rectangle.getLeft() <= point2d.getX();
                glyphInRectangle &= point2d.getX() <= rectangle.getRight();
                glyphInRectangle &= rectangle.getBottom() <= point2d.getY();
                glyphInRectangle &= point2d.getY() <= rectangle.getTop();
            }
            if (glyphInRectangle)
                return false;
        }

        return true;
    }

    List<Rectangle> rectangles;
}
