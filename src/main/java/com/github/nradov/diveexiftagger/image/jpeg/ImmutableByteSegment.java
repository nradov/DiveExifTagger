package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 *
 * @author Nick Radov
 */
abstract class ImmutableByteSegment extends VariableLengthSegment {

    private byte[] contents;

    ImmutableByteSegment() {

    }

    @Override
    void populate(final SeekableByteChannel channel) throws IOException {
        final ByteBuffer dst = ByteBuffer.allocate(java.lang.Short.BYTES);
        channel.read(dst);
        dst.flip();
        final int length = dst.getShort();
        if (length < java.lang.Short.BYTES) {
            throw new IllegalArgumentException("invalid length");
        }
        contents = new byte[length - java.lang.Short.BYTES];
        final ByteBuffer body = ByteBuffer.allocate(contents.length);
        if (channel.read(body) != body.capacity()) {
            throw new IOException("unexpected end of channel");
        }
        body.flip();
        body.get(contents);
    }

    @Override
    int getLength() {
        return java.lang.Short.BYTES + java.lang.Short.BYTES + contents.length;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        dst.putShort(getMarker()).putShort((short) getLength()).put(contents);
        return getLength();
    }

}
