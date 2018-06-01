package mkl.testarea.itext5.extract;

import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.pdf.parser.ExtRenderListener;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.Path;
import com.itextpdf.text.pdf.parser.PathConstructionRenderInfo;
import com.itextpdf.text.pdf.parser.PathPaintingRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * <a href="http://stackoverflow.com/questions/31730278/extract-text-from-pdf-between-two-dividers-with-itextsharp">
 * Extract text from PDF between two dividers with ITextSharp
 * </a>
 * <br>
 * <a href="http://www.tjsc.jus.br/institucional/diario/a2015/20150211600.PDF">
 * 20150211600.PDF
 * </a>
 * <p>
 * This text extraction strategy attempts to extract text sections separated by
 * dividers as in the sample PDF.
 * </p>
 * <p>
 * In the same pass this strategy collects the text chunks (thanks to the
 * {@link LocationTextExtractionStrategy} it is derived from) and the divider
 * lines (due to its implementation of the {@link ExtRenderListener} extra
 * methods). Having parsed a page, the strategy offers a list of {@link Section}
 * instances each representing a section of the page delimited by a divider line
 * above and/or below, cf. the method {@link #getSections()}. {@link Section}
 * implements {@link TextChunkFilter} and, therefore, can be used to retrieve
 * the text in the page sections using the parent class method
 * {@link #getResultantText(TextChunkFilter)}.
 * </p>
 * <p>
 * This is merely a POC, it is designed to extract sections from documents using
 * dividers exactly like the sample document does, i.e. horizontal lines drawn
 * using <code>moveTo-lineTo-stroke</code> as wide as the section is, appearing
 * in the content stream column-wise sorted.
 * </p>
 *
 * @author mkl
 */
public class DividerAwareTextExtrationStrategy extends LocationTextExtractionStrategy implements ExtRenderListener {
    //
    // constructor
    //

    /**
     * The constructor accepts top and bottom margin lines in user space y coordinates
     * and left and right margin lines in user space x coordinates.
     * Text outside those margin lines is ignored.
     */
    public DividerAwareTextExtrationStrategy(float topMargin, float bottomMargin, float leftMargin, float rightMargin) {
        this.topMargin = topMargin;
        this.bottomMargin = bottomMargin;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
    }

    //
    // Divider derived section support
    //

    /**
     * <p>
     * This method returns a {@link List} of {@link Section} instances each representing
     * a section of the page delimited by a divider line above and/or below. The topmost
     * and bottommost sections of each text column are open at the top or the bottom,
     * implicitly delimited by the matching margin line.
     * </p>
     * <p>
     * {@link Section} implements {@link TextChunkFilter}. Thus, these section objects can be
     * used as argument of the parent class method {@link #getResultantText(TextChunkFilter)}.
     * </p>
     */
    public List<Section> getSections() {
        List<Section> result = new ArrayList<Section>();
        // TODO: Sort the array columnwise. In case of the OP's document, the lines already appear in the
        // correct order, so there was no need for sorting in the POC. 

        LineSegment previous = null;
        for (LineSegment line : lines) {
            if (previous == null) {
                result.add(new Section(null, line));
            } else if (Math.abs(previous.getStartPoint().get(Vector.I1) - line.getStartPoint().get(Vector.I1)) < 2) // 2 is a magic number...
            {
                result.add(new Section(previous, line));
            } else {
                result.add(new Section(previous, null));
                result.add(new Section(null, line));
            }
            previous = line;
        }

        return result;
    }

    /**
     * <p>
     * This inner class represents a section of the page delimited by a divider line
     * above and/or below, or the section delimited by the margin lines (not currently
     * used).
     * </p>
     * <p>
     * {@link Section} implements {@link TextChunkFilter} and. Therefore, can be used as
     * argument of the outer parent class method {@link #getResultantText(TextChunkFilter)}.
     * </p>
     */
    public class Section implements TextChunkFilter {
        LineSegment topLine;
        LineSegment bottomLine;

        final float left, right, top, bottom;

        Section(LineSegment topLine, LineSegment bottomLine) {
            float left, right, top, bottom;
            if (topLine != null) {
                this.topLine = topLine;
                top = Math.max(topLine.getStartPoint().get(Vector.I2), topLine.getEndPoint().get(Vector.I2));
                right = Math.max(topLine.getStartPoint().get(Vector.I1), topLine.getEndPoint().get(Vector.I1));
                left = Math.min(topLine.getStartPoint().get(Vector.I1), topLine.getEndPoint().get(Vector.I1));
            } else {
                top = topMargin;
                left = leftMargin;
                right = rightMargin;
            }

            if (bottomLine != null) {
                this.bottomLine = bottomLine;
                bottom = Math.min(bottomLine.getStartPoint().get(Vector.I2), bottomLine.getEndPoint().get(Vector.I2));
                right = Math.max(bottomLine.getStartPoint().get(Vector.I1), bottomLine.getEndPoint().get(Vector.I1));
                left = Math.min(bottomLine.getStartPoint().get(Vector.I1), bottomLine.getEndPoint().get(Vector.I1));
            } else {
                bottom = bottomMargin;
            }

            this.top = top;
            this.bottom = bottom;
            this.left = left;
            this.right = right;
        }

        //
        // TextChunkFilter
        //
        @Override
        public boolean accept(TextChunk textChunk) {
            // TODO: This code only checks the text chunk starting point. One should take the 
            // whole chunk into consideration
            Vector startlocation = textChunk.getStartLocation();
            float x = startlocation.get(Vector.I1);
            float y = startlocation.get(Vector.I2);

            return (left <= x) && (x <= right) && (bottom <= y) && (y <= top);
        }
    }

    //
    // ExtRenderListener implementation
    //

    /**
     * <p>
     * This method stores targets of <code>moveTo</code> in {@link #moveToVector}
     * and targets of <code>lineTo</code> in {@link #lineToVector}. Any unexpected
     * contents or operations result in clearing of the member variables.
     * </p>
     * <p>
     * So this method is implemented for files with divider lines exactly like in
     * the OP's sample file.
     * </p>
     *
     * @see ExtRenderListener#modifyPath(PathConstructionRenderInfo)
     */
    @Override
    public void modifyPath(PathConstructionRenderInfo renderInfo) {
        switch (renderInfo.getOperation()) {
            case PathConstructionRenderInfo.MOVETO: {
                float x = renderInfo.getSegmentData().get(0);
                float y = renderInfo.getSegmentData().get(1);
                moveToVector = new Vector(x, y, 1);
                lineToVector = null;
                break;
            }
            case PathConstructionRenderInfo.LINETO: {
                float x = renderInfo.getSegmentData().get(0);
                float y = renderInfo.getSegmentData().get(1);
                if (moveToVector != null) {
                    lineToVector = new Vector(x, y, 1);
                }
                break;
            }
            default:
                moveToVector = null;
                lineToVector = null;
        }
    }

    /**
     * This method adds the current path to {@link #lines} if it consists
     * of a single line, the operation is no no-op, and the line is
     * approximately horizontal.
     *
     * @see ExtRenderListener#renderPath(PathPaintingRenderInfo)
     */
    @Override
    public Path renderPath(PathPaintingRenderInfo renderInfo) {
        if (moveToVector != null && lineToVector != null &&
                renderInfo.getOperation() != PathPaintingRenderInfo.NO_OP) {
            Vector from = moveToVector.cross(renderInfo.getCtm());
            Vector to = lineToVector.cross(renderInfo.getCtm());
            Vector extent = to.subtract(from);

            if (Math.abs(20 * extent.get(Vector.I2)) < Math.abs(extent.get(Vector.I1))) {
                LineSegment line;
                if (extent.get(Vector.I1) >= 0)
                    line = new LineSegment(from, to);
                else
                    line = new LineSegment(to, from);
                lines.add(line);
            }
        }

        moveToVector = null;
        lineToVector = null;
        return null;
    }

    /* (non-Javadoc)
     * @see com.itextpdf.text.pdf.parser.ExtRenderListener#clipPath(int)
     */
    @Override
    public void clipPath(int rule) {
    }

    //
    // inner members
    //
    final float topMargin, bottomMargin, leftMargin, rightMargin;
    Vector moveToVector = null;
    Vector lineToVector = null;
    final List<LineSegment> lines = new ArrayList<LineSegment>();
}
