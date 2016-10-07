package com.github.nradov.diveexiftagger.divelog.dan;

import java.util.ArrayList;
import java.util.List;

abstract class Field<T extends DataType> {

    final Segment parent;
    final List<Repetition<T>> repetitions = new ArrayList<>();

    Field(final Segment parent) {
        this.parent = parent;
    }

    Field(final Segment parent, final String value) {
        this(parent);
        populateRepetitions(value);
    }

    boolean addRepetition(final Repetition<T> e) {
        return repetitions.add(e);
    }

    void populateRepetitions(final String value) {
        final String[] repetitions = splitRepetitions(value);
        for (final String repetition : repetitions) {
            addRepetition(repetition);
        }
    }

    boolean addRepetition(final String value) {
        return addRepetition(createRepetition(value));
    }

    abstract protected Repetition<T> createRepetition(String value);

    Segment getParent() {
        return parent;
    }

    SegmentGroup getSegmentGroup() {
        return getParent().getParent();
    }

    String getRepetitionSeparatorString() {
        return parent.getRepetitionSeparatorString();
    }

    Repetition<T> getRepetition(final int index) {
        return repetitions.get(index);
    }

    int size() {
        return repetitions.size();
    }

    String[] splitRepetitions(final String s) {
        return s.split(getRepetitionSeparatorString());
    }

    String getComponentSeparatorString() {
        return getSegmentGroup().getComponentSeparatorString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            sb.append(getRepetition(i));
            if (i < size() - 1) {
                sb.append(getRepetitionSeparatorString());
            }
        }
        return sb.toString();
    }
}
