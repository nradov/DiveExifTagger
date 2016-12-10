package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import com.adobe.internal.xmp.XMPException;

abstract class VariableLengthSegment extends Segment {

    private ByteBuffer body;

    private short length;

    @Override
    void populate(final SeekableByteChannel channel)
            throws IOException, XMPException {
        final ByteBuffer dst = ByteBuffer.allocate(Short.BYTES);
        populateLength(channel, dst);
        if (length > Short.BYTES) {
            body = ByteBuffer.allocate(length - dst.capacity());
            if (channel.read(body) != body.capacity()) {
                throw new IOException("unexpected end of channel");
            }
            body.flip();
        }
    }

    void populateLength(final SeekableByteChannel channel, final ByteBuffer dst)
            throws IOException {
        if (channel.read(dst) != dst.capacity()) {
            throw new IllegalStateException();
        }
        dst.flip();
        length = dst.getShort();
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        int bytes = super.read(dst);
        dst.putShort(length);
        bytes += Short.BYTES;
        if (body == null) {
            throw new IllegalStateException();
        }
        if (body.remaining() > dst.remaining()) {
            throw new IllegalStateException(
                    "remaining " + body.remaining() + " > " + dst.remaining());
        }
        dst.put(body);
        bytes += body.capacity();
        return bytes;
    }

    @Override
    public int getLength() {
        return length;
    }

}
