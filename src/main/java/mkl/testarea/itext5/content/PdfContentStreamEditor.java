// $Id$
package mkl.testarea.itext5.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfLiteral;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ContentByteUtils;
import com.itextpdf.text.pdf.parser.ContentOperator;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * <a href="http://stackoverflow.com/questions/35526822/removing-watermark-from-pdf-itextsharp">
 * Removing Watermark from PDF iTextSharp
 * </a>
 * <p>
 * This class presents a simple content stream editing framework. As is it creates an equivalent
 * copy of the original page content stream. To actually edit, simply overwrite the method
 * {@link #write(PdfContentStreamProcessor, PdfLiteral, List)} to not (as in this class) write
 * the given operations as they are but change them in some fancy way.
 * </p>
 *
 * @author mkl
 */
public class PdfContentStreamEditor extends PdfContentStreamProcessor {
    /**
     * This method edits the immediate contents of a page, i.e. its content stream.
     * It explicitly does not descent into form xobjects, patterns, or annotations.
     */
    public void editPage(PdfStamper pdfStamper, int pageNum) throws IOException {
        PdfReader pdfReader = pdfStamper.getReader();
        PdfDictionary page = pdfReader.getPageN(pageNum);
        byte[] pageContentInput = ContentByteUtils.getContentBytesForPage(pdfReader, pageNum);
        page.remove(PdfName.CONTENTS);
        editContent(pageContentInput, page.getAsDict(PdfName.RESOURCES), pdfStamper.getUnderContent(pageNum));
    }

    /**
     * This method processes the content bytes and outputs to the given canvas.
     * It explicitly does not descent into form xobjects, patterns, or annotations.
     */
    public void editContent(byte[] contentBytes, PdfDictionary resources, PdfContentByte canvas) {
        this.canvas = canvas;
        processContent(contentBytes, resources);
        this.canvas = null;
    }

    /**
     * <p>
     * This method writes content stream operations to the target canvas. The default
     * implementation writes them as they come, so it essentially generates identical
     * copies of the original instructions the {@link ContentOperatorWrapper} instances
     * forward to it.
     * </p>
     * <p>
     * Override this method to achieve some fancy editing effect.
     * </p>
     */
    protected void write(PdfContentStreamProcessor processor, PdfLiteral operator, List<PdfObject> operands) throws IOException {
        int index = 0;

        for (PdfObject object : operands) {
            object.toPdf(canvas.getPdfWriter(), canvas.getInternalBuffer());
            canvas.getInternalBuffer().append(operands.size() > ++index ? (byte) ' ' : (byte) '\n');
        }
    }

    //
    // constructor giving the parent a dummy listener to talk to 
    //
    public PdfContentStreamEditor() {
        super(new DummyRenderListener());
    }

    //
    // Overrides of PdfContentStreamProcessor methods
    //
    @Override
    public ContentOperator registerContentOperator(String operatorString, ContentOperator operator) {
        ContentOperatorWrapper wrapper = new ContentOperatorWrapper();
        wrapper.setOriginalOperator(operator);
        ContentOperator formerOperator = super.registerContentOperator(operatorString, wrapper);
        return formerOperator instanceof ContentOperatorWrapper ? ((ContentOperatorWrapper) formerOperator).getOriginalOperator() : formerOperator;
    }

    @Override
    public void processContent(byte[] contentBytes, PdfDictionary resources) {
        this.resources = resources;
        super.processContent(contentBytes, resources);
        this.resources = null;
    }

    //
    // members holding the output canvas and the resources
    //
    protected PdfContentByte canvas = null;
    protected PdfDictionary resources = null;

    //
    // A content operator class to wrap all content operators to forward the invocation to the editor
    //
    class ContentOperatorWrapper implements ContentOperator {
        public ContentOperator getOriginalOperator() {
            return originalOperator;
        }

        public void setOriginalOperator(ContentOperator originalOperator) {
            this.originalOperator = originalOperator;
        }

        @Override
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList<PdfObject> operands) throws Exception {
            if (originalOperator != null && !"Do".equals(operator.toString())) {
                originalOperator.invoke(processor, operator, operands);
            }
            write(processor, operator, operands);
        }

        private ContentOperator originalOperator = null;
    }

    //
    // A dummy render listener to give to the underlying content stream processor to feed events to
    //
    static class DummyRenderListener implements RenderListener {
        @Override
        public void beginTextBlock() {
        }

        @Override
        public void renderText(TextRenderInfo renderInfo) {
        }

        @Override
        public void endTextBlock() {
        }

        @Override
        public void renderImage(ImageRenderInfo renderInfo) {
        }
    }
}
