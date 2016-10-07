package com.github.nradov.diveexiftagger.divelog.dan;

import java.time.Instant;

class DtField extends Field<Dt> implements Dt {

    DtField(final Segment parent, final String value) {
        super(parent, value);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Dt>
            implements Dt {

        Repetition(final String value) {

        }

        @Override
        DtField getParent() {
            return DtField.this;
        }

        @Override
        public Instant toInstant() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }

    @Override
    Repetition getRepetition(final int index) {
        return (Repetition) super.getRepetition(index);
    }

    @Override
    public Instant toInstant() {
        // TODO Auto-generated method stub
        return getRepetition(0).toInstant();
    }
}
