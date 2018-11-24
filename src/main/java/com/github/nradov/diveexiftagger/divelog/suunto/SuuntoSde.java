package com.github.nradov.diveexiftagger.divelog.suunto;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.xml.sax.SAXException;

import com.github.nradov.diveexiftagger.divelog.Dive;
import com.github.nradov.diveexiftagger.divelog.DivesSource;

/**
 * Read data from Suunto
 * <a href="http://www.suunto.com/en-US/Support/Suunto-DM5/" target="_">Dive
 * Manager</a> {@code .sde} export files. A {@code .sde} file is actually a Zip
 * file containing one XML file per dive profile.
 *
 * @author Nick Radov
 */
public class SuuntoSde implements DivesSource {

    private static final Logger LOGGER = Logger
            .getLogger(SuuntoSde.class.getName());

    private final ZoneOffset zoneOffset;
    private final ZipFile zipFile;

    private final NavigableSet<Dive> dives = new TreeSet<>();

    public SuuntoSde(@NonNull final String pathname,
            @NonNull final ZoneOffset zoneOffset) throws ZipException,
            IOException, ParserConfigurationException, SAXException {
        this(Paths.get(pathname), zoneOffset);
    }

    public SuuntoSde(@NonNull final Path file,
            @NonNull final ZoneOffset zoneOffset) throws ZipException,
            IOException, ParserConfigurationException, SAXException {
        this.zoneOffset = zoneOffset;
        zipFile = new ZipFile(file.toFile());
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            LOGGER.log(Level.FINE,
                    "processing dive profile: \"" + entry.getName() + "\"");
            dives.add(new SuuntoXml(zipFile.getInputStream(entry),
                    this.zoneOffset));
        }
    }

    @Override
    public float getDepthMeters(final Instant instant) {
        if (instant == null) {
            throw new IllegalArgumentException("instant is null");
        }
        LOGGER.log(Level.FINER, "looking for dive at " + instant);
        for (final Dive dive : dives) {
            LOGGER.log(Level.FINEST, "checking dive: start = " + dive.getStart()
                    + ", end = " + dive.getEnd());
            if (dive.isDuringDive(instant)) {
                return dive.getDepthMeters(instant);
            }
        }
        throw new IllegalArgumentException("no dive at " + instant);
    }

}
