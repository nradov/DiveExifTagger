package com.github.nradov.diveexiftagger.divelog.dan;

final class IsField extends Field<Is> implements Is {

    IsField(final Segment parent, final String value) {
        super(parent, value);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Is>
            implements Is {

        final String value;

        Repetition(final String value) {
            this.value = value;
        }

        @Override
        IsField getParent() {
            return IsField.this;
        }

    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }
}
