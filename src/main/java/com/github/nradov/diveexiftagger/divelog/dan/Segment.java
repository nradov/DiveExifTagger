package com.github.nradov.diveexiftagger.divelog.dan;

import javax.annotation.Nonnull;

class Segment {

    private final SegmentGroup parent;

    Segment(final SegmentGroup parent) {
        this.parent = parent;
    }

    String[] splitFields(final String s) {
        return s.split(escapeRegexConstructs(getFieldSeparatorString()));
    }

    private static String escapeRegexConstructs(final String s) {
        return s.replace("|", "\\|");
    }

    SegmentGroup getParent() {
        return parent;
    }

    St getFieldSeparator() {
        return parent.getFieldSeparator();
    }

    @Nonnull
    String getFieldSeparatorString() {
        return getParent().getFieldSeparatorString();
    }

    @Nonnull
    String getRepetitionSeparatorString() {
        return getParent().getRepetitionSeparatorString();
    }

}
