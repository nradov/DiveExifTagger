package com.github.nradov.diveexiftagger.divelog.dan;

class StComponent extends Component<St> implements St {

    final String value;

    StComponent(final Repetition<?> parent, final String value) {
        super(parent);
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
