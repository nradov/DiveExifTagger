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
 * @see <a target="_" title="Adobe Developers Association" href=
 *      "https://partners.adobe.com/public/developer/en/tiff/TIFF6.pdf">TIFF
 *      Revision 6.0</a>
 * @author Nick Radov
 */
class TiffImageFileDirectory implements ReadableByteChannel {

    private final List<TiffDirectoryEntry> directoryEntries;
    private TiffImageFileDirectory exif;
    private TiffImageFileDirectory gps;
    private final int nextIfdOffset;

    TiffImageFileDirectory(final byte[] b, final int offset,
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
                exif = new TiffImageFileDirectory(b, entry.getValueLong(),
                        byteOrder);
            } else if (entry.getTag().equals(TiffFieldTag.GpsInfoIfdPointer)) {
                gps = new TiffImageFileDirectory(b, entry.getValueLong(),
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
        for (final TiffDirectoryEntry entry : directoryEntries) {
            bytes += entry.read(dst);
        }
        return bytes;
    }

}
