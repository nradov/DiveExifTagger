package com.github.nradov.diveexiftagger.image.jpeg;

class StartOfFrame extends VariableLengthSegment {

    static final short MARKER = (short) 0xFFC0;

    StartOfFrame() {

    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
