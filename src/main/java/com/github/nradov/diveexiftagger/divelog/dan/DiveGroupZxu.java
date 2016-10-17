package com.github.nradov.diveexiftagger.divelog.dan;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.github.nradov.diveexiftagger.divelog.Dive;

/**
 * Segments ZDH, ZDP, and ZDT repeat for each dive reported.
 *
 * @author Nick Radov
 */
@Immutable
class DiveGroupZxu extends SegmentGroup implements Dive {

    public static final List<String> SEGMENT_IDS = Collections
            .unmodifiableList(Arrays.asList(Zdh.ID, Zdp.ID, Zdt.ID));

    private final Zdh zdh;
    private final List<Zdp> zdp = new ArrayList<>();
    private final Zdt zdt;

    DiveGroupZxu(final Message parent, final List<String> segments) {
        super(parent);
        zdh = firstElementStartingWith(segments, Zdh.ID) == null ? null
                : new Zdh(this, firstElementStartingWith(segments, Zdh.ID));
        zdt = firstElementStartingWith(segments, Zdt.ID) == null ? null
                : new Zdt(this, firstElementStartingWith(segments, Zdt.ID));

        for (final String segment : segments) {
            if (segment.startsWith(Zdp.ID)) {
                zdp.add(new Zdp(this, segment));
            }
        }
    }

    public Zdh getZdh() {
        return zdh;
    }

    public List<Zdp> getZdp() {
        return Collections.unmodifiableList(zdp);
    }

    public Zdt getZdt() {
        return zdt;
    }

    @Override
    public Instant getStart() {
        return convertTs(getZdh().getLeaveSurface());
    }

    @Override
    public Instant getEnd() {
        return convertTs(getZdt().getReachSurface());
    }

    @Override
    public boolean isDuringDive(@Nonnull final Instant instant) {
        return !(instant.isAfter(getEnd()) || instant.isBefore(getStart()));
    }

    @Override
    public float getDepthMeters(@Nonnull final Instant instant) {
        for (final Zdp profile : zdp) {
            final Nm time = profile.getTime();
            final long seconds = convertDecimalMinutesToSeconds(time);
            if (instant.equals(getStart().plus(seconds, ChronoUnit.SECONDS))) {
                return profile.getDepth().floatValue();
            }
        }
        throw new IllegalArgumentException("not during dive");
    }

    @Override
    public int compareTo(final Dive o) {
        return getStart().compareTo(o.getStart());
    }

}
