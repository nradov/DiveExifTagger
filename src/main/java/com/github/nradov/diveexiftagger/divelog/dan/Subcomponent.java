package com.github.nradov.diveexiftagger.divelog.dan;

class Subcomponent<T extends DataType> extends TypedPiece<T> {

    private final Component<?> parent;

    Subcomponent(final Component<?> parent) {
        this.parent = parent;
    }

    public Component<?> getParent() {
        return parent;
    }

}
