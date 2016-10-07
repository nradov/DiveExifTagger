package com.github.nradov.diveexiftagger.divelog.dan;

import java.math.BigDecimal;

class NmComponent extends Component<Nm> implements Nm {

    private final BigDecimal value;

    NmComponent(final Repetition<?> parent, final String s) {
        super(parent);
        value = new BigDecimal(s);
    }

    @Override
    public BigDecimal toBigDecimal() {
        return value;
    }

}
