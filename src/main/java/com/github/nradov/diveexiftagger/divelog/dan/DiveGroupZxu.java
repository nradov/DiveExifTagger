package com.github.nradov.diveexiftagger.divelog.dan;

import java.time.Instant;

import javax.annotation.concurrent.Immutable;

import com.github.nradov.diveexiftagger.divelog.Dive;

@Immutable
class DiveGroupZxu extends SegmentGroup implements Dive {

    private final Zdh zdh;
    private final Zpd zpd;
    private final Zdt zdt;

    DiveGroupZxu(final Message parent, final Zdh zdh, final Zpd zpd,
            final Zdt zdt) {
        super(parent);
        this.zdh = zdh;
        this.zpd = zpd;
        this.zdt = zdt;
    }

    public Zdh getZdh() {
        return zdh;
    }

    public Zpd getZpd() {
        return zpd;
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
    public boolean isDuringDive(final Instant instant) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public float getDepthMeters(final Instant instant) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int compareTo(final Dive o) {
        return getStart().compareTo(o.getStart());
    }

}
