package com.itextpdf.text.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;

/**
 * <p>
 * This class provides methods to name pages, hide named pages, and spawn
 * pages from named pages.
 * </p>
 * <p>
 * Due to reduced visibility of required iText methods, this class had to be
 * put into this package. Alternatively reflection magic may be used.
 * </p>
 * 
 * @author mkl
 */
public class PdfStamperHelper
{
    /** A name */
    public static final PdfName TEMPLATES = new PdfName("Templates");
    /** A name */
    public static final PdfName TEMPLATE = new PdfName("Template"); 
    /** A name */
    public static final PdfName TEMPLATE_INSTANTIATED = new PdfName("TemplateInstantiated"); 
    
    /**
     * This method names a given page. The page in question already has
     * to exist in the original document the given PdfStamper works on.
     */
    public static void createTemplate(PdfStamper pdfStamper, String name, int page) throws IOException, DocumentException
    {
        PdfDictionary pageDic = pdfStamper.stamper.reader.getPageNRelease(page);
        if (pageDic != null && pageDic.getIndRef() != null)
        {
            HashMap<String, PdfObject> namedPages = getNamedPages(pdfStamper);
            namedPages.put(name, pageDic.getIndRef());
            storeNamedPages(pdfStamper);
        }
    }
    
    /**
     * This method hides a given visible named page.
     */
    public static void hideTemplate(PdfStamper pdfStamper, String name) throws IOException, DocumentException
    {
        HashMap<String, PdfObject> namedPages = getNamedPages(pdfStamper);
        PdfObject object = namedPages.get(name);
        if (object == null)
            throw new DocumentException("Document contains no visible template " + name + '.');

        namedPages.remove(name);
        storeNamedPages(pdfStamper);
        
        if (removePage(pdfStamper, (PRIndirectReference)pdfStamper.stamper.reader.getCatalog().get(PdfName.PAGES), (PRIndirectReference) object))
        {
            pdfStamper.stamper.reader.pageRefs.reReadPages();
            // TODO: correctAcroFieldPages 
            
        }
        PdfDictionary pageDict = (PdfDictionary)PdfReader.getPdfObject(object);
        if (pageDict != null)
        {
            pdfStamper.stamper.markUsed(pageDict);
            pageDict.remove(PdfName.PARENT);
            pageDict.remove(PdfName.B);
            pageDict.put(PdfName.TYPE, TEMPLATE);
        }

        HashMap<String, PdfObject> templates = getNamedTemplates(pdfStamper);
        templates.put(name, object);
        storeNamedTemplates(pdfStamper);
    }
    
    /**
     * This method returns a template dictionary.
     */
    public static PdfDictionary getTemplate(PdfStamper pdfStamper, String name) throws DocumentException
    {
        HashMap<String, PdfObject> namedTemplates = getNamedTemplates(pdfStamper);
        PdfObject object = (PdfObject) namedTemplates.get(name);
        if (object == null) {
            HashMap<String, PdfObject> namedPages = getNamedPages(pdfStamper);
            object = namedPages.get(name);
        }
        return (PdfDictionary)PdfReader.getPdfObject(object);
    }
    
