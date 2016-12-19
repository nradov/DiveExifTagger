package com.github.nradov.diveexiftagger.image.jpeg;

class StartOfFrameProgressive extends ImmutableByteSegment {

    static final short MARKER = (short) 0xFFC2;

    StartOfFrameProgressive() {

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
