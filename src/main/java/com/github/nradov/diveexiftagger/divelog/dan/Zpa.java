package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Diver's address.
 *
 * @author Nick Radov
 */
final class Zpa extends Segment {

    private final XadField diverAddress;
    private final TnField phoneNumberHome;
    private final TnField phoneNumberBusiness;
    private final StField emailAddress;
    private final ZceField primaryLanguage;
    private final IsField citizenship;

    Zpa(final Zxl parent, final String value) {
        super(parent);
        final String[] fields = splitFields(value);
        diverAddress = fields.length >= 2 ? new XadField(this, fields[1])
                : null;
        phoneNumberHome = fields.length >= 3 ? new TnField(this, fields[2])
                : null;
        phoneNumberBusiness = fields.length >= 4 ? new TnField(this, fields[3])
                : null;
        emailAddress = fields.length >= 5 ? new StField(this, fields[4]) : null;
        primaryLanguage = fields.length >= 6 ? new ZceField(this, fields[5])
                : null;
        citizenship = fields.length >= 7 ? new IsField(this, fields[6]) : null;
    }

    public XadField getDiverAddress() {
        return diverAddress;
    }

    public TnField getPhoneNumberHome() {
        return phoneNumberHome;
    }

    public TnField getPhoneNumberBusiness() {
        return phoneNumberBusiness;
    }

    public StField getEmailAddress() {
        return emailAddress;
    }

    public ZceField getPrimaryLanguage() {
        return primaryLanguage;
    }

    public IsField getCitizenship() {
        return citizenship;
    }

}
