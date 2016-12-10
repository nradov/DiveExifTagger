package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.adobe.internal.xmp.XMPException;
import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.XMPMetaFactory;

/**
 * Extensible Metadata Platform (XMP).
 *
 * @see <a target="_" title="Wikipedia" href=
 *      "https://en.wikipedia.org/wiki/Extensible_Metadata_Platform">Extensible
 *      Metadata Platform</a>
 * @see <a target="_" href="http://www.adobe.com/products/xmp.html">Extensible
 *      Metadata Platform (XMP)</a>
 * @see <a target="_" title="ISO" href=
 *      "http://www.iso.org/iso/iso_catalogue/catalogue_tc/catalogue_detail.htm?csnumber=57421">
 *      Graphic technology -- Extensible metadata platform (XMP) specification
 *      -- Part 1: Data model, serialization and core properties</a>
 * @author Nick Radov
 */
class App1Xmp extends App1Contents {

    static final String XMP_IDENTIFIER = "http://ns.adobe.com/xap/1.0/";

    final XMPMeta xmp;

    App1Xmp(final byte[] content) throws XMPException {
        xmp = XMPMetaFactory.parseFromBuffer(Arrays.copyOfRange(content,
                XMP_IDENTIFIER.length() + 1, content.length));
        System.err.println(xmp.dumpObject());
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        final byte[] bytes = xmp.toString()
                .getBytes(StandardCharsets.ISO_8859_1);
        dst.put(bytes);
        return bytes.length;
    }

}