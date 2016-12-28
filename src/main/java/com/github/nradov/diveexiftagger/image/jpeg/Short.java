package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteOrder;

/**
 * A 16-bit (2-byte) unsigned integer.
 *
 * @author Nick Radov
 */
class Short extends DataType {

    private final short value;

    Short(final byte[] b, final int newIndex, final ByteOrder byteOrder) {
        value = Utilities.convertToShort(b, newIndex, byteOrder);
    }

    short getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @return 2
     */
    @Override
    int getLength() {
        return FieldType.SHORT.getLength();
    }

    @Override
    public String toString() {
        return java.lang.Long.toUnsignedString(value);
    }

}
