package com.github.nradov.diveexiftagger.divelog.dan;

import javax.annotation.Nonnull;

/**
 * Dive profile.
 *
 * @author Nick Radov
 */
final class Zdp extends Segment {

    final static String ID = "ZDP";

    private final NmField time;
    private final NmField depth;
    private final ZceField gasSwitch;
    private final NmField currentPO2;
    private final StField ascentRateViolation;
    private final StField decompressionViolation;
    private final NmField currentCeiling;
    private final NmField currentWaterTemperature;
    private final StField warningNumber;
    private final NmField mainCylinderPressure;
    private final NmField diluentCylinderPressure;
    // data type is blank in the specification
    private final NmField oxygenFlowRate;
    private final NmField cnsToxicity;
    // field name appears to be misspelled in the specification
    private final NmField otu;
    private final NmField ascentRate;

    Zdp(@Nonnull final SegmentGroup parent, @Nonnull final String value) {
        super(parent);
        if (!value.startsWith(ID)) {
            throw new IllegalArgumentException("wrong segment ID");
        }
        final String[] fields = splitFields(value);
        time = fields.length >= 2 ? new NmField(this, fields[1]) : null;
        depth = fields.length >= 3 ? new NmField(this, fields[2]) : null;
        gasSwitch = fields.length >= 4 ? new ZceField(this, fields[3]) : null;
        currentPO2 = fields.length >= 5 ? new NmField(this, fields[4]) : null;
        ascentRateViolation = fields.length >= 6 ? new StField(this, fields[5])
                : null;
        decompressionViolation = fields.length >= 7
                ? new StField(this, fields[6]) : null;
        currentCeiling = fields.length >= 8 ? new NmField(this, fields[7])
                : null;
        currentWaterTemperature = fields.length >= 9
                ? new NmField(this, fields[8]) : null;
        warningNumber = fields.length >= 10 ? new StField(this, fields[9])
                : null;
        mainCylinderPressure = fields.length >= 11
                ? new NmField(this, fields[10]) : null;
        diluentCylinderPressure = fields.length >= 12
                ? new NmField(this, fields[11]) : null;
        oxygenFlowRate = fields.length >= 13 ? new NmField(this, fields[12])
                : null;
        cnsToxicity = fields.length >= 14 ? new NmField(this, fields[13])
                : null;
        otu = fields.length >= 15 ? new NmField(this, fields[14]) : null;
        ascentRate = fields.length >= 16 ? new NmField(this, fields[15]) : null;
    }

    public NmField getTime() {
        return time;
    }

    public NmField getDepth() {
        return depth;
    }

    public ZceField getGasSwitch() {
        return gasSwitch;
    }

    public NmField getCurrentPO2() {
        return currentPO2;
    }

    public StField getAscentRateViolation() {
        return ascentRateViolation;
    }

    public StField getDecompressionViolation() {
        return decompressionViolation;
    }

    public NmField getCurrentCeiling() {
        return currentCeiling;
    }

    public NmField getCurrentWaterTemperature() {
        return currentWaterTemperature;
    }

    public StField getWarningNumber() {
        return warningNumber;
    }

    public NmField getMainCylinderPressure() {
        return mainCylinderPressure;
    }

    public NmField getDiluentCylinderPressure() {
        return diluentCylinderPressure;
    }

    public NmField getOxygenFlowRate() {
        return oxygenFlowRate;
    }

    public NmField getCnsToxicity() {
        return cnsToxicity;
    }

    public NmField getOtu() {
        return otu;
    }

    public NmField getAscentRate() {
        return ascentRate;
    }
}
