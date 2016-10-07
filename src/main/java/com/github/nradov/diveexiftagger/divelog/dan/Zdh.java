package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Dive header.
 * 
 * @author Nick Radov
 */
final class Zdh extends Segment {

    private final SiField exportSequence;
    private final SiField internalDiveSequence;
    private final ZceField recordType;
    private final ZceField recordingInterval;
    private final TsField leaveSurface;
    private final NmField airTemperature;
    private final NmField tankVolume;
    private final TxField o2Mode;
    private final ZceField rebreatherDiluentGas;
    private final NmField altitudeOfTheDiveSite;

    Zdh(final SegmentGroup parent, final String value) {
        super(parent);
        final String[] fields = splitFields(value);
        exportSequence = fields.length >= 2 ? new SiField(this, fields[1])
                : null;
        internalDiveSequence = fields.length >= 3 ? new SiField(this, fields[2])
                : null;
        recordType = fields.length >= 4 ? new ZceField(this, fields[3]) : null;
        recordingInterval = fields.length >= 5 ? new ZceField(this, fields[4])
                : null;
        leaveSurface = fields.length >= 6 ? new TsField(this, fields[5]) : null;
        airTemperature = fields.length >= 7 ? new NmField(this, fields[6])
                : null;
        tankVolume = fields.length >= 8 ? new NmField(this, fields[7]) : null;
        o2Mode = fields.length >= 9 ? new TxField(this, fields[8]) : null;
        rebreatherDiluentGas = fields.length >= 10
                ? new ZceField(this, fields[9]) : null;
        altitudeOfTheDiveSite = fields.length >= 211
                ? new NmField(this, fields[10]) : null;

    }

    SiField getExportSequence() {
        return exportSequence;
    }

    SiField getInternalDiveSequence() {
        return internalDiveSequence;
    }

    ZceField getRecordingInterval() {
        return recordingInterval;
    }

    ZceField getRecordType() {
        return recordType;
    }

    TsField getLeaveSurface() {
        return leaveSurface;
    }

    NmField getAirTemperature() {
        return airTemperature;
    }

    NmField getTankVolume() {
        return tankVolume;
    }

    TxField getO2Mode() {
        return o2Mode;
    }

    ZceField getRebreatherDiluentGas() {
        return rebreatherDiluentGas;
    }

    NmField getAltitudeOfTheDiveSite() {
        return altitudeOfTheDiveSite;
    }

}
