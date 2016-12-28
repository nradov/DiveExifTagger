package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteOrder;

class SRational extends AbstractRational<SLong> {

    SRational(final byte[] b, final int index, final ByteOrder byteOrder) {
        super(new SLong(b, index, byteOrder),
                new SLong(b, index + FieldType.SLONG.getLength(), byteOrder));
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
