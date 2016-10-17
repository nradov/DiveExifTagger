package com.github.nradov.diveexiftagger.divelog.dan;

import java.util.List;

/**
 * Segments ZDH, ZDP, ZDT, ZDD, and ZSR repeat for each dive reported.
 *
 * @author Nick Radov
 */
final class DiveGroupZxl extends DiveGroupZxu {

    private final Zdd zdd;
    private final Zsr zsr;

    DiveGroupZxl(final Message parent, final List<String> segments) {
        super(parent, segments);
        this.zdd = firstElementStartingWith(segments, Zdd.ID) == null ? null
                : new Zdd(this, firstElementStartingWith(segments, Zdd.ID));
        this.zsr = firstElementStartingWith(segments, Zsr.ID) == null ? null
                : new Zsr(this, firstElementStartingWith(segments, Zsr.ID));
    }

    public Zdd getZdd() {
        return zdd;
    }

    public Zsr getZsr() {
        return zsr;
    }

}
