package mkl.testarea.itext5.extract;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ExtRenderListener;
import com.itextpdf.text.pdf.parser.GraphicsState;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.Path;
import com.itextpdf.text.pdf.parser.PathConstructionRenderInfo;
import com.itextpdf.text.pdf.parser.PathPaintingRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * @author mkl
 */
public class ExtractPaths
{
    /**
     * <a href="http://stackoverflow.com/questions/41728853/how-to-extract-the-color-of-a-rectangle-in-a-pdf-with-itext">
     * How to extract the color of a rectangle in a PDF, with iText
     * </a>
     * <br>
     * <a href="https://drive.google.com/file/d/0B5-hQK6Ic29ZVkp6WXFvY2VQNlk/view?usp=sharing">
     * TestCreation.pdf
     * </a>
     * <p>
     * This test shows how to read paths with their fill and/or stroke color.
     * </p>
     */
    @Test
    public void testExtractFromTestCreation() throws IOException
    {
        ExtRenderListener extRenderListener = new ExtRenderListener()
        {
            @Override
            public void beginTextBlock()                        {   }
            @Override
            public void renderText(TextRenderInfo renderInfo)   {   }
            @Override
            public void endTextBlock()                          {   }
            @Override
            public void renderImage(ImageRenderInfo renderInfo) {   }

            @Override
            public void modifyPath(PathConstructionRenderInfo renderInfo)
            {
                pathInfos.add(renderInfo);
            }

            @Override
            public Path renderPath(PathPaintingRenderInfo renderInfo)
            {
                GraphicsState graphicsState;
                try
                {
                    graphicsState = getGraphicsState(renderInfo);
                }
                catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
                {
                    e.printStackTrace();
                    return null;
                }

                Matrix ctm = graphicsState.getCtm();

                if ((renderInfo.getOperation() & PathPaintingRenderInfo.FILL) != 0)
                {
                    System.out.printf("FILL (%s) ", toString(graphicsState.getFillColor()));
                    if ((renderInfo.getOperation() & PathPaintingRenderInfo.STROKE) != 0)
                        System.out.print("and ");
                }
                if ((renderInfo.getOperation() & PathPaintingRenderInfo.STROKE) != 0)
                {
                    System.out.printf("STROKE (%s) ", toString(graphicsState.getStrokeColor()));
                }

                System.out.print("the path ");

                for (PathConstructionRenderInfo pathConstructionRenderInfo : pathInfos)
                {
                    switch (pathConstructionRenderInfo.getOperation())
                    {
                    case PathConstructionRenderInfo.MOVETO:
                        System.out.printf("move to %s ", transform(ctm, pathConstructionRenderInfo.getSegmentData()));
                        break;
                    case PathConstructionRenderInfo.CLOSE:
                        System.out.printf("close %s ", transform(ctm, pathConstructionRenderInfo.getSegmentData()));
                        break;
                    case PathConstructionRenderInfo.CURVE_123:
                        System.out.printf("curve123 %s ", transform(ctm, pathConstructionRenderInfo.getSegmentData()));
                        break;
                    case PathConstructionRenderInfo.CURVE_13:
                        System.out.printf("curve13 %s ", transform(ctm, pathConstructionRenderInfo.getSegmentData()));
                        break;
                    case PathConstructionRenderInfo.CURVE_23:
                        System.out.printf("curve23 %s ", transform(ctm, pathConstructionRenderInfo.getSegmentData()));
                        break;
                    case PathConstructionRenderInfo.LINETO:
                        System.out.printf("line to %s ", transform(ctm, pathConstructionRenderInfo.getSegmentData()));
                        break;
                    case PathConstructionRenderInfo.RECT:
                        System.out.printf("rectangle %s ", transform(ctm, expandRectangleCoordinates(pathConstructionRenderInfo.getSegmentData())));
                        break;
                    }
                }
                System.out.println();

                pathInfos.clear();
                return null;
            }

            @Override
            public void clipPath(int rule)
            {
            }

            List<Float> transform(Matrix ctm, List<Float> coordinates)
            {
                List<Float> result = new ArrayList<>();
                for (int i = 0; i + 1 < coordinates.size(); i += 2)
                {
                    Vector vector = new Vector(coordinates.get(i), coordinates.get(i + 1), 1);
                    vector = vector.cross(ctm);
                    result.add(vector.get(Vector.I1));
                    result.add(vector.get(Vector.I2));
                }
                return result;
            }

            List<Float> expandRectangleCoordinates(List<Float> rectangle)
            {
                if (rectangle.size() < 4)
                    return Collections.emptyList();
                return Arrays.asList(
                        rectangle.get(0), rectangle.get(1),
                        rectangle.get(0) + rectangle.get(2), rectangle.get(1),
                        rectangle.get(0) + rectangle.get(2), rectangle.get(1) + rectangle.get(3),
                        rectangle.get(0), rectangle.get(1) + rectangle.get(3)
                        );
            }

            String toString(BaseColor baseColor)
            {
                if (baseColor == null)
                    return "DEFAULT";
                return String.format("%s,%s,%s", baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue());
            }

            GraphicsState getGraphicsState(PathPaintingRenderInfo renderInfo) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
            {
                Field gsField = PathPaintingRenderInfo.class.getDeclaredField("gs");
                gsField.setAccessible(true);
                return (GraphicsState) gsField.get(renderInfo);
            }
            
            final List<PathConstructionRenderInfo> pathInfos = new ArrayList<>();
        };

        try (   InputStream resource = getClass().getResourceAsStream("TestCreation.pdf"))
        {
            PdfReader pdfReader = new PdfReader(resource);

            for (int page = 1; page <= pdfReader.getNumberOfPages(); page++)
            {
                System.out.printf("\nPage %s\n====\n", page);

                PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
                parser.processContent(page, extRenderListener);

            }
        }
    }
}
