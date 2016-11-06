package com.github.nradov.diveexiftagger.gpslog;

import javax.annotation.Nonnull;

/**
 * A single set of 2-D (without altitude) or 3-D (with altitude) GPS
 * coordinates.
 *
 * @author Nick Radov
 */
public final class GpsCoordinates {

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
    public GpsCoordinates(final double latitude, final double longitude) {
        this(latitude, longitude, Double.NaN);
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
    public GpsCoordinates(final double latitude, final double longitude,
            final double altitude) {
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

    /**
     * Get the latitude.
     *
     * @return latitude in the range -90 to +90
     */
    @Nonnull
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

}
