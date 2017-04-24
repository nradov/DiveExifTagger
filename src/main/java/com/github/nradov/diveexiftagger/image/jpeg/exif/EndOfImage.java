package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.nio.channels.SeekableByteChannel;

/**
 * End of image.
 *
 * @author Nick Radov
 */
class EndOfImage extends NoPayloadSegment {

    static final short MARKER = (short) 0xFFD9;

    EndOfImage(final SeekableByteChannel channel) {
        // nothing to read
    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
