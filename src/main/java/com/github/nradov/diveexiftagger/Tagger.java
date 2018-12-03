package com.github.nradov.diveexiftagger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.zip.ZipException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.xml.sax.SAXException;

import com.github.nradov.diveexiftagger.divelog.DiveSourceFactory;
import com.github.nradov.diveexiftagger.divelog.DivesSource;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.GPX.Builder;

/**
 * <p>
 * Read depth data from a dive log file and use it to create or update a GPS
 * track log file.
 * </p>
 * 
 * @author Nick Radov
 */
public class Tagger {

	final Path trackLogFile;

	final DivesSource source;

	/** Time zone offset for the local time on the image files and dive logs. */
	final ZoneOffset zoneOffset;

	final private static Options options = new Options();

	public Tagger(@NonNull final Path diveLogsFile, @NonNull final Path trackLogFile)
			throws ZipException, IOException, ParserConfigurationException, SAXException {
		zoneOffset = null;
		source = DiveSourceFactory.create(diveLogsFile, zoneOffset);
		this.trackLogFile =trackLogFile;
	}

	public Tagger(@NonNull final String diveLogPathname, @NonNull final String trackLogFile)
			throws ZipException, IOException, ParserConfigurationException, SAXException {
		this(Paths.get(diveLogPathname), Paths.get(trackLogFile));
	}

	public void writeGpsTrackLog() throws IOException {
		source.
		final Builder builder = GPX.builder()
			    .addTrack(track -> track
			        .addSegment(segment -> segment
			            .addPoint(p -> p.ele(162))));
		final GPX gpx = builder
			    .build();
		GPX.write(gpx, "track.gpx");
	}
	
	public static void main(final String[] args)
			throws ZipException, IOException, ParserConfigurationException, SAXException, ParseException {
		final CommandLineParser parser = new DefaultParser();
		final CommandLine line = parser.parse(options, args);

		final Tagger tagger = new Tagger(args[0], args[1]);
	}

	private static void printHelp() {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ant", options);
	}

}
