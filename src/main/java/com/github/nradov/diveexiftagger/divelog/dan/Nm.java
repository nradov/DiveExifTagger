package com.github.nradov.diveexiftagger.divelog.dan;

import java.math.BigDecimal;

/**
 * Numeric. A number represented as a series of ASCII numeric characters
 * consisting of an optional leading sign (+ or -), the digits and an optional
 * decimal point. In the absence of a sign, the number is assumed to be
 * positive. If there is no decimal point the number is assumed to be an
 * integer. Examples:
 *
 * <pre>
|999|

|-123.792|
 * </pre>
 *
 * Leading zeros, or trailing zeros after a decimal point, are not significant.
 * For example, the following two values with different representations, "01.20"
 * and "1.2", are identical. Except for the optional leading sign (+ or -) and
 * the optional decimal point (.), no non-numeric ASCII characters are allowed.
 * Thus, the value <12 should be encoded as a structured numeric (SN)
 * (preferred) or as a string (ST) (allowed, but not preferred) data type.
 *
 * @author Nick Radov
 */
interface Nm extends DataType {

    BigDecimal toBigDecimal();

}
