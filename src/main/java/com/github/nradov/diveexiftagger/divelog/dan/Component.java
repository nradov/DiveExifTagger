package com.github.nradov.diveexiftagger.divelog.dan;

abstract class Component<T extends DataType> extends TypedPiece<T> {

    private final Repetition<?> parent;

    Component(final Repetition<?> parent) {
        this.parent = parent;
    }

    Repetition<?> getParent() {
        return parent;
    }

}
