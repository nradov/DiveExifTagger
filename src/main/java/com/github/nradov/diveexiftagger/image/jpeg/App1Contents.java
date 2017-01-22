package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * The actual contents of an APP1 segment. The actual format will depend on
 * marker detected at run time so we have separate concrete subclasses for the
 * different options.
 *
 * @author Nick Radov
 */
abstract class App1Contents implements ReadableByteChannel, ContainsField {

    @Override
    public void close() throws IOException {
        // do nothing
    }

    abstract int getLength();

    @Override
    public boolean isOpen() {
        return true;
    }

}
