package com.github.nradov.diveexiftagger.image.jpeg;

class DefineHuffmanTable extends ImmutableByteSegment {

    static final short MARKER = (short) 0xFFC4;

    DefineHuffmanTable() {

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
