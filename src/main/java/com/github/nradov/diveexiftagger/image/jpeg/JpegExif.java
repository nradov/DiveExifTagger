package com.github.nradov.diveexiftagger.image.jpeg;

import static com.github.nradov.diveexiftagger.image.jpeg.Utilities.formatShortAsUnsignedHex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.adobe.internal.xmp.XMPException;

@SuppressWarnings("serial")
public class JpegExif {

    private final List<Segment> segments = new ArrayList<>(8);

    /**
     * Map from JPEG/Exif segment markers to constructors for the corresponding
     * classes.
     */
    // TODO:
    // http://minborgsjavapot.blogspot.com/2014/12/java-8-initializing-maps-in-smartest-way.html
    private static final Map<java.lang.Short, Supplier<? extends Segment>> SEGMENT_MARKER_CTOR_MAP = Collections
            .unmodifiableMap(
                    new HashMap<java.lang.Short, Supplier<? extends Segment>>() {
                        {
                            put(StartOfImage.MARKER, StartOfImage::new);
                            put(ApplicationSpecific1.MARKER,
                                    ApplicationSpecific1::new);
                            put(DefineQuantizationTable.MARKER,
                                    DefineQuantizationTable::new);
                            put(StartOfFrameBaseline.MARKER,
                                    StartOfFrameBaseline::new);
                            put(StartOfFrameProgressive.MARKER,
                                    StartOfFrameProgressive::new);
                            put(DefineHuffmanTable.MARKER,
                                    DefineHuffmanTable::new);
                            put(StartOfScan.MARKER, StartOfScan::new);
                            put(EndOfImage.MARKER, EndOfImage::new);
                            put(DefineRestartInterval.MARKER,
                                    DefineRestartInterval::new);
                        }
                    });

    public JpegExif(final String path) throws IOException, XMPException {
        this(Paths.get(path));
    }

    public JpegExif(final Path path) throws IOException, XMPException {
        // https://examples.javacodegeeks.com/core-java/nio/filechannel/java-nio-channels-filechannel-example/
        this(FileChannel.open(path));
    }

    public JpegExif(final SeekableByteChannel channel)
            throws IOException, XMPException {
        final ByteBuffer dst = ByteBuffer.allocate(java.lang.Short.BYTES);
        while (channel.position() < channel.size()) {
            if (channel.read(dst) != java.lang.Short.BYTES) {
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
    }

    public Optional<Rational> getFieldRational(final FieldTag tag) {
        for (final Segment segment : getSegments()) {
            final Optional<Rational> o = segment.getFieldRational(tag);
            if (o.isPresent()) {
                return o;
            }
        }
        return Optional.empty();
    }

    public Optional<Rational> getGpsAltitude() {
        return getFieldRational(FieldTag.GpsAltitude);
    }

    List<Segment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        getSegments().forEach((k) -> sb.append(k));
        return sb.toString();
    }

}
