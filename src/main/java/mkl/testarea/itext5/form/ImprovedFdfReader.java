// $Id$
package mkl.testarea.itext5.form;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.pdf.FdfReader;
import com.itextpdf.text.pdf.PdfNull;
import com.itextpdf.text.pdf.PdfObject;

/**
 * @author mkl
 */
public class ImprovedFdfReader extends FdfReader
{
    //
    // constructors
    //
    public ImprovedFdfReader(String filename) throws IOException
    {
        super(filename);
    }

    public ImprovedFdfReader(byte[] pdfIn) throws IOException
    {
        super(pdfIn);
    }

    public ImprovedFdfReader(URL url) throws IOException
    {
        super(url);
    }

    public ImprovedFdfReader(InputStream is) throws IOException
    {
        super(is);
    }

    //
    // Improved object reading
    //
    @Override
    protected PdfObject readPRObject() throws IOException
    {
        try
        {
            return super.readPRObject();
        }
        catch (InvalidPdfException e)
        {
            LOGGER.error(String.format("While reading a PdfObject ignored an InvalidPdfException (%s); returning PdfNull.", e.getMessage()), e);
            return PdfNull.PDFNULL;
        }
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ImprovedFdfReader.class);
}
