package com.github.nradov.diveexiftagger.divelog.dan;

class CeComponent extends Component<Ce> implements Ce {

    private final StSubcomponent identifier;
    private final StSubcomponent text;
    private final StSubcomponent nameOfCodingSystem;

    CeComponent(final Repetition<?> parent, final String value) {
        super(parent);
        identifier = new StSubcomponent(this, value);
        text = null;
        nameOfCodingSystem = null;
    }

    @Override
    public St getIdentifier() {
        return identifier;
    }

    @Override
    public St getText() {
        return text;
    }

    @Override
    public St getNameOfCodingSystem() {
        return nameOfCodingSystem;
    }

}
