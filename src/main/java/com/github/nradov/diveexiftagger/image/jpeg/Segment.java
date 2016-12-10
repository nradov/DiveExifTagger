package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

import com.adobe.internal.xmp.XMPException;

abstract class Segment implements ReadableByteChannel {

    /**
     * Get the length of this segment in bytes, including the 2-byte marker.
     */
    abstract int getLength();

    /** Get the two-byte marker used to start this segment. */
    abstract short getMarker();

    void populate(final SeekableByteChannel channel)
            throws IOException, XMPException {
        // do nothing
    }

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
        return Short.BYTES;
    }

}
