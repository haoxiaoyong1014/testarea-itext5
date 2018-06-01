package mkl.testarea.itext5.merge;

import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

/**
 * <a href="http://stackoverflow.com/questions/28991291/how-to-remove-whitespace-on-merge">
 * How To Remove Whitespace on Merge
 * </a>
 * <p>
 * A sample tool implementing this.
 * </p>
 *
 * @author mkl
 */
public class PdfVeryDenseMergeTool
{
    public PdfVeryDenseMergeTool(Rectangle size, float top, float bottom, float gap)
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
        PdfImportedPage importedPage = writer.getImportedPage(reader, page);
        PdfContentByte directContent = writer.getDirectContent();
        
        PageVerticalAnalyzer finder = parser.processContent(page, new PageVerticalAnalyzer());
        if (finder.verticalFlips.size() < 2)
            return;
        Rectangle pageSizeToImport = reader.getPageSize(page);

        int startFlip = finder.verticalFlips.size() - 1;
        boolean first = true;
        while (startFlip > 0)
        {
            if (!first)
                newPage();

            float freeSpace = yPosition - pageSize.getBottom(bottomMargin);
            int endFlip = startFlip + 1;
            while ((endFlip > 1) && (finder.verticalFlips.get(startFlip) - finder.verticalFlips.get(endFlip - 2) < freeSpace))
                endFlip -=2;
            if (endFlip < startFlip)
            {
                float height = finder.verticalFlips.get(startFlip) - finder.verticalFlips.get(endFlip);

                directContent.saveState();
                directContent.rectangle(0, yPosition - height, pageSizeToImport.getWidth(), height);
                directContent.clip();
                directContent.newPath();

                writer.getDirectContent().addTemplate(importedPage, 0, yPosition - (finder.verticalFlips.get(startFlip) - pageSizeToImport.getBottom()));

                directContent.restoreState();
                yPosition -= height + gap;
                startFlip = endFlip - 1;
            }
            else if (!first) 
                throw new IllegalArgumentException(String.format("Page %s content sections too large.", page));
            first = false;
        }
    }

    Document document = null;
    PdfWriter writer = null;
    float yPosition = 0; 

    final Rectangle pageSize;
    final float topMargin;
    final float bottomMargin;
    final float gap;
}
