package com.github.nradov.diveexiftagger.image.jpeg;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Two {@link Long}s: the first represents the numerator of a fraction; the
 * second, the denominator.
 *
 * @author Nick Radov
 */
public class Rational extends DataType {

    private final Long numerator;
    private final Long denominator;

    public Rational(final Long numerator, final Long denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    private static final char DECIMAL_SEPARATOR = new DecimalFormat()
            .getDecimalFormatSymbols().getDecimalSeparator();

    public Rational(final long numerator, final long denominator) {
        if (numerator < 0 || denominator < 0) {
            throw new IllegalArgumentException("unsigned value");
        }
        this.numerator = new Long(numerator);
        this.denominator = new Long(denominator);
    }

    public Rational(final BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("unsigned value");
        }
        final String s = value.toPlainString();
        final int decimalIndex = s.indexOf(DECIMAL_SEPARATOR);
        if (decimalIndex == -1) {
            numerator = new Long(s);
            denominator = Long.ONE;
        } else {
            numerator = new Long(s.substring(0, DECIMAL_SEPARATOR)
                    .concat(s.substring(DECIMAL_SEPARATOR + 1)));
            denominator = new Long(BigInteger.valueOf(10)
                    .pow(s.length() - DECIMAL_SEPARATOR - 1));
        }

    }

    public Long getNumerator() {
        return numerator;
    }

    public Long getDenominator() {
        return denominator;
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
        final BigDecimal numBD = numerator.toBigDecimal();
        final BigDecimal denBD = denominator.toBigDecimal();
        return numBD.divide(denBD, numBD.precision() + denBD.precision(),
                roundingMode);
    }

}
