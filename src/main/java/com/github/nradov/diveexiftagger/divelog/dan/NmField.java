package com.github.nradov.diveexiftagger.divelog.dan;

import java.math.BigDecimal;

class NmField extends Field<Nm> implements Nm {

    NmField(final Segment parent, final String value) {
        super(parent, value);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Nm>
            implements Nm {

        final BigDecimal value;

        Repetition(final String s) {
            value = s.isEmpty() ? null : new BigDecimal(s);
        }

        @Override
        NmField getParent() {
            return NmField.this;
        }

        @Override
        public BigDecimal toBigDecimal() {
            return value;
        }

        @Override
        public float floatValue() {
            return value.floatValue();
        }

    }

    @Override
    Repetition getRepetition(final int index) {
        return (Repetition) super.getRepetition(index);
    }

    @Override
    public BigDecimal toBigDecimal() {
        return getRepetition(0).toBigDecimal();
    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }

    @Override
    public float floatValue() {
        return getRepetition(0).floatValue();
    }

}
