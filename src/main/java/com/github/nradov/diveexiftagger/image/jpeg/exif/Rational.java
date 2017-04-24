package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteOrder;
import java.text.DecimalFormat;

import javax.annotation.Nonnull;

/**
 * Two {@link Long}s: the first represents the numerator of a fraction; the
 * second, the denominator.
 *
 * @author Nick Radov
 */
class Rational extends AbstractRational<Long> {

    private static final char DECIMAL_SEPARATOR = new DecimalFormat()
            .getDecimalFormatSymbols().getDecimalSeparator();

    /**
     * Create a new Rational based on binary data in a JPEG / EXIF file.
     *
     * @param b
     *            file data
     * @param index
     *            byte offset of the Rational value in {@code b}
     * @param byteOrder
     *            byte order of {@code b}
     */
    public Rational(final byte[] b, final int index,
            final ByteOrder byteOrder) {
        super(new Long(b, index, byteOrder),
                new Long(b, index + FieldType.LONG.getLength(), byteOrder));
    }

    /**
     * Create a new {@code Rational} from two {@code long}s.
     *
     * @param numerator
     *            numerator
     * @param denominator
     *            denominator
     * @throws IllegalArgumentException
     *             if either of the parameters is negative, or if
     *             {@code denominator} is 0
     */
    public Rational(final long numerator, final long denominator) {
        super(new Long(numerator), new Long(denominator));
        if (numerator < 0 || denominator < 0) {
            throw new IllegalArgumentException("unsigned value");
        }
        if (denominator == 0) {
            throw new IllegalArgumentException("denominator == 0");
        }
    }

    /**
     * Create a new {@code Rational} from two {@code Long}s.
     *
     * @param numerator
     *            numerator
     * @param denominator
     *            denominator
     */
    public Rational(@Nonnull final Long numerator,
            @Nonnull final Long denominator) {
        super(numerator, denominator);
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

}
