package com.github.nradov.diveexiftagger.divelog.dan;

class XpnField extends Field<Xpn> implements Xpn {

    XpnField(final Segment parent, final String value) {
        super(parent, value);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Xpn>
            implements Xpn {

        private final StComponent familyName;
        private final StComponent givenName;
        private final StComponent middleInitialOrName;
        private final StComponent suffix;
        private final StComponent prefix;
        private final StComponent degree;

        Repetition(final String value) {
            final String[] components = splitComponents(value);
            familyName = components.length >= 1
                    ? new StComponent(this, components[0]) : null;
            givenName = components.length >= 2
                    ? new StComponent(this, components[1]) : null;
            middleInitialOrName = components.length >= 3
                    ? new StComponent(this, components[2]) : null;
            suffix = components.length >= 4
                    ? new StComponent(this, components[3]) : null;
            prefix = components.length >= 5
                    ? new StComponent(this, components[4]) : null;
            degree = components.length >= 6
                    ? new StComponent(this, components[5]) : null;
        }

        @Override
        public StComponent getFamilyName() {
            return familyName;
        }

        @Override
        public StComponent getGivenName() {
            return givenName;
        }

        @Override
        public StComponent getMiddleInitialOrName() {
            return middleInitialOrName;
        }

        @Override
        public StComponent getSuffix() {
            return suffix;
        }

        @Override
        public StComponent getPrefix() {
            return prefix;
        }

        @Override
        public StComponent getDegree() {
            return degree;
        }

        @Override
        XpnField getParent() {
            return XpnField.this;
        }

    }

    @Override
    Repetition getRepetition(final int index) {
        return this.getRepetition(index);
    }

    @Override
    public StComponent getFamilyName() {
        return getRepetition(0).getFamilyName();
    }

    @Override
    public StComponent getGivenName() {
        return getRepetition(0).getGivenName();
    }

    @Override
    public StComponent getMiddleInitialOrName() {
        return getRepetition(0).getMiddleInitialOrName();
    }

    @Override
    public StComponent getSuffix() {
        return getRepetition(0).getSuffix();
    }

    @Override
    public StComponent getPrefix() {
        return getRepetition(0).getPrefix();
    }

    @Override
    public StComponent getDegree() {
        return getRepetition(0).getDegree();
    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }

}
