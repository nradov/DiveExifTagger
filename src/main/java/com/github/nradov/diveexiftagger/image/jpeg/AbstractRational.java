package com.github.nradov.diveexiftagger.image.jpeg;

abstract class AbstractRational<T extends DataType> extends DataType {

    private final T numerator;
    private final T denominator;

    AbstractRational(final T numerator, final T denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public T getNumerator() {
        return numerator;
    }

    public T getDenominator() {
        return denominator;
    }

    @Override
    public String toString() {
        return getNumerator().toString() + " / " + getDenominator().toString();
    }

}
