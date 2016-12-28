package com.github.nradov.diveexiftagger.image.jpeg;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteOrder;
import java.text.DecimalFormat;

/**
 * Two {@link Long}s: the first represents the numerator of a fraction; the
 * second, the denominator.
 *
 * @author Nick Radov
 */
class Rational extends AbstractRational<Long> {

    public Rational(final byte[] b, final int index,
            final ByteOrder byteOrder) {
        super(new Long(b, index, byteOrder),
                new Long(b, index + FieldType.LONG.getLength(), byteOrder));
    }

    public Rational(final Long numerator, final Long denominator) {
        super(numerator, denominator);
    }

    private static final char DECIMAL_SEPARATOR = new DecimalFormat()
            .getDecimalFormatSymbols().getDecimalSeparator();

    public Rational(final long numerator, final long denominator) {
        super(new Long(numerator), new Long(denominator));
        if (numerator < 0 || denominator < 0) {
            throw new IllegalArgumentException("unsigned value");
        }
    }

    /**
     * Divide the numerator by the denominator to convert this rational number
     * into a single decimal value.
     *
     * @param roundingMode
     *            rounding mode to apply
     * @return numerator / denominator
     * @throws ArithmeticException
     *             if denominator is 0, or if the exact quotient does not have a
     *             terminating decimal expansion based on {@code roundingMode}
     * @see BigDecimal#divide(BigDecimal, RoundingMode)
     */
    public BigDecimal toBigDecimal(final RoundingMode roundingMode) {
        final BigDecimal numBD = getNumerator().toBigDecimal();
        final BigDecimal denBD = getDenominator().toBigDecimal();
        final int scale = Math.min(numBD.precision(), denBD.precision());
        return numBD.divide(denBD, scale, roundingMode);
    }

    /**
     * {@inheritDoc}
     *
     * @return 8
     */
    @Override
    int getLength() {
        return FieldType.RATIONAL.getLength();
    }

}
