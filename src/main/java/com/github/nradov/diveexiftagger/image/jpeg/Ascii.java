package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

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

    @Override
    public String toString() {
        return Character.toString(value);
    }

    @Override
    int getLength() {
        return FieldType.ASCII.getLength();
    }

}