    /**
     * This method spawns a template inserting it at the given page number.
     */
    public static void spawnTemplate(PdfStamper pdfStamper, String name, int pageNumber) throws DocumentException, IOException
    {
        PdfDictionary template = getTemplate(pdfStamper, name);
        if (template == null)
            throw new DocumentException("Document contains no template " + name + '.');

        PdfReader reader = pdfStamper.stamper.reader;
        
        // contRef: reference to the content stream of the spawned page;
        // it only inserts the template XObject
        PRIndirectReference contRef = reader.addPdfObject(getTemplateStream(name, reader.getPageSize(template)));
        // resRef: reference to resources dictionary containing a /XObject
        // dictionary in turn containing the template XObject resource
        // carrying the actual template content
        PdfDictionary xobjDict = new PdfDictionary();
        xobjDict.put(new PdfName(name), reader.addPdfObject(getFormXObject(reader, template, pdfStamper.stamper.getCompressionLevel(), name)));
        PdfDictionary resources = new PdfDictionary();
        resources.put(PdfName.XOBJECT, xobjDict);
        PRIndirectReference resRef = reader.addPdfObject(resources);
        
        // page: dictionary of the spawned template page
        PdfDictionary page = new PdfDictionary();
        page.put(PdfName.TYPE, PdfName.PAGE); // not PdfName.TEMPLATE!
        page.put(TEMPLATE_INSTANTIATED, new PdfName(name));
        page.put(PdfName.CONTENTS, contRef);
        page.put(PdfName.RESOURCES, resRef);
        page.mergeDifferent(template); // actually a bit too much. TODO: treat annotations as they should be treated

        PRIndirectReference pref = reader.addPdfObject(page);
        PdfDictionary parent;
        PRIndirectReference parentRef;
        if (pageNumber > reader.getNumberOfPages()) {
            PdfDictionary lastPage = reader.getPageNRelease(reader.getNumberOfPages());
            parentRef = (PRIndirectReference)lastPage.get(PdfName.PARENT);
            parentRef = new PRIndirectReference(reader, parentRef.getNumber());
            parent = (PdfDictionary)PdfReader.getPdfObject(parentRef);
            PdfArray kids = (PdfArray)PdfReader.getPdfObject(parent.get(PdfName.KIDS), parent);
            kids.add(pref);
            pdfStamper.stamper.markUsed(kids);
            reader.pageRefs.insertPage(pageNumber, pref);
        }
        else {
            if (pageNumber < 1)
                pageNumber = 1;
            PdfDictionary firstPage = reader.getPageN(pageNumber);
            PRIndirectReference firstPageRef = reader.getPageOrigRef(pageNumber);
            reader.releasePage(pageNumber);
            parentRef = (PRIndirectReference)firstPage.get(PdfName.PARENT);
            parentRef = new PRIndirectReference(reader, parentRef.getNumber());
            parent = (PdfDictionary)PdfReader.getPdfObject(parentRef);
            PdfArray kids = (PdfArray)PdfReader.getPdfObject(parent.get(PdfName.KIDS), parent);
            ArrayList<PdfObject> ar = kids.getArrayList();
            int len = ar.size();
            int num = firstPageRef.getNumber();
            for (int k = 0; k < len; ++k) {
                PRIndirectReference cur = (PRIndirectReference)ar.get(k);
                if (num == cur.getNumber()) {
                    ar.add(k, pref);
                    break;
                }
            }
            if (len == ar.size())
                throw new RuntimeException("Internal inconsistence.");
            pdfStamper.stamper.markUsed(kids);
            reader.pageRefs.insertPage(pageNumber, pref);
            pdfStamper.stamper.correctAcroFieldPages(pageNumber);
        }
        page.put(PdfName.PARENT, parentRef);
        while (parent != null) {
            pdfStamper.stamper.markUsed(parent);
            PdfNumber count = (PdfNumber)PdfReader.getPdfObjectRelease(parent.get(PdfName.COUNT));
            parent.put(PdfName.COUNT, new PdfNumber(count.intValue() + 1));
            parent = (PdfDictionary)PdfReader.getPdfObject(parent.get(PdfName.PARENT));
        }
    }
    
