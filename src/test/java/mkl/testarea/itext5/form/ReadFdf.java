// $Id$
package mkl.testarea.itext5.form;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.FdfReader;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;

/**
 * @author mkl
 */
public class ReadFdf
{
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    /**
     * <a href="http://stackoverflow.com/questions/37161133/failed-to-add-fdf-with-attachment-annotation">
     * Failed to add fdf with attachment annotation
     * </a>
     * <br/>
     * itext-SO.fdf <i>received via mail from the OP</i>
     * 
     * <p>
     * Indeed, the exception occurs, the `FdfReader` works incorrectly and fails
     * for certain FDFs.
     * </p>
     */
    @Test
    public void testReadFdfFabienLevalois() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("itext-SO.fdf")   )
        {
            FdfReader fdfReader = new FdfReader(resource);
            show(fdfReader);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/37161133/failed-to-add-fdf-with-attachment-annotation">
     * Failed to add fdf with attachment annotation
     * </a>
     * <br/>
     * itext-SO.fdf <i>received via mail from the OP</i>
     * 
     * <p>
     * The {@link ImprovedFdfReader} is a quick fix which works to a certain degree.
     * </p>
     */
    @Test
    public void testReadFdfFabienLevaloisImproved() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("itext-SO.fdf")   )
        {
            FdfReader fdfReader = new ImprovedFdfReader(resource);
            show(fdfReader);
        }
    }
    
    void show(FdfReader fdfReader)
    {
        PdfDictionary catalog = fdfReader.getCatalog();
        catalog = catalog.getAsDict(PdfName.FDF);
        Assert.assertNotNull("FDF catalogue is null", catalog);
        PdfArray annots = catalog.getAsArray(PdfName.ANNOTS);
        Assert.assertNotNull("FDF annotations are null", annots);
        System.out.println(annots);
    }
}
