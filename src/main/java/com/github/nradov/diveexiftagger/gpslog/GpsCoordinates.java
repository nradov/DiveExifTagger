package com.github.nradov.diveexiftagger.gpslog;

import javax.annotation.Nonnull;

import org.apache.commons.imaging.common.RationalNumber;

public final class GpsCoordinates {

	private final double latitude;
	private final double longitude;

	public GpsCoordinates(final double latitude, final double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Nonnull
	public RationalNumber getLatitude() {
		return convertDouble(latitude);
	}

	@Nonnull
	public RationalNumber getLongitude() {
		return convertDouble(longitude);
	}

	private static final int DENOMINATOR = 1000000;
	
	@Nonnull
	private static RationalNumber convertDouble(final double d) {
		return new RationalNumber((int) (d * DENOMINATOR), DENOMINATOR);
	}

}
