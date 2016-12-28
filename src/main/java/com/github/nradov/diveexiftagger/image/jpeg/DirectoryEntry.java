package com.github.nradov.diveexiftagger.image.jpeg;

import static com.github.nradov.diveexiftagger.image.jpeg.Utilities.convertToInt;
import static com.github.nradov.diveexiftagger.image.jpeg.Utilities.convertToShort;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single metadata directory entry.
 *
 * @author Nick Radov
 */
/*
 * This class can't implement ReadableByteChannel because there isn't
 * necessarily a single length or stream of bytes. The actual value may be
 * offset.
 */
class DirectoryEntry {

    /**
     * Number of bytes in a directory entry field, not counting the offset value
     * (if any).
     */
    static final int BYTES = 12;

    private final FieldTag tag;
    private final FieldType type;
    private final List<DataType> value;

    @SuppressWarnings("serial")
    private static final Map<FieldType, DataTypeSupplier> DATA_TYPE_CTOR_MAP = Collections
            .unmodifiableMap(new HashMap<FieldType, DataTypeSupplier>() {
                {
                    for (final FieldType fieldType : EnumSet
                            .allOf(FieldType.class)) {
                        put(fieldType, fieldType.getSupplier());
                    }
                }
            });

    DirectoryEntry(final byte[] tiff, final int index,
            final ByteOrder byteOrder) {
        int newIndex = index;
        tag = FieldTag.valueOf(convertToShort(tiff, newIndex, byteOrder));
        newIndex += java.lang.Short.BYTES;
        type = FieldType.valueOf(convertToShort(tiff, newIndex, byteOrder));
        newIndex += java.lang.Short.BYTES;
        final DataTypeSupplier supplier;
        if (DATA_TYPE_CTOR_MAP.containsKey(type)) {
            supplier = DATA_TYPE_CTOR_MAP.get(type);
        } else {
            throw new IllegalStateException(
                    "no constructor map entry for " + type);
        }
        final int count = convertToInt(tiff, newIndex, byteOrder);
        newIndex += Integer.BYTES;
        value = new ArrayList<>(count);
        if (count * type.getLength() <= Integer.BYTES) {
            // value itself
            for (int i = 0; i < count; i++) {
                value.add(supplier.construct(tiff, newIndex, byteOrder));
                newIndex += type.getLength();
            }
        } else {
            // offset
            int offset = convertToInt(tiff, newIndex, byteOrder);
            newIndex += Integer.BYTES;
            for (int i = 0; i < count; i++) {
                value.add(supplier.construct(tiff, offset, byteOrder));
                offset += type.getLength();
            }
        }
        newIndex += Integer.BYTES;
        System.err.println(this);
    }

    /**
     * Get the field tag. Each tag is assigned a unique 2-byte number to
     * identify the field. The tag numbers in the Exif 0th IFD and 1st IFD are
     * all the same as the TIFF tag numbers.
     *
     * @return metadata field tag
     */
    FieldTag getTag() {
        return tag;
    }

    FieldType getType() {
        return type;
    }

    /**
     * Get the number of values. It should be noted carefully that the count is
     * not the sum of the bytes. In the case of one value of SHORT (16 bits),
     * for example, the count is '1' even though it is 2 Bytes.
     *
     * @return number of values
     */
    short getCount() {
        return (short) value.size();
    }

    List<? super DataType> getValue() {
        return value;
    }

    List<Ascii> getValueAscii() {
        return getValue(FieldType.ASCII);
    }

    List<Byte> getValueByte() {
        return getValue(FieldType.BYTE);
    }

    List<Short> getValueShort() {
        return getValue(FieldType.SHORT);
    }

    List<Long> getValueLong() {
        return getValue(FieldType.LONG);
    }

    List<Undefined> getValueUndefined() {
        return getValue(FieldType.UNDEFINED);
    }

    List<Rational> getValueRational() {
        return getValue(FieldType.RATIONAL);
    }

    @SuppressWarnings("unchecked")
    private <T extends DataType> List<T> getValue(final FieldType expected) {
        if (expected.equals(getType())) {
            return (List<T>) value;
        } else {
            throw new UnsupportedOperationException(
                    "invalid field type: " + getType());
        }
    }

    @SuppressWarnings("serial")
    private static final Map<FieldType, Object> DATA_TYPE_TOSTRING_MAP = Collections
            .unmodifiableMap(new HashMap<FieldType, Object>() {
                {
                    for (final FieldType fieldType : EnumSet
                            .allOf(FieldType.class)) {
                        put(fieldType, fieldType.getSupplier());
                    }
                }
            });

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getTag());
        sb.append(" = ");
        switch (getType()) {
        case ASCII:
            sb.append(Ascii.toStringAscii(getValueAscii()));
            break;
        default:
            sb.append(DataType.toString(value));
            break;
        }
        return sb.toString();
    }

}
