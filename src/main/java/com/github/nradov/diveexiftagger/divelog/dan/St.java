package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * String. String data is left justified with trailing blanks optional. Any
 * displayable (printable) ASCII characters (hexadecimal values between 20 and
 * 7E, inclusive, or ASCII decimal values between 32 and 126), except the
 * defined delimiter characters. Example:
 *
 * <pre>
 * |almost any data at all|
 * </pre>
 *
 * To include any DL7 delimiter character (except the segment terminator) within
 * a string data field, use the appropriate DL7 escape sequence. Usage note: the
 * ST data type is intended for short strings (e.g., less than 200 characters).
 * For longer strings the TX data types should be used.
 *
 * @author Nick Radov
 */
interface St extends DataType {

}
