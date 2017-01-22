package com.github.nradov.diveexiftagger.image.jpeg;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * 32-bit (4-byte) unsigned integer.
 *
 * @author Nick Radov
 */
class Long extends IntegralDataType {

    static final Long ONE = new Long(1);

    private final int value;

    public Long(final BigInteger value) {
        // TODO: deal with overflow
        this.value = value.intValue();
    }

    Long(final byte[] array, final int offset, final ByteOrder byteOrder) {
        value = Utilities.convertToInt(array, offset, byteOrder);
    }

    /**
     * Create a new {@code Long}.
     *
     * @param value
     *            a 32-bit value, treated as <strong>unsigned</strong>
     */
    public Long(final int value) {
        this.value = value;
    }

    public Long(final long value) {
        // TODO: handle overflow
        this((int) value);
    }

    public Long(final String s) {
        value = Integer.parseUnsignedInt(s);
    }

    @Override
    int getLength() {
        return FieldType.LONG.getLength();
    }

    BigDecimal toBigDecimal() {
        return new BigDecimal(toBigInteger());
    }

    BigInteger toBigInteger() {
        return BigInteger.valueOf(Integer.toUnsignedLong(value));
    }

    int toInt() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toUnsignedString(value);
    }

}
