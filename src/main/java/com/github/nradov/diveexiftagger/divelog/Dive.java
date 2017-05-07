package com.github.nradov.diveexiftagger.divelog;

import java.time.Instant;

import javax.annotation.Nonnull;

import org.checkerframework.checker.units.qual.m;

/**
 * Represents a single dive profile as a series of depths at points in time.
 *
 * @author Nick Radov
 */
public interface Dive extends Comparable<Dive> {

	/**
	 * Get the start time for the dive.
	 *
	 * @return time when the dive started
	 */
	@Nonnull
	Instant getStart();

	/**
	 * Get the end time for the dive.
	 *
	 * @return time when the dive ended
	 */
	@Nonnull
	Instant getEnd();

	/**
	 * Check whether a particular point in time occurred during this dive.
	 *
	 * @param instant
	 *            point in time, which may or may not have been during this dive
	 * @return {@code true} if {@code cal} falls within the start and end time
	 *         (inclusive) of this dive; otherwise {@code false}
	 */
	boolean isDuringDive(@Nonnull Instant instant);

	/**
	 * Get the diver's depth at a particular time.
	 *
	 * @param instant
	 *            time during this dive
	 * @return depth in meters
	 * @throws IllegalArgumentException
	 *             if {@code instant} doesn't fall within the start and end
	 *             times (inclusive) of this dive
	 */
	@m
	float getDepthMeters(@Nonnull Instant instant);

	/**
	 * {@inheritDoc}
	 *
	 * @return a negative integer, zero, or a positive integer as the start time
	 *         of this dive is before, equal to, or after the specified dive.
	 * @see #getStart()
	 */
	@Override
	int compareTo(Dive o);

}
