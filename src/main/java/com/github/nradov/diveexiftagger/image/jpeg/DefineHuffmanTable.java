package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

class DefineHuffmanTable extends ImmutableByteSegment {

    static final short MARKER = (short) 0xFFC4;

    DefineHuffmanTable(final SeekableByteChannel channel) throws IOException {
        super(channel);
    }

    @Override
    short getMarker() {
        return MARKER;
    }

}
