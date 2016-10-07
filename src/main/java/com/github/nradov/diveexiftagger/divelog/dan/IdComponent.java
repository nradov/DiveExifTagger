package com.github.nradov.diveexiftagger.divelog.dan;

class IdComponent extends Component<Id> implements Id {

    private final String value;

    IdComponent(final Repetition<?> parent, final String value) {
        super(parent);
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
