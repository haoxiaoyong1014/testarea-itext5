package mkl.testarea.itext5.encryption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class DecryptUserOnly
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "encryption");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/45351357/how-to-decrypt-128bit-rc4-pdf-file-in-java-with-user-password-if-it-is-encrpted">
     * How to decrypt 128bit RC4 pdf file in java with user password if it is encrpted with user as well as owner password
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/pc74oox4y19awin/abc.pdf?dl=0">
     * abc.pdf
     * </a>
     * <p>
     * This code shows how to decrypt an encrypted PDF for which you have the
     * user password, not the owner password. The procedure is closely related
     * to <a href="https://stackoverflow.com/a/27876840/1729265">Bruno's answer
     * here</a>.
     * </p> 
     */
    @Test
    public void testDecryptAbc() throws IOException, DocumentException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        try (   InputStream inputStream = getClass().getResourceAsStream("abc.pdf");
                OutputStream outputStream = new FileOutputStream(new File(RESULT_FOLDER, "abc-decrypted.pdf"))    )
        {
            PdfReader.unethicalreading = true;
            PdfReader reader = new PdfReader(inputStream, "abc123".getBytes());

            Field encryptedField = PdfReader.class.getDeclaredField("encrypted");
            encryptedField.setAccessible(true);
            encryptedField.set(reader, false);

            PdfStamper stamper = new PdfStamper(reader, outputStream);
            stamper.close();
            reader.close();
        }
    }

}
