package org.apache.vysper.xmpp.extension.xep0124;


import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * utility class for charsets
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 */
public class CharsetUtil {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static ThreadLocal<CharsetDecoder> decoderCache = new ThreadLocal<CharsetDecoder>();
    private static ThreadLocal<CharsetEncoder> encoderCache = new ThreadLocal<CharsetEncoder>();

    private static Object getReference(ThreadLocal threadLocal) {
        SoftReference reference = (SoftReference) threadLocal.get();
        if (reference == null) return null; 
        return reference.get();
    }

    private static void setReference(ThreadLocal threadLocal, Object object) {
        threadLocal.set(new SoftReference(object));
    }

    public static CharsetEncoder getEncoder() {
        CharsetEncoder encoder = (CharsetEncoder) getReference(encoderCache);
        if (encoder == null) {
            encoder = UTF8.newEncoder();
            setReference(encoderCache, encoder);
        }
        return encoder;
    }

    public static CharsetDecoder getDecoder() {
        CharsetDecoder decoder = (CharsetDecoder) getReference(decoderCache);
        if (decoder == null) {
            decoder = UTF8.newDecoder();
            setReference(decoderCache, decoder);
        }
        return decoder;
    }
}