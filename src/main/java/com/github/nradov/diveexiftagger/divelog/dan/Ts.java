package com.github.nradov.diveexiftagger.divelog.dan;

import java.time.Instant;

/**
 * Time stamp.
 * <p>
 * {@code Format: YYYY[MM[DD[HHMM[SS[.S[S[S[S]]]]]]]][+/-ZZZZ]^<degree of precision>}
 * </p>
 * Contains the exact time of an event, including the date and time. The date
 * portion of a time stamp follows the rules of a date field and the time
 * portion follows the rules of a time field. The specific data representations
 * used in the DL7 and HL7 encoding rules are compatible with ISO 8824-1987(E).
 *
 * In the current and future versions of DL7, the precision is indicated by
 * limiting the number of digits used, unless the optional second component is
 * present. Thus, YYYY is used to specify a precision of "year," YYYYMM
 * specifies a precision of "month," YYYYMMDD specifies a precision of "day,"
 * YYYYMMDDHH is used to specify a precision of "hour," YYYYMMDDHHMM is used to
 * specify a precision of "minute," YYYYMMDDHHMMSS is used to specify a
 * precision of seconds, and YYYYMMDDHHMMSS.SSSS is used to specify a precision
 * of ten thousandths of a second. In each of these cases, the time zone is an
 * optional component. Maximum length of the time stamp is 26. Examples:
 *
 * |19760704010159-0600| 1:01:59 on July 4, 1976 in the Eastern Standard Time
 * zone. |19760704010159-0500| 1:01:59 on July 4, 1976 in the Eastern Daylight
 * Saving Time zone. |198807050000| Midnight of the night extending from July 4
 * to July 5, 1988 in the local time zone of the sender.
 *
 * |19880705| Same as prior example, but precision extends only to the day.
 * Could be used for a birthdate, if the time of birth is unknown. Both the DL7
 * and HL7 Standards strongly recommends that all systems routinely send the
 * time zone offset but does not require it.. For PDE the time of interest is
 * the local time of the dive, local time of onset of symptoms and local time of
 * treatment. However, since the symptoms may occur with delay while diver is
 * travelling to another time zone, or diver get treated in another time zone,
 * it is important to record time zone offset.
 *
 * @author Nick Radov
 */
interface Ts extends DataType {

    Instant toInstant();

}
