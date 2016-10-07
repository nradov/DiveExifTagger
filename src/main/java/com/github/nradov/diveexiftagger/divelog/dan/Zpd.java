package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Diver identification and demographics.
 *
 * @author Nick Radov
 */
class Zpd extends Segment {

    private final CxField externalId;
    private final CxField internalId;
    private final CxField divingLicense;
    private final StField danMemberNumber;
    private final XpnField diverName;
    private final XpnField diverAlias;
    private final StField mothersMaidenName;
    private final DtField dateOfBirth;
    private final StField birthPlace;
    private final IsField sex;
    private final CqField weight;
    private final CqField height;
    private final NmField yearOfFirstCertification;
    private final NmField certificationLevel;
    private final NmField numberOfDivesInPast12Months;
    private final NmField numberOfDivesInPast5Years;
    private final StField medicalConditions;
    private final StField medications;
    private final NmField smokingCigarettes;

    Zpd(final SegmentGroup parent, final String s) {
        super(parent);
        final String[] split = splitFields(s);
        externalId = split.length >= 2 ? new CxField(this, split[1]) : null;
        internalId = split.length >= 3 ? new CxField(this, split[2]) : null;
        divingLicense = split.length >= 4 ? new CxField(this, split[3]) : null;
        danMemberNumber = split.length >= 5 ? new StField(this, split[4])
                : null;
        diverName = split.length >= 6 ? new XpnField(this, split[5]) : null;
        diverAlias = split.length >= 7 ? new XpnField(this, split[6]) : null;
        mothersMaidenName = split.length >= 8 ? new StField(this, split[7])
                : null;
        dateOfBirth = split.length >= 9 ? new DtField(this, split[8]) : null;
        birthPlace = split.length >= 10 ? new StField(this, split[9]) : null;
        sex = split.length >= 11 ? new IsField(this, split[10]) : null;
        weight = split.length >= 12 ? new CqField(this, split[11]) : null;
        height = split.length >= 13 ? new CqField(this, split[12]) : null;
        yearOfFirstCertification = split.length >= 14
                ? new NmField(this, split[13]) : null;
        certificationLevel = split.length >= 15 ? new NmField(this, split[14])
                : null;
        numberOfDivesInPast12Months = split.length >= 16
                ? new NmField(this, split[15]) : null;
        numberOfDivesInPast5Years = split.length >= 17
                ? new NmField(this, split[16]) : null;
        medicalConditions = split.length >= 18 ? new StField(this, split[17])
                : null;
        medications = split.length >= 19 ? new StField(this, split[18]) : null;
        smokingCigarettes = split.length >= 20 ? new NmField(this, split[19])
                : null;
    }

    public CxField getExternalId() {
        return externalId;
    }

    public CxField getInternalId() {
        return internalId;
    }

    public CxField getDivingLicense() {
        return divingLicense;
    }

    public StField getDanMemberNumber() {
        return danMemberNumber;
    }

    public XpnField getDiverName() {
        return diverName;
    }

    public XpnField getDiverAlias() {
        return diverAlias;
    }

    public StField getMothersMaidenName() {
        return mothersMaidenName;
    }

    public DtField getDateOfBirth() {
        return dateOfBirth;
    }

    public IsField getSex() {
        return sex;
    }

    public StField getBirthPlace() {
        return birthPlace;
    }

    public CqField getWeight() {
        return weight;
    }

    public CqField getHeight() {
        return height;
    }

    public NmField getYearOfFirstCertification() {
        return yearOfFirstCertification;
    }

    public NmField getCertificationLevel() {
        return certificationLevel;
    }

    public NmField getNumberOfDivesInPast12Months() {
        return numberOfDivesInPast12Months;
    }

    public NmField getNumberOfDivesInPast5Years() {
        return numberOfDivesInPast5Years;
    }

    public StField getMedicalConditions() {
        return medicalConditions;
    }

    public StField getMedications() {
        return medications;
    }

    public NmField getSmokingCigarettes() {
        return smokingCigarettes;
    }
}
