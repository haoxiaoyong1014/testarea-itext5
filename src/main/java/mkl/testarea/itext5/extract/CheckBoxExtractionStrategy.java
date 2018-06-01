package mkl.testarea.itext5.extract;

import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.parser.ExtRenderListener;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.Path;
import com.itextpdf.text.pdf.parser.PathConstructionRenderInfo;
import com.itextpdf.text.pdf.parser.PathPaintingRenderInfo;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * <a href="http://stackoverflow.com/questions/40549977/reading-legacy-word-forms-checkboxes-converted-to-pdf">
 * Reading legacy Word forms checkboxes converted to PDF
 * </a>
 * <br>
 * <a href="https://www.dropbox.com/s/4z7ky3yy2yaj53i/Doc1.pdf?dl=0">
 * Doc1.pdf
 * </a>
 * <p>
 * This <code>ExtRenderListener</code> extracts drawn check boxes as presented in the
 * sample PDF provided by the OP.
 * </p>
 * <p>
 * It assumes that the cross is drawn right after the rectangle.
 * </p>
 */
public class CheckBoxExtractionStrategy implements ExtRenderListener
{
    public class Box
    {
        public LineSegment getDiagonal()
        {
            return diagonalA;
        }

        public boolean isChecked()
        {
            return selectedA && selectedB;
        }

        Box(LineSegment diagonalA, LineSegment diagonalB)
        {
            this.diagonalA = diagonalA;
            this.diagonalB = diagonalB;
        }

        void selectDiagonal(LineSegment diagonal)
        {
            if (approximatelyEquals(diagonal, diagonalA))
                selectedA = true;
            else if (approximatelyEquals(diagonal, diagonalB))
                selectedB = true;
        }

        boolean approximatelyEquals(LineSegment a, LineSegment b)
        {
            float permissiveness = a.getLength() / 10.0f;
            if (approximatelyEquals(a.getStartPoint(), b.getStartPoint(), permissiveness) &&
                    approximatelyEquals(a.getEndPoint(), b.getEndPoint(), permissiveness))
                return true;
            if (approximatelyEquals(a.getStartPoint(), b.getEndPoint(), permissiveness) &&
                    approximatelyEquals(a.getEndPoint(), b.getStartPoint(), permissiveness))
                return true;
            return false;
        }

        boolean approximatelyEquals(Vector a, Vector b, float permissiveness)
        {
            return a.subtract(b).length() < permissiveness;
        }

        boolean selectedA = false;
        boolean selectedB = false;
        final LineSegment diagonalA, diagonalB;
    }

    public Iterable<Box> getBoxes()
    {
        return boxes;
    }

    @Override
    public void beginTextBlock() { }

    @Override
    public void endTextBlock() { }

    @Override
    public void renderImage(ImageRenderInfo arg0) { }

    @Override
    public void renderText(TextRenderInfo arg0) { }

    @Override
    public void clipPath(int arg0) { }

    @Override
    public void modifyPath(PathConstructionRenderInfo renderInfo)
    {
        switch (renderInfo.getOperation())
        {
        case PathConstructionRenderInfo.RECT:
        {
            float x = renderInfo.getSegmentData().get(0);
            float y = renderInfo.getSegmentData().get(1);
            float w = renderInfo.getSegmentData().get(2);
            float h = renderInfo.getSegmentData().get(3);
            rectangle = new Rectangle(x, y, x+w, y+h);
        }
        case PathConstructionRenderInfo.MOVETO:
        {
            float x = renderInfo.getSegmentData().get(0);
            float y = renderInfo.getSegmentData().get(1);
            moveToVector = new Vector(x, y, 1);
            lineToVector = null;
            break;
        }
        case PathConstructionRenderInfo.LINETO:
        {
            if (moveToVector != null)
            {
                float x = renderInfo.getSegmentData().get(0);
                float y = renderInfo.getSegmentData().get(1);
                lineToVector = new Vector(x, y, 1);
            }
            break;
        }
        default:
            moveToVector = null;
            lineToVector = null;
        }
    }

    @Override
    public Path renderPath(PathPaintingRenderInfo renderInfo)
    {
        if (renderInfo.getOperation() != PathPaintingRenderInfo.NO_OP)
        {
            if (rectangle != null)
            {
                Vector a = new Vector(rectangle.getLeft(), rectangle.getBottom(), 1).cross(renderInfo.getCtm());
                Vector b = new Vector(rectangle.getRight(), rectangle.getBottom(), 1).cross(renderInfo.getCtm());
                Vector c = new Vector(rectangle.getRight(), rectangle.getTop(), 1).cross(renderInfo.getCtm());
                Vector d = new Vector(rectangle.getLeft(), rectangle.getTop(), 1).cross(renderInfo.getCtm());

                Box box = new Box(new LineSegment(a, c), new LineSegment(b, d));
                boxes.add(box);
                
            }
            if (moveToVector != null && lineToVector != null)
            {
                if (!boxes.isEmpty())
                {
                    Vector from = moveToVector.cross(renderInfo.getCtm());
                    Vector to = lineToVector.cross(renderInfo.getCtm());

                    boxes.get(boxes.size() - 1).selectDiagonal(new LineSegment(from, to));
                }
            }
        }

        moveToVector = null;
        lineToVector = null;
        rectangle = null;
        return null;
    }

    Vector moveToVector = null;
    Vector lineToVector = null;
    Rectangle rectangle = null;

    final List<LineSegment> lines = new ArrayList<LineSegment>();
    final List<Box> boxes = new ArrayList<Box>();
}
