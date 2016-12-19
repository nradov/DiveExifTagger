package com.github.nradov.diveexiftagger.image.jpeg;

public class EndOfImage extends NoPayloadSegment {

    static final short MARKER = (short) 0xFFD9;

    @Override
    short getMarker() {
        return MARKER;
    }

}
