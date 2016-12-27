package com.github.nradov.diveexiftagger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import com.github.nradov.diveexiftagger.divelog.DiveSourceFactory;
import com.github.nradov.diveexiftagger.divelog.DivesSource;
import com.github.nradov.diveexiftagger.gpslog.GpsLogSource;

/**
 * <p>
 * Add EXIF GPS latitude, longitude, and/or altitude (depth) tags to image files
 * based on logged dive profiles and/or GPS tracks.
 * </p>
 * <p>
 * <strong>Note:</strong> This application is still experimental and hasn't been
 * widely tested. It could damage or delete your image files. Make backup copies
 * before running.
 * </p>
 *
 * @see <a href=
 *      "https://github.com/moovida/jgrasstools/blob/master/jgrassgears/src/main/java/org/jgrasstools/gears/io/exif/ExifGpsWriter.java">
 *      jgrasstools</a>
 * @author Nick Radov
 */
public class Tagger {

    final Path imageFile;

    @CheckForNull
    final DivesSource source;
    @CheckForNull
    final GpsLogSource gpsSource;

    /** Time zone offset for the local time on the image files and dive logs. */
    @CheckForNull
    final ZoneOffset zoneOffset;

    final private static Options options = new Options();

    public Tagger(@Nonnull final Path imageFile,
            @Nonnull final Path diveLogsFile) throws ZipException, IOException,
            ParserConfigurationException, SAXException {
        this.imageFile = imageFile;
        zoneOffset = null;
        source = DiveSourceFactory.create(diveLogsFile, zoneOffset);
        gpsSource = null;
    }

    public Tagger(@Nonnull final String imagePathname,
            @Nonnull final String diveLogPathname) throws ZipException,
            IOException, ParserConfigurationException, SAXException {
        this(Paths.get(imagePathname), Paths.get(diveLogPathname));
    }

    public static void main(final String[] args)
            throws ZipException, IOException, ParserConfigurationException,
            SAXException, ParseException {
        final CommandLineParser parser = new DefaultParser();
        final CommandLine line = parser.parse(options, args);

        final Tagger tagger = new Tagger(args[0], args[1]);
        tagger.tagFiles();
    }

    private static void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ant", options);
    }

    public void tagFiles() throws IOException {
        if (Files.isDirectory(imageFile)) {
            try (final DirectoryStream<Path> directoryStream = Files
                    .newDirectoryStream(imageFile)) {
                for (final Path directoryEntry : directoryStream) {
                    tagFile(directoryEntry);
                }
            }
        } else {
            tagFile(imageFile);
        }
    }

    // http://johnbokma.com/java/obtaining-image-metadata.html
    private void tagFile(final Path path) throws IOException {

    }

    private static final Pattern EXIF_DATE_TIME_PATTERN = Pattern.compile(
            "\\A[12]\\d{3}:[01]\\d:[0-3]\\d [0-2]\\d:[0-5]\\d:[0-5]\\d\\z");

    /**
     * Convert an EXIF date/time value.
     *
     * @param dateTime
     *            date/time value in "YYYY:MM:DD HH:MM:SS" format
     * @see <a target="_" title="Aware Systems" href=
     *      "http://www.awaresystems.be/imaging/tiff/tifftags/privateifd/exif/datetimeoriginal.html">
     *      TIFF Tag DateTimeOriginal</a>
     * @return the converted point in time
     */
    public static Instant convertExifDateTime(final String dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime is null");
        }
        if (!EXIF_DATE_TIME_PATTERN.matcher(dateTime).matches()) {
            throw new IllegalArgumentException(
                    "dateTime doesn't match pattern " + EXIF_DATE_TIME_PATTERN);
        }

        final int year = Integer.parseInt(dateTime.substring(0, 4));
        final int month = Integer.parseInt(dateTime.substring(5, 7));
        final int dayOfMonth = Integer.parseInt(dateTime.substring(8, 10));
        final int hour = Integer.parseInt(dateTime.substring(11, 13));
        final int minute = Integer.parseInt(dateTime.substring(14, 16));
        final int second = Integer.parseInt(dateTime.substring(17));
        final LocalDateTime localDateTime = LocalDateTime.of(year, month,
                dayOfMonth, hour, minute, second, 0);
        // TODO: support other time zones
        return localDateTime.atZone(ZoneId.of("America/Los_Angeles"))
                .toInstant();
    }
}
