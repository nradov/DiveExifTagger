package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Dive details.
 *
 * @author Nick Radov
 */
final class Zdd extends Segment {
    private final SiField exportSequence;
    private final SiField internalDiveSequence;
    private final ZceField purpose;
    private final ZceField program;
    private final ZceField environment;
    private final ZceField platform;
    private final ZceField divePlan;
    private final ZceField diveTable;
    private final ZceField dress;
    private final ZceField apparatus;
    private final ZceField gasSource;
    private final ZceField breathingGas;
    private final ZceField decompression;

    Zdd(final DiveGroupZxl parent, final String value) {
        super(parent);
        final String[] fields = splitFields(value);
        exportSequence = fields.length >= 2 ? new SiField(this, fields[1])
                : null;
        internalDiveSequence = fields.length >= 3 ? new SiField(this, fields[2])
                : null;
        purpose = fields.length >= 4 ? new ZceField(this, fields[3]) : null;
        program = fields.length >= 5 ? new ZceField(this, fields[4]) : null;
        environment = fields.length >= 6 ? new ZceField(this, fields[5]) : null;
        platform = fields.length >= 7 ? new ZceField(this, fields[6]) : null;
        divePlan = fields.length >= 8 ? new ZceField(this, fields[7]) : null;
        diveTable = fields.length >= 9 ? new ZceField(this, fields[8]) : null;
        dress = fields.length >= 10 ? new ZceField(this, fields[9]) : null;
        apparatus = fields.length >= 11 ? new ZceField(this, fields[10]) : null;
        gasSource = fields.length >= 12 ? new ZceField(this, fields[11]) : null;
        breathingGas = fields.length >= 13 ? new ZceField(this, fields[12])
                : null;
        decompression = fields.length >= 14 ? new ZceField(this, fields[13])
                : null;
    }

    public SiField getExportSequence() {
        return exportSequence;
    }

    public SiField getInternalDiveSequence() {
        return internalDiveSequence;
    }

    public ZceField getPurpose() {
        return purpose;
    }

    public ZceField getProgram() {
        return program;
    }

    public ZceField getEnvironment() {
        return environment;
    }

    public ZceField getPlatform() {
        return platform;
    }

    public ZceField getDivePlan() {
        return divePlan;
    }

    public ZceField getDiveTable() {
        return diveTable;
    }

    public ZceField getDress() {
        return dress;
    }

    public ZceField getApparatus() {
        return apparatus;
    }

    public ZceField getGasSource() {
        return gasSource;
    }

    public ZceField getBreathingGas() {
        return breathingGas;
    }

    public ZceField getDecompression() {
        return decompression;
    }

}
