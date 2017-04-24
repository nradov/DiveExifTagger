package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * Start Of Frame (Baseline DCT).
 *
 * @author Nick Radov
 */
class StartOfFrameBaseline extends ImmutableByteSegment {

    static final short MARKER = (short) 0xFFC0;

    StartOfFrameBaseline(final SeekableByteChannel channel) throws IOException {
        super(channel);
    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
