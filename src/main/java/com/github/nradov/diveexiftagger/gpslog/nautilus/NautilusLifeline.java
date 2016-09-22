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
 * Retrieve GPS log data exported from a
 * <a href="http://www.nautiluslifeline.com/" target="_">Nautilus Lifeline</a>.
 * 
 * @author Nick Radov
 */
public class NautilusLifeline implements GpsLogSource {

	/**
	 * GPS Point Format is: `<latitude>,<longitude> @ <UTC/GMT-time>`
	 */
	private static final Pattern LOG_POINT_PATTERN = Pattern
			.compile("\\A[+-],[+-] @ [12]\\d{3}-[01]\\d-[03]\\d [0-2]\\d:[0-5]\\d:[0-5]\\d\\z");

	public NautilusLifeline(final String exportPath) throws IOException {
		this(Paths.get(exportPath));
	}

	public NautilusLifeline(final Path export) throws IOException {
		try (final Stream<String> stream = Files.lines(export)) {
			stream.forEach(System.out::println);
		}
	}

	@Override
	public GpsCoordinates getCoordinatesByTemporalProximity(final Instant instant, final Duration tolerance) {
		// TODO Auto-generated method stub
		return null;
	}
}
