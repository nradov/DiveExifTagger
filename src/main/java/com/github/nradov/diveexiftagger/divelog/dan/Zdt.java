package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Dive trailer.
 *
 * @author Nick Radov
 */
final class Zdt extends Segment {

    final static String ID = "ZDT";

    private final SiField exportSequence;
    private final SiField internalDiveSequence;
    private final NmField maximumDepth;
    private final TsField reachSurface;
    private final NmField minWaterTemperature;
    private final NmField pressureDropMainTank;

    Zdt(final SegmentGroup parent, final String value) {
        super(parent);
        final String[] fields = splitFields(value);
        exportSequence = fields.length >= 2 ? new SiField(this, fields[1])
                : null;
        internalDiveSequence = fields.length >= 3 ? new SiField(this, fields[2])
                : null;
        maximumDepth = fields.length >= 4 ? new NmField(this, fields[3]) : null;
        reachSurface = fields.length >= 5 ? new TsField(this, fields[4]) : null;
        minWaterTemperature = fields.length >= 6 ? new NmField(this, fields[5])
                : null;
        pressureDropMainTank = fields.length >= 7 ? new NmField(this, fields[6])
                : null;
    }

    public SiField getExportSequence() {
        return exportSequence;
    }

    public NmField getMaximumDepth() {
        return maximumDepth;
    }

    public SiField getInternalDiveSequence() {
        return internalDiveSequence;
    }

    public NmField getMinWaterTemperature() {
        return minWaterTemperature;
    }

    public TsField getReachSurface() {
        return reachSurface;
    }

    public NmField getPressureDropMainTank() {
        return pressureDropMainTank;
    }

}
