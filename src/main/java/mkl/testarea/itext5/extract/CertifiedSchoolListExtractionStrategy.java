package mkl.testarea.itext5.extract;

import static com.itextpdf.text.pdf.parser.Vector.I1;
import static com.itextpdf.text.pdf.parser.Vector.I2;

import java.io.IOException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * <a href="http://stackoverflow.com/questions/32688572/how-do-i-extract-rows-from-a-pdf-file-into-a-csv-file">
 * How do I extract rows from a PDF file into a csv file?
 * </a>
 * <br/>
 * <a href="https://studyinthestates.dhs.gov/assets/certified-school-list-9-16-2015.pdf">
 * certified-school-list-9-16-2015.pdf
 * </a>
 * <p>
 * This render listener extracts the "certified-school-list-9-16-2015.pdf" data as tab-separated text lines.
 * </p>
 * 
 * @author mkl
 */
public class CertifiedSchoolListExtractionStrategy implements RenderListener
{
    public CertifiedSchoolListExtractionStrategy(Appendable data, Appendable nonData)
    {
        this.data = data;
        this.nonData = nonData;
    }

    //
    // RenderListener implementation
    //
    @Override
    public void beginTextBlock() { }

    @Override
    public void endTextBlock() { }

    @Override
    public void renderImage(ImageRenderInfo renderInfo) { }

    @Override
    public void renderText(TextRenderInfo renderInfo)
    {
        try
        {
            Vector startPoint = renderInfo.getBaseline().getStartPoint();
            BaseColor fillColor = renderInfo.getFillColor();
            if (fillColor instanceof GrayColor && ((GrayColor)fillColor).getGray() == 0)
            {
                if (debug)
                    data.append(String.format("%4d\t%3.3f %3.3f\t%s\n", chunk, startPoint.get(I1), startPoint.get(I2), renderInfo.getText()));
                for (TextRenderInfo info : renderInfo.getCharacterRenderInfos())
                {
                    renderCharacter(info);
                }
            }
            else
            {
                if (debug)
                    nonData.append(String.format("%4d\t%3.3f %3.3f\t%s\n", chunk, startPoint.get(I1), startPoint.get(I2), renderInfo.getText()));
                if (currentField > -1)
                    finishEntry();
                entryBuilder.append(renderInfo.getText());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            chunk++;
        }
    }

    public void renderCharacter(TextRenderInfo renderInfo) throws IOException
    {
        Vector startPoint = renderInfo.getBaseline().getStartPoint();

        float x = startPoint.get(I1);

        if (currentField > -1)
        {
            if (isInCurrentField(x))
            {
                entryBuilder.append(renderInfo.getText());
                return;
            }
            if (isInNextField(x))
            {
                currentField++;
                entryBuilder.append('\t').append(renderInfo.getText());
                return;
            }
            finishEntry();
//            nonData.append(String.format("%4d\t%3.3f %3.3f\t%s\n", chunk, startPoint.get(I1), startPoint.get(I2), renderInfo.getText()));
        }
        if (isInNextField(x))
        {
            finishEntry();
            currentField = 0;
        }
        entryBuilder.append(renderInfo.getText());
    }

    public void close() throws IOException
    {
        finishEntry();
    }

    boolean isInCurrentField(float x)
    {
        if (currentField == -1)
            return false;

        if (x < fieldstarts[currentField])
            return false;

        if (currentField == fieldstarts.length - 1)
            return true;

        return x <= fieldstarts[currentField + 1];
    }

    boolean isInNextField(float x)
    {
        if (currentField == fieldstarts.length - 1)
            return false;

        if (x < fieldstarts[currentField + 1])
            return false;

        if (currentField == fieldstarts.length - 2)
            return true;

        return x <= fieldstarts[currentField + 2];
    }

    void finishEntry() throws IOException
    {
        if (entryBuilder.length() > 0)
        {
            if (currentField == fieldstarts.length - 1)
            {
                data.append(entryBuilder).append('\n');
            }
            else
            {
                nonData.append(entryBuilder).append('\n');
            }

            entryBuilder.setLength(0);
        }
        currentField = -1;
    }

    //
    // hidden members
    //
    final Appendable data, nonData;
    boolean debug = false;

    int chunk = 0;
    int currentField = -1;
    StringBuilder entryBuilder = new StringBuilder();

    final int[] fieldstarts = {20, 254, 404, 415, 431, 508, 534};
}
