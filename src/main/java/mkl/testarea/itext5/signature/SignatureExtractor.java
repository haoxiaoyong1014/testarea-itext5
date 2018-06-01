/* $Id$ */
package mkl.testarea.itext5.signature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;


/**
 * This utility class extracts all signatures contained in a given PDF file.
 *
 * @author mkl
 */
public class SignatureExtractor {
    /**
     * <p>
     * This class as a stand-alone utility extracts all signatures contained
     * in the PDF files given as command line parameters.
     * </p>
     * <p>
     * The signatures are extracted into the folder in which the original PDF
     * files reside, their names are built from the original file name, the
     * signature field name, and an extension.
     * </p>
     * <p>
     * If the embedded container is DER encoded and can successfully be parsed,
     * only the actual container bytes are exported and the extension ".cms" is
     * used. Otherwise the complete content of the signature field /Contents is
     * exported and the extension ".raw" is used.
     * </p>
     * <p>
     * Additionally, if the embedded signature container can successfully be
     * parsed, a textual ASN.1 representation is also exported using the
     * extension ".asn".
     * </p>
     * <p>
     * BEWARE: DO NOT USE THE CODE OF THIS METHOD FOR AUTOMATED PRODUCTION USE!
     * On the one hand the signature field names are used without sanitizing in
     * the names of the exported files; this improves readability but will fail
     * for names containing interesting characters (or, even worse, may not
     * fail for names including injected paths). On the other hand the original
     * file name, too, is used without sanitizing; the code will try and write
     * to "interesting" locations denoted in these names, too.
     * </p>
     */
    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            final File file = new File(arg);
            if (file.exists()) {
                final SignatureExtractor extractor = new SignatureExtractor(file);
                Map<String, SignatureData> signatures = extractor.extractSignatures();
                for (Entry<String, SignatureData> entry : signatures.entrySet()) {
                    SignatureData data = entry.getValue();
                    final String extension;
                    final byte[] bytes;
                    if (data.getSignatureContainer() != null) {
                        bytes = data.getSignatureContainer();
                        extension = ".cms";
                    } else {
                        bytes = data.getRawContents();
                        extension = ".raw";
                    }
                    String sanitizedName = entry.getKey().replace(':', '_');
                    FileOutputStream output = new FileOutputStream(new File(file.getParent(), file.getName() + "." + sanitizedName + extension));
                    output.write(bytes);
                    output.close();
                    /*
                    if (data.contentInfo != null)
                    {
                        FileWriter writer = new FileWriter(new File(file.getParent(), file.getName() + "." + sanitizedName + ".asn"));
                        writer.write(data.contentInfo.toString());
                        writer.close();
                    }
                    */
                }
            } else
                System.err.println("File does not exist: " + file);
        }
    }

    //
    // constructors
    //
    public SignatureExtractor(File file) throws IOException {
        this(new PdfReader(new RandomAccessFileOrArray(file.getPath()), null));
    }

    public SignatureExtractor(PdfReader pdfReader) throws IOException {
        reader = pdfReader;
    }

    //
    // public methods
    //

    /**
     * This method extracts integrated signature information from the PDF
     * this class is instantiated with.
     *
     * @return
     */
    //@SuppressWarnings("unchecked")
    public Map<String, SignatureData> extractSignatures() {
        final Map<String, SignatureData> result = new HashMap<String, SignatureData>();
        final AcroFields fields = reader.getAcroFields();
        for (String name : fields.getSignatureNames()) {
            PdfDictionary sigDict = fields.getSignatureDictionary(name);
            PdfString contents = sigDict.getAsString(PdfName.CONTENTS);
            PdfName subFilter = sigDict.getAsName(PdfName.SUBFILTER);
            if (contents != null) {
                byte[] contentBytes = contents.getOriginalBytes();
                byte[] containerBytes = null;
                /*
                ContentInfo contentInfo = null;
                try {
                    contentInfo = new ContentInfoImpl(contentBytes);
                    byte[] bytes = contentInfo.getEncoded();
                    if (bytes.length <= contentBytes.length)
                    {
                        boolean equal = true;
                        for (int i = 0; i < bytes.length; i++)
                        {
                            if (bytes[i] != contentBytes[i])
                            {
                                System.err.println("Re-encoded differs at " + i);
                                equal = false;
                                break;
                            }
                        }
                        if (equal)
                            containerBytes = bytes;
                    }
                    else
                    {
                        System.err.println("Re-encoded data too long");
                    }
                }
                catch (GeneralSecurityException e)
                {
                    System.err.println("Failure decoding content as container.");
                    e.printStackTrace();
                }
                */

                Date signingTime = null;
                Object pdfDateEntry = sigDict.get(PdfName.M);
                if (pdfDateEntry != null) {
                    Calendar cal = PdfDate.decode(pdfDateEntry.toString());
                    if (cal != null) {
                        signingTime = cal.getTime();
                    }
                }

                result.put(name, new SignatureData(/*contentInfo,*/ containerBytes, contentBytes, subFilter, signingTime));
            }
        }
        return result;
    }

    /**
     * This class contains raw individual PDF signature data, i.e. the data
     * originally contained in the signature /Contents field, the parsed and
     * re-encoded signature container (if it essentially is a starting segment
     * of the original data), the signature dictionary signing time and sub
     * filter, and the PAdES LTV validation related information dictionary
     * key for this signature.
     */
    public class SignatureData {
        /**
         * The PAdES LTV validation related information dictionary key for
         * this signature derived from the other attributes of this instance.
         */
        public byte[] getVriKey() {
            return ETSI_RFC3161.equals(subFilter) ? signatureContainer : rawContents;
        }

        /**
         * The parsed and re-encoded signature container (if it essentially
         * is a starting segment of the original data).
         */
        public byte[] getSignatureContainer() {
            return signatureContainer;
        }

        /**
         * The data originally contained in the signature /Contents field.
         */
        public byte[] getRawContents() {
            return rawContents;
        }

        /**
         * The signature dictionary sub filter.
         */
        public PdfName getSubFilter() {
            return subFilter;
        }

        /**
         * The signature dictionary signing time.
         */
        public Date getSigningTime() {
            return signingTime;
        }

        SignatureData(/*ContentInfo contentInfo,*/ byte[] signatureContainer, byte[] rawContents, PdfName subFilter, Date signingTime) {
            //this.contentInfo = contentInfo;
            this.signatureContainer = signatureContainer;
            this.rawContents = rawContents;
            this.subFilter = subFilter;
            this.signingTime = signingTime;
        }

        //final ContentInfo contentInfo; // For internal use only
        final byte[] signatureContainer;
        final byte[] rawContents;
        final PdfName subFilter;
        final Date signingTime;
    }

    //
    // members
    //
    final PdfReader reader;
    static final PdfName ETSI_RFC3161 = new PdfName("ETSI.RFC3161");
}
