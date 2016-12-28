package com.github.nradov.diveexiftagger.image.jpeg;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
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

    abstract Optional<List<Rational>> getFieldRational(FieldTag tag);

    abstract int getLength();

}
