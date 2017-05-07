package com.github.nradov.diveexiftagger.gpslog;

import static org.junit.Assert.assertEquals;

import java.time.Instant;

import org.junit.Test;

public class GpsCoordinatesTest {

	@Test
	public void testInterpolate() {
		final GpsCoordinates p0 = new GpsCoordinates(Instant.parse("2017-04-23T18:10:00Z"), 10d, 20d, 30d);
		final GpsCoordinates p1 = new GpsCoordinates(Instant.parse("2017-04-23T18:20:00Z"), 20d, 40d, 60d);
		final GpsCoordinates middle = p0.interpolate(Instant.parse("2017-04-23T18:15:00Z"), p1);

		assertEquals(15d, middle.getLatitude(), 0.01d);
		assertEquals(30d, middle.getLongitude(), 0.01d);
		assertEquals(45d, middle.getAltitude(), 0.01d);
	}

}
