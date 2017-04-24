package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

final class Utilities {

    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private Utilities() {
        // do not instantiate
    }

    // http://stackoverflow.com/questions/5616052/how-can-i-convert-a-4-byte-array-to-an-integer
    static short convertToShort(final byte[] array, final int offset,
            final ByteOrder byteOrder) {
        return ByteBuffer.wrap(array, offset, java.lang.Short.BYTES)
                .order(byteOrder).getShort();
    }

    /**
     * Convert 4 bytes to an integer.
     *
     * @param array
     *            array containing the bytes to be converted
     * @param offset
     *            starting position within the array
     * @param byteOrder
     *            byte order (big or little endian)
     * @return the converted value
     */
    static int convertToInt(final byte[] array, final int offset,
            final ByteOrder byteOrder) {
        return ByteBuffer.wrap(array, offset, Integer.BYTES).order(byteOrder)
                .getInt();
    }

    static ByteBuffer convertToByteBuffer(final short value) {
        final ByteBuffer bb = ByteBuffer.allocate(java.lang.Short.BYTES);
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
        final String hex = "000"
                + Integer.toHexString(java.lang.Short.toUnsignedInt(s))
                        .toUpperCase(Locale.US);
        return "0x" + hex.substring(hex.length() - 4);
    }

    static String convertByteArrayToString(final byte[] b) {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < b.length; i++) {
            sb.append(java.lang.Byte.toUnsignedInt(b[i]));
            if (i < b.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }

}
