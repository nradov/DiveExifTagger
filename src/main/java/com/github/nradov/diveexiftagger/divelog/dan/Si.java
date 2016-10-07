package com.github.nradov.diveexiftagger.divelog.dan;

import java.math.BigInteger;

/**
 * Sequence ID. A non-negative integer in the form of a NM field. The uses of
 * this data type are defined in the chapters defining the segments and messages
 * in which it appears.
 *
 * @author Nick Radov
 */
interface Si extends DataType {

    BigInteger toBigInteger();

}
