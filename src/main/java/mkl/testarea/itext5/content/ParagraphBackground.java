package mkl.testarea.itext5.content;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <a href="http://stackoverflow.com/questions/34522776/java-pdf-creation-using-itext">
 * Java PDF creation using iTEXT
 * </a>
 *
 * <p>
 * This page event listener draws uninterrupted paragraph backgrounds. It is inspired by
 * Bruno Lowagie's <code>ParagraphBorder</code> from his answer to
 * <a href="http://stackoverflow.com/a/30055977/1729265">
 * How to add border to paragraph in itext pdf library in java?
 * </a>
 * </p>
 *
 * @author mkl
 */
public class ParagraphBackground extends PdfPageEventHelper {
    public BaseColor color = BaseColor.YELLOW;

    public void setColor(BaseColor color) {
        this.color = color;
    }

    public boolean active = false;

    public void setActive(boolean active) {
        this.active = active;
    }

    public float offset = 5;
    public float startPosition;

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        startPosition = document.top();
    }

    @Override
    public void onParagraph(PdfWriter writer, Document document, float paragraphPosition) {
        this.startPosition = paragraphPosition;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        if (active) {
            PdfContentByte cb = writer.getDirectContentUnder();
            cb.saveState();
            cb.setColorFill(color);
            cb.rectangle(document.left(), document.bottom() - offset,
                    document.right() - document.left(), startPosition - document.bottom());
            cb.fill();
            cb.restoreState();
        }
    }

    @Override
    public void onParagraphEnd(PdfWriter writer, Document document, float paragraphPosition) {
        if (active) {
            PdfContentByte cb = writer.getDirectContentUnder();
            cb.saveState();
            cb.setColorFill(color);
            cb.rectangle(document.left(), paragraphPosition - offset,
                    document.right() - document.left(), startPosition - paragraphPosition);
            cb.fill();
            cb.restoreState();
        }
    }
}