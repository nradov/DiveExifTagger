package com.github.nradov.diveexiftagger.image.jpeg;

class DefineQuantizationTable extends VariableLengthSegment {

    static final short MARKER = (short) 0xFFDB;

    DefineQuantizationTable() {

    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
