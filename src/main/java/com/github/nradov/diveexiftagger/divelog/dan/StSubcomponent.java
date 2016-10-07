package com.github.nradov.diveexiftagger.divelog.dan;

class StSubcomponent extends Subcomponent<St> implements St {

    private final String value;

    StSubcomponent(final Component<?> parent, final String value) {
        super(parent);
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
