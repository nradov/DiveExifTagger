package com.github.nradov.diveexiftagger.divelog;

import java.time.Instant;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.qual.m;

public class Point {

	private final Instant instant;

	@m
	private final float depth;

	public Point(@NonNull final Instant instant, @m final float depth) {
		this.instant = instant;
		this.depth = depth;
	}

	public Instant getInstant() {
		return instant;
	}

	@m
	public float getDepth() {
		return depth;
	}

}
