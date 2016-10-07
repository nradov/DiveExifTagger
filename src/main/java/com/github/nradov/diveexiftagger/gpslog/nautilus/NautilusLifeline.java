package com.github.nradov.diveexiftagger.gpslog.nautilus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.github.nradov.diveexiftagger.gpslog.GpsCoordinates;
import com.github.nradov.diveexiftagger.gpslog.GpsLogSource;

/**
 * Retrieve GPS track log data exported from a
 * <a href="http://www.nautiluslifeline.com/" target="_">Nautilus Lifeline</a>.
 * The exported file must start with a header something like this.
 *
 * <pre>
GPS Coordinates Exported at: 2016-06-18 16:55:27.957000

GPS Point Format is: `&lt;latitude&gt;,&lt;longitude&gt; @ &lt;UTC/GMT-time&gt;`
 * </pre>
 *
 * Individual track points are on some of the lines that follow. It only logs
 * points occasionally so a high temporal tolerance may be needed when tagging
 * image files.
 *
 * @author Nick Radov
 */
public class NautilusLifeline implements GpsLogSource {

    /**
     * GPS Point Format is: `<latitude>,<longitude> @ <UTC/GMT-time>`
     */
    private static final Pattern LOG_POINT_PATTERN = Pattern.compile(
            "\\A[+-],[+-] @ [12]\\d{3}-[01]\\d-[03]\\d [0-2]\\d:[0-5]\\d:[0-5]\\d\\z");

    public NautilusLifeline(final String exportPath) throws IOException {
        this(Paths.get(exportPath));
    }

    /**
     * Create a new {@code NautilusLifeline} by parsing an exported text file.
     *
     * @param export
     *            exported GPS points file
     * @throws IOException
     *             if the file can't be read
     */
    public NautilusLifeline(final Path export) throws IOException {
        try (final Stream<String> stream = Files.lines(export)) {
            stream.forEach(System.out::println);
        }
    }

    @Override
    public GpsCoordinates getCoordinatesByTemporalProximity(
            final Instant instant, final Duration tolerance) {
        // TODO Auto-generated method stub
        return null;
    }
}
