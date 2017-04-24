package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

class StartOfFrameProgressive extends ImmutableByteSegment {

    static final short MARKER = (short) 0xFFC2;

    StartOfFrameProgressive(final SeekableByteChannel channel)
            throws IOException {
        super(channel);
    }

    @Override
    short getMarker() {
        return MARKER;
    }

    @Override
    int getLength() {
        throw new UnsupportedOperationException("not implemented yet");
    }

}
