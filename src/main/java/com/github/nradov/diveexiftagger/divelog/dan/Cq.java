package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Composite quantity with units.
 *
 * @author Nick Radov
 */
interface Cq extends DataType {

    /**
     * Quantity.
     */
    Nm getQuantity();

    Ce getUnits();

}
