package com.github.nradov.diveexiftagger.image.jpeg;

import static com.github.nradov.diveexiftagger.image.jpeg.TiffUtilities.formatShortAsUnsignedHex;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.adobe.internal.xmp.XMPException;

@SuppressWarnings("serial")
public class JpegExif implements Serializable {

    private final List<Segment> segments = new ArrayList<>(7);

    /** End of image. */
    private static final short EOI = (short) 0xFFD9;

    // TODO:
    // http://minborgsjavapot.blogspot.com/2014/12/java-8-initializing-maps-in-smartest-way.html
    private static final Map<Short, Supplier<? extends Segment>> SEGMENT_MARKER_CTOR_MAP = Collections
            .unmodifiableMap(new HashMap<Short, Supplier<? extends Segment>>() {
                {
                    put(StartOfImage.MARKER, StartOfImage::new);
                    put(ApplicationSpecific1.MARKER, ApplicationSpecific1::new);
                    put(DefineQuantizationTable.MARKER,
                            DefineQuantizationTable::new);
                    put(StartOfFrame.MARKER, StartOfFrame::new);
                    put(DefineHuffmanTable.MARKER, DefineHuffmanTable::new);
                    put(StartOfScan.MARKER, StartOfScan::new);
                }
            });

    protected JpegExif() {
        // for serialization only
    }

    public JpegExif(final Path path) throws IOException, XMPException {
        // https://examples.javacodegeeks.com/core-java/nio/filechannel/java-nio-channels-filechannel-example/
        this(FileChannel.open(path));
    }

    public JpegExif(final SeekableByteChannel channel)
            throws IOException, XMPException {
        final ByteBuffer dst = ByteBuffer.allocate(2);
        while (channel.position() < channel.size()) {
            if (channel.read(dst) != 2) {
                throw new IllegalStateException();
            }
            dst.flip();
            final short marker = dst.getShort();
            if (SEGMENT_MARKER_CTOR_MAP.containsKey(marker)) {
                final Segment segment = SEGMENT_MARKER_CTOR_MAP.get(marker)
                        .get();
                segment.populate(channel);
                segments.add(segment);
            } else {
                throw new IllegalArgumentException("unexpected marker: "
                        + formatShortAsUnsignedHex(marker));
            }
            dst.flip();
        }
    }

    void write(final Path path) throws IOException {
        write(FileChannel.open(path, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND));
    }

    void write(final FileChannel channel) throws IOException {
        long position = 0;
        for (final Segment segment : segments) {
            final int length = segment.getLength();
            channel.transferFrom(segment, position, length);
            position += length;
        }
        channel.write(TiffUtilities.convertToByteBuffer(EOI));
    }

}
