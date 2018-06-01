package mkl.testarea.itext5.content;

import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <a href="https://stackoverflow.com/questions/45823741/how-to-find-all-occurrences-of-specific-text-in-a-pdf-and-insert-a-page-break-ab">
 * How to find all occurrences of specific text in a PDF and insert a page break above?
 * </a>
 * <p>
 * This abstract utility class <i>inserts a page break above</i> a custom set
 * of vertical positions.
 * </p>
 * <p>
 * Note that you explicitly have to add the top and bottom of the page
 * to the result of {@link #determineSplitPositions(PdfReader, int)} if
 * you want the whole page area as this class does not automatically add
 * them; this allows us to e.g. drop headers and footers.
 * </p>
 *
 * @author mkl
 */
public abstract class AbstractPdfPageSplittingTool {
    public AbstractPdfPageSplittingTool(Rectangle size, float top) {
        this.pageSize = size;
        this.topMargin = top;
    }

    public void split(OutputStream outputStream, PdfReader... inputs) throws DocumentException, IOException {
        try {
            openDocument(outputStream);
            for (PdfReader reader : inputs) {
                split(reader);
            }
        } finally {
            closeDocument();
        }
    }

    void openDocument(OutputStream outputStream) throws DocumentException {
        final Document document = new Document(pageSize, 36, 36, topMargin, 36);
        final PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();
        this.document = document;
        this.writer = writer;
        newPage();
    }

    void closeDocument() {
        try {
            document.close();
        } finally {
            this.document = null;
            this.writer = null;
            this.yPosition = 0;
        }
    }

    void newPage() {
        document.newPage();
        yPosition = pageSize.getTop(topMargin);
    }

    void split(PdfReader reader) throws IOException {
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            split(reader, page);
        }
    }

    void split(PdfReader reader, int page) throws IOException {
        PdfImportedPage importedPage = writer.getImportedPage(reader, page);
        PdfContentByte directContent = writer.getDirectContent();
        yPosition = pageSize.getTop();

        Rectangle pageSizeToImport = reader.getPageSize(page);
        float[] borderPositions = determineSplitPositions(reader, page);
        if (borderPositions == null || borderPositions.length < 2)
            return;

        for (int borderIndex = 0; borderIndex + 1 < borderPositions.length; borderIndex++) {
            float height = borderPositions[borderIndex] - borderPositions[borderIndex + 1];
            if (height <= 0)
                continue;

            directContent.saveState();
            directContent.rectangle(0, yPosition - height, pageSizeToImport.getWidth(), height);
            directContent.clip();
            directContent.newPath();

            writer.getDirectContent().addTemplate(importedPage, 0, yPosition - (borderPositions[borderIndex] - pageSizeToImport.getBottom()));

            directContent.restoreState();
            newPage();
        }
    }

    protected abstract float[] determineSplitPositions(PdfReader reader, int page);

    Document document = null;
    PdfWriter writer = null;
    float yPosition = 0;

    final Rectangle pageSize;
    final float topMargin;
}
