package mkl.testarea.itext5.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mkl.testarea.itext5.merge.PageVerticalAnalyzer;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * <a href="http://stackoverflow.com/questions/33492792/how-can-i-extract-subscript-superscript-properly-from-a-pdf-using-itextsharp">
 * How can I extract subscript / superscript properly from a PDF using iTextSharp?
 * </a>
 * <br/>
 * <a href="http://www.mass.gov/courts/docs/lawlib/300-399cmr/310cmr7.pdf">310cmr7.pdf</a>
 * <p>
 * This {@link RenderListener} tries to identify horizontal text lines
 * by projecting the text bounding boxes onto the y axis. It assumes that
 * these projections do not overlap for text from different lines, even
 * in case of subscripts and superscripts.
 * </p>
 * <p>
 * This class essentially is a reduced form of the {@link PageVerticalAnalyzer}.
 * </p>
 * 
 * @author mkl
 */
public class TextLineFinder implements RenderListener
{
    @Override
    public void beginTextBlock() { }
    @Override
    public void endTextBlock() { }
    @Override
    public void renderImage(ImageRenderInfo renderInfo) { }

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
