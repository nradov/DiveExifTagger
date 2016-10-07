package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Record header.
 *
 * @author Nick Radov
 */
class Zrh extends Segment {

    private final StField fieldSeparator;
    private final StField encodingCharacters;
    private final ZceField recordingDiveComputer;
    private final StField rdcSerialNumber;
    private final ZceField depthPressureUnit;
    private final ZceField altitudeUnit;
    private final ZceField temperatureUnit;
    private final ZceField tankPressureUnit;
    private final ZceField tankVolumeUnit;

    Zrh(final Message parent, final String s) {
        super(parent);
        if (s.length() < 10) {
            throw new IllegalArgumentException("too short");
        }
        fieldSeparator = new StField(this, s.substring(3, 4));
        final String[] fields = splitFields(s.substring(4));
        encodingCharacters = new StField(this, fields[0]);
        recordingDiveComputer = fields.length >= 2
                ? new ZceField(this, fields[1]) : null;
        rdcSerialNumber = fields.length >= 3 ? new StField(this, fields[2])
                : null;
        depthPressureUnit = fields.length >= 4 ? new ZceField(this, fields[3])
                : null;
        altitudeUnit = fields.length >= 5 ? new ZceField(this, fields[4])
                : null;
        temperatureUnit = fields.length >= 6 ? new ZceField(this, fields[5])
                : null;
        tankPressureUnit = fields.length >= 7 ? new ZceField(this, fields[6])
                : null;
        tankVolumeUnit = fields.length >= 8 ? new ZceField(this, fields[7])
                : null;
    }

    @Override
    public StField getFieldSeparator() {
        return fieldSeparator;
    }

    public StField getEncodingCharacters() {
        return encodingCharacters;
    }

    public ZceField getRecordingDiveComputer() {
        return recordingDiveComputer;
    }

    public StField getRdcSerialNumber() {
        return rdcSerialNumber;
    }

    public ZceField getDepthPressureUnit() {
        return depthPressureUnit;
    }

    public ZceField getTemperatureUnit() {
        return temperatureUnit;
    }

    public ZceField getAltitudeUnit() {
        return altitudeUnit;
    }

    public ZceField getTankPressureUnit() {
        return tankPressureUnit;
    }

    public ZceField getTankVolumeUnit() {
        return tankVolumeUnit;
    }

}
