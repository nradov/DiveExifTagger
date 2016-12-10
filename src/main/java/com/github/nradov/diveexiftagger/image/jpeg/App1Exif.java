package com.github.nradov.diveexiftagger.image.jpeg;

import static com.github.nradov.diveexiftagger.image.jpeg.TiffUtilities.convertToInt;
import static com.github.nradov.diveexiftagger.image.jpeg.TiffUtilities.convertToShort;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class App1Exif extends App1Contents {

    static final byte[] EXIF = { 'E', 'x', 'i', 'f', 0, 0 };

    private static final String BYTE_ORDER_LITTLE_ENDIAN = "II";

    private static final String BYTE_ORDER_BIG_ENDIAN = "MM";

    private static final short VERSION_NUMBER = 42;

    private ByteOrder byteOrder;

    private final List<TiffImageFileDirectory> ifds = new ArrayList<>(2);

    App1Exif(final byte[] content) throws IOException {
        // http://www.fileformat.info/format/tiff/corion.htm
        int index = 0;
        final String byteOrder = new String(content, index,
                BYTE_ORDER_LITTLE_ENDIAN.length(), StandardCharsets.US_ASCII);
        index += BYTE_ORDER_LITTLE_ENDIAN.length();
        if (BYTE_ORDER_LITTLE_ENDIAN.equals(byteOrder)) {
            this.byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else if (BYTE_ORDER_BIG_ENDIAN.equals(byteOrder)) {
            this.byteOrder = ByteOrder.BIG_ENDIAN;
        } else {
            throw new IllegalArgumentException(
                    "unexpected byte order: " + byteOrder);
        }
        final short versionNumber = convertToShort(content, index,
                this.byteOrder);
        index += Short.BYTES;
        if (versionNumber != VERSION_NUMBER) {
            throw new IllegalArgumentException(
                    "unexpected version number: " + versionNumber);
        }
        final int offsetOfIfd = convertToInt(content, index, this.byteOrder);
        index += Integer.BYTES;
        ifds.add(new TiffImageFileDirectory(content, offsetOfIfd,
                this.byteOrder));
    }

    ByteOrder getByteOrder() {
        return byteOrder;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        int bytes = 0;
        dst.put(EXIF);
        bytes += EXIF.length;
        // TODO: put the endian
        for (final TiffImageFileDirectory dir : ifds) {
            bytes += dir.read(dst);
        }
        return bytes;
    }

}
