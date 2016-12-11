package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

final class TiffUtilities {

    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private TiffUtilities() {
        // do not instantiate
    }

    // http://stackoverflow.com/questions/5616052/how-can-i-convert-a-4-byte-array-to-an-integer
    static short convertToShort(final byte[] array, final int offset,
            final ByteOrder byteOrder) {
        return ByteBuffer.wrap(array, offset, Short.BYTES).order(byteOrder)
                .getShort();
    }

    static int convertToInt(final byte[] array, final int offset,
            final ByteOrder byteOrder) {
        return ByteBuffer.wrap(array, offset, Integer.BYTES).order(byteOrder)
                .getInt();
    }

    static ByteBuffer convertToByteBuffer(final short value) {
        final ByteBuffer bb = ByteBuffer.allocate(Short.BYTES);
        bb.putShort(value);
        return bb;
    }

    static byte[] convertToBytes(final short value) {
        return convertToByteBuffer(value).array();
    }

    static byte[] convertToBytes(final int value, final ByteOrder byteOrder) {
        final ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        bb.order(byteOrder);
        bb.putInt(value);
        return bb.array();
    }

    static String formatShortAsUnsignedHex(final short s) {
        final String hex = "000" + Integer.toHexString(Short.toUnsignedInt(s))
                .toUpperCase(Locale.US);
        return "0x" + hex.substring(hex.length() - 4);
    }

}
