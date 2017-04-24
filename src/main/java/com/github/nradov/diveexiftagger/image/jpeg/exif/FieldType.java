package com.github.nradov.diveexiftagger.image.jpeg.exif;

import static com.github.nradov.diveexiftagger.image.jpeg.exif.Utilities.formatShortAsUnsignedHex;

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
enum FieldType {

    /** 8-bit unsigned integer. */
    BYTE(1, java.lang.Byte.BYTES, Byte.class, Byte::new),

    /**
     * 8-bit byte that contains a 7-bit ASCII code; the last byte must be NUL
     * (binary zero).
     */
    ASCII(2, java.lang.Byte.BYTES, Ascii.class, Ascii::new),

    /** 16-bit (2-byte) unsigned integer. */
    SHORT(3, java.lang.Short.BYTES, Short.class, Short::new),

    /** 32-bit (4-byte) unsigned integer. */
    LONG(4, Integer.BYTES, Long.class, Long::new),

    /**
     * Two {@link #LONG}s: the first represents the numerator of a fraction; the
     * second, the denominator.
     */
    RATIONAL(5, Integer.BYTES * 2, Rational.class, Rational::new),

    /** An 8-bit signed (twos-complement) integer. */
    SBYTE(6, java.lang.Byte.BYTES, SByte.class, SByte::new),

    /**
     * An 8-bit byte that may contain anything, depending on the definition of
     * the field.
     */
    UNDEFINED(7, java.lang.Byte.BYTES, Undefined.class, Undefined::new),

    /** A 16-bit (2-byte) signed (twos-complement) integer. */
    SSHORT(8, java.lang.Short.BYTES, SShort.class, SShort::new),

    /** A 32-bit (4-byte) signed (twos-complement) integer. */
    SLONG(9, Integer.BYTES, SLong.class, SLong::new),

    /**
     * Two {@link #SLONG}’s: the first represents the numerator of a fraction,
     * the second the denominator.
     */
    SRATIONAL(10, Integer.BYTES * 2, SRational.class, SRational::new),

    /** Single precision (4-byte) IEEE format. */
    FLOAT(11, java.lang.Float.BYTES, Float.class, Float::new),

    /** Double precision (8-byte) IEEE format. */
    DOUBLE(12, java.lang.Double.BYTES, Double.class, Double::new);

    @SuppressWarnings("serial")
    private static final Map<java.lang.Short, FieldType> MAP = Collections
            .unmodifiableMap(new HashMap<java.lang.Short, FieldType>() {
                {
                    for (final FieldType value : FieldType.values()) {
                        put(value.getType(), value);
                    }
                }
            });

    static FieldType valueOf(final short type) {
        if (MAP.containsKey(type)) {
            return MAP.get(type);
        } else {
            throw new IllegalArgumentException(
                    "unsupported type: " + formatShortAsUnsignedHex(type));
        }
    }

    private final short type;
    private final int length;

    private final Class<? extends DataType> valueClass;

    private final DataTypeSupplier supplier;

    /*
     * TODO: It seems like there should be a way to derive the DataTypeSupplier
     * object from the Class object but I can't figure out the right syntax.
     */
    private FieldType(final int type, final int length,
            final Class<? extends DataType> valueClass,
            final DataTypeSupplier supplier) {
        this.type = (short) type;
        this.length = length;
        this.valueClass = valueClass;
        this.supplier = supplier;
    }

    /**
     * Get the number of bytes per value.
     *
     * @return count of bytes in a single value of this data type
     */
    int getLength() {
        return length;
    }

    /**
     * Get a method reference to the constructor for the value class
     * corresponding to this data type.
     *
     * @return constructor reference
     */
    DataTypeSupplier getSupplier() {
        return supplier;
    }

    /**
     * Get the numeric directory entry type indicator.
     *
     * @return 1 &ndash; 10
     */
    short getType() {
        return type;
    }

    Class<? extends DataType> getValueClass() {
        return valueClass;
    }

}
