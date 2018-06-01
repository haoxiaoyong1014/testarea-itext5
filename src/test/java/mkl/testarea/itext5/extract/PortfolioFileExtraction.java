package mkl.testarea.itext5.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;

/**
 * This test deals with extraction of files from portfolios, portable collections.
 * 
 * @author mkl
 */
public class PortfolioFileExtraction
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/31789791/extract-folders-from-portfolio-pdf-java">
     * extract folders from portfolio pdf java
     * </a>
     * <br>
     * <a href="https://drive.google.com/file/d/0BzuoJ_PwA_0DTmlJbFNtQnc5Wkk/view?usp=sharing">
     * samplePortfolio11.pdf
     * </a>
     * <p>
     * This test executes the original code presented by the OP which extracts all
     * portfolio files immediately into the same target folder.
     * </p>
     */
    @Test
    public void testSamplePortfolio11Original() throws IOException
    {
        try ( InputStream resourceStream = getClass().getResourceAsStream("samplePortfolio11.pdf") )
        {
            PdfReader reader = new PdfReader(resourceStream);

            extractAttachments(reader, new File(RESULT_FOLDER, "samplePortfolio11Original").getAbsolutePath());
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/31789791/extract-folders-from-portfolio-pdf-java">
     * extract folders from portfolio pdf java
     * </a>
     * <br>
     * <a href="https://drive.google.com/file/d/0BzuoJ_PwA_0DTmlJbFNtQnc5Wkk/view?usp=sharing">
     * samplePortfolio11.pdf
     * </a>
     * <p>
     * This test executes the extended code which extracts all portfolio files into the target
     * folder determined by their portfolio folder.
     * </p>
     */
    @Test
    public void testSamplePortfolio11Folders() throws IOException, DocumentException
    {
        try ( InputStream resourceStream = getClass().getResourceAsStream("samplePortfolio11.pdf") )
        {
            PdfReader reader = new PdfReader(resourceStream);

            extractAttachmentsWithFolders(reader, new File(RESULT_FOLDER, "samplePortfolio11Folders").getAbsolutePath());
        }
    }

    /**
     * These two methods ({@link #extractAttachments(PdfReader, String)} and
     * {@link #extractAttachment(PdfReader, File, PdfString, PdfDictionary)})
     * essentially are the OP's original code posted in his question. They
     * extract files without the folder structure.
     */
    public static void extractAttachments(PdfReader reader, String dir) throws IOException
    {
        File folder = new File(dir);
        folder.mkdirs();

        PdfDictionary root = reader.getCatalog();

        PdfDictionary names = root.getAsDict(PdfName.NAMES);
        System.out.println("" + names.getKeys().toString());
        PdfDictionary embedded = names.getAsDict(PdfName.EMBEDDEDFILES);
        System.out.println("" + embedded.toString());

        PdfArray filespecs = embedded.getAsArray(PdfName.NAMES);

        //System.out.println(filespecs.getAsString(root1));
        for (int i = 0; i < filespecs.size();)
        {
            extractAttachment(reader, folder, filespecs.getAsString(i++), filespecs.getAsDict(i++));
        }
    }

    protected static void extractAttachment(PdfReader reader, File dir, PdfString name, PdfDictionary filespec) throws IOException
    {
        PRStream stream;
        FileOutputStream fos;
        String filename;
        PdfDictionary refs = filespec.getAsDict(PdfName.EF);
        // System.out.println(""+refs.getKeys().toString());

        for (Object key : refs.getKeys())
        {
            stream = (PRStream) PdfReader.getPdfObject(refs.getAsIndirectObject((PdfName) key));

            filename = filespec.getAsString((PdfName) key).toString();

            // System.out.println("" + filename);
            fos = new FileOutputStream(new File(dir, filename));
            fos.write(PdfReader.getStreamBytes(stream));
            fos.flush();
            fos.close();
        }
    }

    /**
     * <p>
     * These two methods ({@link #extractAttachmentsWithFolders(PdfReader, String)} and
     * {@link #extractAttachment(PdfReader, Map, PdfString, PdfDictionary)}) extend the
     * functionality of the OP's original code posted in his question. They extract files
     * with the folder structure.
     * </p>
     * <p>
     * The information concerning the portfolio folder structure is retrieved using
     * the method {@link #retrieveFolders(PdfReader, File)} and its helper method
     * {@link #collectFolders(Map, PdfDictionary, File)}.
     * </p>
     */
    public static void extractAttachmentsWithFolders(PdfReader reader, String dir) throws IOException, DocumentException
    {
        File folder = new File(dir);
        folder.mkdirs();

        Map<Integer, File> folders = retrieveFolders(reader, folder);

        PdfDictionary root = reader.getCatalog();

        PdfDictionary names = root.getAsDict(PdfName.NAMES);
        System.out.println("" + names.getKeys().toString());
        PdfDictionary embedded = names.getAsDict(PdfName.EMBEDDEDFILES);
        System.out.println("" + embedded.toString());

        PdfArray filespecs = embedded.getAsArray(PdfName.NAMES);

        for (int i = 0; i < filespecs.size();)
        {
            extractAttachment(reader, folders, folder, filespecs.getAsString(i++), filespecs.getAsDict(i++));
        }
    }

    protected static void extractAttachment(PdfReader reader, Map<Integer, File> dirs, File dir, PdfString name, PdfDictionary filespec) throws IOException
    {
        PRStream stream;
        FileOutputStream fos;
        String filename;
        PdfDictionary refs = filespec.getAsDict(PdfName.EF);

        File dirHere = dir;
        String nameString = name.toUnicodeString();
        if (nameString.startsWith("<"))
        {
            int closing = nameString.indexOf('>');
            if (closing > 0)
            {
                int folderId = Integer.parseInt(nameString.substring(1, closing));
                File folderFile = dirs.get(folderId);
                if (folderFile != null)
                    dirHere = folderFile;
            }
        }

        for (PdfName key : refs.getKeys())
        {
            stream = (PRStream) PdfReader.getPdfObject(refs.getAsIndirectObject(key));

            filename = filespec.getAsString(key).toString();

            // System.out.println("" + filename);
            fos = new FileOutputStream(new File(dirHere, filename));
            fos.write(PdfReader.getStreamBytes(stream));
            fos.flush();
            fos.close();
        }
    }

    static Map<Integer, File> retrieveFolders(PdfReader reader, File baseDir) throws DocumentException
    {
        Map<Integer, File> result = new HashMap<Integer, File>();

        PdfDictionary root = reader.getCatalog();
        PdfDictionary collection = root.getAsDict(PdfName.COLLECTION);
        if (collection == null)
            throw new DocumentException("Document has no Collection dictionary");
        PdfDictionary folders = collection.getAsDict(FOLDERS);
        if (folders == null)
            throw new DocumentException("Document collection has no folders dictionary");
        
        collectFolders(result, folders, baseDir);

        return result;
    }

    static void collectFolders(Map<Integer, File> collection, PdfDictionary folder, File baseDir)
    {
        PdfString name = folder.getAsString(PdfName.NAME);
        File folderDir = new File(baseDir, name.toString());
        folderDir.mkdirs();
        PdfNumber id = folder.getAsNumber(PdfName.ID);
        collection.put(id.intValue(), folderDir);

        PdfDictionary next = folder.getAsDict(PdfName.NEXT);
        if (next != null)
            collectFolders(collection, next, baseDir);
        PdfDictionary child = folder.getAsDict(CHILD);
        if (child != null)
            collectFolders(collection, child, folderDir);
    }

    final static PdfName FOLDERS = new PdfName("Folders");
    final static PdfName CHILD = new PdfName("Child");
}
