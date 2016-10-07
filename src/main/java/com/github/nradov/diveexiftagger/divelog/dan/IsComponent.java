package com.github.nradov.diveexiftagger.divelog.dan;

class IsComponent extends Component<Is> implements Is {

    private final String value;

    IsComponent(final Repetition<?> parent, final String value) {
        super(parent);
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
