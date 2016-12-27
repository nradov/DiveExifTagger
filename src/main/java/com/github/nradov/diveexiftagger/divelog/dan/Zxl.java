package com.github.nradov.diveexiftagger.divelog.dan;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Diver demographics, Dive profile &amp; Dive log details.
 *
 * @author Nick Radov
 */
public final class Zxl extends Zxu {

    private Zpd zpd;
    private Zpa zpa;

    public Zxl(final Path path) throws IOException {
        super(path);
    }

    Zpd getZpd() {
        return zpd;
    }

    Zpa getZpa() {
        return zpa;
    }

    @Override
    DiveGroupZxl getDiveGroup(final int index) {
        return (DiveGroupZxl) super.getDiveGroup(index);
    }

}
