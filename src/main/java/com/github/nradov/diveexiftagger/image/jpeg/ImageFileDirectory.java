package com.github.nradov.diveexiftagger.image.jpeg;

import static com.github.nradov.diveexiftagger.image.jpeg.TiffUtilities.convertToInt;
import static com.github.nradov.diveexiftagger.image.jpeg.TiffUtilities.convertToShort;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TIFF image file directory (IFD).
 *
 * @author Nick Radov
 */
class ImageFileDirectory implements ReadableByteChannel {

    private final List<TiffDirectoryEntry> directoryEntries;
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
                .size() < numberOfDirectoryEntries; index += TiffDirectoryEntry.BYTES) {
            final TiffDirectoryEntry entry = new TiffDirectoryEntry(b, index,
                    byteOrder);
            directoryEntries.add(entry);
        }
        nextIfdOffset = convertToInt(b, index, byteOrder);
        for (final TiffDirectoryEntry entry : directoryEntries) {
            if (entry.getTag().equals(TiffFieldTag.ExifIfdPointer)) {
                exif = new ImageFileDirectory(b, entry.getValueLong(),
                        byteOrder);
            } else if (entry.getTag().equals(TiffFieldTag.GpsInfoIfdPointer)) {
                gps = new ImageFileDirectory(b, entry.getValueLong(),
                        byteOrder);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final TiffDirectoryEntry field : getDirectoryEntries()) {
            sb.append(field);
            sb.append(TiffUtilities.LINE_SEPARATOR);
        }
        return sb.toString();
    }

    short getNumberOfDirectoryEntries() {
        return (short) directoryEntries.size();
    }

    List<TiffDirectoryEntry> getDirectoryEntries() {
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
        if (directoryEntries.size() > Short.MAX_VALUE) {
            throw new IllegalStateException("too many directory entries");
        }
        dst.putShort((short) directoryEntries.size());
        bytes += Short.BYTES;
        for (final TiffDirectoryEntry entry : directoryEntries) {
            bytes += entry.read(dst);
        }
        dst.putInt(nextIfdOffset);
        bytes += Integer.BYTES;
        return bytes;
    }

}
