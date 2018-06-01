package mkl.testarea.itext5.content;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfShading;
import com.itextpdf.text.pdf.PdfShadingPattern;

/**
 * <a href="https://stackoverflow.com/questions/46869670/itext-5-create-pdfpcell-containing-2-background-colors-with-text-overlap">
 * iText 5: create PdfPcell containing 2 background colors with text overlap
 * </a>
 * <p>
 * This table cell event listener creates a cell background
 * which represents a percentile value.
 * </p>
 * 
 * @author mkl
 */
public class PercentileCellBackground implements PdfPCellEvent {
    public PercentileCellBackground(float percent, BaseColor leftColor, BaseColor rightColor) {
        this.percent = percent;
        this.leftColor = leftColor;
        this.rightColor = rightColor;
    }

    @Override
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        PdfContentByte canvas = canvases[PdfPTable.BACKGROUNDCANVAS];

        float xTransition = position.getLeft() + (position.getRight() - position.getLeft()) * (percent/100.0f);
        float yTransition = (position.getTop() + position.getBottom()) / 2f;
        float radius = (position.getRight() - position.getLeft()) * 0.025f;
        PdfShading axial = PdfShading.simpleAxial(canvas.getPdfWriter(),
                xTransition - radius, yTransition, xTransition + radius, yTransition, leftColor, rightColor);
        PdfShadingPattern shading = new PdfShadingPattern(axial);

        canvas.saveState();
        canvas.setShadingFill(shading);
        canvas.rectangle(position.getLeft(), position.getBottom(), position.getWidth(), position.getHeight());
//        canvas.clip();
        canvas.fill();
        canvas.restoreState();
    }

    final float percent;
    final BaseColor leftColor;
    final BaseColor rightColor;
}
