package com.github.nradov.diveexiftagger.image.jpeg;

class StartOfScan extends ImmutableByteSegment {

    static final short MARKER = (short) 0xFFDA;

    StartOfScan() {
    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
