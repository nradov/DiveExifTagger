package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Segment containing only a marker and length.
 *
 * @author Nick Radov
 */
public abstract class NoPayloadSegment extends Segment {

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        dst.putShort(getMarker());
        return getLength();
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
