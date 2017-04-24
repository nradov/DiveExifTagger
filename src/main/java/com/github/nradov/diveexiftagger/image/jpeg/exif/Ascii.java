package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * An 8-bit byte containing one 7-bit ASCII code. The final byte is terminated
 * with NULL.
 *
 * @author Nick Radov
 */
class Ascii extends DataType {

    final char value;

    Ascii(final byte[] bytes, final int offset, final ByteOrder byteOrder) {
        value = new String(bytes, offset, 1, StandardCharsets.ISO_8859_1)
                .charAt(0);
    }

    char getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Character.toString(value);
    }

    static String toStringAscii(final List<Ascii> l) {
        final StringBuilder sb = new StringBuilder(l.size() + 2);
        sb.append('"');
        for (final Ascii a : l) {
            sb.append(a.getValue());
        }
        sb.append('"');
        return sb.toString();
    }

    @Override
    int getLength() {
        return FieldType.ASCII.getLength();
    }

}
