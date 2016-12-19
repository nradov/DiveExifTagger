package com.github.nradov.diveexiftagger.image.jpeg;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 32-bit (4-byte) unsigned integer.
 *
 * @author Nick Radov
 */
public class Long extends DataType {

    private final int value;

    public static final Long ONE = new Long(1);

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
        this.value = Integer.parseUnsignedInt(s);
    }

    public Long(final BigInteger value) {
        // TODO: deal with overflow
        this.value = value.intValue();
    }

    public BigInteger toBigInteger() {
        return BigInteger.valueOf(Integer.toUnsignedLong(value));
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(toBigInteger());
    }

    @Override
    public String toString() {
        return Integer.toUnsignedString(value);
    }

}
