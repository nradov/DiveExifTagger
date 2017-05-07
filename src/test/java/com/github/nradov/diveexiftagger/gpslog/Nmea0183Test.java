package com.github.nradov.diveexiftagger.gpslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

public class Nmea0183Test {

	@Test
	public void testNmea0183String() throws IOException, URISyntaxException {
		final GpsLogSource nmea = new Nmea0183(Paths.get(getClass().getResource("/1704040.LOG").toURI()));
		final Instant instant = Instant.parse("2017-04-04T16:05:07Z");
		final Duration tolerance = Duration.of(1, ChronoUnit.SECONDS);
		final GpsCoordinates coords = nmea.getCoordinatesByTemporalProximity(instant, tolerance);
		assertNotNull(coords);
		assertEquals(39.6003083333333333d, coords.getLatitude(), 0.0001d);
		assertEquals(106.5113466666666667d, coords.getLongitude(), 0.0001d);
		assertEquals(2579.45d, coords.getAltitude(), 0.01d);
	}

	@Test
	public void testGetCoordinatesByTemporalProximity() {
		fail("Not yet implemented");
	}

}
