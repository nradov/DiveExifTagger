package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * File segment header. Identifies the sender and the type of file.
 *
 * @author Nick Radov
 */
class Fsh extends Segment {

    static final String ID = "FSH";

    private final StField fieldSeparator;
    private final StField encodingCharacters;
    // data type is defined as ST in the specification but the samples contain
    // components?
    private final StField fileSendingApplication;
    private final ZceField messageType;
    private final TsField fileCreationDateTime;

    Fsh(final SegmentGroup parent, final String s) {
        super(parent);
        if (s.length() < 10) {
            throw new IllegalArgumentException("value too short");
        }
        fieldSeparator = new StSeparatorField(this, s.substring(3, 4));
        final String[] fields = splitFields(s.substring(4));
        encodingCharacters = fields.length >= 2
                ? new StSeparatorField(this, fields[1]) : null;
        fileSendingApplication = fields.length >= 3
                ? new StField(this, fields[2]) : null;
        messageType = fields.length >= 4 ? new ZceField(this, fields[3]) : null;
        fileCreationDateTime = fields.length >= 5 ? new TsField(this, fields[4])
                : null;
    }

    @Override
    StField getFieldSeparator() {
        return fieldSeparator;
    }

    @Override
    String getFieldSeparatorString() {
        return getFieldSeparator().toString();
    }

    StField getEncodingCharacters() {
        return encodingCharacters;
    }

    @Override
    String getRepetitionSeparatorString() {
        return getEncodingCharacters().toString().substring(1, 2);
    }

    /**
     * See Table 9.
     *
     * @return
     */
    St getFileSendingApplication() {
        return fileSendingApplication;
    }

    /**
     * ZXU = Uploaded profile; ZXL = Uploaded profile and Dive log details.
     *
     * @return
     */
    ZceField getMessageType() {
        return messageType;
    }

    TsField getFileCreationDateTime() {
        return fileCreationDateTime;
    }

}
