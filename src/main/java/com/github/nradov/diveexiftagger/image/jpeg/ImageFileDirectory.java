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

/**
 * TIFF image file directory (IFD).
 *
 * @author Nick Radov
 */
class ImageFileDirectory implements ReadableByteChannel {

    private final List<DirectoryEntry> directoryEntries;
    private ImageFileDirectory exif;
    private ImageFileDirectory gps;
    private final int nextIfdOffset;

    ImageFileDirectory(final byte[] b, final int offset,
            final ByteOrder byteOrder) {
        int index = offset;
        final short numberOfDirectoryEntries = convertToShort(b, index,
                byteOrder);
        directoryEntries = new ArrayList<>(numberOfDirectoryEntries);
        for (index += 2; directoryEntries
                .size() < numberOfDirectoryEntries; index += DirectoryEntry.BYTES) {
            final DirectoryEntry entry = new DirectoryEntry(b, index,
                    byteOrder);
            directoryEntries.add(entry);
        }
        nextIfdOffset = convertToInt(b, index, byteOrder);
        for (final DirectoryEntry entry : directoryEntries) {
            if (entry.getTag().equals(FieldTag.ExifIfdPointer)) {
                exif = new ImageFileDirectory(b, entry.getValueLong(),
                        byteOrder);
            } else if (entry.getTag().equals(FieldTag.GpsInfoIfdPointer)) {
                gps = new ImageFileDirectory(b, entry.getValueLong(),
                        byteOrder);
            }
        }
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

    short getNumberOfDirectoryEntries() {
        return (short) directoryEntries.size();
    }

    List<DirectoryEntry> getDirectoryEntries() {
        return Collections.unmodifiableList(directoryEntries);
    }

    int getNextIfdOffset() {
        return nextIfdOffset;
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
        int bytes = 0;
        if (directoryEntries.size() > java.lang.Short.MAX_VALUE) {
            throw new IllegalStateException("too many directory entries");
        }
        dst.putShort((short) directoryEntries.size());
        bytes += java.lang.Short.BYTES;
        for (final DirectoryEntry entry : directoryEntries) {
            bytes += entry.read(dst);
        }
        dst.putInt(nextIfdOffset);
        bytes += Integer.BYTES;
        return bytes;
    }

    public Optional<Rational> getFieldRational(final FieldTag tag) {
        for (final DirectoryEntry entry : directoryEntries) {
            if (entry.getTag().equals(tag)) {
                return Optional.of(entry.getValueRational());
            }
        }
        if (exif != null) {
            final Optional<Rational> exifValue = exif.getFieldRational(tag);
            if (exifValue.isPresent()) {
                return exifValue;
            }
        }
        if (gps != null) {
            final Optional<Rational> gpsValue = gps.getFieldRational(tag);
            if (gpsValue.isPresent()) {
                return gpsValue;
            }
        }
        return Optional.empty();
    }

    int getLength() {
        throw new UnsupportedOperationException("not implemented yet");
    }

}
