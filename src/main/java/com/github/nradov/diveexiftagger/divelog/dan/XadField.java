package com.github.nradov.diveexiftagger.divelog.dan;

final class XadField extends Field<Xad> implements Xad {

    XadField(final Segment parent, final String value) {
        super(parent, value);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Xad>
            implements Xad {

        private final StComponent streetAddress;
        private final StComponent otherDesignation;
        private final StComponent city;
        private final StComponent stateOrProvince;
        private final StComponent zipOrPostalCode;
        private final IdComponent country;
        private final IdComponent addressType;
        private final StComponent otherGeographicDesignation;

        Repetition(final String value) {
            final String[] components = splitComponents(value);
            streetAddress = components.length >= 1
                    ? new StComponent(this, components[0]) : null;
            otherDesignation = components.length >= 2
                    ? new StComponent(this, components[1]) : null;
            city = components.length >= 3 ? new StComponent(this, components[2])
                    : null;
            stateOrProvince = components.length >= 4
                    ? new StComponent(this, components[3]) : null;
            zipOrPostalCode = components.length >= 5
                    ? new StComponent(this, components[4]) : null;
            country = components.length >= 6
                    ? new IdComponent(this, components[5]) : null;
            addressType = components.length >= 7
                    ? new IdComponent(this, components[6]) : null;
            otherGeographicDesignation = components.length >= 8
                    ? new StComponent(this, components[7]) : null;
        }

        @Override
        public StComponent getStreetAddress() {
            return streetAddress;
        }

        @Override
        public StComponent getOtherDesignation() {
            return otherDesignation;
        }

        @Override
        public StComponent getCity() {
            return city;
        }

        @Override
        public StComponent getStateOrProvince() {
            return stateOrProvince;
        }

        @Override
        public StComponent getZipOrPostalCode() {
            return zipOrPostalCode;
        }

        @Override
        public IdComponent getCountry() {
            return country;
        }

        @Override
        public IdComponent getAddressType() {
            return addressType;
        }

        @Override
        public StComponent getOtherGeographicDesignation() {
            return otherGeographicDesignation;
        }

        @Override
        Field<Xad> getParent() {
            return XadField.this;
        }

    }

    @Override
    Repetition getRepetition(final int index) {
        return (Repetition) super.getRepetition(index);
    }

    @Override
    public St getStreetAddress() {
        return getRepetition(0).getStreetAddress();
    }

    @Override
    public St getOtherDesignation() {
        return getRepetition(0).getOtherDesignation();
    }

    @Override
    public St getCity() {
        return getRepetition(0).getCity();
    }

    @Override
    public St getStateOrProvince() {
        return getRepetition(0).getStateOrProvince();
    }

    @Override
    public St getZipOrPostalCode() {
        return getRepetition(0).getZipOrPostalCode();
    }

    @Override
    public Id getCountry() {
        return getRepetition(0).getCountry();
    }

    @Override
    public Id getAddressType() {
        return getRepetition(0).getAddressType();
    }

    @Override
    public St getOtherGeographicDesignation() {
        return getRepetition(0).getOtherGeographicDesignation();
    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }

}
