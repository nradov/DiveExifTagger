package com.github.nradov.diveexiftagger.image.jpeg;

import static com.github.nradov.diveexiftagger.image.jpeg.Utilities.convertToInt;
import static com.github.nradov.diveexiftagger.image.jpeg.Utilities.convertToShort;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * TIFF image file directory (IFD).
 *
 * @author Nick Radov
 */
class ImageFileDirectory extends ContainsField implements ReadableByteChannel {

    private final List<DirectoryEntry> directoryEntries;
    private ImageFileDirectory exif;
    private ImageFileDirectory gps;
    private final int nextIfdOffset;

    ImageFileDirectory(final byte[] b, final int offset,
            final ByteOrder byteOrder) {
        int index = offset;
        final short numberOfDirectoryEntries = convertToShort(b, index,
                byteOrder);
        index += java.lang.Short.BYTES;
        directoryEntries = new ArrayList<>(numberOfDirectoryEntries);
        while (directoryEntries.size() < numberOfDirectoryEntries) {
            final DirectoryEntry entry = new DirectoryEntry(b, index,
                    byteOrder);
            index += DirectoryEntry.BYTES;
            directoryEntries.add(entry);
        }
        nextIfdOffset = convertToInt(b, index, byteOrder);
        for (final DirectoryEntry entry : directoryEntries) {
            if (entry.getTag().equals(FieldTag.ExifIfdPointer)) {
                exif = new ImageFileDirectory(b,
                        entry.getValueLong().get(0).toInt(), byteOrder);
            } else if (entry.getTag().equals(FieldTag.GpsInfoIfdPointer)) {
                gps = new ImageFileDirectory(b,
                        entry.getValueLong().get(0).toInt(), byteOrder);
            }
        }
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    List<DirectoryEntry> getDirectoryEntries() {
        return Collections.unmodifiableList(directoryEntries);
    }

    @Override
    @Nonnull
    public <T extends DataType> Optional<List<T>> getField(
            @Nonnull final FieldTag tag, @Nonnull final Class<T> clazz) {
        for (final DirectoryEntry entry : directoryEntries) {
            if (entry.getTag().equals(tag)) {
                return Optional.of(entry.getValue(clazz));
            }
        }
        if (exif != null) {
            final Optional<List<T>> exifValue = exif.getField(tag, clazz);
            if (exifValue.isPresent()) {
                return exifValue;
            }
        }
        if (gps != null) {
            final Optional<List<T>> gpsValue = gps.getField(tag, clazz);
            if (gpsValue.isPresent()) {
                return gpsValue;
            }
        }
        return Optional.empty();
    }

    @Override
    @Nonnull
    public Optional<List<Rational>> getFieldRational(
            final @Nonnull FieldTag tag) {
        return getField(tag, Rational.class);
    }

    int getLength() {
        int length = 0;
        for (final DirectoryEntry e : directoryEntries) {
            length += DirectoryEntry.BYTES;
            if (e.getCount() * e.getType().getLength() > Integer.BYTES) {
                // actual value is offset
                length += e.getCount() * e.getType().getLength();
            }
        }
        length += exif == null ? 0 : exif.getLength();
        length += gps == null ? 0 : gps.getLength();
        length += Integer.BYTES;
        return length;
    }

    int getNextIfdOffset() {
        return nextIfdOffset;
    }

    short getNumberOfDirectoryEntries() {
        return (short) directoryEntries.size();
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        int bytes = 0;
        if (directoryEntries.size() > java.lang.Short.MAX_VALUE) {
            throw new IllegalStateException("too many directory entries");
        }
        dst.putShort((short) directoryEntries.size());
        bytes += java.lang.Short.BYTES;
        for (final DirectoryEntry entry : directoryEntries) {
            dst.putShort(entry.getTag().getTag());
            bytes += java.lang.Short.BYTES;
            dst.putShort(entry.getType().getType());
            bytes += java.lang.Short.BYTES;
            dst.putShort(entry.getCount());
            bytes += java.lang.Short.BYTES;
            if (entry.getCount()
                    * entry.getType().getLength() <= Integer.BYTES) {
                // value directly in the entry
                for (short i = 0; i < entry.getCount(); i++) {
                    switch (entry.getType()) {
                    case BYTE:
                        dst.put(entry.getValueByte().get(i).getValue());
                        break;
                    default:
                        throw new IllegalStateException(
                                "unsupported data type " + entry.getType());
                    }
                    bytes += entry.getType().getLength();
                }
                // padding
                for (int i = entry.getCount()
                        * entry.getType().getLength(); i < Integer.BYTES; i++) {
                    dst.put((byte) 0);
                    bytes += java.lang.Byte.BYTES;
                }
            } else {
                // offset
                throw new UnsupportedOperationException("not implemented yet");
            }
            bytes += Integer.BYTES;
        }
        if (exif != null) {
            bytes += exif.read(dst);
        }
        if (gps != null) {
            bytes += gps.read(dst);
        }
        dst.putInt(nextIfdOffset);
        bytes += Integer.BYTES;
        return bytes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final DirectoryEntry field : getDirectoryEntries()) {
            sb.append(field);
            sb.append(Utilities.LINE_SEPARATOR);
        }
        return sb.toString();
    }

}
