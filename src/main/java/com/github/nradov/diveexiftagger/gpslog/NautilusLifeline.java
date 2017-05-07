package com.github.nradov.diveexiftagger.gpslog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Retrieve GPS track log data exported from a
 * <a href="http://www.nautiluslifeline.com/radio-support" target="_">Nautilus
 * Lifeline Radio</a> (now discontinued). The exported file must start with a
 * header something like this.
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
public class NautilusLifeline extends GpsLogSource {

	/**
	 * GPS Point Format is: `<latitude>,<longitude> @ <UTC/GMT-time>`
	 */
	private static final Pattern LOG_POINT_PATTERN = Pattern
			.compile("\\A[+-],[+-] @ [12]\\d{3}-[01]\\d-[03]\\d [0-2]\\d:[0-5]\\d:[0-5]\\d\\z");

	public NautilusLifeline(@NonNull final String exportPath) throws IOException {
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
	public NautilusLifeline(@NonNull final Path export) throws IOException {
		try (final Stream<String> stream = Files.lines(export)) {
			stream.forEach(System.out::println);
		}
	}

}
