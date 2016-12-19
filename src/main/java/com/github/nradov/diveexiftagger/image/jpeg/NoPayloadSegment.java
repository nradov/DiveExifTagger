package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public abstract class NoPayloadSegment extends Segment {

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        dst.putShort(getMarker());
        return getLength();
    }

    @Override
    void populate(final SeekableByteChannel channel) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @return 2
     */
    @Override
    public int getLength() {
        return java.lang.Short.BYTES;
    }

}
