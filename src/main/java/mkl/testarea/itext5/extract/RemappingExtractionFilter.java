package mkl.testarea.itext5.extract;

import java.lang.reflect.Field;

import com.itextpdf.text.pdf.DocumentFont;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * <a href="http://stackoverflow.com/questions/33500819/itextsharp-pdfbox-text-extract-fails-for-certain-pdfs">
 * ITextSharp / PDFBox text extract fails for certain pdfs
 * </a>
 * <br/>
 * <a href="https://www.dropbox.com/s/8x5lnvmw6mv8ko8/Vol16_2.pdf?dl=0">
 * Vol16_2.pdf
 * </a>
 * <p>
 * This {@link TextExtractionStrategy} wrapper is evil. It replaces the text
 * in a {@link TextRenderInfo} instance by mapping it using the Differences
 * of the Encoding of the font assuming the differences to contain a starting
 * 1 only followed by names all of which are built as /Gxx, xx being the
 * hexadecimal representation of the ASCII code (as Unicode subset) of the
 * glyph rendered.
 * </p>
 * <p>
 * It is only useful for documents like the one presented by the OP.
 * </p>
 * 
 * @author mkl
 */
public class RemappingExtractionFilter implements TextExtractionStrategy
{
    public RemappingExtractionFilter(TextExtractionStrategy strategy) throws NoSuchFieldException, SecurityException
    {
        this.strategy = strategy;
        this.stringField = TextRenderInfo.class.getDeclaredField("text");
        this.stringField.setAccessible(true);
    }

    @Override
    public void renderText(TextRenderInfo renderInfo)
    {
        DocumentFont font =renderInfo.getFont();
        PdfDictionary dict = font.getFontDictionary();
        PdfDictionary encoding = dict.getAsDict(PdfName.ENCODING);
        PdfArray diffs = encoding.getAsArray(PdfName.DIFFERENCES);

        ;
        StringBuilder builder = new StringBuilder();
        for (byte b : renderInfo.getPdfString().getBytes())
        {
            PdfName name = diffs.getAsName((char)b);
            String s = name.toString().substring(2);
            int i = Integer.parseUnsignedInt(s, 16);
            builder.append((char)i);
        }

        try
        {
            stringField.set(renderInfo, builder.toString());
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        strategy.renderText(renderInfo);
    }

    @Override
    public void beginTextBlock()
    {
        strategy.beginTextBlock();
    }

    @Override
    public void endTextBlock()
    {
        strategy.endTextBlock();
    }

    @Override
    public void renderImage(ImageRenderInfo renderInfo)
    {
        strategy.renderImage(renderInfo);
    }

    @Override
    public String getResultantText()
    {
        return strategy.getResultantText();
    }

    final TextExtractionStrategy strategy;
    final Field stringField;
}