    //
    // helper methods
    //
    /**
     * This method recursively removes a given page from the given page tree.
     */
    static boolean removePage(PdfStamper pdfStamper, PRIndirectReference pageTree, PRIndirectReference pageToRemove)
    {
        PdfDictionary pageDict = (PdfDictionary)PdfReader.getPdfObject(pageTree);
        PdfArray kidsPR = (PdfArray)PdfReader.getPdfObject(pageDict.get(PdfName.KIDS));
        if (kidsPR != null) {
            ArrayList<PdfObject> kids = kidsPR.getArrayList();
            boolean removed = false;
            for (int k = 0; k < kids.size(); ++k){
                PRIndirectReference obj = (PRIndirectReference)kids.get(k);
                if (pageToRemove.getNumber() == obj.getNumber() && pageToRemove.getGeneration() == obj.getGeneration())
                {
                    kids.remove(k);
                    pdfStamper.stamper.markUsed(pageTree);
                    removed = true;
                    break;
                }
                else if (removePage(pdfStamper, (PRIndirectReference)obj, pageToRemove))
                {
                    removed = true;
                    break;
                }
            }
            if (removed)
            {
                PdfNumber count = (PdfNumber) PdfReader.getPdfObjectRelease(pageDict.get(PdfName.COUNT));
                pageDict.put(PdfName.COUNT, new PdfNumber(count.intValue() + 1));
                pdfStamper.stamper.markUsed(pageTree);
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns the uncompressed bytes of a content PDF object.
     */
    static byte[] pageContentsToArray(PdfReader reader, PdfObject contents, RandomAccessFileOrArray file) throws IOException{
        if (contents == null)
            return new byte[0];
        if (file == null)
            file = reader.getSafeFile();
        ByteArrayOutputStream bout = null;
        if (contents.isStream()) {
            return PdfReader.getStreamBytes((PRStream)contents, file);
        }
        else if (contents.isArray()) {
            PdfArray array = (PdfArray)contents;
            ArrayList<PdfObject> list = array.getArrayList();
            bout = new ByteArrayOutputStream();
            for (int k = 0; k < list.size(); ++k) {
                PdfObject item = PdfReader.getPdfObjectRelease(list.get(k));
                if (item == null || !item.isStream())
                    continue;
                byte[] b = PdfReader.getStreamBytes((PRStream)item, file);
                bout.write(b);
                if (k != list.size() - 1)
                    bout.write('\n');
            }
            return bout.toByteArray();
        }
        else
            return new byte[0];
    }

    /**
     * This method returns a PDF stream object containing a copy of the
     * contents of the given template page with the given name.<br>
     * To make Acrobat 9 happy with this template XObject when checking
     * for signature validity, the /Size has to be changed to be the size
     * of the stream that would have been generated by Acrobat itself
     * when spawning the given template.
     */
    static PdfStream getFormXObject(PdfReader reader, PdfDictionary page, int compressionLevel, String name) throws IOException {
        Rectangle pageSize = reader.getPageSize(page);
        final PdfLiteral MATRIX = new PdfLiteral("[1 0 0 1 " + -getXOffset(pageSize) + " " + -getYOffset(pageSize) + "]");
        PdfDictionary dic = new PdfDictionary();
        dic.put(PdfName.RESOURCES, PdfReader.getPdfObjectRelease(page.get(PdfName.RESOURCES)));
        dic.put(PdfName.TYPE, PdfName.XOBJECT);
        dic.put(PdfName.SUBTYPE, PdfName.FORM);
        dic.put(PdfName.BBOX, page.get(PdfName.MEDIABOX));
        dic.put(PdfName.MATRIX, MATRIX);
        dic.put(PdfName.FORMTYPE, PdfReaderInstance.ONE);
        dic.put(PdfName.NAME, new PdfName(name));

        PdfStream stream;
        PdfObject contents = PdfReader.getPdfObjectRelease(page.get(PdfName.CONTENTS));
        byte bout[] = null;
        if (contents != null)
            bout = pageContentsToArray(reader, contents, reader.getSafeFile());
        else
            bout = new byte[0];
        byte[] embedded = new byte[bout.length + 4];
        System.arraycopy(bout, 0, embedded, 2, bout.length);
        embedded[0] = 'q';
        embedded[1] = 10;
        embedded[embedded.length - 2] = 'Q';
        embedded[embedded.length - 1] = 10;
        stream = new PdfStream(embedded);
        stream.putAll(dic);
        stream.flateCompress(compressionLevel);
        PdfObject filter = stream.get(PdfName.FILTER);
        if (filter != null && !(filter instanceof PdfArray))
            stream.put(PdfName.FILTER, new PdfArray(filter));
        return stream;
    }
    
    /**
     * This method returns the content stream object for a spawned
     * template.
     */
    static PdfStream getTemplateStream(String name, Rectangle pageSize)
    {
        int x = getXOffset(pageSize);
        int y = getYOffset(pageSize);
        String content = "q 1 0 0 1 " + x + " " + y + " cm /" + name + " Do Q";
        return new PdfStream(PdfEncodings.convertToBytes(content, null));
    }

    /**
     * This method returns the center x offset for the given page rectangle.
     */
    static int getXOffset(Rectangle pageSize)
    {
        return Math.round((pageSize.getLeft() + pageSize.getRight()) / 2);
    }
    
    /**
     * This method returns the center y offset for the given page rectangle.
     */
    static int getYOffset(Rectangle pageSize)
    {
        return Math.round((pageSize.getTop() + pageSize.getBottom()) / 2);
    }
    
    /**
     * This method returns the /Names name dictionary of the document; if
     * the document does not have one yet, it generates one.<br>
     * Beware! If the document contains a name dictionary as an indirect
     * object, the dictionary shall be written to but once; this /includes/
     * writes by the {@link PdfStamper}.
     */
    static PdfDictionary getNameDictionary(PdfStamper pdfStamper)
    {
        PdfDictionary catalog = pdfStamper.stamper.reader.getCatalog();
        PdfDictionary names = (PdfDictionary)PdfReader.getPdfObject(catalog.get(PdfName.NAMES), catalog);
        if (names == null) {
            names = new PdfDictionary();
            catalog.put(PdfName.NAMES, names);
            pdfStamper.stamper.markUsed(catalog);
        }
        return names;
    }
    
    final static Map<PdfStamper, HashMap<String, PdfObject>> namedPagesByStamper = new HashMap<>();
    
    static HashMap<String, PdfObject> getNamedPages(PdfStamper pdfStamper) throws DocumentException
    {
        if (namedPagesByStamper.containsKey(pdfStamper))
            return namedPagesByStamper.get(pdfStamper);

        final PdfDictionary nameDictionary = getNameDictionary(pdfStamper);
        PdfObject pagesObject = PdfReader.getPdfObjectRelease(nameDictionary.get(PdfName.PAGES));
        if (pagesObject != null && !(pagesObject instanceof PdfDictionary))
            throw new DocumentException("Pages name dictionary is neither a PdfDictionary nor null");
        HashMap<String, PdfObject> namesMap = PdfNameTree.readTree((PdfDictionary)pagesObject);
        namedPagesByStamper.put(pdfStamper, namesMap);
        return namesMap;
    }
    
    static void storeNamedPages(PdfStamper pdfStamper) throws IOException
    {
        if (namedPagesByStamper.containsKey(pdfStamper))
        {
            final HashMap<String, PdfObject> pages = namedPagesByStamper.get(pdfStamper);
            final PdfDictionary nameDictionary = getNameDictionary(pdfStamper);
            pdfStamper.stamper.markUsed(nameDictionary);
            if (pages.isEmpty())
                nameDictionary.remove(PdfName.PAGES);
            else {
                final PdfDictionary tree = PdfNameTree.writeTree(pages, pdfStamper.stamper);
                nameDictionary.put(PdfName.PAGES, pdfStamper.stamper.addToBody(tree).getIndirectReference());
            }
        }
    }
    
    final static Map<PdfStamper, HashMap<String, PdfObject>> namedTemplatesByStamper = new HashMap<>();
    
    static HashMap<String, PdfObject> getNamedTemplates(PdfStamper pdfStamper) throws DocumentException
    {
        if (namedTemplatesByStamper.containsKey(pdfStamper))
            return namedTemplatesByStamper.get(pdfStamper);

        final PdfDictionary nameDictionary = getNameDictionary(pdfStamper);
        PdfObject templatesObject = PdfReader.getPdfObjectRelease(nameDictionary.get(TEMPLATES));
        if (templatesObject != null && !(templatesObject instanceof PdfDictionary))
            throw new DocumentException("Templates name dictionary is neither a PdfDictionary nor null");
        HashMap<String, PdfObject> templatesMap = PdfNameTree.readTree((PdfDictionary)templatesObject);
        namedTemplatesByStamper.put(pdfStamper, templatesMap);
        return templatesMap;
    }
    
    static void storeNamedTemplates(PdfStamper pdfStamper) throws IOException
    {
        if (namedTemplatesByStamper.containsKey(pdfStamper))
        {
            final HashMap<String, PdfObject> templates = namedTemplatesByStamper.get(pdfStamper);
            final PdfDictionary nameDictionary = getNameDictionary(pdfStamper);
            pdfStamper.stamper.markUsed(nameDictionary);
            if (templates.isEmpty())
                nameDictionary.remove(TEMPLATES);
            else {
                final PdfDictionary tree = PdfNameTree.writeTree(templates, pdfStamper.stamper);
                nameDictionary.put(TEMPLATES, pdfStamper.stamper.addToBody(tree).getIndirectReference());
            }
        }
    }
}
