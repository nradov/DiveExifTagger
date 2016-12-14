package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

abstract class App1Contents implements ReadableByteChannel {

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    abstract public Optional<Rational> getFieldRational(TiffFieldTag tag);

}
