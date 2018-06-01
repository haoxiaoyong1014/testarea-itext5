package mkl.testarea.itext5.copy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfCopy.PageStamp;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

/**
 * @author mkl
 */
public class TableOfContents
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "copy");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/45539604/toc-content-over-write-issue">
     * TOC content over write issue
     * </a>
     * <p>
     * This is the original code by arj with some additions to make it work
     * </p>
     */
    @Test
    public void testAddTocLikeArj() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream(/*"cjk-with-fonts.pdf"*/"/mkl/testarea/itext5/content/test3.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "toc-like-arj.pdf")) )
        {
            Document document = new Document();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PdfCopy copy = new PdfCopy(document, bos);
            document.open();

            PdfImportedPage page;
            PageStamp stamp;
            int noOfPages;

            TocModel[] toc = new TocModel[]
                    {
                            new TocModel("1.   Introduction", 1),
                            new TocModel("2.   Abstract", 3),
                            new TocModel("3.   Implementation", 4),
                            new TocModel("3.01 Implementation Part 01", 5),
                            new TocModel("3.02 Implementation Part 02", 6),
                            new TocModel("3.03 Implementation Part 03", 7),
                            new TocModel("3.04 Implementation Part 04", 7),
                            new TocModel("3.05 Implementation Part 05", 9),
                            new TocModel("3.06 Implementation Part 06", 11),
                            new TocModel("3.07 Implementation Part 07", 11),
                            new TocModel("3.08 Implementation Part 08", 11),
                            new TocModel("3.09 Implementation Part 09", 12),
                            new TocModel("3.10 Implementation Part 10", 13),
                            new TocModel("3.11 Implementation Part 11", 15),
                            new TocModel("3.12 Implementation Part 12", 16),
                            new TocModel("3.13 Implementation Part 13", 17),
                            new TocModel("3.14 Implementation Part 14", 17),
                            new TocModel("3.15 Implementation Part 15", 19),
                            new TocModel("3.16 Implementation Part 16", 21),
                            new TocModel("3.17 Implementation Part 17", 21),
                            new TocModel("3.18 Implementation Part 18", 21),
                            new TocModel("3.19 Implementation Part 19", 22),
                            new TocModel("3.20 Implementation Part 20", 23),
                            new TocModel("3.21 Implementation Part 21", 25),
                            new TocModel("3.22 Implementation Part 22", 26),
                            new TocModel("3.23 Implementation Part 23", 27),
                            new TocModel("3.24 Implementation Part 24", 27),
                            new TocModel("3.25 Implementation Part 25", 29),
                            new TocModel("3.26 Implementation Part 26", 31),
                            new TocModel("3.27 Implementation Part 27", 31),
                            new TocModel("3.28 Implementation Part 28", 31),
                            new TocModel("3.29 Implementation Part 29", 32),
                            new TocModel("3.30 Implementation Part 30", 33),
                            new TocModel("3.31 Implementation Part 31", 25),
                            new TocModel("3.32 Implementation Part 32", 26),
                            new TocModel("3.33 Implementation Part 33", 27),
                            new TocModel("3.34 Implementation Part 34", 27),
                            new TocModel("3.35 Implementation Part 35", 29),
                            new TocModel("3.36 Implementation Part 36", 31),
                            new TocModel("3.37 Implementation Part 37", 31),
                            new TocModel("3.38 Implementation Part 38", 31),
                            new TocModel("3.39 Implementation Part 39", 32),
                            new TocModel("3.40 Implementation Part 40", 33),
                    };

            PdfReader tocReader = new PdfReader(/*"toc.pdf"*/resource);
            page = copy.getImportedPage(tocReader, 1);
            stamp = copy.createPageStamp(page);
            int tocPageCount = 1;
            Paragraph paragraph;
            PdfAction action;
            PdfAnnotation link;
            float y = 770;
            PdfImportedPage newPage = null;
            Rectangle pagesize = tocReader.getPageSize(1);
            ColumnText colTxt = new ColumnText(stamp.getOverContent());
            colTxt.setSimpleColumn(36, 36, 559, y);
            for (TocModel tocModel : toc) {
                paragraph = new Paragraph(tocModel.getTitle());
                paragraph.add(new Chunk(new DottedLineSeparator()));
                paragraph.add(String.valueOf(tocModel.getPageNo()));
                colTxt.addElement(paragraph);
                colTxt.go();
                // seting toc action
                action = PdfAction.gotoLocalPage("p" + tocModel.getPageNo(), false);
                link = new PdfAnnotation(copy, 36, colTxt.getYLine(), 559, y,action);
                stamp.addAnnotation(link);
                y = colTxt.getYLine();
            }

            int status = colTxt.go();
            status = colTxt.go();
            if (ColumnText.hasMoreText(status)) {
                PdfContentByte canvas = stamp.getOverContent();
                canvas.addTemplate(page, 0, 0);
                colTxt.setCanvas(canvas);
                colTxt.setSimpleColumn(new Rectangle(36, 36, 559, 806));
                colTxt.go();
            }

            stamp.alterContents();
            copy.addPage(page);
            document.close();
            logger.info("Finished TOC !!!");

            tocReader = new PdfReader(bos.toByteArray());
            noOfPages =tocReader.getNumberOfPages();
            tocReader.selectPages(String.format("%d, 1-%d", noOfPages, noOfPages - 1));
            PdfStamper stamper = new PdfStamper(tocReader, /*new FileOutputStream(outPutDirectory + "merge.pdf")*/result);

            stamper.close();
            logger.info("merging completed!!!");
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/45539604/toc-content-over-write-issue">
     * TOC content over write issue
     * </a>
     * <p>
     * This is the original code by arj improved for multiple toc pages.
     * </p>
     */
    @Test
    public void testAddTocLikeArjImproved() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream(/*"cjk-with-fonts.pdf"*/"/mkl/testarea/itext5/content/test3.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "toc-like-arj-improved.pdf")) )
        {
            Document document = new Document();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PdfCopy copy = new PdfCopy(document, bos);
            document.open();
            byte[] resourceBytes = IOUtils.toByteArray(resource);

            PdfImportedPage page;
            PageStamp stamp;
            int noOfPages;

            TocModel[] toc = new TocModel[]
                    {
                            new TocModel("1.   Introduction", 1),
                            new TocModel("2.   Abstract", 3),
                            new TocModel("3.   Implementation", 4),
                            new TocModel("3.01 Implementation Part 01", 5),
                            new TocModel("3.02 Implementation Part 02", 6),
                            new TocModel("3.03 Implementation Part 03", 7),
                            new TocModel("3.04 Implementation Part 04", 7),
                            new TocModel("3.05 Implementation Part 05", 9),
                            new TocModel("3.06 Implementation Part 06", 11),
                            new TocModel("3.07 Implementation Part 07", 11),
                            new TocModel("3.08 Implementation Part 08", 11),
                            new TocModel("3.09 Implementation Part 09", 12),
                            new TocModel("3.10 Implementation Part 10", 13),
                            new TocModel("3.11 Implementation Part 11", 15),
                            new TocModel("3.12 Implementation Part 12", 16),
                            new TocModel("3.13 Implementation Part 13", 17),
                            new TocModel("3.14 Implementation Part 14", 17),
                            new TocModel("3.15 Implementation Part 15", 19),
                            new TocModel("3.16 Implementation Part 16", 21),
                            new TocModel("3.17 Implementation Part 17", 21),
                            new TocModel("3.18 Implementation Part 18", 21),
                            new TocModel("3.19 Implementation Part 19", 22),
                            new TocModel("3.20 Implementation Part 20", 23),
                            new TocModel("3.21 Implementation Part 21", 25),
                            new TocModel("3.22 Implementation Part 22", 26),
                            new TocModel("3.23 Implementation Part 23", 27),
                            new TocModel("3.24 Implementation Part 24", 27),
                            new TocModel("3.25 Implementation Part 25", 29),
                            new TocModel("3.26 Implementation Part 26", 31),
                            new TocModel("3.27 Implementation Part 27", 31),
                            new TocModel("3.28 Implementation Part 28", 31),
                            new TocModel("3.29 Implementation Part 29", 32),
                            new TocModel("3.30 Implementation Part 30", 33),
                            new TocModel("3.31 Implementation Part 31", 25),
                            new TocModel("3.32 Implementation Part 32", 26),
                            new TocModel("3.33 Implementation Part 33", 27),
                            new TocModel("3.34 Implementation Part 34", 27),
                            new TocModel("3.35 Implementation Part 35", 29),
                            new TocModel("3.36 Implementation Part 36", 31),
                            new TocModel("3.37 Implementation Part 37", 31),
                            new TocModel("3.38 Implementation Part 38", 31),
                            new TocModel("3.39 Implementation Part 39", 32),
                            new TocModel("3.40 Implementation Part 40", 33),
                    };

            PdfReader tocReader = new PdfReader(/*"toc.pdf"*/resourceBytes);
            page = copy.getImportedPage(tocReader, 1);
            stamp = copy.createPageStamp(page);
            int tocPageCount = 1;
            Paragraph paragraph;
            PdfAction action;
            PdfAnnotation link;
            float y = 770;
            ColumnText colTxt = new ColumnText(stamp.getOverContent());
            colTxt.setSimpleColumn(36, 36, 559, y);
            for (TocModel tocModel : toc) {
                paragraph = new Paragraph(tocModel.getTitle());
                paragraph.add(new Chunk(new DottedLineSeparator()));
                paragraph.add(String.valueOf(tocModel.getPageNo()));
                colTxt.addElement(paragraph);
                if (ColumnText.hasMoreText(colTxt.go()))
                {
                    stamp.alterContents();
                    copy.addPage(page);
                    tocReader = new PdfReader(/*"toc.pdf"*/resourceBytes);
                    page = copy.getImportedPage(tocReader, 1);
                    tocPageCount++;
                    stamp = copy.createPageStamp(page);
                    y = 770;
                    colTxt = new ColumnText(stamp.getOverContent());
                    colTxt.setSimpleColumn(36, 36, 559, y);
                    colTxt.go();
                }
                // seting toc action
                action = PdfAction.gotoLocalPage("p" + tocModel.getPageNo(), false);
                link = new PdfAnnotation(copy, 36, colTxt.getYLine(), 559, y,action);
                stamp.addAnnotation(link);
                y = colTxt.getYLine();
            }

            stamp.alterContents();
            copy.addPage(page);

            document.close();
            logger.info("Finished TOC !!!");

            tocReader = new PdfReader(bos.toByteArray());
            noOfPages =tocReader.getNumberOfPages();
            tocReader.selectPages(String.format("%d-%d, 1-%d", noOfPages - tocPageCount + 1, noOfPages, noOfPages - tocPageCount));
            PdfStamper stamper = new PdfStamper(tocReader, /*new FileOutputStream(outPutDirectory + "merge.pdf")*/result);

            stamper.close();
            logger.info("merging completed!!!");
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/45539604/toc-content-over-write-issue">
     * TOC content over write issue
     * </a>
     * <p>
     * This is alternative code for multiple toc pages.
     * </p>
     */
    @Test
    public void testAddTocLikeArjAlternative() throws IOException, DocumentException
    {
        try (   InputStream resource = getClass().getResourceAsStream(/*"cjk-with-fonts.pdf"*/"/mkl/testarea/itext5/content/test3.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "toc-like-arj-alternative.pdf")) )
        {
            Document document = new Document();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PdfCopy copy = new PdfCopy(document, bos);
            document.open();
            byte[] resourceBytes = IOUtils.toByteArray(resource);

            PdfImportedPage page;
            PageStamp stamp;
            int noOfPages;

            TocModel[] toc = new TocModel[]
                    {
                            new TocModel("1.   Introduction", 1),
                            new TocModel("2.   Abstract", 3),
                            new TocModel("3.   Implementation", 4),
                            new TocModel("3.01 Implementation Part 01", 5),
                            new TocModel("3.02 Implementation Part 02", 6),
                            new TocModel("3.03 Implementation Part 03", 7),
                            new TocModel("3.04 Implementation Part 04", 7),
                            new TocModel("3.05 Implementation Part 05", 9),
                            new TocModel("3.06 Implementation Part 06", 11),
                            new TocModel("3.07 Implementation Part 07", 11),
                            new TocModel("3.08 Implementation Part 08", 11),
                            new TocModel("3.09 Implementation Part 09", 12),
                            new TocModel("3.10 Implementation Part 10", 13),
                            new TocModel("3.11 Implementation Part 11", 15),
                            new TocModel("3.12 Implementation Part 12", 16),
                            new TocModel("3.13 Implementation Part 13", 17),
                            new TocModel("3.14 Implementation Part 14", 17),
                            new TocModel("3.15 Implementation Part 15", 19),
                            new TocModel("3.16 Implementation Part 16", 21),
                            new TocModel("3.17 Implementation Part 17", 21),
                            new TocModel("3.18 Implementation Part 18", 21),
                            new TocModel("3.19 Implementation Part 19", 22),
                            new TocModel("3.20 Implementation Part 20", 23),
                            new TocModel("3.21 Implementation Part 21", 25),
                            new TocModel("3.22 Implementation Part 22", 26),
                            new TocModel("3.23 Implementation Part 23", 27),
                            new TocModel("3.24 Implementation Part 24", 27),
                            new TocModel("3.25 Implementation Part 25", 29),
                            new TocModel("3.26 Implementation Part 26", 31),
                            new TocModel("3.27 Implementation Part 27", 31),
                            new TocModel("3.28 Implementation Part 28", 31),
                            new TocModel("3.29 Implementation Part 29", 32),
                            new TocModel("3.30 Implementation Part 30", 33),
                            new TocModel("3.31 Implementation Part 31", 25),
                            new TocModel("3.32 Implementation Part 32", 26),
                            new TocModel("3.33 Implementation Part 33", 27),
                            new TocModel("3.34 Implementation Part 34", 27),
                            new TocModel("3.35 Implementation Part 35", 29),
                            new TocModel("3.36 Implementation Part 36", 31),
                            new TocModel("3.37 Implementation Part 37", 31),
                            new TocModel("3.38 Implementation Part 38", 31),
                            new TocModel("3.39 Implementation Part 39", 32),
                            new TocModel("3.40 Implementation Part 40", 33),
                            new TocModel("3.41 Implementation Part 41", 35),
                            new TocModel("3.42 Implementation Part 42", 36),
                            new TocModel("3.43 Implementation Part 43", 37),
                            new TocModel("3.44 Implementation Part 44", 37),
                            new TocModel("3.45 Implementation Part 45", 39),
                            new TocModel("3.46 Implementation Part 46", 41),
                            new TocModel("3.47 Implementation Part 47", 41),
                            new TocModel("3.48 Implementation Part 48", 41),
                            new TocModel("3.49 Implementation Part 49", 42),
                            new TocModel("3.50 Implementation Part 50", 43),
                    };


            final PdfReader tocBackgroundReader = new PdfReader(/*"toc.pdf"*/resourceBytes);
            Document tocDocument = new Document(tocBackgroundReader.getCropBox(1));
            ByteArrayOutputStream tocBaos = new ByteArrayOutputStream();
            PdfWriter tocWriter = PdfWriter.getInstance(tocDocument, tocBaos);
            tocWriter.setPageEvent(new PdfPageEventHelper() {
                PdfImportedPage stationary = tocWriter.getImportedPage(tocBackgroundReader, 1);
                @Override
                public void onEndPage(PdfWriter writer, Document document)
                {
                    writer.getDirectContentUnder().addTemplate(stationary, 0, 0);
                }
            });
            tocDocument.open();
            for (TocModel tocModel : toc) {
                PdfAction action = PdfAction.gotoLocalPage("p" + tocModel.getPageNo(), false);

                Paragraph paragraph = new Paragraph();
                Chunk chunk = new Chunk(tocModel.getTitle());
                chunk.setAction(action);
                paragraph.add(chunk);
                chunk = new Chunk(new DottedLineSeparator());
                chunk.setAction(action);
                paragraph.add(chunk);
                chunk = new Chunk(String.valueOf(tocModel.getPageNo()));
                chunk.setAction(action);
                paragraph.add(chunk);
                tocDocument.add(paragraph);
            }
            tocDocument.close();

            PdfReader tocReader = new PdfReader(tocBaos.toByteArray());
            int tocPageCount = tocReader.getNumberOfPages();
            copy.addDocument(tocReader);

            document.close();
            logger.info("Finished TOC !!!");

            tocReader = new PdfReader(bos.toByteArray());
            noOfPages = tocReader.getNumberOfPages();
            tocBackgroundReader.selectPages(String.format("%d-%d, 1-%d", noOfPages - tocPageCount + 1, noOfPages, noOfPages - tocPageCount));
            PdfStamper stamper = new PdfStamper(tocReader, /*new FileOutputStream(outPutDirectory + "merge.pdf")*/result);

            stamper.close();
            logger.info("merging completed!!!");
        }
    }

    static class TocModel
    {
        String title;
        int pageNo;
        public TocModel(String title, int pageNo)
        {
            this.pageNo = pageNo;
            this.title = title;
        }
        String getTitle()
        {
            return title;
        }
        int getPageNo()
        {
            return pageNo;
        }
    }

    Logger logger = Logger.getAnonymousLogger();
}
