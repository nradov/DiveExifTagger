package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import com.adobe.internal.xmp.XMPException;

@FunctionalInterface
interface SegmentSupplier {

    Segment construct(final SeekableByteChannel channel)
            throws IOException, XMPException;

}
