package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

import mkl.testarea.itext5.extract.SearchTextLocationExtractionStrategy;
import mkl.testarea.itext5.extract.SearchTextLocationExtractionStrategy.TextRectangle;

/**
 * @author mkl
 */
public class SplitPages {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/45823741/how-to-find-all-occurrences-of-specific-text-in-a-pdf-and-insert-a-page-break-ab">
     * How to find all occurrences of specific text in a PDF and insert a page break above?
     * </a>
     * <p>
     * This test shows how to split the pages of a document in three equal
     * parts using the {@link AbstractPdfPageSplittingTool}.
     * </p>
     * <p>
     * Note that we explicitly have to add the top and bottom of the page
     * to <code>borders</code> as the {@link AbstractPdfPageSplittingTool}
     * does not automatically add them.
     * </p>
     */
    @Test
    public void testSplitDocumentInThirds() throws IOException, DocumentException {
        try (InputStream resource = getClass().getResourceAsStream("document.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "document-in-thirds.pdf"))) {
            AbstractPdfPageSplittingTool tool = new AbstractPdfPageSplittingTool(PageSize.A4, 36) {
                @Override
                protected float[] determineSplitPositions(PdfReader reader, int page) {
                    Rectangle pageSize = reader.getPageSize(page);
                    float thirdHeight = pageSize.getHeight() / 3.0f;
                    float[] result = new float[] { pageSize.getTop(), pageSize.getTop(thirdHeight),
                            pageSize.getBottom(thirdHeight), pageSize.getBottom() };
                    return result;
                }
            };
            tool.split(result, new PdfReader(resource));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/45823741/how-to-find-all-occurrences-of-specific-text-in-a-pdf-and-insert-a-page-break-ab">
     * How to find all occurrences of specific text in a PDF and insert a page break above?
     * </a>
     * <p>
     * This test shows how to <i>find all occurrences of specific text in a PDF</i>
     * using the {@link SearchTextLocationExtractionStrategy} and then <i>insert a
     * page break above</i> using the {@link AbstractPdfPageSplittingTool}.
     * </p>
     * <p>
     * Note (1) that we don't check for duplicate values in the <code>borders</code>
     * collection as the {@link AbstractPdfPageSplittingTool} ignores duplicate
     * entries and (2) that we explicitly have to add the top and bottom of the
     * page to <code>borders</code> as the {@link AbstractPdfPageSplittingTool}
     * does not automatically add them; this allows us to e.g. drop headers and
     * footers by adding borders excluding them.
     * </p>
     */
    @Test
    public void testSplitDocumentAboveAngestellter() throws IOException, DocumentException {
        try (InputStream resource = getClass().getResourceAsStream("document.pdf");
                OutputStream result = new FileOutputStream(
                        new File(RESULT_FOLDER, "document-above-Angestellter.pdf"))) {
            AbstractPdfPageSplittingTool tool = new AbstractPdfPageSplittingTool(PageSize.A4, 36) {
                @Override
                protected float[] determineSplitPositions(PdfReader reader, int page) {
                    Collection<TextRectangle> locations = Collections.emptyList();
                    try {
                        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
                        SearchTextLocationExtractionStrategy strategy = new SearchTextLocationExtractionStrategy(
                                Pattern.compile("Angestellter"));
                        parser.processContent(page, strategy, Collections.emptyMap()).getResultantText();
                        locations = strategy.getLocations(null);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    List<Float> borders = new ArrayList<>();
                    for (TextRectangle rectangle : locations)
                    {
                        borders.add((float)rectangle.getMaxY());
                    }

                    Rectangle pageSize = reader.getPageSize(page);
                    borders.add(pageSize.getTop());
                    borders.add(pageSize.getBottom());
                    Collections.sort(borders, Collections.reverseOrder());

                    float[] result = new float[borders.size()];
                    for (int i=0; i < result.length; i++)
                        result[i] = borders.get(i);
                    return result;
                }
            };
            tool.split(result, new PdfReader(resource));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/46466747/how-to-split-a-pdf-page-in-java">
     * How to split a PDF page in java?
     * </a>
     * <p>
     * This test shows how to split the pages of a document into tiles of A6
     * size using the {@link Abstract2DPdfPageSplittingTool}.
     * </p>
     */
    @Test
    public void testSplitDocumentA6() throws IOException, DocumentException {
        try (InputStream resource = getClass().getResourceAsStream("document.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "document-A6.pdf"))) {
            Abstract2DPdfPageSplittingTool tool = new Abstract2DPdfPageSplittingTool() {
                @Override
                protected Iterable<Rectangle> determineSplitRectangles(PdfReader reader, int page) {
                    Rectangle targetSize = PageSize.A6;
                    List<Rectangle> rectangles = new ArrayList<>();
                    Rectangle pageSize = reader.getPageSize(page);
                    for (float y = pageSize.getTop(); y > pageSize.getBottom() + 5; y-=targetSize.getHeight()) {
                        for (float x = pageSize.getLeft(); x < pageSize.getRight() - 5; x+=targetSize.getWidth()) {
                            rectangles.add(new Rectangle(x, y - targetSize.getHeight(), x + targetSize.getWidth(), y));
                        }
                    }
                    return rectangles;
                }
            };
            tool.split(result, new PdfReader(resource));
        }
    }
}
