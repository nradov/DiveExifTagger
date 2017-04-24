package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class Float extends DataType {

    private final float value;

    Float(final byte[] array, final int offset, final ByteOrder byteOrder) {
        value = ByteBuffer.wrap(array, offset, java.lang.Float.BYTES)
                .order(byteOrder).getFloat();
    }

    @Override
    int getLength() {
        return FieldType.FLOAT.getLength();
    }

}
