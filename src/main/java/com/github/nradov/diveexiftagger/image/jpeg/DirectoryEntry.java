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

    static final int BYTES = 12;

    private final byte[] tiff;
    private final ByteOrder byteOrder;
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
        this.tiff = tiff;
        this.byteOrder = byteOrder;
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

    List<? extends DataType> getValue() {
        return value;
    }

    Ascii getValueAscii() {
        return getFirstValue(FieldType.ASCII);
    }

    Byte getValueByte() {
        return getFirstValue(FieldType.BYTE);
    }

    Short getValueShort() {
        return getFirstValue(FieldType.SHORT);
    }

    Long getValueLong() {
        return getFirstValue(FieldType.LONG);
    }

    Undefined getValueUndefined() {
        return getFirstValue(FieldType.UNDEFINED);
    }

    Rational getValueRational() {
        return getFirstValue(FieldType.RATIONAL);
    }

    @SuppressWarnings("unchecked")
    private <T extends DataType> T getFirstValue(final FieldType expected) {
        if (expected.equals(getType())) {
            return (T) expected.getValueClass().cast(value.get(0));
        } else {
            throw new UnsupportedOperationException(
                    "invalid field type: " + getType());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getTag());
        sb.append(" = ");
        sb.append(value);
        return sb.toString();
    }

}
