// $Id$
package mkl.testarea.itext5.extract;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.PdfImageObject.ImageBytesType;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * @author mkl
 */
public class ImageExtraction
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/36936524/itextsharp-extracted-cmyk-image-is-inverted">
     * iTextSharp: Extracted CMYK Image is inverted
     * </a>
     * <br/>
     * <a href="http://docdro.id/ZoHmiAd">sampleCMYK.pdf</a>
     * <p>
     * The issue is just the same in iText 5.
     * </p>
     */
    @Test
    public void testExtractCmykImage() throws IOException
    {
        try  (InputStream resourceStream = getClass().getResourceAsStream("sampleCMYK.pdf") )
        {
            PdfReader reader = new PdfReader(resourceStream);
            for (int page = 1; page <= reader.getNumberOfPages(); page++)
            {
                PdfReaderContentParser parser = new PdfReaderContentParser(reader);
                parser.processContent(page, new RenderListener()
                {
                    @Override
                    public void beginTextBlock() { }

                    @Override
                    public void renderText(TextRenderInfo renderInfo) { }

                    @Override
                    public void endTextBlock() { }

                    @Override
                    public void renderImage(ImageRenderInfo renderInfo)
                    {
                        try
                        {
                            byte[] bytes = renderInfo.getImage().getImageAsBytes();
                            ImageBytesType type = renderInfo.getImage().getImageBytesType();
                            if (bytes != null && type != null)
                            {
                                Files.write(new File(RESULT_FOLDER, "sampleCMYK-" + index++ + "." + type.getFileExtension()).toPath(), bytes);
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    
                    int index = 0;
                });

            }
        }        
    }

    /**
     * <a href="https://stackoverflow.com/questions/47101222/how-to-decode-image-with-asciihexdecode">
     * How to decode image with /ASCIIHexDecode
     * </a>
     * <br/>
     * <a href="https://1drv.ms/b/s!AjcEvFO-aWLMkbtXNVl_rmUXv6nnBQ">
     * test.pdf
     * </a>
     * <p>
     * The issue can be reproduced. Without studying the format
     * of the integrated image in detail, though, it is hard to
     * tell whether this is a bug in iText or a bug in the PDF.
     * </p>
     */
    @Test
    public void testExtractImageLikeSteveB() throws IOException
    {
        try (InputStream resource = getClass().getResourceAsStream("testSteveB.pdf")) {
            PdfReader reader = new PdfReader(resource);
            for (int pageNumber = 1; pageNumber <= reader.getNumberOfPages(); pageNumber++)
            {
                PdfDictionary dictionary = reader.getPageN(pageNumber);
                FindImages(reader, dictionary);
            }
        }
    }

    /**
     * @see #testExtractImageLikeSteveB()
     */
    private static List<BufferedImage> FindImages(PdfReader reader, PdfDictionary pdfPage) throws IOException
    {
        List<BufferedImage> result = new ArrayList<>(); 
        Iterable<PdfObject> imgPdfObject = FindImageInPDFDictionary(pdfPage);
        for (PdfObject image : imgPdfObject)
        {
            int xrefIndex = ((PRIndirectReference)image).getNumber();
            PdfObject stream = reader.getPdfObject(xrefIndex);
            // Exception occurs here :
            PdfImageObject pdfImage = new PdfImageObject((PRStream)stream);
            BufferedImage img = pdfImage.getBufferedImage();

            // Do something with the image
            result.add(img);
        }
        return result;
    }

    /**
     * @see #testExtractImageLikeSteveB()
     */
    private static Iterable<PdfObject> FindImageInPDFDictionary(PdfDictionary pg)
    {
        PdfDictionary res = (PdfDictionary)PdfReader.getPdfObject(pg.get(PdfName.RESOURCES));
        PdfDictionary xobj = (PdfDictionary)PdfReader.getPdfObject(res.get(PdfName.XOBJECT));

        List<PdfObject> result = new ArrayList<>();
        if (xobj != null)
        {
            for (PdfName name : xobj.getKeys())
            {
                PdfObject obj = xobj.get(name);
                if (obj.isIndirect())
                {
                    PdfDictionary tg = (PdfDictionary)PdfReader.getPdfObject(obj);

                    PdfName type = (PdfName)PdfReader.getPdfObject(tg.get(PdfName.SUBTYPE));

                    //image at the root of the pdf
                    if (PdfName.IMAGE.equals(type))
                    {
                        result.add(obj);
                    }// image inside a form
                    else if (PdfName.FORM.equals(type))
                    {
                        for (PdfObject nestedObj : FindImageInPDFDictionary(tg))
                        {
                            result.add(nestedObj);
                        }
                    } //image inside a group
                    else if (PdfName.GROUP.equals(type))
                    {
                        for (PdfObject nestedObj : FindImageInPDFDictionary(tg))
                        {
                            result.add(nestedObj);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * <a href="https://stackoverflow.com/questions/48355703/how-to-decode-a-pdfimageobject-with-filter-flatedecode-runlengthdecode">
     * How to decode a PdfImageObject with filter “[/FlateDecode, /RunLengthDecode]”
     * </a>
     * <br/>
     * <a href="http://www.jpproducties.nl/content/Example.pdf">
     * Example.pdf
     * </a>
     * <p>
     * Indeed, the issue can be reproduced, it is due to an off-by-one
     * error in the <b>RunLengthDecode</b> filter implementation.
     * </p>
     */
    @Test
    public void testExtractImageFromJohnVanDePolsExample() throws IOException
    {
        RenderListener listener = new RenderListener()
        {
            public void beginTextBlock() { }
            public void endTextBlock() { }
            public void renderText(TextRenderInfo renderInfo) { }

            public void renderImage(ImageRenderInfo renderInfo) {
                try {
                    PdfImageObject imageObject = renderInfo.getImage();
                    if (imageObject == null)
                        System.out.printf("Image %s could not be read.", renderInfo.getRef().getNumber());
                    else
                        Files.write(new File(RESULT_FOLDER, String.format("Example-%s.%s", renderInfo.getRef().getNumber(), imageObject.getFileType())).toPath(), imageObject.getImageAsBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        try (InputStream resource = getClass().getResourceAsStream("Example.pdf")) {
            PdfReader reader = new PdfReader(resource);
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            for (int pageNumber = 1; pageNumber <= reader.getNumberOfPages(); pageNumber++)
            {
                parser.processContent(pageNumber, listener);
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49726774/itext-pdf-outputs-devicecmyk-image-very-dark">
     * iText PDF outputs DeviceCMYK image very dark
     * </a>
     * <br/>
     * <a href="http://www.visibility911.org/downloads/media/thermite-fingerprint.pdf">
     * thermite-fingerprint.pdf
     * </a>
     * <p>
     * Indeed, the issue can be reproduced.
     * </p>
     */
    @Test
    public void testExtractImageFromThermiteFingerprint() throws IOException
    {
        RenderListener listener = new RenderListener()
        {
            public void beginTextBlock() { }
            public void endTextBlock() { }
            public void renderText(TextRenderInfo renderInfo) { }

            public void renderImage(ImageRenderInfo renderInfo) {
                try {
                    PdfImageObject imageObject = renderInfo.getImage();
                    if (imageObject == null)
                        System.out.printf("Image %s could not be read.", renderInfo.getRef().getNumber());
                    else
                        Files.write(new File(RESULT_FOLDER, String.format("thermite-fingerprint-%s.%s", renderInfo.getRef().getNumber(), imageObject.getFileType())).toPath(), imageObject.getImageAsBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        try (InputStream resource = getClass().getResourceAsStream("thermite-fingerprint.pdf")) {
            PdfReader reader = new PdfReader(resource);
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            for (int pageNumber = 1; pageNumber <= reader.getNumberOfPages(); pageNumber++)
            {
                parser.processContent(pageNumber, listener);
            }
        }
    }
}
