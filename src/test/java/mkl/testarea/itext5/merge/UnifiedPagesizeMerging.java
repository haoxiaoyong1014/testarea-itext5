package mkl.testarea.itext5.merge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfRectangle;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * This test focuses on PDF merging to a single page size.
 * 
 * @author mkl
 */
public class UnifiedPagesizeMerging
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/28945802/get-pdf-be-written-in-the-center-of-the-page-itextsharp">
     * Get PDF be written in the center of the page itextsharp
     * </a>
     * <p>
     * A Java solution...
     * </p>
     */
    @Test
    public void testMerge() throws IOException, DocumentException
    {
        try (
                InputStream testA4Stream = getClass().getResourceAsStream("testA4.pdf");
                InputStream fromStream = getClass().getResourceAsStream("from.pdf");
                InputStream prefaceStream = getClass().getResourceAsStream("preface.pdf");
                InputStream type3Stream = getClass().getResourceAsStream("Test_Type3_Problem.pdf");
            )
        {
            final List<PdfReader> readers = new ArrayList<PdfReader>(
                    Arrays.asList(new PdfReader(testA4Stream), new PdfReader(fromStream),
                            new PdfReader(prefaceStream), new PdfReader(type3Stream)));

            for (int run = 0; run < readers.size(); run++)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                Document document = new Document();
                PdfCopy copy = new PdfCopy(document, baos);
                document.open();
                for (PdfReader reader: readers)
                {
                    copy.addDocument(reader);
                }
                document.close();

                File target = new File(RESULT_FOLDER, String.format("unified-pagesize-%s.pdf", run));
                FileOutputStream output = new FileOutputStream(target);

                PdfReader reader = new PdfReader(baos.toByteArray());
                Rectangle unifiedSize = reader.getCropBox(1);
                unifiedSize.setRotation(reader.getPageRotation(1));
                float width, height;
                if (unifiedSize.getRotation() % 180 == 0)
                {
                    width = unifiedSize.getWidth();
                    height = unifiedSize.getHeight();
                }
                else
                {
                    height = unifiedSize.getWidth();
                    width = unifiedSize.getHeight();
                }

                for (int pageNumber = 1; pageNumber <= reader.getNumberOfPages(); pageNumber++)
                {
                    Rectangle centeredCrop = centerIn(reader.getCropBox(pageNumber), reader.getPageRotation(pageNumber), width, height);
                    Rectangle enhancedMedia = enhanceBy(reader.getPageSize(pageNumber), centeredCrop);
                    reader.getPageN(pageNumber).put(PdfName.CROPBOX, new PdfRectangle(centeredCrop));
                    reader.getPageN(pageNumber).put(PdfName.MEDIABOX, new PdfRectangle(enhancedMedia));
                }

                PdfStamper stamper = new PdfStamper(reader, output);
                stamper.close();

                readers.add(readers.remove(0));
            }
        }
    }

    Rectangle centerIn(Rectangle source, int rotation, float width, float height)
    {
        if (rotation % 180 != 0)
        {
            float temp = height;
            height = width;
            width = temp;
        }

        float halfWidthToRemove = (source.getWidth() - width) / 2.0f;
        float halfHeightToRemove = (source.getHeight() - height) / 2.0f;
        return new Rectangle(source.getLeft(halfWidthToRemove), source.getBottom(halfHeightToRemove),
                source.getRight(halfWidthToRemove), source.getTop(halfHeightToRemove));
    }

    Rectangle enhanceBy(Rectangle source, Rectangle addition)
    {
        Rectangle result = new Rectangle(source);
        if (addition.getLeft() < result.getLeft())
            result.setLeft(addition.getLeft());
        if (addition.getRight() > result.getRight())
            result.setRight(addition.getRight());
        if (addition.getBottom() < result.getBottom())
            result.setBottom(addition.getBottom());
        if (addition.getTop() > result.getTop())
            result.setTop(addition.getTop());
        return result;
    }
}
