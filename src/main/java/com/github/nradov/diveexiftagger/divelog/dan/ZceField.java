package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Coded element field.
 *
 * @author Nick Radov
 */
class ZceField extends Field<Zce> implements Zce {

    ZceField(final Segment parent, final String value) {
        super(parent, value);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Zce>
            implements Zce {

        private final StComponent identifier;
        private final StComponent codingSchemes;
        private final StComponent text;

        Repetition(final String value) {
            final String[] components = splitComponents(value);
            identifier = components.length >= 1
                    ? new StComponent(this, components[0]) : null;
            codingSchemes = components.length >= 2
                    ? new StComponent(this, components[1]) : null;
            text = components.length >= 3 ? new StComponent(this, components[2])
                    : null;
        }

        @Override
        ZceField getParent() {
            return ZceField.this;
        }

        @Override
        public StComponent getIdentifier() {
            return identifier;
        }

        @Override
        public StComponent getCodingSchemes() {
            return codingSchemes;
        }

        @Override
        public StComponent getText() {
            return text;
        }

    }

    @Override
    Repetition getRepetition(final int index) {
        return getRepetition(index);
    }

    @Override
    public StComponent getIdentifier() {
        return getRepetition(0).getIdentifier();
    }

    @Override
    public StComponent getCodingSchemes() {
        return getRepetition(0).getCodingSchemes();
    }

    @Override
    public StComponent getText() {
        return getRepetition(0).getText();
    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }

}
