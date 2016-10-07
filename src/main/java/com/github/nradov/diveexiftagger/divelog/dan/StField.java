package com.github.nradov.diveexiftagger.divelog.dan;

class StField extends Field<St> implements St {

    StField(final Segment parent) {
        super(parent);
    }

    StField(final Segment parent, final String s) {
        super(parent, s);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<St>
            implements St {

        final String value;

        Repetition(final String value) {
            this.value = value;
        }

        @Override
        StField getParent() {
            return StField.this;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }

}
