package com.github.nradov.diveexiftagger.gpslog;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nonnull;

/**
 * Source of logged GPS latitude / longitude data. It can be queried to
 * determine (approximately) where the photographer was when a particular
 * picture was taken. An implementation class is needed for each log file
 * format.
 * 
 * @author Nick Radov
 */
public interface GpsLogSource {

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
	 *             if {@code instant} is negative, or if no GPS coordinates were
	 *             logged within {@code tolerance}
	 */
	@Nonnull
	GpsCoordinates getCoordinatesByTemporalProximity(Instant instant, Duration tolerance);

}
