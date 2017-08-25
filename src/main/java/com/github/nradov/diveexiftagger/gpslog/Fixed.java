package com.github.nradov.diveexiftagger.gpslog;

import java.time.Duration;
import java.time.Instant;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Source which always returns the same coordinates regardless of time. This is
 * useful for tagging a batch of images to a single point, such as a GPS reading
 * manually recorded on a dive boat.
 *
 * @author Nick Radov
 *
 */
public class Fixed extends GpsLogSource {

	private final GpsCoordinates coords;

	/**
	 * Create a new {@code Fixed} based on a single set of GPS coordinates.
	 *
	 * @param coords
	 *            single set of GPS coordinates that will be returned by every
	 *            call to
	 *            {@link #getCoordinatesByTemporalProximity(Instant, Duration)}
	 */
	public Fixed(final GpsCoordinates coords) {
		this.coords = coords;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param instant
	 *            ignored
	 * @param tolerance
	 *            ignored
	 * @return the value of the {@code coords} parameter passed to the
	 *         constructor
	 */
	@Override
	@NonNull
	public GpsCoordinates getCoordinatesByTemporalProximity(@NonNull final Instant instant,
			@NonNull final Duration tolerance) {
		return coords;
	}

}
