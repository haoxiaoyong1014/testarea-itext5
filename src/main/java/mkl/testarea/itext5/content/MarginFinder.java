package mkl.testarea.itext5.content;

import java.util.ArrayList;
import java.util.List;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.pdf.parser.ExtRenderListener;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.Path;
import com.itextpdf.text.pdf.parser.PathConstructionRenderInfo;
import com.itextpdf.text.pdf.parser.PathPaintingRenderInfo;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextMarginFinder;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * Allows you to find the rectangle that contains all the content in a page.
 * <p>
 * This class is derived from the iText class {@link TextMarginFinder}. While that original class only implements
 * {@link RenderListener} and even there ignores {@link RenderListener#renderImage(ImageRenderInfo)}, this class
 * implements {@link ExtRenderListener} and also respects {@link RenderListener#renderImage(ImageRenderInfo)} calls.
 * 
 * @see TextMarginFinder
 */
public class MarginFinder implements ExtRenderListener {
    private Rectangle2D.Float textRectangle = null;
    private Rectangle2D.Float currentPathRectangle = null;
    
    /**
     * Method invokes by the PdfContentStreamProcessor.
     * Passes a TextRenderInfo for every text chunk that is encountered.
     * We'll use this object to obtain coordinates.
     * @see RenderListener#renderText(TextRenderInfo)
     */
    public void renderText(TextRenderInfo renderInfo) {
        if (textRectangle == null)
            textRectangle = renderInfo.getDescentLine().getBoundingRectange();
        else
            textRectangle.add(renderInfo.getDescentLine().getBoundingRectange());
        
        textRectangle.add(renderInfo.getAscentLine().getBoundingRectange());
    }

    /**
     * Getter for the left margin.
     * @return the X position of the left margin
     */
    public float getLlx() {
        return textRectangle.x;
    }

    /**
     * Getter for the bottom margin.
     * @return the Y position of the bottom margin
     */
    public float getLly() {
        return textRectangle.y;
    }

    /**
     * Getter for the right margin.
     * @return the X position of the right margin
     */
    public float getUrx() {
        return textRectangle.x + textRectangle.width;
    }

    /**
     * Getter for the top margin.
     * @return the Y position of the top margin
     */
    public float getUry() {
        return textRectangle.y + textRectangle.height;
    }

    /**
     * Gets the width of the text block.
     * @return a width
     */
    public float getWidth() {
        return textRectangle.width;
    }
    
    /**
     * Gets the height of the text block.
     * @return a height
     */
    public float getHeight() {
        return textRectangle.height;
    }
    
    /**
     * @see RenderListener#beginTextBlock()
     */
    public void beginTextBlock() {
        // do nothing
    }

    /**
     * @see RenderListener#endTextBlock()
     */
    public void endTextBlock() {
        // do nothing
    }

    /**
     * @see RenderListener#renderImage(ImageRenderInfo)
     */
    public void renderImage(ImageRenderInfo renderInfo)
    {
        Matrix imageCtm = renderInfo.getImageCTM();
        Vector a = new Vector(0, 0, 1).cross(imageCtm);
        Vector b = new Vector(1, 0, 1).cross(imageCtm);
        Vector c = new Vector(0, 1, 1).cross(imageCtm);
        Vector d = new Vector(1, 1, 1).cross(imageCtm);
        LineSegment bottom = new LineSegment(a, b);
        LineSegment top = new LineSegment(c, d);
        if (textRectangle == null)
            textRectangle = bottom.getBoundingRectange();
        else
            textRectangle.add(bottom.getBoundingRectange());
        
        textRectangle.add(top.getBoundingRectange());
    }

    @Override
    public void modifyPath(PathConstructionRenderInfo renderInfo)
    {
        List<Vector> points = new ArrayList<Vector>();
        if (renderInfo.getOperation() == PathConstructionRenderInfo.RECT)
        {
            float x = renderInfo.getSegmentData().get(0);
            float y = renderInfo.getSegmentData().get(1);
            float w = renderInfo.getSegmentData().get(2);
            float h = renderInfo.getSegmentData().get(3);
            points.add(new Vector(x, y, 1));
            points.add(new Vector(x+w, y, 1));
            points.add(new Vector(x, y+h, 1));
            points.add(new Vector(x+w, y+h, 1));
        }
        else if (renderInfo.getSegmentData() != null)
        {
            for (int i = 0; i < renderInfo.getSegmentData().size()-1; i+=2)
            {
                points.add(new Vector(renderInfo.getSegmentData().get(i), renderInfo.getSegmentData().get(i+1), 1));
            }
        }

        for (Vector point: points)
        {
            point = point.cross(renderInfo.getCtm());
            Rectangle2D.Float pointRectangle = new Rectangle2D.Float(point.get(Vector.I1), point.get(Vector.I2), 0, 0);
            if (currentPathRectangle == null)
                currentPathRectangle = pointRectangle;
            else
                currentPathRectangle.add(pointRectangle);
        }
    }

    @Override
    public Path renderPath(PathPaintingRenderInfo renderInfo)
    {
        if (renderInfo.getOperation() != PathPaintingRenderInfo.NO_OP)
        {
            if (textRectangle == null)
                textRectangle = currentPathRectangle;
            else
                textRectangle.add(currentPathRectangle);
        }
        currentPathRectangle = null;

        return null;
    }

    @Override
    public void clipPath(int rule)
    {
        // TODO Auto-generated method stub
        
    }
}
