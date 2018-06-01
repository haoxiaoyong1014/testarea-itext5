package mkl.testarea.itext5.signature;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.io.RASInputStream;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.security.PdfPKCS7;

/**
 * <a href="http://stackoverflow.com/questions/29648437/itext-doesnt-process-correct-siganture-fields-on-strange-situations">
 * iText doesn't process correct siganture fields on strange situations
 * </a>
 * <br>
 * <a href="https://drive.google.com/file/d/0BwBTJ2YupEtaaWo5WmhWbm0zcUU/view?usp=sharing">
 * FirstPage11P0022AD_20150202164018_307494.pdf
 * </a>
 * <p>
 * The PDF contains two signature fields with the name "Signature1", an older one only referenced from
 * page annotations, and a newer one, referenced from both the page annotations and the AcroForm Fields.
 * </p>
 * <p>
 * iText as is only sees the older signature field. The test {@link #verify(PdfReader)} here explicitly
 * only inspects the fields referenced from the AcroForm Fields.
 * </p>
 * @author mkl
 */
public class VerifyAcroFormSignatures
{
    @BeforeClass
    public static void setUp() throws Exception
    {
        BouncyCastleProvider bcp = new BouncyCastleProvider();
        //Security.addProvider(bcp);
        Security.insertProviderAt(bcp, 1);
    }

    @Test
    public void testSigned_g() throws IOException, GeneralSecurityException
    {
        try (   InputStream resource = getClass().getResourceAsStream("signed_g.pdf")   )
        {
            System.out.printf("\nsigned_g.pdf\n------------\n");
            PdfReader reader = new PdfReader(resource);
            verify(reader);
        }
    }

    @Test
    public void testSigned_2g() throws IOException, GeneralSecurityException
    {
        try (   InputStream resource = getClass().getResourceAsStream("signed_2g.pdf")   )
        {
            System.out.printf("\nsigned_2g.pdf\n-------------\n");
            PdfReader reader = new PdfReader(resource);
            verify(reader);
        }
    }

