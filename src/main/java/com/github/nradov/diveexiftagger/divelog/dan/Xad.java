package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Extended address.
 *
 * @author Nick Radov
 */
interface Xad extends DataType {

    St getStreetAddress();

    St getOtherDesignation();

    St getCity();

    St getStateOrProvince();

    St getZipOrPostalCode();

    Id getCountry();

    Id getAddressType();

    St getOtherGeographicDesignation();

}
