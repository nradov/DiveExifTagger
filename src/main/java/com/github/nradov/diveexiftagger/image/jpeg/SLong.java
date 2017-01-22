package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteOrder;

/**
 * A 32-bit (4-byte) signed integer (2's complement notation).
 *
 * @author Nick Radov
 */
class SLong extends IntegralDataType {

    private final int value;

    SLong(final byte[] array, final int offset, final ByteOrder byteOrder) {
        value = Utilities.convertToInt(array, offset, byteOrder);
    }

    /**
     * {@inheritDoc}
     *
     * @return 4
     */
    @Override
    int getLength() {
        return FieldType.SLONG.getLength();
    }

    int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

}