    /**
     * Checking the signatures in a file signed along the lines of
     * <a href="http://stackoverflow.com/questions/30400728/signing-pdf-with-pdfbox-and-bouncycastle">
     * Signing PDF with PDFBox and BouncyCastle
     * </a> by LoneWolf on stackoverflow, cf. {@link mkl.testarea.pdfbox1.sign.SignLikeLoneWolf}.
     */
    @Test
    public void testTest_signedLikeLoneWolf() throws IOException, GeneralSecurityException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test_signedLikeLoneWolf.pdf")   )
        {
            System.out.printf("\ntest_signedLikeLoneWolf.pdf\n------------\n");
            PdfReader reader = new PdfReader(resource);
            verify(reader);
        }
    }

    /**
     * Checking the signatures in the OP's file referenced from the AcroForm Fields.  
     */
    @Test
    public void testFirstPage11P0022AD_20150202164018_307494() throws IOException, GeneralSecurityException
    {
        try (   InputStream resource = getClass().getResourceAsStream("FirstPage11P0022AD_20150202164018_307494.pdf")   )
        {
            System.out.printf("\nFirstPage11P0022AD_20150202164018_307494.pdf\n------------\n");
            PdfReader reader = new PdfReader(resource);
            verify(reader);
        }
    }

    /**
     * This method checks the signatures referenced from the AcroForm Fields.  
     */
    void verify(PdfReader reader) throws GeneralSecurityException
    {
        PdfDictionary top = (PdfDictionary)PdfReader.getPdfObjectRelease(reader.getCatalog().get(PdfName.ACROFORM));
        if (top == null)
        {
            System.out.println("No AcroForm, so nothing to verify");
            return;
        }

        PdfArray arrfds = (PdfArray)PdfReader.getPdfObjectRelease(top.get(PdfName.FIELDS));
        if (arrfds == null || arrfds.isEmpty())
        {
            System.out.println("No AcroForm Fields, so nothing to verify");
            return;
        }

        for (PdfObject object : arrfds)
        {
            object = PdfReader.getPdfObject(object);
            if (object == null)
            {
                System.out.println("* A null entry.");
            }
            else if (!object.isDictionary())
            {
                System.out.println("* A non-dictionary entry.");
            }
            else
            {
                verify(reader, (PdfDictionary) object, null, null, null, false);
            }
        }
    }

    void verify(PdfReader reader, PdfObject object, String baseName, PdfName baseType, PdfObject baseValue, boolean processed) throws GeneralSecurityException
    {
        if (object == null)
        {
            System.out.println("* A null entry.");
        }
        else if (!object.isDictionary())
        {
            System.out.println("* A non-dictionary entry.");
        }
        else
        {
            PdfDictionary field = (PdfDictionary) object;
            
            String name;
            PdfName type;
            PdfObject value;

            PdfString partialObject = field.getAsString(PdfName.T);
            if (partialObject == null)
            {
                System.out.println("* An anonymous entry.");
                name = baseName;
            }
            else
            {
                String partial = partialObject.toString();
                System.out.printf("* A named entry: %s\n", partial);
                name = baseName == null ? partial : String.format("%s:%s", baseName, partial);
            }
            System.out.printf("  FQP: %s\n", name == null ? "" : name);

            PdfName typeHere = field.getAsName(PdfName.FT);
            if (typeHere != null)
            {
                type = typeHere;
                System.out.printf("  Type: %s\n", type);
            }
            else
            {
                type = baseType;
                if (type == null)
                {
                    System.out.printf("  Type: -\n");
                }
                else
                {
                    System.out.printf("  Type: %s (inherited)\n", type);
                }
            }

            PdfObject valueHere = field.getDirectObject(PdfName.V);
            if (valueHere != null)
            {
                value = valueHere;
                processed = false;
                System.out.printf("  Value: present\n");
            }
            else
            {
                value = baseValue;
                if (value == null)
                {
                    System.out.printf("  Value: -\n");
                }
                else
                {
                    System.out.printf("  Value: inherited\n");
                }
            }

            if (PdfName.SIG.equals(type) && value instanceof PdfDictionary && !processed)
            {
                processed = verify(reader, (PdfDictionary) value);
            }

            PdfArray kids = field.getAsArray(PdfName.KIDS);
            if (kids != null)
            {
                for (PdfObject kid : kids)
                {
                    verify(reader, kid, name, type, value, processed);
                }
            }
        }
    }

    boolean verify(PdfReader reader, PdfDictionary value) throws GeneralSecurityException
    {
        PdfArray byteRange = value.getAsArray(PdfName.BYTERANGE);
        if (byteRange == null || byteRange.isEmpty())
        {
            System.out.printf("  Signed range: missing\n");
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            builder.append("  Signed range:");
            for (PdfObject arrObj: byteRange)
            {
                builder.append(' ').append(arrObj);
            }
            int byteRangeSize = byteRange.size();
            if (byteRangeSize % 2 == 1)
            {
                builder.append(" (Invalid: odd number of entries)");
            }
            else
            {
                StringBuilder interoperability = new StringBuilder();
                if (byteRangeSize != 4)
                {
                    interoperability.append(", not exactly one gap");
                }
                int rangeStart = byteRange.getAsNumber(0).intValue();
                if (rangeStart != 0)
                {
                    interoperability.append(", first range does not start at 0");
                }
                for (int i = 2; i < byteRangeSize; i+=2)
                {
                    int lastRangeEnd = rangeStart + byteRange.getAsNumber(i-1).intValue();
                    rangeStart = byteRange.getAsNumber(i).intValue();
                    if (lastRangeEnd > rangeStart)
                    {
                        interoperability.append(", unordered or overlapping ranges");
                        break;
                    }
                }
                if (interoperability.length() > 0)
                {
                    builder.append(" (Interoperability issues").append(interoperability).append(')');
                }
                int finalRangeEnd = byteRange.getAsNumber(byteRangeSize-2).intValue() + byteRange.getAsNumber(byteRangeSize-1).intValue();
                if (finalRangeEnd == reader.getFileLength())
                {
                    builder.append(" (covers whole file)");
                }
                else
                {
                    builder.append(" (covers partial file up to ").append(finalRangeEnd).append(")");
                }
            }
            System.out.println(builder);
        }

        PdfPKCS7 pkcs7 = verifySignature(reader, value, null);
        System.out.printf("  Validity: %s\n", pkcs7.verify());
        return pkcs7 != null;
    }

    /**
     * Copied from {@link AcroFields#verifySignature(String, String)} and changed to work on the
     * given signature dictionary.
     */
    public PdfPKCS7 verifySignature(PdfReader reader, PdfDictionary v, String provider) {
        if (v == null)
            return null;
        try {
            PdfName sub = v.getAsName(PdfName.SUBFILTER);
            PdfString contents = v.getAsString(PdfName.CONTENTS);
            PdfPKCS7 pk = null;
            if (sub.equals(PdfName.ADBE_X509_RSA_SHA1)) {
                PdfString cert = v.getAsString(PdfName.CERT);
                if (cert == null)
                    cert = v.getAsArray(PdfName.CERT).getAsString(0);
                pk = new PdfPKCS7(contents.getOriginalBytes(), cert.getBytes(), provider);
            }
            else
                pk = new PdfPKCS7(contents.getOriginalBytes(), sub, provider);
            updateByteRange(reader, pk, v);
            PdfString str = v.getAsString(PdfName.M);
            if (str != null)
                pk.setSignDate(PdfDate.decode(str.toString()));
            PdfObject obj = PdfReader.getPdfObject(v.get(PdfName.NAME));
            if (obj != null) {
              if (obj.isString())
                pk.setSignName(((PdfString)obj).toUnicodeString());
              else if(obj.isName())
                pk.setSignName(PdfName.decodeName(obj.toString()));
            }
            str = v.getAsString(PdfName.REASON);
            if (str != null)
                pk.setReason(str.toUnicodeString());
            str = v.getAsString(PdfName.LOCATION);
            if (str != null)
                pk.setLocation(str.toUnicodeString());
            return pk;
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Copied from {@link AcroFields#updateByteRange(PdfPKCS7, PdfDictionary)}.
     */
    private void updateByteRange(PdfReader reader, PdfPKCS7 pkcs7, PdfDictionary v) {
        PdfArray b = v.getAsArray(PdfName.BYTERANGE);
        RandomAccessFileOrArray rf = reader.getSafeFile();
        InputStream rg = null;
        try {
            rg = new RASInputStream(new RandomAccessSourceFactory().createRanged(rf.createSourceView(), b.asLongArray()));
            byte buf[] = new byte[8192];
            int rd;
            while ((rd = rg.read(buf, 0, buf.length)) > 0) {
                pkcs7.update(buf, 0, rd);
            }
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        } finally {
            try {
                if (rg != null) rg.close();
            } catch (IOException e) {
                // this really shouldn't ever happen - the source view we use is based on a Safe view, which is a no-op anyway
                throw new ExceptionConverter(e);
            }
        }
    }
}
