package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.nio.ByteOrder;

class SByte extends DataType {

    private final byte value;

    SByte(final byte[] b, final int index, final ByteOrder byteOrder) {
        value = b[index];
    }

    /**
     * {@inheritDoc}
     *
     * @return 2
     */
    @Override
    int getLength() {
        return FieldType.SBYTE.getLength();
    }

}
