package mkl.testarea.itext5.extract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.pdf.parser.ExtRenderListener;
import com.itextpdf.text.pdf.parser.Path;
import com.itextpdf.text.pdf.parser.PathConstructionRenderInfo;
import com.itextpdf.text.pdf.parser.PathPaintingRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * This {@link ExtRenderListener} extends {@link FreeSpaceFinder} to also
 * take vector graphics into account.
 * 
 * @author mkl
 */
public class FreeSpaceFinderExt extends FreeSpaceFinder implements ExtRenderListener
{
    //
    // constructors
    //
    public FreeSpaceFinderExt(Rectangle2D initialBox, float minWidth, float minHeight)
    {
        this(Collections.singleton(initialBox), minWidth, minHeight);
    }

    public FreeSpaceFinderExt(Collection<Rectangle2D> initialBoxes, float minWidth, float minHeight)
    {
    	super(initialBoxes, minWidth, minHeight);
    }

    //
    // Additional ExtRenderListener methods
    //
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
        	remove(currentPathRectangle);
        currentPathRectangle = null;

        return null;
    }

    @Override
    public void clipPath(int rule)
    {
        // TODO Auto-generated method stub
        
    }

    Rectangle2D.Float currentPathRectangle = null;
}
