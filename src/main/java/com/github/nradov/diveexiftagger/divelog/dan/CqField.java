package com.github.nradov.diveexiftagger.divelog.dan;

class CqField extends Field<Cq> implements Cq {

    CqField(final Segment parent, final String s) {
        super(parent, s);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Cq>
            implements Cq {

        private final NmComponent quantity;
        private final CeComponent units;

        Repetition(final String s) {
            final String[] components = splitComponents(s);
            quantity = components.length >= 1
                    ? new NmComponent(this, components[0]) : null;
            units = components.length >= 2
                    ? new CeComponent(this, components[1]) : null;
        }

        @Override
        Field<Cq> getParent() {
            return CqField.this;
        }

        @Override
        public NmComponent getQuantity() {
            return quantity;
        }

        @Override
        public CeComponent getUnits() {
            return units;
        }

    }

    @Override
    Repetition getRepetition(final int index) {
        return getRepetition(index);
    }

    @Override
    public Nm getQuantity() {
        return getRepetition(0).getQuantity();
    }

    @Override
    public Ce getUnits() {
        return getRepetition(0).getUnits();
    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }
}
