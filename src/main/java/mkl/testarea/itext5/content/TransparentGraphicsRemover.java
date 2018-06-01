// $Id$
package mkl.testarea.itext5.content;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfLiteral;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;

/**
 * <a href="http://stackoverflow.com/questions/35526822/removing-watermark-from-pdf-itextsharp">
 * Removing Watermark from PDF iTextSharp
 * </a>
 * <br/>
 * <a href="https://www.dropbox.com/s/qvlo1v9uzgpu4nj/test3.pdf?dl=0">
 * test3.pdf
 * </a>
 * <p>
 * This class is a {@link PdfContentStreamEditor} which changes all vector graphics drawing
 * operations so that operations which would create somewhat transparent result are dropped.
 * This suffices to not draw the watermark in the OP's sample PDF.
 * </p>
 * <p>
 * Beware: This sample editor is very simple:
 * </p>
 * <ul>
 * <li>It only considers transparency created by the ExtGState parameters ca and CA, it
 * in particular ignores masks.
 * <li>It does not look for operations saving or restoring the graphics state.
 * </ul>
 * <p>
 * These limitations can easily be lifted but require more code than appropriate for a
 * stackoverflow answer.
 * </p>
 *
 * @author mkl
 */
public class TransparentGraphicsRemover extends PdfContentStreamEditor {
    @Override
    protected void write(PdfContentStreamProcessor processor, PdfLiteral operator, List<PdfObject> operands) throws IOException {
        String operatorString = operator.toString();
        if ("gs".equals(operatorString)) {
            updateTransparencyFrom((PdfName) operands.get(0));
        }

        PdfLiteral[] mapping = operatorMapping.get(operatorString);

        if (mapping != null) {
            int index = 0;
            if (strokingAlpha < 1)
                index |= 1;
            if (nonStrokingAlpha < 1)
                index |= 2;

            operator = mapping[index];
            operands.set(operands.size() - 1, operator);
        }

        super.write(processor, operator, operands);
    }

    // The current transparency values; beware: save and restore state operations are ignored!
    float strokingAlpha = 1;
    float nonStrokingAlpha = 1;

    void updateTransparencyFrom(PdfName gsName) {
        PdfDictionary extGState = getGraphicsStateDictionary(gsName);
        if (extGState != null) {
            PdfNumber number = extGState.getAsNumber(PdfName.ca);
            if (number != null)
                nonStrokingAlpha = number.floatValue();
            number = extGState.getAsNumber(PdfName.CA);
            if (number != null)
                strokingAlpha = number.floatValue();
        }
    }

    PdfDictionary getGraphicsStateDictionary(PdfName gsName) {
        PdfDictionary extGStates = resources.getAsDict(PdfName.EXTGSTATE);
        return extGStates.getAsDict(gsName);
    }

    //
    // Map from an operator name to an array of operations it becomes depending
    // on the current graphics state:
    //
    // * [0] the operation in case of no transparancy
    // * [1] the operation in case of stroking transparency
    // * [2] the operation in case of non-stroking transparency
    // * [3] the operation in case of stroking and non-stroking transparency
    //
    static Map<String, PdfLiteral[]> operatorMapping = new HashMap<String, PdfLiteral[]>();

    static {
        PdfLiteral _S = new PdfLiteral("S");
        PdfLiteral _s = new PdfLiteral("s");
        PdfLiteral _f = new PdfLiteral("f");
        PdfLiteral _fStar = new PdfLiteral("f*");
        PdfLiteral _B = new PdfLiteral("B");
        PdfLiteral _BStar = new PdfLiteral("B*");
        PdfLiteral _b = new PdfLiteral("b");
        PdfLiteral _bStar = new PdfLiteral("b*");
        PdfLiteral _n = new PdfLiteral("n");

        operatorMapping.put("S", new PdfLiteral[]{_S, _n, _S, _n});
        operatorMapping.put("s", new PdfLiteral[]{_s, _n, _s, _n});
        operatorMapping.put("f", new PdfLiteral[]{_f, _f, _n, _n});
        operatorMapping.put("F", new PdfLiteral[]{_f, _f, _n, _n});
        operatorMapping.put("f*", new PdfLiteral[]{_fStar, _fStar, _n, _n});
        operatorMapping.put("B", new PdfLiteral[]{_B, _f, _S, _n});
        operatorMapping.put("B*", new PdfLiteral[]{_BStar, _fStar, _S, _n});
        operatorMapping.put("b", new PdfLiteral[]{_b, _f, _s, _n});
        operatorMapping.put("b*", new PdfLiteral[]{_bStar, _fStar, _s, _n});
    }
}
