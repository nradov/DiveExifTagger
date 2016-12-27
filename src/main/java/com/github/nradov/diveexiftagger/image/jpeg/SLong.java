package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteOrder;

class SLong extends DataType {

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

}
