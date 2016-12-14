package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import com.adobe.internal.xmp.XMPException;

class StartOfScan extends VariableLengthSegment {

    static final short MARKER = (short) 0xFFDA;

    private ByteBuffer compressed;

    StartOfScan() {
    }

    @Override
    void populate(final SeekableByteChannel channel)
            throws IOException, XMPException {
        super.populate(channel);
        // read the compressed data
        compressed = ByteBuffer
                .allocate((int) (channel.size() - channel.position()));
        if (channel.read(compressed) != compressed.capacity()) {
            throw new IllegalStateException();
        }
        compressed.flip();
    }

    @Override
    short getMarker() {
        return MARKER;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        int bytes = super.read(dst);
        // TODO: calculate the correct limit
        dst.limit(dst.capacity());
        dst.put(compressed);
        bytes += compressed.capacity();
        return bytes;
    }

}
