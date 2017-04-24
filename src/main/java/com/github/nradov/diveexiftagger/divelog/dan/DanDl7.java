package com.github.nradov.diveexiftagger.divelog.dan;

import java.time.Instant;

import com.github.nradov.diveexiftagger.divelog.DivesSource;

/**
 * Divers Alert Network Dive Log 7 format.
 *
 * @author Nick Radov
 */
public class DanDl7 implements DivesSource {

    @Override
    public float getDepthMeters(final Instant instant) {
        throw new UnsupportedOperationException();
    }

}
