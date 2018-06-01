package mkl.testarea.itext5.extract;

import java.lang.reflect.Field;
import java.util.List;

import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * <a href="http://stackoverflow.com/questions/33492792/how-can-i-extract-subscript-superscript-properly-from-a-pdf-using-itextsharp">
 * How can I extract subscript / superscript properly from a PDF using iTextSharp?
 * </a>
 * <br/>
 * <a href="http://www.mass.gov/courts/docs/lawlib/300-399cmr/310cmr7.pdf">310cmr7.pdf</a>
 * <p>
 * This {@link TextExtractionStrategy} uses a {@link TextLineFinder} to identify
 * horizontal text lines and then uses these informations to sort the text chunks.
 * </p>
 * @deprecated use {@link HorizontalTextExtractionStrategy2} with iText versions
 *  after 5.5.9-SNAPSHOT, Commit 53526e4854fcb80c86cbc2e113f7a07401dc9a67
 *  ("Refactor LocationTextExtractionStrategy...")
 * @author mkl
 */
public class HorizontalTextExtractionStrategy extends LocationTextExtractionStrategy
{
    public class HorizontalTextChunk extends TextChunk
    {
        public HorizontalTextChunk(String string, Vector startLocation, Vector endLocation, float charSpaceWidth)
        {
            super(string, startLocation, endLocation, charSpaceWidth);
        }

        @Override
        public int compareTo(TextChunk rhs)
        {
            if (rhs instanceof HorizontalTextChunk)
            {
                HorizontalTextChunk horRhs = (HorizontalTextChunk) rhs;
                int rslt = Integer.compare(getLineNumber(), horRhs.getLineNumber());
                if (rslt != 0) return rslt;
                return Float.compare(getStartLocation().get(Vector.I1), rhs.getStartLocation().get(Vector.I1));
            }
            else
                return super.compareTo(rhs);
        }

// As of Commit 53526e4854fcb80c86cbc2e113f7a07401dc9a67 ("Refactor LocationTextExtractionStrategy...")
// TextChunk changed; to keep this class compilable, drop references to a super method sameLine().
// For use with newer iText versions, use HorizontalTextExtractionStrategy2 instead.
//
//        @Override
        public boolean sameLine(TextChunk as)
        {
            if (as instanceof HorizontalTextChunk)
            {
                HorizontalTextChunk horAs = (HorizontalTextChunk) as;
                return getLineNumber() == horAs.getLineNumber();
            }
//            else
//                return super.sameLine(as);
            return false;
        }

        public int getLineNumber()
        {
            Vector startLocation = getStartLocation();
            float y = startLocation.get(Vector.I2);
            List<Float> flips = textLineFinder.verticalFlips;
            if (flips == null || flips.isEmpty())
                return 0;
            if (y < flips.get(0))
                return flips.size() / 2 + 1;
            for (int i = 1; i < flips.size(); i+=2)
            {
                if (y < flips.get(i))
                {
                    return (1 + flips.size() - i) / 2;
                }
            }
            return 0;
        }
    }

    @Override
    public void renderText(TextRenderInfo renderInfo)
    {
        textLineFinder.renderText(renderInfo);

        LineSegment segment = renderInfo.getBaseline();
        if (renderInfo.getRise() != 0){ // remove the rise from the baseline - we do this because the text from a super/subscript render operations should probably be considered as part of the baseline of the text the super/sub is relative to 
            Matrix riseOffsetTransform = new Matrix(0, -renderInfo.getRise());
            segment = segment.transformBy(riseOffsetTransform);
        }
        TextChunk location = new HorizontalTextChunk(renderInfo.getText(), segment.getStartPoint(), segment.getEndPoint(), renderInfo.getSingleSpaceWidth());
        getLocationalResult().add(location);        
    }

    public HorizontalTextExtractionStrategy() throws NoSuchFieldException, SecurityException
    {
        locationalResultField = LocationTextExtractionStrategy.class.getDeclaredField("locationalResult");
        locationalResultField.setAccessible(true);

        textLineFinder = new TextLineFinder();
    }

    @SuppressWarnings("unchecked")
    List<TextChunk> getLocationalResult()
    {
        try
        {
            return (List<TextChunk>) locationalResultField.get(this);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    final Field locationalResultField;
    final TextLineFinder textLineFinder;
}
