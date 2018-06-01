package mkl.testarea.itext5.content;

import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <a href="https://stackoverflow.com/questions/46466747/how-to-split-a-pdf-page-in-java">
 * How to split a PDF page in java?
 * </a>
 * <p>
 * This abstract utility class splits each source page into multiple
 * pages each representing one of a custom set of page sub areas.
 * </p>
 *
 * @author mkl
 */
public abstract class Abstract2DPdfPageSplittingTool {
    public void split(OutputStream outputStream, PdfReader... inputs) throws DocumentException, IOException {
        try {
            initDocument(outputStream);
            for (PdfReader reader : inputs) {
                split(reader);
            }
        } finally {
            closeDocument();
        }
    }

    void initDocument(OutputStream outputStream) throws DocumentException {
        final Document document = new Document(PageSize.A4);
        final PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        this.document = document;
        this.writer = writer;
    }

    void closeDocument() {
        try {
            document.close();
        } finally {
            this.document = null;
            this.writer = null;
        }
    }

    void newPage(Rectangle pageSize) {
        document.setPageSize(pageSize);
        if (!document.isOpen())
            document.open();
        else
            document.newPage();
    }

    void split(PdfReader reader) throws IOException {
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            split(reader, page);
        }
    }

    void split(PdfReader reader, int page) throws IOException {
        PdfImportedPage importedPage = writer.getImportedPage(reader, page);

        Rectangle pageSizeToImport = reader.getPageSize(page);
        Iterable<Rectangle> rectangles = determineSplitRectangles(reader, page);

        for (Rectangle rectangle : rectangles) {
            newPage(rectangle);
            PdfContentByte directContent = writer.getDirectContent();
            directContent.saveState();
            directContent.rectangle(rectangle.getLeft(), rectangle.getBottom(), rectangle.getWidth(), rectangle.getHeight());
            directContent.clip();
            directContent.newPath();

            writer.getDirectContent().addTemplate(importedPage, -pageSizeToImport.getLeft(), -pageSizeToImport.getBottom());

            directContent.restoreState();
        }
    }

    protected abstract Iterable<Rectangle> determineSplitRectangles(PdfReader reader, int page);

    Document document = null;
    PdfWriter writer = null;
}
