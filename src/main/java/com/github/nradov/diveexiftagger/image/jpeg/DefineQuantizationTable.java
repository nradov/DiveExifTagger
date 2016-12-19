package com.github.nradov.diveexiftagger.image.jpeg;

class DefineQuantizationTable extends ImmutableByteSegment {

    static final short MARKER = (short) 0xFFDB;

    DefineQuantizationTable() {

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
