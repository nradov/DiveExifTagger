package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Safety and outcome report.
 *
 * @author Nick Radov
 */
final class Zsr extends Segment {

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
