package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteOrder;

/**
 * An 8-bit byte that may take any value depending on the field definition.
 *
 * @author Nick Radov
 */
class Undefined extends DataType {

    private final byte value;

    Undefined(final byte[] b, final int index, final ByteOrder byteOrder) {
        value = b[index];
    }

    /**
     * {@inheritDoc}
     *
     * @return 1
     */
    @Override
    int getLength() {
        return FieldType.UNDEFINED.getLength();
    }

}
