package com.github.nradov.diveexiftagger.image.jpeg;

/**
 * Start of image.
 *
 * @author Nick Radov
 */
class StartOfImage extends NoPayloadSegment {

    static final short MARKER = (short) 0xFFD8;

    StartOfImage() {
    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
