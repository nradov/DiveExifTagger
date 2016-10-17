package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Safety and outcome report.
 *
 * @author Nick Radov
 */
final class Zsr extends Segment {

    static final String ID = "ZSR";

    private final SiField exportSequence;
    private final SiField internalDiveSequence;
    private final ZceField stateOfRestBeforeDive;
    private final NmField alcoholBeforeDive;
    private final ZceField exerciseBeforeDive;
    private final StField medicationBeforeDive;
    private final NmField visibility;
    private final NmField current;
    private final ZceField thermalComfort;
    private final ZceField workload;
    private final ZceField problems;
    private final ZceField equipmentMalfunctions;
    private final NmField anySymptoms;
    private final NmField exposureToAltitude;
    private final StField comments;
    private final NmField surfaceIntervalBeforeAltitudeExposure;
    private final DtField dateOfFlight;
    private final NmField totalLengthOfExposure;
    private final NmField altitudeOfExposure;
    private final NmField wereYouTreatedInHyperbaricChamber;
    private final StField nameAndLocationOfChamber;
    private final NmField numberOfRecompressionTreatments;

    Zsr(final DiveGroupZxl parent, final String value) {
        super(parent);
        final String[] fields = splitFields(value);
        exportSequence = fields.length >= 2 ? new SiField(this, fields[1])
                : null;
        internalDiveSequence = fields.length >= 3 ? new SiField(this, fields[2])
                : null;
        stateOfRestBeforeDive = fields.length >= 4
                ? new ZceField(this, fields[3]) : null;
        alcoholBeforeDive = fields.length >= 5 ? new NmField(this, fields[4])
                : null;
        exerciseBeforeDive = fields.length >= 6 ? new ZceField(this, fields[5])
                : null;
        medicationBeforeDive = fields.length >= 7 ? new StField(this, fields[6])
                : null;
        visibility = fields.length >= 8 ? new NmField(this, fields[7]) : null;
        current = fields.length >= 9 ? new NmField(this, fields[8]) : null;
        thermalComfort = fields.length >= 10 ? new ZceField(this, fields[9])
                : null;
        workload = fields.length >= 11 ? new ZceField(this, fields[10]) : null;
        problems = fields.length >= 12 ? new ZceField(this, fields[11]) : null;
        equipmentMalfunctions = fields.length >= 13
                ? new ZceField(this, fields[12]) : null;
        anySymptoms = fields.length >= 14 ? new NmField(this, fields[13])
                : null;
        exposureToAltitude = fields.length >= 15 ? new NmField(this, fields[14])
                : null;
        comments = fields.length >= 16 ? new StField(this, fields[15]) : null;
        surfaceIntervalBeforeAltitudeExposure = fields.length >= 17
                ? new NmField(this, fields[16]) : null;
        dateOfFlight = fields.length >= 18 ? new DtField(this, fields[17])
                : null;
        totalLengthOfExposure = fields.length >= 19
                ? new NmField(this, fields[18]) : null;
        altitudeOfExposure = fields.length >= 20 ? new NmField(this, fields[19])
                : null;
        wereYouTreatedInHyperbaricChamber = fields.length >= 21
                ? new NmField(this, fields[20]) : null;
        nameAndLocationOfChamber = fields.length >= 22
                ? new StField(this, fields[21]) : null;
        numberOfRecompressionTreatments = fields.length >= 23
                ? new NmField(this, fields[22]) : null;
    }

    public SiField getExportSequence() {
        return exportSequence;
    }

    public SiField getInternalDiveSequence() {
        return internalDiveSequence;
    }

    public ZceField getStateOfRestBeforeDive() {
        return stateOfRestBeforeDive;
    }

    public NmField getAlcoholBeforeDive() {
        return alcoholBeforeDive;
    }

    public ZceField getExerciseBeforeDive() {
        return exerciseBeforeDive;
    }

    public StField getMedicationBeforeDive() {
        return medicationBeforeDive;
    }

    public NmField getVisibility() {
        return visibility;
    }

    public NmField getCurrent() {
        return current;
    }

    public ZceField getThermalComfort() {
        return thermalComfort;
    }

    public ZceField getWorkload() {
        return workload;
    }

    public ZceField getProblems() {
        return problems;
    }

    public ZceField getEquipmentMalfunctions() {
        return equipmentMalfunctions;
    }

    public NmField getAnySymptoms() {
        return anySymptoms;
    }

    public NmField getExposureToAltitude() {
        return exposureToAltitude;
    }

    public StField getComments() {
        return comments;
    }

    public NmField getSurfaceIntervalBeforeAltitudeExposure() {
        return surfaceIntervalBeforeAltitudeExposure;
    }

    public DtField getDateOfFlight() {
        return dateOfFlight;
    }

    public NmField getTotalLengthOfExposure() {
        return totalLengthOfExposure;
    }

    public NmField getAltitudeOfExposure() {
        return altitudeOfExposure;
    }

    public NmField getWereYouTreatedInHyperbaricChamber() {
        return wereYouTreatedInHyperbaricChamber;
    }

    public StField getNameAndLocationOfChamber() {
        return nameAndLocationOfChamber;
    }

    public NmField getNumberOfRecompressionTreatments() {
        return numberOfRecompressionTreatments;
    }

}
