package com.github.nradov.diveexiftagger.image.jpeg;

import static com.github.nradov.diveexiftagger.image.jpeg.TiffUtilities.formatShortAsUnsignedHex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The field types and their sizes.
 *
 * @see <a target="_" title="Adobe Developers Association" href=
 *      "https://partners.adobe.com/public/developer/en/tiff/TIFF6.pdf">TIFF
 *      Revision 6.0</a> Section 2 page 15
 * @author Nick Radov
 */
enum TiffFieldType {

    /** 8-bit unsigned integer. */
    BYTE(1, Byte.BYTES),

    /**
     * 8-bit byte that contains a 7-bit ASCII code; the last byte must be NUL
     * (binary zero).
     */
    ASCII(2, Byte.BYTES),

    /** 16-bit (2-byte) unsigned integer. */
    SHORT(3, Short.BYTES),

    /** 32-bit (4-byte) unsigned integer. */
    LONG(4, Integer.BYTES),

    /**
     * Two {@link #LONG}s: the first represents the numerator of a fraction; the
     * second, the denominator.
     */
    RATIONAL(5, Integer.BYTES * 2),

    /** An 8-bit signed (twos-complement) integer. */
    SBYTE(6, Byte.BYTES),

    /**
     * An 8-bit byte that may contain anything, depending on the definition of
     * the field.
     */
    UNDEFINED(7, Byte.BYTES),

    /** A 16-bit (2-byte) signed (twos-complement) integer. */
    SSHORT(8, Short.BYTES),

    /** A 32-bit (4-byte) signed (twos-complement) integer. */
    SLONG(9, Integer.BYTES),

    /**
     * Two {@link #SLONG}’s: the first represents the numerator of a fraction,
     * the second the denominator.
     */
    SRATIONAL(10, Integer.BYTES * 2),

    /** Single precision (4-byte) IEEE format. */
    FLOAT(11, Float.BYTES),

    /** Double precision (8-byte) IEEE format. */
    DOUBLE(12, Double.BYTES);

    private final short type;
    private int length;

    private TiffFieldType(final int type, final int length) {
        this.type = (short) type;
        this.length = length;
    }

    short getType() {
        return type;
    }

    int getLength() {
        return length;
    }

    @SuppressWarnings("serial")
    private static final Map<Short, TiffFieldType> MAP = Collections
            .unmodifiableMap(new HashMap<Short, TiffFieldType>() {
                {
                    for (final TiffFieldType value : TiffFieldType.values()) {
                        put(value.getType(), value);
                    }
                }
            });

    static TiffFieldType valueOf(final short type) {
        if (MAP.containsKey(type)) {
            return MAP.get(type);
        } else {
            throw new IllegalArgumentException(
                    "unsupported type: " + formatShortAsUnsignedHex(type));
        }
    }

}
