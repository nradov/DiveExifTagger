package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;

class StartOfImage extends Segment {

    static final short MARKER = (short) 0xFFD8;

    private static final int LENGTH = 2;

    StartOfImage() {
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        dst.putShort(MARKER);
        return LENGTH;
    }

    /**
     * {@inheritDoc}
     *
     * @return 2
     */
    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
