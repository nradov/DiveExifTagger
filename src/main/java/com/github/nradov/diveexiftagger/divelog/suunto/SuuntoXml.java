package com.github.nradov.diveexiftagger.divelog.suunto;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.nradov.diveexiftagger.divelog.Dive;

/**
 * <p>
 * Single dive profile in Suunto XML format. The format is simple with some
 * basic header information at the top followed by a series of data point
 * elements each containing a time, depth, and temperature (actual temperature
 * readings are only present in a subset of points).
 * </p>
 *
 * <p>
 * <strong>Note:</strong> This class is intended to process input from trusted
 * sources only and could be vulnerable to XML exploits.
 * </p>
 *
 * @author Nick Radov
 */
public class SuuntoXml implements Dive {

    private final ZoneOffset zoneOffset;
    private final Instant start, end;

    private final Element suunto;

    public SuuntoXml(final InputStream is, final ZoneOffset zoneOffset)
            throws ParserConfigurationException, SAXException, IOException {
        this.zoneOffset = zoneOffset;

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document doc = db.parse(is);
        suunto = doc.getDocumentElement();

        final String date = suunto.getElementsByTagName("DATE").item(0)
                .getTextContent();
        final int dayOfMonth = Integer.valueOf(date.substring(0, 2));
        final int month = Integer.valueOf(date.substring(3, 5));
        final int year = Integer.valueOf(date.substring(6));
        final String time = suunto.getElementsByTagName("TIME").item(0)
                .getTextContent();
        final int hourOfDay = Integer.valueOf(time.substring(0, 2));
        final int minute = Integer.valueOf(time.substring(3, 5));
        final int second = Integer.valueOf(time.substring(6));
        // TODO: support other time zones
        start = LocalDateTime
                .of(year, month, dayOfMonth, hourOfDay, minute, second)
                .atZone(ZoneId.of("America/Los_Angeles")).toInstant();
        final int diveTimeSec = Integer.valueOf(suunto
                .getElementsByTagName("DIVETIMESEC").item(0).getTextContent());
        end = start.plusSeconds(diveTimeSec);
    }

    @Override
    public Instant getStart() {
        return start;
    }

    @Override
    public Instant getEnd() {
        return end;
    }

    private float[] depths;

    private void populateDepths() {
        final int sampleCount = Integer.valueOf(suunto
                .getElementsByTagName("SAMPLECNT").item(0).getTextContent());
        final int sampleInterval = Integer
                .valueOf(suunto.getElementsByTagName("SAMPLEINTERVAL").item(0)
                        .getTextContent());
        final int diveSeconds = sampleCount * sampleInterval;
        depths = new float[diveSeconds + 1];
        final NodeList samples = suunto.getElementsByTagName("SAMPLE");
        for (int i = 0; i < samples.getLength(); i++) {
            final Element sample = (Element) samples.item(i);
            final int sampleTime = Integer
                    .valueOf(sample.getElementsByTagName("SAMPLETIME").item(0)
                            .getTextContent());
            if (sampleTime > diveSeconds) {
                throw new IllegalStateException("sample at time " + sampleTime
                        + " doesn't match header");
            }
            final float depth = Float.valueOf(sample
                    .getElementsByTagName("DEPTH").item(0).getTextContent());
            depths[sampleTime] = depth;
        }
    }

    @Override
    public float getDepthMeters(@Nonnull final Instant instant) {
        if (!isDuringDive(instant)) {
            throw new IllegalArgumentException(
                    instant + " is not during this dive");
        }

        final int diveSecs = (int) ChronoUnit.SECONDS.between(start, end);
        if (depths == null) {
            populateDepths();
        }
        return depths[diveSecs];
    }

    @Override
    public int compareTo(final Dive o) {
        return getStart().compareTo(o.getStart());
    }

    @Override
    public boolean isDuringDive(@Nonnull final Instant instant) {
        return !(instant.isAfter(end) || instant.isBefore(start));
    }

}
