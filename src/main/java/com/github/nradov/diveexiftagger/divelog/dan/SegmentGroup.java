package com.github.nradov.diveexiftagger.divelog.dan;

import java.math.BigDecimal;
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

    /**
     * Get the first list element that starts with a particular prefix.
     *
     * @param list
     *            list to search
     * @param prefix
     *            prefix to search for
     * @return first list element starting with prefix, or {@code null} if there
     *         is no match
     */
    static String firstElementStartingWith(@Nonnull final List<String> list,
            @Nonnull final String prefix) {
        for (final String s : list) {
            if (s != null && s.startsWith(prefix)) {
                return s;
            }
        }
        return null;
    }

    private static final long SECONDS_PER_MINUTE = 60;

    private static final BigDecimal MULTIPLICAND = BigDecimal
            .valueOf(SECONDS_PER_MINUTE);

    /**
     * Convert a number of minutes in an NM field to seconds.
     *
     * @param time
     *            minutes in decimal form
     * @return seconds
     */
    static long convertDecimalMinutesToSeconds(final Nm time) {
        final BigDecimal bd = time.toBigDecimal();
        return (bd.longValue() * SECONDS_PER_MINUTE) + bd
                .remainder(BigDecimal.ONE).multiply(MULTIPLICAND).longValue();
    }

}
