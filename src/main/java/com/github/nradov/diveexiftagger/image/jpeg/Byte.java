package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * An 8-bit unsigned integer.
 *
 * @author Nick Radov
 */
class Byte extends DataType {

    private final byte value;

    Byte(final byte value) {
        this.value = value;
    }

    Byte(final byte[] b, final int index, final ByteOrder byteOrder) {
        value = b[index];
    }

    @Override
    public String toString() {
        return new String(new byte[] { value }, StandardCharsets.ISO_8859_1);
    }

    /**
     * {@inheritDoc}
     *
     * @return 1
     */
    @Override
    int getLength() {
        return 1;
    }

    byte getValue() {
        return value;
    }

}
