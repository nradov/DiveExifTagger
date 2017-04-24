package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.nio.ByteOrder;

class Double extends DataType {

    Double(final byte[] array, final int offset, final ByteOrder byteOrder) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * {@inheritDoc}
     *
     * @return 8
     */
    @Override
    int getLength() {
        return FieldType.DOUBLE.getLength();
    }

}
