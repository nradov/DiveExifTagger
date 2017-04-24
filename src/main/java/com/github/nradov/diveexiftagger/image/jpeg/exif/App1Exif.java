package com.github.nradov.diveexiftagger.image.jpeg.exif;

import static com.github.nradov.diveexiftagger.image.jpeg.exif.Utilities.convertToInt;
import static com.github.nradov.diveexiftagger.image.jpeg.exif.Utilities.convertToShort;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

class App1Exif extends App1Contents {

	private static final class ByteOrderConstants {

		private static final String LITTLE_ENDIAN = "II";
		private static final String BIG_ENDIAN = "MM";

	}

	static final byte[] EXIF = { 'E', 'x', 'i', 'f', 0, 0 };

	private static final short VERSION_NUMBER = 42;

	private ByteOrder byteOrder;

	private final List<ImageFileDirectory> ifds = new ArrayList<>(2);

	App1Exif(final byte[] content) throws IOException {
		// http://www.fileformat.info/format/tiff/corion.htm
		int index = 0;
		final String byteOrder = new String(content, index, ByteOrderConstants.LITTLE_ENDIAN.length(),
				StandardCharsets.US_ASCII);
		index += ByteOrderConstants.LITTLE_ENDIAN.length();
		if (ByteOrderConstants.LITTLE_ENDIAN.equals(byteOrder)) {
			this.byteOrder = ByteOrder.LITTLE_ENDIAN;
		} else if (ByteOrderConstants.BIG_ENDIAN.equals(byteOrder)) {
			this.byteOrder = ByteOrder.BIG_ENDIAN;
		} else {
			throw new IllegalArgumentException("unexpected byte order: " + byteOrder);
		}
		final short versionNumber = convertToShort(content, index, this.byteOrder);
		index += java.lang.Short.BYTES;
		if (versionNumber != VERSION_NUMBER) {
			throw new IllegalArgumentException("unexpected version number: " + versionNumber);
		}
		final int offsetOfIfd0 = convertToInt(content, index, this.byteOrder);
		index += Integer.BYTES;
		final ImageFileDirectory ifd0 = new ImageFileDirectory(content, offsetOfIfd0, this.byteOrder);
		index += ifd0.getLength();
		ifds.add(ifd0);
		final int offsetOfIfd1 = convertToInt(content, index, this.byteOrder);
		index += Integer.BYTES;
		final ImageFileDirectory ifd1 = new ImageFileDirectory(content, offsetOfIfd1, this.byteOrder);
		index += ifd1.getLength();
		ifds.add(ifd1);
		if (index != content.length) {
			throw new IllegalStateException("expected " + content.length + " bytes but read " + index);
		}
	}

	ByteOrder getByteOrder() {
		return byteOrder;
	}

	@Override
	@Nonnull
	public <T extends DataType> Optional<List<T>> getField(@Nonnull final FieldTag tag, @Nonnull final Class<T> clazz) {
		for (final ContainsField ifd : ifds) {
			final Optional<List<T>> o = ifd.getField(tag, clazz);
			if (o.isPresent()) {
				return o;
			}
		}
		return Optional.empty();
	}

	@Override
	int getLength() {
		int length =
				// Exif marker
				EXIF.length
						// byte order
						+ ByteOrderConstants.LITTLE_ENDIAN.length()
						// version
						+ java.lang.Short.BYTES
						// offset of IFD
						+ java.lang.Integer.BYTES;
		for (final ImageFileDirectory dir : ifds) {
			length += dir.getLength();
		}
		return length;
	}

	@Override
	public int read(final ByteBuffer dst) throws IOException {
		int bytes = 0;
		dst.put(EXIF);
		bytes += EXIF.length;
		final String byteOrderString;
		if (ByteOrder.LITTLE_ENDIAN.equals(byteOrder)) {
			byteOrderString = ByteOrderConstants.LITTLE_ENDIAN;
		} else if (ByteOrder.BIG_ENDIAN.equals(byteOrder)) {
			byteOrderString = ByteOrderConstants.BIG_ENDIAN;
		} else {
			throw new IllegalStateException("unexpected byte order: " + byteOrder);
		}
		dst.put(byteOrderString.getBytes(StandardCharsets.US_ASCII));
		bytes += byteOrderString.length();
		for (final ImageFileDirectory dir : ifds) {
			bytes += dir.read(dst);
		}
		return bytes;
	}

}
