package mkl.testarea.itext5.merge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.itextpdf.text.pdf.parser.ExtRenderListener;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.Path;
import com.itextpdf.text.pdf.parser.PathConstructionRenderInfo;
import com.itextpdf.text.pdf.parser.PathPaintingRenderInfo;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * This {@link ExtRenderListener}looks for vertical sections of use in a page.
 * <p>
 * Beware, this is a mere proof of concept. Especially {@link #modifyPath(PathConstructionRenderInfo)}
 * needs to be improved, see the comment there.
 * 
 * @author mkl
 */
public class PageVerticalAnalyzer implements ExtRenderListener
{
    @Override
    public void beginTextBlock() { }
    @Override
    public void endTextBlock() { }
    @Override
    public void clipPath(int rule) { }

    /*
     * @see RenderListener#renderText(TextRenderInfo)
     */
    @Override
    public void renderText(TextRenderInfo renderInfo)
    {
        LineSegment ascentLine = renderInfo.getAscentLine();
        LineSegment descentLine = renderInfo.getDescentLine();
        float[] yCoords = new float[]{
                ascentLine.getStartPoint().get(Vector.I2),
                ascentLine.getEndPoint().get(Vector.I2),
                descentLine.getStartPoint().get(Vector.I2),
                descentLine.getEndPoint().get(Vector.I2)
        };
        Arrays.sort(yCoords);
        addVerticalUseSection(yCoords[0], yCoords[3]);
    }

    /*
     * @see RenderListener#renderImage(ImageRenderInfo)
     */
    @Override
    public void renderImage(ImageRenderInfo renderInfo)
    {
        Matrix ctm = renderInfo.getImageCTM();
        float[] yCoords = new float[4];
        for (int x=0; x < 2; x++)
            for (int y=0; y < 2; y++)
            {
                Vector corner = new Vector(x, y, 1).cross(ctm);
                yCoords[2*x+y] = corner.get(Vector.I2);
            }
        Arrays.sort(yCoords);
        addVerticalUseSection(yCoords[0], yCoords[3]);
    }

    static class SubPathSection
    {
        public SubPathSection(float x, float y, Matrix m)
        {
            float effectiveY = getTransformedY(x, y, m);
            pathFromY = effectiveY;
            pathToY = effectiveY;
        }

        void extendTo(float x, float y, Matrix m)
        {
            float effectiveY = getTransformedY(x, y, m);
            if (effectiveY < pathFromY)
                pathFromY = effectiveY;
            else if (effectiveY > pathToY)
                pathToY = effectiveY;
        }
        
        float getTransformedY(float x, float y, Matrix m)
        {
            return new Vector(x, y, 1).cross(m).get(Vector.I2);
        }
        
        float getFromY()
        {
            return pathFromY;
        }
        
        float getToY()
        {
            return pathToY;
        }
        
        private float pathFromY;
        private float pathToY;
    }

    /*
     * Beware: The implementation is not correct as it includes the control points of curves
     * which may be far outside the actual curve.
     * 
     * @see ExtRenderListener#modifyPath(PathConstructionRenderInfo)
     */
    @Override
    public void modifyPath(PathConstructionRenderInfo renderInfo)
    {
        Matrix ctm = renderInfo.getCtm();
        List<Float> segmentData = renderInfo.getSegmentData();

        switch (renderInfo.getOperation())
        {
        case PathConstructionRenderInfo.MOVETO:
            subPath = null;
        case PathConstructionRenderInfo.LINETO:
        case PathConstructionRenderInfo.CURVE_123:
        case PathConstructionRenderInfo.CURVE_13:
        case PathConstructionRenderInfo.CURVE_23:
            for (int i = 0; i < segmentData.size()-1; i+=2)
            {
                if (subPath == null)
                {
                    subPath = new SubPathSection(segmentData.get(i), segmentData.get(i+1), ctm);
                    path.add(subPath);
                }
                else
                    subPath.extendTo(segmentData.get(i), segmentData.get(i+1), ctm);
            }
            break;
        case PathConstructionRenderInfo.RECT:
            float x = segmentData.get(0);
            float y = segmentData.get(1);
            float w = segmentData.get(2);
            float h = segmentData.get(3);
            SubPathSection section = new SubPathSection(x, y, ctm);
            section.extendTo(x+w, y, ctm);
            section.extendTo(x, y+h, ctm);
            section.extendTo(x+w, y+h, ctm);
            path.add(section);
        case PathConstructionRenderInfo.CLOSE:
            subPath = null;
            break;
        default:
                
        }
    }

    /*
     * @see ExtRenderListener#renderPath(PathPaintingRenderInfo)
     */
    @Override
    public Path renderPath(PathPaintingRenderInfo renderInfo)
    {
        if (renderInfo.getOperation() != PathPaintingRenderInfo.NO_OP)
        {
            for (SubPathSection section : path)
                addVerticalUseSection(section.getFromY(), section.getToY());
        }

        path.clear();
        subPath = null;
        return null;
    }

    List<SubPathSection> path = new ArrayList<SubPathSection>();
    SubPathSection subPath = null;
    
    /**
     * This method marks the given interval as used.
     */
    void addVerticalUseSection(float from, float to)
    {
        if (to < from)
        {
            float temp = to;
            to = from;
            from = temp;
        }

        int i=0, j=0;
        for (; i<verticalFlips.size(); i++)
        {
            float flip = verticalFlips.get(i);
            if (flip < from)
                continue;

            for (j=i; j<verticalFlips.size(); j++)
            {
                flip = verticalFlips.get(j);
                if (flip < to)
                    continue;
                break;
            }
            break;
        }
        boolean fromOutsideInterval = i%2==0;
        boolean toOutsideInterval = j%2==0;

        while (j-- > i)
            verticalFlips.remove(j);
        if (toOutsideInterval)
            verticalFlips.add(i, to);
        if (fromOutsideInterval)
            verticalFlips.add(i, from);
    }

    final List<Float> verticalFlips = new ArrayList<Float>();
}
