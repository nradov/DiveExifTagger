package com.github.nradov.diveexiftagger.divelog;

import java.time.Instant;

/**
 * Source of zero or more dive profiles. A dive profile is a series of depths at
 * points in time.
 * 
 * @author Nick Radov
 */
public interface DivesSource {

	/**
	 * Get the depth the diver was at for a particular time.
	 * 
	 * @param instant
	 *            time during a dive.
	 * @return depth in meters as a positive number
	 * @throws IllegalArgumentException
	 *             if {@code instant} is not during a dive
	 */
	float getDepthMeters(Instant instant);

}
