package com.github.nradov.diveexiftagger.image.jpeg;

import javax.annotation.Nonnull;

abstract class AbstractRational<T extends DataType> extends DataType {

    @Nonnull
    private final T numerator;
    @Nonnull
    private final T denominator;

    AbstractRational(@Nonnull final T numerator, @Nonnull final T denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Nonnull
    public T getDenominator() {
        return denominator;
    }

    @Nonnull
    public T getNumerator() {
        return numerator;
    }

    @Override
    public String toString() {
        return getNumerator().toString() + " / " + getDenominator().toString();
    }

}
