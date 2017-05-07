package com.github.nradov.diveexiftagger.divelog;

import java.time.Instant;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.qual.m;

/**
 * Source of zero or more dive profiles. A dive profile is a series of depths at
 * points in time.
 *
 * @author Nick Radov
 */
public interface DivesSource {

	/**
	 * Get the depth the diver was at for a particular time. For lake dives
	 * above sea level (altitude diving) a further adjustment may be needed
	 * before saving this value in an EXIF GPS altitude field.
	 *
	 * @param instant
	 *            time during a dive.
	 * @return depth in meters as a positive number
	 * @throws IllegalArgumentException
	 *             if {@code instant} is not during a dive
	 */
	@m
	float getDepthMeters(@NonNull Instant instant);

}
