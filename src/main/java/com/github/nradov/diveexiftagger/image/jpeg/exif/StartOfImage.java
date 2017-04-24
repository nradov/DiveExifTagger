package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.nio.channels.SeekableByteChannel;

/**
 * Start of image.
 *
 * @author Nick Radov
 */
class StartOfImage extends NoPayloadSegment {

    static final short MARKER = (short) 0xFFD8;

    StartOfImage(final SeekableByteChannel channel) {
    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
