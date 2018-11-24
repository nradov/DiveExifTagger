package com.github.nradov.diveexiftagger.divelog.dan;

import org.checkerframework.checker.nullness.qual.NonNull;

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

    @NonNull
    String getFieldSeparatorString() {
        return getParent().getFieldSeparatorString();
    }

    @NonNull
    String getComponentSeparatorString() {
        return getParent().getComponentSeparatorString();
    }

    @NonNull
    String getRepetitionSeparatorString() {
        return getParent().getRepetitionSeparatorString();
    }

}
