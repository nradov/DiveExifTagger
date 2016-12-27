package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteOrder;

class SRational extends DataType {

    private final SLong numerator;
    private final SLong denominator;

    SRational(final byte[] b, final int index, final ByteOrder byteOrder) {
        this.numerator = new SLong(b, index, byteOrder);
        this.denominator = new SLong(b, index + FieldType.SLONG.getLength(),
                byteOrder);
    }

    /**
     * {@inheritDoc}
     *
     * @return 8
     */
    @Override
    int getLength() {
        return FieldType.SRATIONAL.getLength();
    }

}
