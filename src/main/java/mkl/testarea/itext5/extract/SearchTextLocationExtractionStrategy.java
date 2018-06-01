package mkl.testarea.itext5.extract;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * <a href=
 * "https://stackoverflow.com/questions/45823741/how-to-find-all-occurrences-of-specific-text-in-a-pdf-and-insert-a-page-break-ab">
 * How to find all occurrences of specific text in a PDF and insert a page break
 * above? </a>
 * <p>
 * This text extraction strategy <i>finds all occurrences of specific text in a
 * PDF</i>, it actually even allows the use of a regular expression.
 * </p>
 * 
 * @author mkl
 */
public class SearchTextLocationExtractionStrategy extends LocationTextExtractionStrategy {
    public SearchTextLocationExtractionStrategy(Pattern pattern) {
        super(new TextChunkLocationStrategy() {
            public TextChunkLocation createLocation(TextRenderInfo renderInfo, LineSegment baseline) {
                // while baseLine has been changed to not neutralize
                // effects of rise, ascentLine and descentLine explicitly
                // have not: We want the actual positions.
                return new AscentDescentTextChunkLocation(baseline, renderInfo.getAscentLine(),
                        renderInfo.getDescentLine(), renderInfo.getSingleSpaceWidth());
            }
        });
        this.pattern = pattern;
    }

    static Field locationalResultField = null;
    static Method filterTextChunksMethod = null;
    static Method startsWithSpaceMethod = null;
    static Method endsWithSpaceMethod = null;
    static Field textChunkTextField = null;
    static Method textChunkSameLineMethod = null;
    static {
        try {
            locationalResultField = LocationTextExtractionStrategy.class.getDeclaredField("locationalResult");
            locationalResultField.setAccessible(true);
            filterTextChunksMethod = LocationTextExtractionStrategy.class.getDeclaredMethod("filterTextChunks",
                    List.class, TextChunkFilter.class);
            filterTextChunksMethod.setAccessible(true);
            startsWithSpaceMethod = LocationTextExtractionStrategy.class.getDeclaredMethod("startsWithSpace",
                    String.class);
            startsWithSpaceMethod.setAccessible(true);
            endsWithSpaceMethod = LocationTextExtractionStrategy.class.getDeclaredMethod("endsWithSpace", String.class);
            endsWithSpaceMethod.setAccessible(true);
            textChunkTextField = TextChunk.class.getDeclaredField("text");
            textChunkTextField.setAccessible(true);
            textChunkSameLineMethod = TextChunk.class.getDeclaredMethod("sameLine", TextChunk.class);
            textChunkSameLineMethod.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Collection<TextRectangle> getLocations(TextChunkFilter chunkFilter) {
        Collection<TextRectangle> result = new ArrayList<>();
        try {
            List<TextChunk> filteredTextChunks = (List<TextChunk>) filterTextChunksMethod.invoke(this,
                    locationalResultField.get(this), chunkFilter);
            Collections.sort(filteredTextChunks);

            StringBuilder sb = new StringBuilder();
            List<AscentDescentTextChunkLocation> locations = new ArrayList<>();
            TextChunk lastChunk = null;
            for (TextChunk chunk : filteredTextChunks) {
                String chunkText = (String) textChunkTextField.get(chunk);
                if (lastChunk == null) {
                    // Nothing to compare with at the end
                } else if ((boolean) textChunkSameLineMethod.invoke(chunk, lastChunk)) {
                    // we only insert a blank space if the trailing character of the previous string
                    // wasn't a space,
                    // and the leading character of the current string isn't a space
                    if (isChunkAtWordBoundary(chunk, lastChunk)
                            && !((boolean) startsWithSpaceMethod.invoke(this, chunkText))
                            && !((boolean) endsWithSpaceMethod.invoke(this, chunkText))) {
                        sb.append(' ');
                        LineSegment spaceBaseLine = new LineSegment(lastChunk.getEndLocation(),
                                chunk.getStartLocation());
                        locations.add(new AscentDescentTextChunkLocation(spaceBaseLine, spaceBaseLine, spaceBaseLine,
                                chunk.getCharSpaceWidth()));
                    }
                } else {
                    assert sb.length() == locations.size();
                    Matcher matcher = pattern.matcher(sb);
                    while (matcher.find()) {
                        int i = matcher.start();
                        Vector baseStart = locations.get(i).getStartLocation();
                        TextRectangle textRectangle = new TextRectangle(matcher.group(), baseStart.get(Vector.I1),
                                baseStart.get(Vector.I2));
                        for (; i < matcher.end(); i++) {
                            AscentDescentTextChunkLocation location = locations.get(i);
                            textRectangle.add(location.getAscentLine().getBoundingRectange());
                            textRectangle.add(location.getDescentLine().getBoundingRectange());
                        }

                        result.add(textRectangle);
                    }

                    sb.setLength(0);
                    locations.clear();
                }
                sb.append(chunkText);
                locations.add((AscentDescentTextChunkLocation) chunk.getLocation());
                lastChunk = chunk;
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void renderText(TextRenderInfo renderInfo) {
        for (TextRenderInfo info : renderInfo.getCharacterRenderInfos())
            super.renderText(info);
    }

    public static class AscentDescentTextChunkLocation extends TextChunkLocationDefaultImp {
        public AscentDescentTextChunkLocation(LineSegment baseLine, LineSegment ascentLine, LineSegment descentLine,
                float charSpaceWidth) {
            super(baseLine.getStartPoint(), baseLine.getEndPoint(), charSpaceWidth);
            this.ascentLine = ascentLine;
            this.descentLine = descentLine;
        }

        public LineSegment getAscentLine() {
            return ascentLine;
        }

        public LineSegment getDescentLine() {
            return descentLine;
        }

        final LineSegment ascentLine;
        final LineSegment descentLine;
    }

    public class TextRectangle extends Rectangle2D.Float {
        public TextRectangle(final String text, final float xStart, final float yStart) {
            super(xStart, yStart, 0, 0);
            this.text = text;
        }

        public String getText() {
            return text;
        }

        final String text;
    }

    final Pattern pattern;
}
