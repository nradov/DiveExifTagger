package com.github.nradov.diveexiftagger.gpslog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * GPS log source for
 * <a target="_" title="National Marine Electronics Association" href=
 * "http://www.nmea.org/">NMEA</a> 0183 files, including some Canon cameras.
 * <em>Note:</em> Currently
 *
 * @author Nick Radov
 * @see <a target="_" href="http://www.gpsinformation.org/dale/nmea.htm">NMEA
 *      data</a>
 * @see <a target="_" title="Wikipedia" href=
 *      "https://en.wikipedia.org/wiki/NMEA_0183">NMEA 0183</a>
 */
// TODO: merge the data from GGA and RMC sentences since one has altitude and
// the other has the date
public class Nmea0183 extends GpsLogSource {

	private static final String SENTENCE_BEGIN = "$";

	private static final String GPS_RECEIVER_PREFIX = "GP";

	private static final int SENTENCE_PREFIX_LENGTH = (SENTENCE_BEGIN + GPS_RECEIVER_PREFIX).length();

	private static final class SentenceDataType {

		private static final String FIX_INFORMATION = "GGA";

		private static final String RECOMMENDED_MINIMUM_DATA_FOR_GPS = "RMC";

	}

	private static final int MINIMUM_SENTENCE_LENGTH = SENTENCE_PREFIX_LENGTH
			+ SentenceDataType.FIX_INFORMATION.length();

	/**
	 * Create a new {@code Nmea0183} object by parsing an NMEA 0183 GPS log
	 * file.
	 *
	 * @param path
	 *            log file path
	 * @throws IOException
	 *             if any error occurs while reading the log file
	 */
	public Nmea0183(final String path) throws IOException {
		this(Paths.get(path));
	}

	/**
	 * Create a new {@code Nmea0183} object by parsing an NMEA 0183 GPS log
	 * file.
	 *
	 * @param path
	 *            log file path
	 * @throws IOException
	 *             if any error occurs while reading the log file
	 */
	public Nmea0183(final Path path) throws IOException {
		/*
		 * Fix Information (GGA) sentences don't contain the date so we have to
		 * parse that out of the file name
		 */
		final String fileName = path.getName(path.getNameCount() - 1).toString();
		if (!fileName.toUpperCase(Locale.US).endsWith(".LOG")) {
			throw new IllegalArgumentException("unsupported file extension \"" + fileName + "\"");
		}
		final String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
		// only works after 2000
		final String iso = "20" + fileNameWithoutExtension.substring(0, 2) + "-"
				+ fileNameWithoutExtension.substring(2, 4) + "-" + fileNameWithoutExtension.substring(4, 6)
				+ "T00:00:00.00Z";
		final Instant base = Instant.parse(iso);

		try (final Stream<String> stream = Files.lines(path, StandardCharsets.US_ASCII)) {
			stream.filter(line -> line.startsWith(SENTENCE_BEGIN + GPS_RECEIVER_PREFIX))
					.forEach(sentence -> addPoint(sentence, base));
		}
	}

	private static final String FIELD_DELIMITER = ",";

	private transient GpsCoordinates lastPoint;

	private void addPoint(final String sentence, final Instant base) {
		if (sentence.length() >= MINIMUM_SENTENCE_LENGTH) {
			validateChecksum(sentence);
			final String[] fields = sentence.split(FIELD_DELIMITER);
			final String dataType = fields[0].substring(SENTENCE_PREFIX_LENGTH);
			switch (dataType) {
			case SentenceDataType.FIX_INFORMATION:
				addPointFixInformation(fields, base);
				break;
			case SentenceDataType.RECOMMENDED_MINIMUM_DATA_FOR_GPS:
				addPointRecommendedMinimumDataForGps(fields);
				break;
			default: // ignore unrecognized data types
			}
		}
	}

	private static final double MINUTES_TO_DECIMAL_CONVERSION = 10d / 6d;

	private void addPointFixInformation(final String[] fields, final Instant base) {
		if (fields[1].length() < 6) {
			throw new IllegalArgumentException("time too short");
		}
		final int hours = Integer.parseInt(fields[1].substring(0, 2));
		final int minutes = Integer.parseInt(fields[1].substring(2, 4));
		final int seconds = Integer.parseInt(fields[1].substring(4, 6));
		// TODO: handle varying number of decimal places
		final int millis;
		if (fields[1].indexOf('.') == -1) {
			millis = 0;
		} else {
			millis = Integer.parseInt(fields[1].substring(fields[1].indexOf('.') + 1));
		}
		final Instant time = base.plus(hours, ChronoUnit.HOURS).plus(minutes, ChronoUnit.MINUTES)
				.plus(seconds, ChronoUnit.SECONDS).plus(millis, ChronoUnit.MILLIS);

		final double rawLatitude = Double.parseDouble(fields[2]) / 100d;
		final int latitudeDegrees = new Double(rawLatitude).intValue();
		final double latitudeMinutes = rawLatitude - latitudeDegrees;
		final double latitude = getLatitudeDirectionMultiplier(fields[3]) * latitudeDegrees
				+ latitudeMinutes * MINUTES_TO_DECIMAL_CONVERSION;

		final double rawLongitude = Double.parseDouble(fields[4]) / 100d;
		final int longitudeDegrees = new Double(rawLongitude).intValue();
		final double longitudeMinutes = rawLongitude - longitudeDegrees;
		final double longitude = getLongitudeDirectionMultiplier(fields[5]) * longitudeDegrees
				+ longitudeMinutes * MINUTES_TO_DECIMAL_CONVERSION;

		final double altitude = Double.parseDouble(fields[9]);
		if (!"M".equalsIgnoreCase(fields[10])) {
			throw new IllegalArgumentException("unexpected altitude units " + fields[10]);
		}

		addPoint(new GpsCoordinates(time, latitude, longitude, altitude));
	}

	private static int getLatitudeDirectionMultiplier(final String direction) {
		switch (direction) {
		case "N":
			return 1;
		case "S":
			return -1;
		default:
			throw new IllegalArgumentException("invalid latitude direction " + direction);
		}
	}

	private static int getLongitudeDirectionMultiplier(final String direction) {
		switch (direction) {
		case "W":
			return 1;
		case "E":
			return -1;
		default:
			throw new IllegalArgumentException("invalid longitude direction " + direction);
		}
	}

	private void addPointRecommendedMinimumDataForGps(final String[] fields) {

	}

	private static final String CHECKSUM_DELIMITER = "*";

	private static final int HEXADECIMAL_RADIX = 16;

	private static void validateChecksum(final String sentence) {
		final int checksumDelimiterIndex = sentence.indexOf(CHECKSUM_DELIMITER);
		if (checksumDelimiterIndex == -1) {
			return;
		}
		final int checksumCalculated = calculateChecksum(
				sentence.substring(SENTENCE_BEGIN.length(), checksumDelimiterIndex));
		final int checksumInSentence = Integer.parseInt(sentence.substring(checksumDelimiterIndex + 1),
				HEXADECIMAL_RADIX);
		if (checksumCalculated != checksumInSentence) {
			throw new IllegalArgumentException("invalid checksum " + checksumInSentence);
		}
	}

	private static int calculateChecksum(final String s) {
		int checksum = 0;

		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (c < ' ' || c > '~') {
				throw new IllegalArgumentException("illegal character '" + c + "' at index " + i);
			}
			checksum ^= c;
		}

		return checksum;
	}

}
