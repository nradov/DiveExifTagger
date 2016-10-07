package com.github.nradov.diveexiftagger.gpslog;

import java.io.IOException;
import java.nio.file.Path;

import com.github.nradov.diveexiftagger.gpslog.nautilus.NautilusLifeline;

/**
 * Factory class to create a {@code GpsLogSource} based on an input file.
 *
 * @author Nick Radov
 */
public final class GpsLogFactory {

    private GpsLogFactory() {
        // static methods only
    }

    public static GpsLogSource create(final Path path) throws IOException {
        // TODO: parse the file to check the actual format
        return new NautilusLifeline(path);
    }

}
