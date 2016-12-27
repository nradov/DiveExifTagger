package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

abstract class Segment implements ReadableByteChannel {

    /**
     * Get the length of this segment in bytes, including the 2-byte marker.
     *
     * @return segment length in bytes
     */
    abstract int getLength();

    /**
     * Get the two-byte marker used to start this segment.
     *
     * @return marker which starts this segment
     */
    abstract short getMarker();

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // nothing to do
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        dst.putShort(getMarker());
        return java.lang.Short.BYTES;
    }

    public Optional<? extends DataType> getField(final FieldTag tag) {
        return Optional.empty();
    }

    public Optional<Rational> getFieldRational(final FieldTag tag) {
        return Optional.empty();
    }

    @Override
    public String toString() {
        final ByteBuffer b = ByteBuffer.allocate(getLength());
        try {
            if (read(b) != getLength()) {
                throw new IllegalStateException();
            }
        } catch (final IOException e) {
            // can't throw a checked exception here
            throw new IllegalStateException(e);
        }
        return new String(b.array(), StandardCharsets.ISO_8859_1);
    }

}
