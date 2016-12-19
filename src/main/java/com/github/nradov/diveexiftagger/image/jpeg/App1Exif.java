package com.github.nradov.diveexiftagger.image.jpeg;

import static com.github.nradov.diveexiftagger.image.jpeg.Utilities.convertToInt;
import static com.github.nradov.diveexiftagger.image.jpeg.Utilities.convertToShort;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class App1Exif extends App1Contents {

    static final byte[] EXIF = { 'E', 'x', 'i', 'f', 0, 0 };

    private static final class ByteOrderConstants {

        private static final String LITTLE_ENDIAN = "II";
        private static final String BIG_ENDIAN = "MM";

    }

    private static final short VERSION_NUMBER = 42;

    private ByteOrder byteOrder;

    private final List<ImageFileDirectory> ifds = new ArrayList<>(2);

    App1Exif(final byte[] content) throws IOException {
        // http://www.fileformat.info/format/tiff/corion.htm
        int index = 0;
        final String byteOrder = new String(content, index,
                ByteOrderConstants.LITTLE_ENDIAN.length(),
                StandardCharsets.US_ASCII);
        index += ByteOrderConstants.LITTLE_ENDIAN.length();
        if (ByteOrderConstants.LITTLE_ENDIAN.equals(byteOrder)) {
            this.byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else if (ByteOrderConstants.BIG_ENDIAN.equals(byteOrder)) {
            this.byteOrder = ByteOrder.BIG_ENDIAN;
        } else {
            throw new IllegalArgumentException(
                    "unexpected byte order: " + byteOrder);
        }
        final short versionNumber = convertToShort(content, index,
                this.byteOrder);
        index += java.lang.Short.BYTES;
        if (versionNumber != VERSION_NUMBER) {
            throw new IllegalArgumentException(
                    "unexpected version number: " + versionNumber);
        }
        final int offsetOfIfd = convertToInt(content, index, this.byteOrder);
        index += Integer.BYTES;
        ifds.add(new ImageFileDirectory(content, offsetOfIfd, this.byteOrder));
    }

    ByteOrder getByteOrder() {
        return byteOrder;
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
            throw new IllegalStateException(
                    "unexpected byte order: " + byteOrder);
        }
        dst.put(byteOrderString.getBytes(StandardCharsets.US_ASCII));
        bytes += byteOrderString.length();
        for (final ImageFileDirectory dir : ifds) {
            bytes += dir.read(dst);
        }
        return bytes;
    }

    @Override
    public Optional<Rational> getFieldRational(final FieldTag tag) {
        for (final ImageFileDirectory ifd : ifds) {
            final Optional<Rational> o = ifd.getFieldRational(tag);
            if (o.isPresent()) {
                return o;
            }
        }
        return Optional.empty();
    }

    @Override
    int getLength() {
        int length = EXIF.length + ByteOrderConstants.LITTLE_ENDIAN.length();
        for (final ImageFileDirectory dir : ifds) {
            length += dir.getLength();
        }
        return length;
    }

}
