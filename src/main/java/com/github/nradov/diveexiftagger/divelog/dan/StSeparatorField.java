package com.github.nradov.diveexiftagger.divelog.dan;

class StSeparatorField extends StField {

    StSeparatorField(final Segment parent, final String value) {
        super(parent);
        addRepetition(createRepetition(value));
    }

}
