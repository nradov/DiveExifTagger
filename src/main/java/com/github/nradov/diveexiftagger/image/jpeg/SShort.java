package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteOrder;

class SShort extends DataType {

    private final short value;

    public SShort(final byte[] b, final int newIndex,
            final ByteOrder byteOrder) {
        value = Utilities.convertToShort(b, newIndex, byteOrder);
    }

    /**
     * {@inheritDoc}
     *
     * @return 2
     */
    @Override
    int getLength() {
        return FieldType.SSHORT.getLength();
    }

}
