package com.github.nradov.diveexiftagger.divelog.dan;

abstract class Repetition<T extends DataType> extends TypedPiece<T> {

    abstract Field<T> getParent();

    String getComponentSeparatorString() {
        return getParent().getComponentSeparatorString();
    }

    String[] splitComponents(final String s) {
        return s.split(getComponentSeparatorString());
    }

}
