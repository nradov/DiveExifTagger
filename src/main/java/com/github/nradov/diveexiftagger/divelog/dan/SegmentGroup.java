package com.github.nradov.diveexiftagger.divelog.dan;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract class SegmentGroup {

    private final SegmentGroup parent;

    private final List<Segment> segments = new ArrayList<>();

    SegmentGroup() {
        this.parent = null;
    }

    SegmentGroup(final SegmentGroup parent) {
        this.parent = parent;
    }

    /**
     * Terminates a segment record. This value cannot be changed by
     * implementers.
     */
    final static String SEGMENT_TERMINATOR = "\\u0D";

    @Nonnull
    St getFieldSeparator() {
        return parent.getFieldSeparator();
    }

    @Nonnull
    St getEncodingCharacters() {
        return parent.getEncodingCharacters();
    }

    @Nonnull
    final String getFieldSeparatorString() {
        return getFieldSeparator().toString();
    }

    @Nonnull
    final String getComponentSeparatorString() {
        return getEncodingCharacters().toString().substring(0, 1);
    }

    @Nonnull
    final String getRepetitionSeparatorString() {
        return getEncodingCharacters().toString().substring(1, 2);
    }

    @Nonnull
    final String getComponentBracketsString() {
        return getEncodingCharacters().toString().substring(2, 4);
    }

    @Nonnull
    final String getSegmentExtensionString() {
        return getEncodingCharacters().toString().substring(4, 6);
    }

    int size() {
        return segments.size();
    }

    Segment getSegment(final int index) {
        return segments.get(index);
    }

    @Nonnull
    <T extends Segment> Optional<T> getFirstSegment(final Class<T> clazz) {
        return getSegments(clazz).findFirst();
    }

    @SuppressWarnings("unchecked")
    <T extends Segment> Stream<T> getSegments(@Nonnull final Class<T> clazz) {
        return (Stream<T>) segments.stream()
                .filter(segment -> clazz.isAssignableFrom(segment.getClass()));
    }

    @Nullable
    public SegmentGroup getParent() {
        return parent;
    }

    static Instant convertTs(final Ts ts) {
        throw new UnsupportedOperationException();
    }

}
