package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;

class StartOfScan extends VariableLengthSegment {

    static final short MARKER = (short) 0xFFDA;

    private final List<Component> components;

    private final byte startOfSpectralSelection;
    private final byte endOfSpectralSelection;
    private final byte successiveApproximation;

    private final byte[] compressed;

    @Override
    short getMarker() {
        return MARKER;
    }

    StartOfScan(final SeekableByteChannel channel) throws IOException {
        ByteBuffer dst = ByteBuffer.allocate(java.lang.Short.BYTES);
        channel.read(dst);
        dst.flip();
        final int length = dst.getShort();
        if (length < java.lang.Short.BYTES) {
            throw new IOException("invalid length: " + length);
        }
        dst = ByteBuffer.allocate(java.lang.Byte.BYTES);
        channel.read(dst);
        dst.flip();
        final byte numberOfComponents = dst.get();
        if (numberOfComponents < 1 || numberOfComponents > 4) {
            throw new IOException(
                    "invalid number of components: " + numberOfComponents);
        }
        if (length != 6 + (2 * numberOfComponents)) {
            throw new IOException("invalid length: " + length);
        }
        components = new ArrayList<>(numberOfComponents);
        for (byte i = 0; i < numberOfComponents; i++) {
            dst = ByteBuffer.allocate(2);
            channel.read(dst);
            dst.flip();
            components.add(new Component(dst.get(), dst.get()));
        }
        dst = ByteBuffer.allocate(3);
        channel.read(dst);
        dst.flip();
        startOfSpectralSelection = dst.get();
        endOfSpectralSelection = dst.get();
        successiveApproximation = dst.get();

        compressed = new byte[(int) (channel.size() - channel.position()
                - java.lang.Short.BYTES)];
        dst = ByteBuffer.allocate(compressed.length);
        if (channel.read(dst) != compressed.length) {
            throw new IOException("mismatched scan data length");
        }
        dst.flip();
        dst.get(compressed);
    }

    @Override
    int getLength() {
        return
        // marker
        java.lang.Short.BYTES
                // length
                + java.lang.Short.BYTES
                // number of components
                + java.lang.Byte.BYTES
                // components
                + components.size()
                // start of spectral selection
                + java.lang.Byte.BYTES
                // end of spectral selection
                + java.lang.Byte.BYTES
                // successive approximation
                + java.lang.Byte.BYTES
                // scan data
                + compressed.length;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        dst.putShort(getMarker());
        final int numberOfComponents = components.size();
        if (numberOfComponents < 1 || numberOfComponents > 4) {
            throw new IOException(
                    "invalid number of components: " + numberOfComponents);
        }
        dst.putShort((short) (6 + (2 * components.size())));
        components.forEach((component) -> {
            dst.put(component.id);
            dst.put(component.huffmanTable);
        });
        dst.put(startOfSpectralSelection);
        dst.put(endOfSpectralSelection);
        dst.put(successiveApproximation);
        dst.put(compressed);
        return getLength();
    }

    /**
     * Scan component.
     *
     * @author Nick Radov
     */
    static class Component {

        private final byte id;
        private final byte huffmanTable;

        Component(final byte id, final byte huffmanTable) {
            this.id = id;
            this.huffmanTable = huffmanTable;
        }

    }

}
