package com.github.nradov.diveexiftagger.divelog.dan;

import java.math.BigInteger;

final class SiField extends Field<Si> implements Si {

    SiField(final Segment parent, final String value) {
        super(parent, value);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Si>
            implements Si {

        final BigInteger value;

        Repetition(final String value) {
            this.value = new BigInteger(value);
        }

        @Override
        SiField getParent() {
            return SiField.this;
        }

        @Override
        public BigInteger toBigInteger() {
            return value;
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
    public BigInteger toBigInteger() {
        return getRepetition(0).toBigInteger();
    }

}
