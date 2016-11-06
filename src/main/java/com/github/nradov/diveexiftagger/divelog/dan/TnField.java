package com.github.nradov.diveexiftagger.divelog.dan;

final class TnField extends Field<Tn> implements Tn {

    public TnField(final Segment parent, final String value) {
        super(parent, value);
    }

    final class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Tn>
            implements Tn {

        private final NmComponent countryCode;
        private final NmComponent areaCityCode;
        private final NmComponent phoneNumber;
        private final NmComponent extension;
        private final StComponent anyText;

        Repetition(final String value) {
            final String[] components = splitComponents(value);
            countryCode = components.length >= 1
                    ? new NmComponent(this, components[0]) : null;
            areaCityCode = components.length >= 2
                    ? new NmComponent(this, components[1]) : null;
            phoneNumber = components.length >= 3
                    ? new NmComponent(this, components[2]) : null;
            extension = components.length >= 4
                    ? new NmComponent(this, components[3]) : null;
            anyText = components.length >= 5
                    ? new StComponent(this, components[4]) : null;
        }

        @Override
        public Nm getCountryCode() {
            return countryCode;
        }

        @Override
        public Nm getAreaCityCode() {
            return areaCityCode;
        }

        @Override
        public Nm getPhoneNumber() {
            return phoneNumber;
        }

        @Override
        public Nm getExtension() {
            return extension;
        }

        @Override
        public St getAnyText() {
            return anyText;
        }

        @Override
        Field<Tn> getParent() {
            return TnField.this;
        }

    }

    @Override
    Repetition getRepetition(final int index) {
        return (Repetition) super.getRepetition(index);
    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }

    @Override
    public Nm getCountryCode() {
        return getRepetition(0).getCountryCode();
    }

    @Override
    public Nm getAreaCityCode() {
        return getRepetition(0).getAreaCityCode();
    }

    @Override
    public Nm getPhoneNumber() {
        return getRepetition(0).getPhoneNumber();
    }

    @Override
    public Nm getExtension() {
        return getRepetition(0).getExtension();
    }

    @Override
    public St getAnyText() {
        return getRepetition(0).getAnyText();
    }

}
