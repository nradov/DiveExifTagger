package com.github.nradov.diveexiftagger.gpslog;

import java.time.Duration;
import java.time.Instant;

/**
 * GPS Exchange Format (GPX).
 *
 * @see <a href="https://en.wikipedia.org/wiki/GPS_Exchange_Format" target="_"
 *      title="Wikipedia">GPS Exchange Format</a>
 * @author Nick Radov
 */
public class Gpx extends GpsLogSource {

	@Override
	public GpsCoordinates getCoordinatesByTemporalProximity(final Instant instant, final Duration tolerance) {
		// TODO Auto-generated method stub
		return null;
	}

}
