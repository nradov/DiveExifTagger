package com.github.nradov.diveexiftagger.divelog.dan;

import java.time.Instant;

class TsField extends Field<Ts> implements Ts {

    TsField(final Segment parent, final String value) {
        super(parent, value);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Ts>
            implements Ts {

        Repetition(final String value) {

        }

        @Override
        public Instant toInstant() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        Field<Ts> getParent() {
            return TsField.this;
        }

    }

    @Override
    Repetition getRepetition(final int index) {
        return (Repetition) super.getRepetition(index);
    }

    @Override
    public Instant toInstant() {
        return getRepetition(0).toInstant();
    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }

}
