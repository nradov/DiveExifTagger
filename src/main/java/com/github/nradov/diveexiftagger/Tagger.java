package com.github.nradov.diveexiftagger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

import javax.annotation.CheckForNull;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.xml.sax.SAXException;

import com.github.nradov.diveexiftagger.divelog.DiveSourceFactory;
import com.github.nradov.diveexiftagger.divelog.DivesSource;
import com.github.nradov.diveexiftagger.gpslog.GpsLogSource;

/**
 * Add EXIF GPS altitude (depth) tags to image files based on logged dive
 * profiles.
 * 
 * @author Nick Radov
 */
public class Tagger {

	final File imageFile;

	@CheckForNull
	final DivesSource source;
	@CheckForNull
	final GpsLogSource gpsSource;

	/** Time zone offset for the local time on the image files and dive logs. */
	@CheckForNull
	final ZoneOffset zoneOffset;

	final private static Options options = new Options();

	public Tagger(final File imageFile, final File diveLogsFile)
			throws ZipException, IOException, ParserConfigurationException, SAXException {
		this.imageFile = imageFile;
		zoneOffset = null;
		source = DiveSourceFactory.create(diveLogsFile, zoneOffset);
		gpsSource = null;
	}

	public Tagger(final String imagePathname, final String diveLogPathname)
			throws ZipException, IOException, ParserConfigurationException, SAXException {
		this(new File(imagePathname), new File(diveLogPathname));
	}

	public static void main(final String[] args) throws ZipException, IOException, ImageWriteException,
			ImageReadException, ParserConfigurationException, SAXException, ParseException {
		final CommandLineParser parser = new DefaultParser();
		final CommandLine line = parser.parse(options, args);

		final Tagger tagger = new Tagger(args[0], args[1]);
		tagger.tagFiles();
	}

	private static void printHelp() {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ant", options);
	}

	public void tagFiles() throws ImageWriteException, ImageReadException, IOException {
		if (imageFile.isDirectory()) {
			for (final File directoryEntry : imageFile.listFiles()) {
				tagFile(directoryEntry);
			}
		} else {
			tagFile(imageFile);
		}
	}

	private void tagFile(final File file) throws IOException, ImageWriteException, ImageReadException {
		final File dst = File.createTempFile(this.getClass().getSimpleName(), ".jpg");

		try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(dst));) {
			// note that metadata might be null if no metadata is found.
			final ImageMetadata metadata = Imaging.getMetadata(file);
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (jpegMetadata == null) {
				throw new IllegalArgumentException(
						"file \"" + file.getCanonicalPath() + "\" does not contain metadata");
			}
			// note that exif might be null if no Exif metadata is found.
			final TiffImageMetadata exif = jpegMetadata.getExif();
			if (exif == null) {
				throw new IllegalArgumentException(
						"file \"" + file.getCanonicalPath() + "\" does not contain EXIF metadata");
			}

			TiffOutputSet outputSet = exif.getOutputSet();

			/*
			 * if file does not contain any exif metadata, we create an empty
			 * set of exif metadata. Otherwise, we keep all of the other
			 * existing tags
			 */
			if (null == outputSet) {
				outputSet = new TiffOutputSet();
			}

			final String dateTimeOriginal = exif.getFieldValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)[0];

			final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
			exifDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF);
			exifDirectory.add(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF,
					(byte) GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF_VALUE_BELOW_SEA_LEVEL);
			exifDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
			// TODO: add correction for altitude diving
			exifDirectory.add(GpsTagConstants.GPS_TAG_GPS_ALTITUDE,
					convertFloat(source.getDepthMeters(convertExifDateTime(dateTimeOriginal))));

			System.err.println(dst.getAbsolutePath());

			new ExifRewriter().updateExifMetadataLossless(file, os, outputSet);
			if (!file.delete()) {
				throw new IllegalStateException("failed to delete file \"" + file.getCanonicalPath() + "\"");
			}
			if (!dst.renameTo(file)) {
				throw new IllegalStateException("failed to rename file \"" + file.getCanonicalPath() + "\"");
			}
		}
	}

	private static int DENOMINATOR = 100;

	private static RationalNumber convertFloat(final float f) {
		return new RationalNumber((int) (f * DENOMINATOR), DENOMINATOR);
	}

	private static final Pattern EXIF_DATE_TIME_PATTERN = Pattern
			.compile("\\A[12]\\d{3}:[01]\\d:[0-3]\\d [0-2]\\d:[0-5]\\d:[0-5]\\d\\z");

	/**
	 * Convert an EXIF date/time value.
	 * 
	 * @param dateTime
	 *            date/time value in "YYYY:MM:DD HH:MM:SS" format
	 * @see <a target="_" title="Aware Systems" href=
	 *      "http://www.awaresystems.be/imaging/tiff/tifftags/privateifd/exif/datetimeoriginal.html">
	 *      TIFF Tag DateTimeOriginal</a>
	 */
	private Instant convertExifDateTime(final String dateTime) {
		if (dateTime == null) {
			throw new IllegalArgumentException("dateTime is null");
		}
		if (!EXIF_DATE_TIME_PATTERN.matcher(dateTime).matches()) {
			throw new IllegalArgumentException("dateTime doesn't match pattern " + EXIF_DATE_TIME_PATTERN);
		}

		final int year = Integer.parseInt(dateTime.substring(0, 4));
		final int month = Integer.parseInt(dateTime.substring(5, 7));
		final int dayOfMonth = Integer.parseInt(dateTime.substring(8, 10));
		final int hour = Integer.parseInt(dateTime.substring(11, 13));
		final int minute = Integer.parseInt(dateTime.substring(14, 16));
		final int second = Integer.parseInt(dateTime.substring(17));
		final LocalDateTime localDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, 0);
		// TODO: support other time zones
		return localDateTime.atZone(ZoneId.of("America/Los_Angeles")).toInstant();
	}
}
