package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

abstract class Segment extends ContainsField implements ReadableByteChannel {

	@Override
	public void close() throws IOException {
		// nothing to do
	}

	@Override
	@Nonnull
	public <T extends DataType> Optional<List<T>> getField(final FieldTag tag, final Class<T> clazz) {
		return Optional.empty();
	}

	@Override
	@Nonnull
	public Optional<List<Rational>> getFieldRational(final FieldTag tag) {
		return getField(tag, Rational.class);
	}

	/**
	 * Get the length of this segment in bytes, including the 2-byte marker.
	 *
	 * @return segment length in bytes
	 */
	abstract int getLength();

	/**
	 * Get the two-byte marker used to start this segment.
	 *
	 * @return marker which starts this segment
	 */
	abstract short getMarker();

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public int read(final ByteBuffer dst) throws IOException {
		dst.putShort(getMarker());
		return java.lang.Short.BYTES;
	}

	@Override
	public String toString() {
		final ByteBuffer b = ByteBuffer.allocate(getLength());
		try {
			if (read(b) != getLength()) {
				throw new IllegalStateException();
			}
		} catch (final IOException e) {
			// can't throw a checked exception here
			throw new IllegalStateException(e);
		}
		return new String(b.array(), StandardCharsets.ISO_8859_1);
	}

}
