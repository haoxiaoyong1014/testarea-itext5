package mkl.testarea.itext5.meta;

import java.lang.reflect.Field;

import org.junit.Test;

/**
 * This test shows the {@link DefaultCounter} message.
 * 
 * Update: {@link com.itextpdf.text.log.DefaultCounter} has been removed. Thus, nothing to show anymore.
 * 
 * @author mkl
 */
public class ShowDefaultCounterMessage
{
    @Test
    public void retrieveAndShowDefaultCounterMessage() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
//        Field messageField = com.itextpdf.text.log.DefaultCounter.class.getDeclaredField("message");
//        messageField.setAccessible(true);
//        byte[] messageBytes = (byte[]) messageField.get(null);
//        System.out.println(new String(messageBytes));
    }
}
