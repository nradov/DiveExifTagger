package com.github.nradov.diveexiftagger.divelog.suunto;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Enumeration;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.github.nradov.diveexiftagger.divelog.Dive;
import com.github.nradov.diveexiftagger.divelog.DivesSource;

/**
 * Read data from Suunto
 * <a href="http://www.suunto.com/en-US/Support/Suunto-DM5/" target="_">Dive
 * Manager</a> {@code .sde} export files.
 * 
 * @author Nick Radov
 */
public class SuuntoSde implements DivesSource {

	private static final Logger LOGGER=Logger.getLogger(SuuntoSde.class.getName());

	private final ZoneOffset zoneOffset;
	private final ZipFile zipFile;

	private NavigableSet<Dive> dives = new TreeSet<Dive>();

	public SuuntoSde(final String pathname, final ZoneOffset zoneOffset)
			throws ZipException, IOException, ParserConfigurationException, SAXException {
		this(new File(pathname), zoneOffset);
	}

	public SuuntoSde(final File file, final ZoneOffset zoneOffset) throws ZipException, IOException, ParserConfigurationException, SAXException {
		this.zoneOffset = zoneOffset;
		zipFile = new ZipFile(file);
		final Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			final ZipEntry entry = entries.nextElement();
			LOGGER.log(Level.FINE, "processing dive profile: \"" + entry.getName() + "\"");
			dives.add(new SuuntoXml(zipFile.getInputStream(entry), this.zoneOffset));
		}
	}

	@Override
	public float getDepthMeters(final Instant instant) {
		if (instant == null) {
			throw new IllegalArgumentException("instant is null");
		}
		LOGGER.log(Level.FINER, "looking for dive at " + instant);
		for (final Dive dive : dives) {
			LOGGER.log(Level.FINEST, "checking dive: start = " + dive.getStart()+ ", end = "
					+ dive.getEnd());
			if (instant.isBefore(dive.getStart())) {
				// dives are listed in order of start time
				break;
			}
			if (instant.compareTo(dive.getStart()) >= 0 && instant.compareTo(dive.getEnd()) <= 0) {
				return dive.getDepthMeters(instant);
			}
		}
		throw new IllegalArgumentException("no dive at " + instant);
	}

}
