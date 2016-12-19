package com.github.nradov.diveexiftagger.image.jpeg;

/**
 * Define Restart Interval. Specifies the interval between RSTn markers, in
 * macroblocks. This marker is followed by two bytes indicating the fixed size
 * so it can be treated like any other variable size segment.
 *
 * @author Nick Radov
 */
class DefineRestartInterval extends ImmutableByteSegment {

    static final short MARKER = (short) 0xFFDD;

    @Override
    short getMarker() {
        return MARKER;
    }

    /**
     * {@inheritDoc}
     *
     * @return 2
     */
    @Override
    int getLength() {
        return java.lang.Short.BYTES;
    }

}
