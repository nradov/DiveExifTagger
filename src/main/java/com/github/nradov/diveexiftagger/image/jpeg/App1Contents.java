package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Optional;

/**
 * The actual contents of an APP1 segment. The actual format will depend on
 * marker detected at run time so we have separate concrete subclasses for the
 * different options.
 *
 * @author Nick Radov
 */
abstract class App1Contents extends ContainsField
        implements ReadableByteChannel {

    @Override
    public void close() throws IOException {
        // do nothing
    }

    @Override
    public Optional<List<Rational>> getFieldRational(final FieldTag tag) {
        return getField(tag, Rational.class);
    }

    abstract int getLength();

    @Override
    public boolean isOpen() {
        return true;
    }

}
