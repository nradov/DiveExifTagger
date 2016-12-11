package com.github.nradov.diveexiftagger.image.jpeg;

import static com.github.nradov.diveexiftagger.image.jpeg.TiffUtilities.convertToBytes;
import static com.github.nradov.diveexiftagger.image.jpeg.TiffUtilities.convertToInt;
import static com.github.nradov.diveexiftagger.image.jpeg.TiffUtilities.convertToShort;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class TiffDirectoryEntry implements ReadableByteChannel {

    private final byte[] tiff;
    private final ByteOrder byteOrder;
    private final short tag;
    private final short type;
    private final int count;
    private final int valueOrOffset;

    static final int BYTES = Short.BYTES + Short.BYTES + Integer.BYTES
            + Integer.BYTES;

    TiffDirectoryEntry(final byte[] tiff, final int index,
            final ByteOrder byteOrder) {
        this.tiff = tiff;
        this.byteOrder = byteOrder;
        int newIndex = index;
        tag = convertToShort(tiff, newIndex, byteOrder);
        newIndex += Short.BYTES;
        type = convertToShort(tiff, newIndex, byteOrder);
        newIndex += Short.BYTES;
        count = convertToInt(tiff, newIndex, byteOrder);
        newIndex += Integer.BYTES;
        valueOrOffset = convertToInt(tiff, newIndex, byteOrder);
        System.err.println(this);
    }

    TiffFieldTag getTag() {
        return TiffFieldTag.valueOf(tag);
    }

    TiffFieldType getType() {
        return TiffFieldType.valueOf(type);
    }

    int getCount() {
        return count;
    }

    int getValueOrOffset() {
        return valueOrOffset;
    }

    byte[] getValueByte() {
        if (getCount() <= Integer.BYTES) {
            final byte[] b = convertToBytes(getValueOrOffset(), byteOrder);
            if (b.length > getCount()) {
                return Arrays.copyOfRange(b, 0, getCount());
            } else {
                return b;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    String getValueAscii() {
        if (TiffFieldType.ASCII.equals(getType())) {
            return new String(tiff, valueOrOffset, count,
                    StandardCharsets.ISO_8859_1);
        } else {
            throw new UnsupportedOperationException(
                    "invalid field type: " + getType());
        }
    }

    String getValueUndefined() {
        final String s;
        if (TiffFieldType.UNDEFINED.equals(getType())) {
            if (getCount() <= Integer.BYTES) {
                final byte[] b = convertToBytes(getValueOrOffset(), byteOrder);
                s = new String(b, StandardCharsets.ISO_8859_1);
            } else {
                s = new String(tiff, valueOrOffset, count,
                        StandardCharsets.ISO_8859_1);
            }
            if (s.length() > getCount()) {
                return s.substring(0, getCount());
            }
            return s;
        } else {
            throw new UnsupportedOperationException(
                    "invalid field type: " + getType());
        }
    }

    short getValueShort() {
        if (TiffFieldType.SHORT.equals(getType())) {
            return (short) valueOrOffset;
        } else {
            throw new UnsupportedOperationException(
                    "invalid field type: " + getType());
        }
    }

    int getValueLong() {
        if (TiffFieldType.LONG.equals(getType())) {
            return valueOrOffset;
        } else {
            throw new UnsupportedOperationException(
                    "invalid field type: " + getType());
        }
    }

    int getValueRationalNumerator() {
        if (TiffFieldType.RATIONAL.equals(getType())
                || TiffFieldType.SRATIONAL.equals(getType())) {
            // TODO: should be long
            return convertToInt(tiff, valueOrOffset, byteOrder);
        } else {
            throw new UnsupportedOperationException(
                    "invalid field type: " + getType());
        }
    }

    int getValueRationalDenominator() {
        if (TiffFieldType.RATIONAL.equals(getType())
                || TiffFieldType.SRATIONAL.equals(getType())) {
            return convertToInt(tiff, valueOrOffset + Integer.BYTES, byteOrder);
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
        switch (getType()) {
        case ASCII:
            sb.append('"').append(getValueAscii()).append('"');
            break;
        case BYTE:
            sb.append(convertByteArrayToString(getValueByte()));
            break;
        case SHORT:
            sb.append(Short.toUnsignedInt(getValueShort()));
            break;
        case LONG:
            sb.append(Integer.toUnsignedString(getValueLong()));
            break;
        case RATIONAL:
        case SRATIONAL:
            sb.append(Integer.toUnsignedString(getValueRationalNumerator()))
                    .append('/').append(Integer
                            .toUnsignedString(getValueRationalDenominator()));
            break;
        case UNDEFINED:
            sb.append('"').append(getValueUndefined()).append('"');
            break;
        default:
            throw new UnsupportedOperationException(
                    "unsupported field type: " + getType());
        }
        return sb.toString();
    }

    private static String convertByteArrayToString(final byte[] b) {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < b.length; i++) {
            sb.append(Byte.toUnsignedInt(b[i]));
            if (i < b.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        dst.putShort(tag);
        dst.putShort(type);
        dst.putInt(count);
        dst.putInt(valueOrOffset);
        return BYTES;
    }

}
