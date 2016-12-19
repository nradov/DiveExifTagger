package com.github.nradov.diveexiftagger.image.jpeg;

/**
 * Start Of Frame (Baseline DCT).
 *
 * @author Nick Radov
 */
class StartOfFrameBaseline extends ImmutableByteSegment {

    static final short MARKER = (short) 0xFFC0;

    StartOfFrameBaseline() {

    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
