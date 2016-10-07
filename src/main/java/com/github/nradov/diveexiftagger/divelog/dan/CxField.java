package com.github.nradov.diveexiftagger.divelog.dan;

class CxField extends Field<Cx> implements Cx {

    CxField(final Segment parent, final String s) {
        super(parent, s);
    }

    class Repetition
            extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Cx>
            implements Cx {

        private final StComponent id;
        private final StComponent checkDigit;
        private final IdComponent codeIdentifyingTheCheckDigitSchemeEmployed;
        private final HdComponent assigningAuthority;
        private final IsComponent identifierTypeCode;

        Repetition(final String value) {
            final String[] components = splitComponents(value);
            id = components.length >= 1 ? new StComponent(this, components[0])
                    : null;
            checkDigit = components.length >= 2
                    ? new StComponent(this, components[1]) : null;
            codeIdentifyingTheCheckDigitSchemeEmployed = components.length >= 3
                    ? new IdComponent(this, components[2]) : null;
            assigningAuthority = components.length >= 4
                    ? new HdComponent(this, components[3]) : null;
            identifierTypeCode = components.length >= 5
                    ? new IsComponent(this, components[4]) : null;
        }

        @Override
        CxField getParent() {
            return CxField.this;
        }

        public StComponent getId() {
            return id;
        }

        public StComponent getCheckDigit() {
            return checkDigit;
        }

        public IdComponent getCodeIdentifyingTheCheckDigitSchemeEmployed() {
            return codeIdentifyingTheCheckDigitSchemeEmployed;
        }

        public HdComponent getAssigningAuthority() {
            return assigningAuthority;
        }

        public IsComponent getIdentifierTypeCode() {
            return identifierTypeCode;
        }

    }

    @Override
    protected Repetition createRepetition(final String value) {
        return new Repetition(value);
    }

}
