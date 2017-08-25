package com.github.nradov.diveexiftagger.gpslog;

import java.time.Duration;
import java.time.Instant;
import java.util.SortedSet;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Source of logged GPS latitude / longitude track data. It can be queried to
 * determine (approximately) where the photographer was when a particular
 * picture was taken. An implementation class is needed for each log file
 * format.
 *
 * @author Nick Radov
 */
public abstract class GpsLogSource {

	private boolean interpolate = false;

	private final SortedSet<GpsCoordinates> coords = new TreeSet<>();

	boolean addPoint(final GpsCoordinates point) {
		return coords.add(point);
	}

	/**
	 * Get the GPS coordinates that were logged an a particular instant in time.
	 *
	 * @param instant
	 *            target instant in time
	 * @return GPS coordinates that were logged at {@code instant}
	 * @throws IllegalArgumentException
	 *             if no GPS coordinates were logged
	 */
	@NonNull
	public GpsCoordinates getCoordinates(@NonNull final Instant instant) {
		return getCoordinatesByTemporalProximity(instant, Duration.ZERO);
	}

	@NonNull
	public GpsCoordinates getCoordinatesByTemporalProximity(@NonNull final Instant instant,
			@NonNull final Duration tolerance) {
		final Instant low = instant.minus(tolerance);
		final Instant high = instant.plus(tolerance);

		// TODO: optimize this to use a binary search instead of iteration so
		// that it's O(log(n)) instead of O(n)
		for (final GpsCoordinates point : coords) {
			if (point.getTimestamp().equals(instant)) {
				// exact match
				return point;
			} else if (low.compareTo(point.getTimestamp()) <= 0 && high.compareTo(point.getTimestamp()) >= 0) {
				// within tolerance
				// TODO: interpolate between the two closest points
				return point;
			}
		}
		throw new IllegalArgumentException("no coordinates at " + instant);
	}

	public boolean isInterpolate() {
		return interpolate;
	}

	/**
	 * Set whether to interpolate between the two closest points.
	 * 
	 * @param interpolate
	 */
	public void setInterpolate(final boolean interpolate) {
		this.interpolate = interpolate;
	}

}
