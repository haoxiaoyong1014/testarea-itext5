package mkl.testarea.itext5.extract;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
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
 * dividers or differently colored headings as in the sample PDF.
 * </p>
 * <p>
 * This strategy (thanks to the {@link DividerAwareTextExtrationStrategy} it is
 * derived from) already can extract sections separated by dividers. To also
 * separate by differently colored headings, it adds the ascender lines of
 * text in a given color to the list of recognized divider lines.
 * </p>
 * <p>
 * This is merely a POC, it is designed to extract sections from documents using
 * dividers and headers exactly like the sample document does, i.e. horizontal
 * lines drawn using <code>moveTo-lineTo-stroke</code> as wide as the section is,
 * appearing in the content stream column-wise sorted, and heading lines drawn as
 * a single text drawing command starting at the same x coordinates as the 
 * divider lines do. 
 * </p>
 * 
 * @author mkl
 */
public class DividerAndColorAwareTextExtractionStrategy extends DividerAwareTextExtrationStrategy
{
    //
    // constructor
    //
    public DividerAndColorAwareTextExtractionStrategy(float topMargin, float bottomMargin, float leftMargin, float rightMargin, BaseColor headerColor)
    {
        super(topMargin, bottomMargin, leftMargin, rightMargin);
        this.headerColor = headerColor;
    }

    //
    // DividerAwareTextExtrationStrategy overrides
    //
    /**
     * <p>
     * As the {@link DividerAwareTextExtrationStrategy#lines} are not
     * properly sorted anymore (the additional lines come after all
     * divider lines of the same column), we have to sort that {@link List}
     * first.
     * </p>
     * <p>
     * Please be aware that the {@link Comparator} used here is not really
     * proper: It ignores a certain difference in the x coordinate which
     * makes it not really transitive. It only works if the individual lines
     * of the same column have approximately the same starting x coordinate
     * differing clearly from those of different columns.
     * </p>
     */
    @Override
    public List<Section> getSections()
    {
        Collections.sort(lines, new Comparator<LineSegment>()
        {
            @Override
            public int compare(LineSegment o1, LineSegment o2)
            {
                Vector start1 = o1.getStartPoint();
                Vector start2 = o2.getStartPoint();

                float v1 = start1.get(Vector.I1), v2 = start2.get(Vector.I1);
                if (Math.abs(v1 - v2) < 2)
                {
                    v1 = start2.get(Vector.I2);
                    v2 = start1.get(Vector.I2);
                }

                return Float.compare(v1, v2);
            }
        });

        return super.getSections();
    }

    /**
     * <p>
     * The ascender lines of text rendered using a fill color approximately
     * like the given header color are added to the divider lines.
     * </p>
     * <p>
     * Beware: we add the ascender line of each chunk in the given color.
     * We actually should join the ascender lines of all text chunks forming
     * a header line. As the blue header lines in the sample document consist
     * of merely a single chunk, we don't need to in this sample code.   
     * </p>
     */
    @Override
    public void renderText(TextRenderInfo renderInfo)
    {
        if (approximates(renderInfo.getFillColor(), headerColor))
        {
            lines.add(renderInfo.getAscentLine());
        }
        
        super.renderText(renderInfo);
    }

    /**
     * This method checks whether two colors are approximately equal. As the
     * sample document only uses CMYK colors, only this comparison has been
     * implemented yet.
     */
    boolean approximates(BaseColor colorA, BaseColor colorB)
    {
        if (colorA == null || colorB == null)
            return colorA == colorB;
        if (colorA instanceof CMYKColor && colorB instanceof CMYKColor)
        {
            CMYKColor cmykA = (CMYKColor) colorA;
            CMYKColor cmykB = (CMYKColor) colorB;
            float c = Math.abs(cmykA.getCyan() - cmykB.getCyan());
            float m = Math.abs(cmykA.getMagenta() - cmykB.getMagenta());
            float y = Math.abs(cmykA.getYellow() - cmykB.getYellow());
            float k = Math.abs(cmykA.getBlack() - cmykB.getBlack());
            return c+m+y+k < 0.01;
        }
        // TODO: Implement comparison for other color types
        return false;
    }

    final BaseColor headerColor;
}
