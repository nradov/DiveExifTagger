package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import com.adobe.internal.xmp.XMPException;

/**
 * Application-specific. For example, an Exif JPEG file uses an APP1 marker to
 * store metadata, laid out in a structure based closely on TIFF.
 *
 * @author Nick Radov
 */
class ApplicationSpecific1 extends VariableLengthSegment {

    static final short MARKER = (short) 0xFFE1;

    private App1Contents contents;

    ApplicationSpecific1() {
    }

    @Override
    short getMarker() {
        return MARKER;
    }

    @Override
    void populate(final SeekableByteChannel channel)
            throws IOException, XMPException {
        final ByteBuffer dst = ByteBuffer.allocate(java.lang.Short.BYTES);
        populateLength(channel, dst);
        if (getLength() > java.lang.Short.BYTES) {
            final ByteBuffer body = ByteBuffer
                    .allocate(getLength() - dst.capacity());
            if (channel.read(body) != body.capacity()) {
                throw new IOException("unexpected end of channel");
            }
            body.flip();
            final byte[] content = new byte[body.capacity()];
            body.get(content);
            if (arrayStartsWith(content, App1Exif.EXIF)) {
                contents = new App1Exif(Arrays.copyOfRange(content,
                        App1Exif.EXIF.length, content.length));
            } else if (content.length >= App1Xmp.XMP_IDENTIFIER.length()
                    && App1Xmp.XMP_IDENTIFIER.equals(new String(content, 0,
                            App1Xmp.XMP_IDENTIFIER.length(),
                            StandardCharsets.ISO_8859_1))) {
                contents = new App1Xmp(content);
            } else {
                throw new IllegalArgumentException(
                        "unexpected segment content: " + new String(content,
                                StandardCharsets.ISO_8859_1));
            }
        }
    }

    private static boolean arrayStartsWith(final byte[] array,
            final byte[] prefix) {
        if (array.length >= prefix.length) {
            final byte[] arrayStart = Arrays.copyOfRange(array, 0,
                    prefix.length);
            return Arrays.equals(arrayStart, prefix);
        }
        return false;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        return contents.read(dst);
    }

    public Optional<Rational> getFieldRational(final TiffFieldTag tag) {
        return contents.getFieldRational(tag);
    }

}
