package com.github.nradov.diveexiftagger.image.jpeg.exif;

import static com.github.nradov.diveexiftagger.image.jpeg.exif.Utilities.convertToInt;
import static com.github.nradov.diveexiftagger.image.jpeg.exif.Utilities.convertToShort;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.github.nradov.diveexiftagger.image.jpeg.exif.FieldTag.Proprietary;

/**
 * Single metadata field in an {@link ImageFileDirectory}.
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

	private final ImageFileDirectory makerNote;

	@SuppressWarnings("serial")
	private static final Map<FieldType, DataTypeSupplier> DATA_TYPE_CTOR_MAP = Collections
			.unmodifiableMap(new HashMap<FieldType, DataTypeSupplier>() {
				{
					for (final FieldType fieldType : EnumSet.allOf(FieldType.class)) {
						put(fieldType, fieldType.getSupplier());
					}
				}
			});
	@SuppressWarnings("serial")
	private static final Map<FieldType, Object> DATA_TYPE_TOSTRING_MAP = Collections
			.unmodifiableMap(new HashMap<FieldType, Object>() {
				{
					for (final FieldType fieldType : EnumSet.allOf(FieldType.class)) {
						put(fieldType, fieldType.getSupplier());
					}
				}
			});
	private final FieldTag tag;

	private final FieldType type;

	private final List<DataType> value;

	DirectoryEntry(final byte[] tiff, final int index, final ByteOrder byteOrder, final Proprietary proprietary) {
		int newIndex = index;
		tag = FieldTag.valueOf(proprietary, convertToShort(tiff, newIndex, byteOrder));
		newIndex += java.lang.Short.BYTES;
		type = FieldType.valueOf(convertToShort(tiff, newIndex, byteOrder));
		newIndex += java.lang.Short.BYTES;
		final DataTypeSupplier supplier;
		if (DATA_TYPE_CTOR_MAP.containsKey(type)) {
			supplier = DATA_TYPE_CTOR_MAP.get(type);
		} else {
			throw new IllegalStateException("no constructor map entry for " + type);
		}
		final int count = convertToInt(tiff, newIndex, byteOrder);
		newIndex += Integer.BYTES;
		if (getTag().equals(FieldTag.MakerNote)) {
			value = null;
			// offset
			final int offset = convertToInt(tiff, newIndex, byteOrder);
			newIndex += Integer.BYTES;
			makerNote = new CanonMakerNote(tiff, offset, byteOrder);
		} else {
			makerNote = null;
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
		}

		System.err.println(this);
	}

	/**
	 * Get the number of values. It should be noted carefully that the count is
	 * not the sum of the bytes. In the case of one value of {@link Short} (16
	 * bits), for example, the count is '1' even though it is 2 Bytes.
	 *
	 * @return number of values
	 */
	short getCount() {
		if (makerNote == null) {
			if (value == null) {
				throw new IllegalStateException("no value");
			}
			return (short) value.size();
		} else {
			return (short) makerNote.getLength();
		}
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

	List<? super DataType> getValue() {
		return value;
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	<T extends DataType> List<T> getValue(@Nonnull final Class<T> clazz) {
		if (clazz.equals(getType().getValueClass())) {
			return (List<T>) value;
		} else {
			throw new UnsupportedOperationException("invalid field type: " + getType());
		}
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	<T extends DataType> List<T> getValue(final FieldType expected) {
		return (List<T>) getValue(expected.getValueClass());
	}

	List<Ascii> getValueAscii() {
		return getValue(FieldType.ASCII);
	}

	List<Byte> getValueByte() {
		return getValue(FieldType.BYTE);
	}

	List<Long> getValueLong() {
		return getValue(FieldType.LONG);
	}

	List<Rational> getValueRational() {
		return getValue(FieldType.RATIONAL);
	}

	List<Short> getValueShort() {
		return getValue(FieldType.SHORT);
	}

	List<Undefined> getValueUndefined() {
		return getValue(FieldType.UNDEFINED);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getTag());
		sb.append(" (");
		sb.append(Utilities.formatShortAsUnsignedHex(getTag().getTag()));
		sb.append(") = ");
		if (getTag().equals(FieldTag.MakerNote)) {
			// special case for proprietary MakerNote tags
			// TODO: check for the camera manufacturer to determine the format
			sb.append(makerNote);
		} else {
			switch (getType()) {
			case ASCII:
				sb.append(Ascii.toStringAscii(getValueAscii()));
				break;
			default:
				sb.append(DataType.toString(value));
				break;
			}
		}
		return sb.toString();
	}

}
