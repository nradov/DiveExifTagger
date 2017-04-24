package com.github.nradov.diveexiftagger.gpslog;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;

import javax.annotation.Nonnull;

/**
 * A single set of 2-D (without altitude) or 3-D (with altitude) GPS
 * coordinates.
 *
 * @author Nick Radov
 */
public final class GpsCoordinates implements Comparable<GpsCoordinates> {

	@Nonnull
	private final Instant timestamp;
	private final double latitude;
	private final double longitude;
	private final double altitude;

	/**
	 * Create a new {@code GpsCoordinates} without an altitude fix.
	 *
	 * @param latitude
	 *            latitude in degrees between -90 and +90 (inclusive)
	 * @param longitude
	 *            longitude in degrees between -180 and +180 (inclusive)
	 */
	public GpsCoordinates(@Nonnull final Instant timestamp, final double latitude, final double longitude) {
		this(timestamp, latitude, longitude, Double.NaN);
	}

	private static final double NORTH_POLE_LATITUDE = 90d;
	private static final double SOUTH_POLE_LATITUDE = -90d;

	private static final double MAX_LONGITUDE = 180d;
	private static final double MIN_LONGITUDE = -180d;

	/**
	 * Create a new {@code GpsCoordinates}, possibly with an altitude fix.
	 *
	 * @param latitude
	 *            latitude in degrees between -90 and +90 (inclusive)
	 * @param longitude
	 *            longitude in degrees between -180 and +180 (inclusive)
	 * @param altitude
	 *            altitude in meters (negative for below sea level); pass
	 *            {@link Double#NaN} if there is no altitude fix for this point
	 * @throws IllegalArgumentException
	 *             if {@code latitude} or {@code longitude} is out of range
	 */
	public GpsCoordinates(@Nonnull final Instant timestamp, final double latitude, final double longitude,
			final double altitude) {
		this.timestamp = timestamp;
		if (latitude > NORTH_POLE_LATITUDE || latitude < SOUTH_POLE_LATITUDE) {
			throw new IllegalArgumentException("latitude out of range");
		}
		if (longitude > MAX_LONGITUDE || longitude < MIN_LONGITUDE) {
			throw new IllegalArgumentException("longitude out of range");
		}

		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the latitude.
	 *
	 * @return latitude in the range -90 to +90
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Get the longitude.
	 *
	 * @return longitude in the range -180 to +180
	 */
	@Nonnull
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Check whether this set of coordinates includes an altitude fix.
	 *
	 * @return {@code true} if this point has a valid altitude (depth);
	 *         otherwise {@code false}
	 */
	public boolean hasAltitude() {
		return !Double.isNaN(altitude);
	}

	/**
	 * Get the altitude.
	 *
	 * @return altitude in meters (negative for below sea level)
	 * @throws IllegalStateException
	 *             if this set of coordinates doesn't include an altitude
	 */
	@Nonnull
	public double getAltitude() {
		if (hasAltitude()) {
			return altitude;
		} else {
			throw new IllegalStateException("no altitude");
		}
	}

	@Override
	public int compareTo(final GpsCoordinates o) {
		return timestamp.compareTo(o.timestamp);
	}

	/**
	 * Perform a linear interpolation between this point and another point.
	 *
	 * @param target
	 *            time between this point and the other point
	 * @param other
	 *            the other point
	 * @return new set of GPS coordinates interpolated between this point and
	 *         the other point; the altitude will only be set if both original
	 *         points have an altitude
	 * @throws IllegalArgumentException
	 *             if {@code target} is not inside the time period bounded by
	 *             this point and {@code other} (can only interpolate between
	 *             two points)
	 */
	@Nonnull
	public GpsCoordinates interpolate(@Nonnull final Instant target, @Nonnull final GpsCoordinates other) {
		if ((target.isBefore(timestamp) && target.isBefore(other.timestamp))
				|| (target.isAfter(timestamp) && target.isAfter(other.timestamp))) {
			throw new IllegalArgumentException(
					target + " is not inside the period " + timestamp + " to " + other.timestamp);
		}

		final BigDecimal x0 = new BigDecimal(timestamp.toEpochMilli(), MathContext.DECIMAL128);
		final BigDecimal x1 = new BigDecimal(other.timestamp.toEpochMilli(), MathContext.DECIMAL128);
		final BigDecimal x = new BigDecimal(target.toEpochMilli(), MathContext.DECIMAL128);

		final double latitude = interpolate(x0, new BigDecimal(getLatitude(), MathContext.DECIMAL128), x1,
				new BigDecimal(other.getLatitude(), MathContext.DECIMAL128), x);
		// TODO: fix this so it works properly crossing from -180 to +180
		// (discontinuity)
		final double longitude = interpolate(x0, new BigDecimal(getLongitude(), MathContext.DECIMAL128), x1,
				new BigDecimal(other.getLongitude(), MathContext.DECIMAL128), x);
		final double altitude;
		if (hasAltitude() && other.hasAltitude()) {
			altitude = interpolate(x0, new BigDecimal(getAltitude(), MathContext.DECIMAL128), x1,
					new BigDecimal(other.getAltitude(), MathContext.DECIMAL128), x);
		} else {
			altitude = Double.NaN;
		}
		return new GpsCoordinates(target, latitude, longitude, altitude);
	}

	/**
	 * Perform a linear interpolation between two points.
	 *
	 * @see <a target="_" title="Wikipedia" href=
	 *      "https://en.wikipedia.org/wiki/Linear_interpolation">Linear
	 *      interpolation</a>
	 */
	private static double interpolate(final BigDecimal x0, final BigDecimal y0, final BigDecimal x1,
			final BigDecimal y1, final BigDecimal x) {
		return y0.multiply(BigDecimal.ONE.subtract(x.subtract(x0).divide(x1.subtract(x0))))
				.add(y1.multiply(BigDecimal.ONE.subtract(x1.subtract(x).divide(x1.subtract(x0))))).doubleValue();
	}
}
