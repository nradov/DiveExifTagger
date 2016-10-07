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
            value = new BigDecimal(s);
        }

        @Override
        NmField getParent() {
            return NmField.this;
        }

        @Override
        public BigDecimal toBigDecimal() {
            return value;
        }

    }

    @Override
    Repetition getRepetition(final int index) {
        return (Repetition) super.getRepetition(index);
    }

    @Override
    public BigDecimal toBigDecimal() {
        return this.getRepetition(0).toBigDecimal();
    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }

}
