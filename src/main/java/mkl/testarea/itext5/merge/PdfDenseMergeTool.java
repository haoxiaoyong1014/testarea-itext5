package mkl.testarea.itext5.merge;

import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextMarginFinder;

/**
 * <a href="http://stackoverflow.com/questions/27988503/how-can-i-combine-multiple-pdf-files-excluding-page-breaks-using-itextsharp">
 * How can I combine multiple PDF files excluding page breaks using iTextSharp?
 * </a>
 * <p>
 * A sample tool implementing this.
 * </p>
 *
 * @author mkl
 */
public class PdfDenseMergeTool
{
    public PdfDenseMergeTool(Rectangle size, float top, float bottom, float gap)
    {
        this.pageSize = size;
        this.topMargin = top;
        this.bottomMargin = bottom;
        this.gap = gap;
    }

    public void merge(OutputStream outputStream, Iterable<PdfReader> inputs) throws DocumentException, IOException
    {
        try
        {
            openDocument(outputStream);
            for (PdfReader reader: inputs)
            {
                merge(reader);
            }
        }
        finally
        {
            closeDocument();
        }
        
    }

    void openDocument(OutputStream outputStream) throws DocumentException
    {
        final Document document = new Document(pageSize, 36, 36, topMargin, bottomMargin);
        final PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();
        this.document = document;
        this.writer = writer;
        newPage();
    }

    void closeDocument()
    {
        try
        {
            document.close();
        }
        finally
        {
            this.document = null;
            this.writer = null;
            this.yPosition = 0;
        }
    }
    
    void newPage()
    {
        document.newPage();
        yPosition = pageSize.getTop(topMargin);
    }

    void merge(PdfReader reader) throws IOException
    {
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        for (int page = 1; page <= reader.getNumberOfPages(); page++)
        {
            merge(reader, parser, page);
        }
    }

    void merge(PdfReader reader, PdfReaderContentParser parser, int page) throws IOException
    {
        TextMarginFinder finder = parser.processContent(page, new TextMarginFinder());
        Rectangle pageSizeToImport = reader.getPageSize(page);
        float heightToImport = finder.getHeight();
        float maxHeight = pageSize.getHeight() - topMargin - bottomMargin;
        if (heightToImport > maxHeight)
        {
            throw new IllegalArgumentException(String.format("Page %s content too large; height: %s, limit: %s.", page, heightToImport, maxHeight));
        }

        if (heightToImport > yPosition - pageSize.getBottom(bottomMargin))
        {
            newPage();
        }
        else if (!writer.isPageEmpty())
        {
            heightToImport += gap;
        }
        yPosition -= heightToImport;

        PdfImportedPage importedPage = writer.getImportedPage(reader, page);
        writer.getDirectContent().addTemplate(importedPage, 0, yPosition - (finder.getLly() - pageSizeToImport.getBottom()));
    }

    Document document = null;
    PdfWriter writer = null;
    float yPosition = 0; 

    final Rectangle pageSize;
    final float topMargin;
    final float bottomMargin;
    final float gap;
}
