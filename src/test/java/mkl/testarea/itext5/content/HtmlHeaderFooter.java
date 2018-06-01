package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * @author mkl
 */
public class HtmlHeaderFooter
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/41912927/itextpdf-adding-headers-and-footers-complex-format">
     * ItextPDF Adding Headers and Footers Complex Format
     * </a>
     * <p>
     * The header rectangle simply could not swallow all the content, it is much too small.
     * </p>
     */
    @Test
    public void CreatePdfLikeOZWolverine()
    {
        HtmlHeaderFooter htmlHeaderFooter = this;
        htmlHeaderFooter.FOOTER = "<h2>Footer Only Line</h2>";
        htmlHeaderFooter.HEADER = "<p>----Header Start---</p>"
//                + "<p><img alt=\"\" src=\"http://localhost:8080/DocGen/resources/images/main_header.jpg\" style=\"height:126px; width:683px\" /></p>"
                + "<p><img alt=\"\" src=\"http://engineeringtutorial.com/wp-content/uploads/2016/07/Transformer-Open-and-Short-Circuit-Tests.png\" style=\"height:126px; width:683px\" /></p>"
                + "<p>--Header End--</p>";

        //htmlHeaderFooter.setPageSize(xml2pdf.getPageSize());
        htmlHeaderFooter.pageSize = com.itextpdf.text.PageSize.A4; 
        htmlHeaderFooter.leftMargin = 30;
        htmlHeaderFooter.rightMargin = 30;
        htmlHeaderFooter.topMargin = 30;
        htmlHeaderFooter.bottomMargin = 30;

        htmlHeaderFooter.DEST = new File(RESULT_FOLDER, "salidaConHeaderAndFooter.pdf").getAbsolutePath();
        htmlHeaderFooter.createPdfAlt("PDFCompleto1.pdf", getClass().getResourceAsStream("test3.pdf"));
    }

    private String DEST = null;//"results/events/html_header_footer.pdf";
    private String HEADER = null;
    private String FOOTER = null;

    private float leftMargin;
    private float rightMargin;
    private float topMargin;
    private float bottomMargin;

    private Rectangle pageSize = null;

    /**
     * <a href="http://stackoverflow.com/questions/41912927/itextpdf-adding-headers-and-footers-complex-format">
     * ItextPDF Adding Headers and Footers Complex Format
     * </a>
     * <p>
     * In {@link #onEndPage(PdfWriter, Document)} the too small rectangle has been enlarged.
     * </p>
     */
    public class HeaderFooter extends PdfPageEventHelper
    {
        protected ElementList header;
        protected ElementList footer;

        public HeaderFooter() throws IOException
        {
            header = XMLWorkerHelper.parseToElementList(HEADER, null);
            footer = XMLWorkerHelper.parseToElementList(FOOTER, null);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document)
        {
            try
            {
                ColumnText ct = new ColumnText(writer.getDirectContent());
                // vvv required change of the rectangle height.
                ct.setSimpleColumn(new Rectangle(36, 832, 559, /*810*/0));
                for (Element e : header)
                {
                    System.out.println("Element on header: " + e.toString());
                    ct.addElement(e);
                }
                ct.go();
                ct.setSimpleColumn(new Rectangle(36, 10, 559, 32));
                for (Element e : footer)
                {
                    System.out.println("Element on footer: " + e.toString());
                    ct.addElement(e);
                }
                ct.go();
            }
            catch (DocumentException de)
            {
                throw new ExceptionConverter(de);
            }
        }
    }

    public void createPdfAlt(String outputFile, InputStream inputStream)
    {
        Document document = new Document(pageSize, leftMargin, rightMargin, topMargin, bottomMargin);

        FileOutputStream outputStream;
        try
        {
            outputStream = new FileOutputStream(DEST);
            //System.out.println("Doc: " + document.);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setPageEvent(new HeaderFooter());
            document.open();

            PdfContentByte cb = writer.getDirectContent();

            // Load existing PDF
            PdfReader reader = new PdfReader(inputStream);
            PdfImportedPage page = writer.getImportedPage(reader, 1); 
            //  document.setPageSize(reader.getPageSize(1));
            // Copy first page of existing PDF into output PDF
            document.newPage();
            cb.addTemplate(page, 0, 0);
            document.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DocumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
