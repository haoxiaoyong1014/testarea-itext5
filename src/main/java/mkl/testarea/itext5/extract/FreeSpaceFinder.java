package mkl.testarea.itext5.extract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * @author mkl
 */
public class FreeSpaceFinder implements RenderListener {
    //
    // constructors
    //
    public FreeSpaceFinder(Rectangle2D initialBox, float minWidth, float minHeight) {
        this(Collections.singleton(initialBox), minWidth, minHeight);
    }

    public FreeSpaceFinder(Collection<Rectangle2D> initialBoxes, float minWidth, float minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;

        freeSpaces = initialBoxes;
    }

    //
    // RenderListener implementation
    //
    @Override
    public void renderText(TextRenderInfo renderInfo) {
        try {
            Rectangle2D usedSpace = renderInfo.getAscentLine().getBoundingRectange();
            usedSpace.add(renderInfo.getDescentLine().getBoundingRectange());
            remove(usedSpace);
        } catch (ArrayIndexOutOfBoundsException aioube) {
            System.err.printf("!!! Ignoring text render info due to translation problem: %s\n", renderInfo);
            aioube.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.itextpdf.text.pdf.parser.RenderListener#renderImage(com.itextpdf.text.pdf.parser.ImageRenderInfo)
     */
    @Override
    public void renderImage(ImageRenderInfo renderInfo) {
        Matrix imageMatrix = renderInfo.getImageCTM();

        Vector image00 = rect00.cross(imageMatrix);
        Vector image01 = rect01.cross(imageMatrix);
        Vector image10 = rect10.cross(imageMatrix);
        Vector image11 = rect11.cross(imageMatrix);

        Rectangle2D usedSpace = new Rectangle2D.Float(image00.get(Vector.I1), image00.get(Vector.I2), 0, 0);
        usedSpace.add(image01.get(Vector.I1), image01.get(Vector.I2));
        usedSpace.add(image10.get(Vector.I1), image10.get(Vector.I2));
        usedSpace.add(image11.get(Vector.I1), image11.get(Vector.I2));

        remove(usedSpace);
    }

    @Override
    public void beginTextBlock() {
    }

    @Override
    public void endTextBlock() {
    }

    //
    // helpers
    //
    void remove(Rectangle2D usedSpace) {
        final double minX = usedSpace.getMinX();
        final double maxX = usedSpace.getMaxX();
        final double minY = usedSpace.getMinY();
        final double maxY = usedSpace.getMaxY();

        final Collection<Rectangle2D> newFreeSpaces = new ArrayList<Rectangle2D>();

        for (Rectangle2D freeSpace : freeSpaces) {
            final Collection<Rectangle2D> newFragments = new ArrayList<Rectangle2D>();
            if (freeSpace.intersectsLine(minX, minY, maxX, minY))
                newFragments.add(new Rectangle2D.Double(freeSpace.getMinX(), freeSpace.getMinY(), freeSpace.getWidth(), minY - freeSpace.getMinY()));
            if (freeSpace.intersectsLine(minX, maxY, maxX, maxY))
                newFragments.add(new Rectangle2D.Double(freeSpace.getMinX(), maxY, freeSpace.getWidth(), freeSpace.getMaxY() - maxY));
            if (freeSpace.intersectsLine(minX, minY, minX, maxY))
                newFragments.add(new Rectangle2D.Double(freeSpace.getMinX(), freeSpace.getMinY(), minX - freeSpace.getMinX(), freeSpace.getHeight()));
            if (freeSpace.intersectsLine(maxX, minY, maxX, maxY))
                newFragments.add(new Rectangle2D.Double(maxX, freeSpace.getMinY(), freeSpace.getMaxX() - maxX, freeSpace.getHeight()));
            if (newFragments.isEmpty()) {
                add(newFreeSpaces, freeSpace);
            } else {
                for (Rectangle2D fragment : newFragments) {
                    if (fragment.getHeight() >= minHeight && fragment.getWidth() >= minWidth) {
                        add(newFreeSpaces, fragment);
                    }
                }
            }
        }

        freeSpaces = newFreeSpaces;
    }

    void add(Collection<Rectangle2D> rectangles, Rectangle2D addition) {
        final Collection<Rectangle2D> toRemove = new ArrayList<Rectangle2D>();
        boolean isContained = false;
        for (Rectangle2D rectangle : rectangles) {
            if (rectangle.contains(addition)) {
                isContained = true;
                break;
            }
            if (addition.contains(rectangle))
                toRemove.add(rectangle);
        }
        rectangles.removeAll(toRemove);
        if (!isContained)
            rectangles.add(addition);
    }

    //
    // hidden members
    //
    Collection<Rectangle2D> freeSpaces = null;
    final float minWidth;
    final float minHeight;

    final static Vector rect00 = new Vector(0, 0, 1);
    final static Vector rect01 = new Vector(0, 1, 1);
    final static Vector rect10 = new Vector(1, 0, 1);
    final static Vector rect11 = new Vector(1, 1, 1);
}
