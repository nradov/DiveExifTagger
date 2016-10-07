package com.github.nradov.diveexiftagger.divelog.dan;

final class DiveGroupZxl extends DiveGroupZxu {

    private final Zdd zdd;
    private final Zsr zsr;

    DiveGroupZxl(final Message parent, final Zdh zdh, final Zpd zpd,
            final Zdt zdt, final Zdd zdd, final Zsr zsr) {
        super(parent, zdh, zpd, zdt);
        this.zdd = zdd;
        this.zsr = zsr;
    }

    public Zdd getZdd() {
        return zdd;
    }

    public Zsr getZsr() {
        return zsr;
    }

}
