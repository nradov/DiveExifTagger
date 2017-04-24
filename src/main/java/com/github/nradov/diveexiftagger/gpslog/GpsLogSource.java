package com.github.nradov.diveexiftagger.gpslog;

import java.time.Duration;
import java.time.Instant;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;

/**
 * Source of logged GPS latitude / longitude track data. It can be queried to
 * determine (approximately) where the photographer was when a particular
 * picture was taken. An implementation class is needed for each log file
 * format.
 *
 * @author Nick Radov
 */
public abstract class GpsLogSource {

	protected final SortedSet<GpsCoordinates> coords = new TreeSet<>();

	/**
	 * Get the GPS coordinates that were logged an a particular instant in time.
	 *
	 * @param instant
	 *            target instant in time
	 * @return GPS coordinates that were logged at {@code instant}
	 * @throws IllegalArgumentException
	 *             if no GPS coordinates were logged
	 */
	@Nonnull
	public GpsCoordinates getCoordinates(@Nonnull final Instant instant) {
		return getCoordinatesByTemporalProximity(instant, Duration.ZERO);
	}

	/**
	 * Get the GPS coordinates that were logged closest to a particular instant
	 * in time.
	 *
	 * @param instant
	 *            target instant in time
	 * @param tolerance
	 *            maximum tolerance between {@code instant} and the closest data
	 *            point
	 * @return GPS coordinates that were logged closest to {@code instant} and
	 *         are within {@code tolerance}
	 * @throws IllegalArgumentException
	 *             if no GPS coordinates were logged within {@code tolerance}
	 */
	@Nonnull
	public abstract GpsCoordinates getCoordinatesByTemporalProximity(@Nonnull Instant instant,
			@Nonnull Duration tolerance);

}
