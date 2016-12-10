package com.github.nradov.diveexiftagger.image.jpeg;

class DefineHuffmanTable extends VariableLengthSegment {

    static final short MARKER = (short) 0xFFC4;

    DefineHuffmanTable() {

    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
