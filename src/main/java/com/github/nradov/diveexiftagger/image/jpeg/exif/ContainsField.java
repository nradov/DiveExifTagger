package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * May contain searchable metadata fields.
 *
 * @author Nick Radov
 */
public abstract class ContainsField {

	// TODO: Add getter methods for the other field types.

	/**
	 * Search for a metadata field with a particular tag.
	 * 
	 * @param tag
	 *            metadata field tag
	 * @param clazz
	 *            expected field type
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public abstract <T extends DataType> Optional<List<T>> getField(FieldTag tag, Class<T> clazz);

	/**
	 * Search for a {@code Ascii} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<Ascii>> getFieldAscii(@Nonnull final FieldTag tag) {
		return getField(tag, Ascii.class);
	}

	/**
	 * Search for a {@code Byte} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<Byte>> getFieldByte(@Nonnull final FieldTag tag) {
		return getField(tag, Byte.class);
	}

	/**
	 * Search for a {@code Double} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<Double>> getFieldDouble(@Nonnull final FieldTag tag) {
		return getField(tag, Double.class);
	}

	/**
	 * Search for a {@code Float} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<Float>> getFieldFloat(@Nonnull final FieldTag tag) {
		return getField(tag, Float.class);
	}

	/**
	 * Search for a {@code Long} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<Long>> getFieldLong(@Nonnull final FieldTag tag) {
		return getField(tag, Long.class);
	}

	/**
	 * Search for a {@code Rational} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<Rational>> getFieldRational(@Nonnull final FieldTag tag) {
		return getField(tag, Rational.class);
	}

	/**
	 * Search for a {@code SByte} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<SByte>> getFieldSByte(@Nonnull final FieldTag tag) {
		return getField(tag, SByte.class);
	}

	/**
	 * Search for a {@code Short} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<Short>> getFieldShort(@Nonnull final FieldTag tag) {
		return getField(tag, Short.class);
	}

	/**
	 * Search for a {@code Rational} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<SLong>> getFieldSLong(@Nonnull final FieldTag tag) {
		return getField(tag, SLong.class);
	}

	/**
	 * Search for a {@code SRational} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<SRational>> getFieldSRational(@Nonnull final FieldTag tag) {
		return getField(tag, SRational.class);
	}

	/**
	 * Search for a {@code SShort} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<SShort>> getFieldSShort(@Nonnull final FieldTag tag) {
		return getField(tag, SShort.class);
	}

	/**
	 * Search for a {@code Undefined} metadata field with a particular tag.
	 *
	 * @param tag
	 *            metadata field tag
	 * @return any matching field values if they exist
	 * @throws IllegalArgumentException
	 *             if the field is the wrong type
	 */
	@Nonnull
	public Optional<List<Undefined>> getFieldUndefined(@Nonnull final FieldTag tag) {
		return getField(tag, Undefined.class);
	}

}
