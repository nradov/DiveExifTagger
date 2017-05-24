package com.github.nradov.diveexiftagger.image.jpeg.exif;

import static com.github.nradov.diveexiftagger.image.jpeg.exif.FieldType.ASCII;
import static com.github.nradov.diveexiftagger.image.jpeg.exif.FieldType.BYTE;
import static com.github.nradov.diveexiftagger.image.jpeg.exif.FieldType.LONG;
import static com.github.nradov.diveexiftagger.image.jpeg.exif.FieldType.RATIONAL;
import static com.github.nradov.diveexiftagger.image.jpeg.exif.FieldType.SHORT;
import static com.github.nradov.diveexiftagger.image.jpeg.exif.FieldType.SRATIONAL;
import static com.github.nradov.diveexiftagger.image.jpeg.exif.FieldType.UNDEFINED;
import static com.github.nradov.diveexiftagger.image.jpeg.exif.Utilities.formatShortAsUnsignedHex;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * All supported metadata field tags.
 *
 * @author Nick Radov
 * @see <a title="ExifTool by Phil Harvey" target="_" href=
 *      "http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/EXIF.html">EXIF
 *      Tags</a>
 * @see <a title="Exiv2" target="_" href="http://www.exiv2.org/tags.html">
 *      Metadata reference tables</a>
 */
public enum FieldTag {

	/** The lens aperture. The unit is the APEX value. */
	ApertureValue(37378, RATIONAL),

	/**
	 * Person who created the image.
	 * <p>
	 * Note: some older TIFF files used this tag for storing Copyright
	 * information.
	 * </p>
	 */
	Artist(315, ASCII),

	/**
	 * The number of bits per image component. In this standard each component
	 * of the image is 8 bits, so the value for this tag is 8. See also
	 * SamplesPerPixel. In JPEG compressed data a JPEG marker is used instead of
	 * this tag.
	 */
	BitsPerSample(258, SHORT),

	/**
	 * The value of brightness. The unit is the APEX value. Ordinarily it is
	 * given in the range of -99.99 to 99.99. Note that if the numerator of the
	 * recorded value is FFFFFFFF.H, Unknown shall be indicated.
	 */
	BrightnessValue(37379, SRATIONAL),

	/**
	 * This tag records the owner of a camera used in photography as an ASCII
	 * string.
	 */
	CameraOwnerName(42032, ASCII),

	/**
	 * Indicates the color filter array (CFA) geometric pattern of the image
	 * sensor when a one-chip color area sensor is used. It does not apply to
	 * all sensing methods.
	 */
	CfaPattern(41730, UNDEFINED),

	/**
	 * The color space information tag (ColorSpace) is always recorded as the
	 * color space specifier.
	 * <p>
	 * Normally sRGB (=1) is used to define the color space based on the PC
	 * monitor conditions and environment. If a color space other than sRGB is
	 * used, Uncalibrated (=FFFF.H) is set. Image data recorded as Uncalibrated
	 * can be treated as sRGB when it is converted to Flashpix. On sRGB see
	 * Annex E.
	 * </p>
	 */
	ColorSpace(40961, SHORT),

	/**
	 * Information specific to compressed data. The channels of each component
	 * are arranged in order from the 1st component to the 4th. For uncompressed
	 * data the data arrangement is given in the
	 * {@link #PhotometricInterpretation} tag. However, since
	 * PhotometricInterpretation can only express the order of Y,Cb and Cr, this
	 * tag is provided for cases when compressed data uses components other than
	 * Y, Cb, and Cr and to enable support of other sequences.
	 */
	ComponentsConfiguration(37121, UNDEFINED),

	/**
	 * Information specific to compressed data. The compression mode used for a
	 * compressed image is indicated in unit bits per pixel.
	 */
	CompressedBitsPerPixel(37122, RATIONAL),

	/**
	 * The compression scheme used for the image data. When a primary image is
	 * JPEG compressed, this designation is not necessary and is omitted. When
	 * thumbnails use JPEG compression, this tag value is set to 6.
	 */
	Compression(259, SHORT),

	/**
	 * This tag indicates the direction of contrast processing applied by the
	 * camera when the image was shot.
	 */
	Contrast(41992, SHORT),

	/**
	 * Copyright notice.
	 * <p>
	 * Copyright notice of the person or organization that claims the copyright
	 * to the image. The complete copyright statement should be listed in this
	 * field including any dates and statements of claims. For example,
	 * "Copyright, John Smith, 19xx. All rights reserved."
	 * </p>
	 */
	Copyright(33432, ASCII),

	/**
	 * This tag indicates the use of special processing on image data, such as
	 * rendering geared to output. When special processing is performed, the
	 * reader is expected to disable or minimize any further processing.
	 */
	CustomRendered(41985, SHORT),

	/**
	 * Date and time of image creation.
	 * <p>
	 * The format is: "YYYY:MM:DD HH:MM:SS", with hours like those on a 24-hour
	 * clock, and one space character between the date and the time. The length
	 * of the string, including the terminating NUL, is 20 bytes.
	 * </p>
	 */
	DateTime(306, ASCII),

	/**
	 * The date and time when the image was stored as digital data. If, for
	 * example, an image was captured by DSC and at the same time the file was
	 * recorded, then the DateTimeOriginal and DateTimeDigitized will have the
	 * same contents. The format is "YYYY:MM:DD HH:MM:SS" with time shown in
	 * 24-hour format, and the date and time separated by one blank character
	 * [20.H]. When the date and time are unknown, all the character spaces
	 * except colons (":") may be filled with blank characters, or else the
	 * Interoperability field may be filled with blank characters. The character
	 * string length is 20 bytes including NULL for termination. When the field
	 * is left blank, it is treated as unknown.
	 */
	DateTimeDigitized(36868, ASCII),

	/**
	 * The date and time when the original image data was generated. For a DSC
	 * the date and time the picture was taken are recorded. The format is
	 * "YYYY:MM:DD HH:MM:SS" with time shown in 24-hour format, and the date and
	 * time separated by one blank character [20.H]. When the date and time are
	 * unknown, all the character spaces except colons (":") may be filled with
	 * blank characters, or else the Interoperability field may be filled with
	 * blank characters. The character string length is 20 bytes including NULL
	 * for termination. When the field is left blank, it is treated as unknown.
	 */
	DateTimeOriginal(36867, ASCII),

	/**
	 * This tag indicates information on the picture-taking conditions of a
	 * particular camera model. The tag is used only to indicate the
	 * picture-taking conditions in the reader.
	 */
	DeviceSettingDescription(41995, UNDEFINED),

	/**
	 * This tag indicates the digital zoom ratio when the image was shot. If the
	 * numerator of the recorded value is 0, this indicates that digital zoom
	 * was not used.
	 */
	DigitalZoomRatio(41988, RATIONAL),

	/**
	 * Exif IFD is a set of tags for recording Exif-specific attribute
	 * information. It is pointed to by the offset from the TIFF header (Value
	 * Offset) indicated by an Exif private tag value.
	 * <p>
	 * A pointer to the Exif IFD. Interoperability, Exif IFD has the same
	 * structure as that of the IFD specified in TIFF. Ordinarily, however, it
	 * does not contain image data as in the case of TIFF.
	 * </p>
	 */
	ExifIfdPointer(34665, LONG),

	/**
	 * The version of this standard supported. Nonexistence of this field is
	 * taken to mean nonconformance to the standard (see section 4.2).
	 * Conformance to this standard is indicated by recording "0220" as 4-byte
	 * ASCII. Since the type is UNDEFINED, there is no NULL for termination.
	 */
	ExifVersion(36864, UNDEFINED),

	/**
	 * The exposure bias. The unit is the APEX value. Ordinarily it is given in
	 * the range of –99.99 to 99.99.
	 */
	ExposureBiasValue(37380, SRATIONAL),

	/**
	 * Indicates the exposure index selected on the camera or input device at
	 * the time the image is captured.
	 */
	ExposureIndex(41493, RATIONAL),

	/**
	 * This tag indicates the exposure mode set when the image was shot. In
	 * auto-bracketing mode, the camera shoots a series of frames of the same
	 * scene at different exposure settings.
	 */
	ExposureMode(41986, SHORT),

	/**
	 * The class of the program used by the camera to set exposure when the
	 * picture is taken. The tag values are as follows.
	 */
	ExposureProgram(34850, SHORT),

	/**
	 * This tag indicates the status of flash when the image was shot. Bit 0
	 * indicates the flash firing status, bits 1 and 2 indicate the flash return
	 * status, bits 3 and 4 indicate the flash mode, bit 5 indicates whether the
	 * flash function is present, and bit 6 indicates "red eye" mode (see Figure
	 * 11).
	 */
	Flash(37385, SHORT),

	/**
	 * Indicates the strobe energy at the time the image is captured, as
	 * measured in Beam Candle Power Seconds (BCPS).
	 */
	FlashEnergy(41483, RATIONAL),

	/** Exposure time, given in seconds (sec). */
	ExposureTime(33434, RATIONAL),

	/**
	 * Indicates the image source. If a DSC recorded the image, this tag value
	 * of this tag always be set to 3, indicating that the image was recorded on
	 * a DSC.
	 */
	FileSource(41728, UNDEFINED),

	/**
	 * The Flashpix format version supported by a FPXR file. If the FPXR
	 * function supports Flashpix format Ver. 1.0, this is indicated similarly
	 * to ExifVersion by recording "0100" as 4-byte ASCII. Since the type is
	 * UNDEFINED, there is no NULL for termination.
	 */
	FlashpixVersion(40960, UNDEFINED),

	/** The F number. */
	FNumber(33437, RATIONAL),

	/**
	 * The actual focal length of the lens, in mm. Conversion is not made to the
	 * focal length of a 35 mm film camera.
	 */
	FocalLength(37386, RATIONAL),

	/**
	 * This tag indicates the equivalent focal length assuming a 35mm film
	 * camera, in mm. A value of 0 means the focal length is unknown. Note that
	 * this tag differs from the {@link #FocalLength} tag.
	 */
	FocalLengthIn35mmFilm(41989, SHORT),

	/**
	 * Indicates the unit for measuring {@link #FocalPlaneXResolution} and
	 * {@link #FocalPlaneYResolution}. This value is the same as the
	 * {@link #ResolutionUnit}.
	 */
	FocalPlaneResolutionUnit(41488, SHORT),

	/**
	 * Indicates the number of pixels in the image width (X) direction per
	 * {@link #FocalPlaneResolutionUnit} on the camera focal plane.
	 */
	FocalPlaneXResolution(41486, RATIONAL),

	/**
	 * Indicates the number of pixels in the image height (Y) direction per
	 * {@link #FocalPlaneResolutionUnit} on the camera focal plane.
	 */
	FocalPlaneYResolution(41487, RATIONAL),

	/** This tag indicates the degree of overall image gain adjustment. */
	GainControl(41991, SHORT),

	/**
	 * Indicates the altitude based on the reference in {@link #GpsAltitudeRef}.
	 * Altitude is expressed as one {@link Rational} value. The reference unit
	 * is meters.
	 */
	GpsAltitude(6, RATIONAL),

	/**
	 * Indicates the altitude used as the reference altitude. If the reference
	 * is sea level and the altitude is above sea level, 0 is given. If the
	 * altitude is below sea level, a value of 1 is given and the altitude is
	 * indicated as an absolute value in the {@link #GpsAltitude} tag. The
	 * reference unit is meters. Note that this tag is {@link Byte} type, unlike
	 * other reference tags.
	 */
	GpsAltitudeRef(5, BYTE),

	/**
	 * A character string recording the name of the GPS area. The first byte
	 * indicates the character code used (Table 6､ Table 7), and this is
	 * followed by the name of the GPS area. Since the Type is not ASCII, NULL
	 * termination is not necessary.
	 */
	GpsAreaInformation(28, UNDEFINED),

	/**
	 * A character string recording date and time information relative to UTC
	 * (Coordinated Universal Time). The format is "YYYY:MM:DD." The length of
	 * the string is 11 bytes including NULL.
	 */
	GpsDateStamp(29, ASCII),

	/**
	 * Indicates the bearing to the destination point. The range of values is
	 * from 0.00 to 359.99.
	 */
	GpsDestBearing(24, RATIONAL),

	/**
	 * Indicates the reference used for giving the bearing to the destination
	 * point. 'T' denotes true direction and 'M' is magnetic direction.
	 */
	GpsDestBearingRef(23, ASCII),

	/** Indicates the distance to the destination point. */
	GpsDestDistance(26, RATIONAL),

	/**
	 * Indicates the unit used to express the distance to the destination point.
	 * 'K', 'M' and 'N' represent kilometers, miles and knots.
	 */
	GpsDestDistanceRef(25, ASCII),

	/**
	 * Indicates the latitude of the destination point. The latitude is
	 * expressed as three RATIONAL values giving the degrees, minutes, and
	 * seconds, respectively. If latitude is expressed as degrees, minutes and
	 * seconds, a typical format would be dd/1,mm/1,ss/1. When degrees and
	 * minutes are used and, for example, fractions of minutes are given up to
	 * two decimal places, the format would be dd/1,mmmm/100,0/1.
	 */
	GpsDestLatitude(20, RATIONAL),

	/**
	 * Indicates whether the latitude of the destination point is north or south
	 * latitude. The ASCII value 'N' indicates north latitude, and 'S' is south
	 * latitude.
	 */
	GpsDestLatitudeRef(19, ASCII),

	/**
	 * Indicates the longitude of the destination point. The longitude is
	 * expressed as three RATIONAL values giving the degrees, minutes, and
	 * seconds, respectively. If longitude is expressed as degrees, minutes and
	 * seconds, a typical format would be ddd/1,mm/1,ss/1. When degrees and
	 * minutes are used and, for example, fractions of minutes are given up to
	 * two decimal places, the format would be ddd/1,mmmm/100,0/1.
	 */
	GpsDestLongitude(22, RATIONAL),

	/**
	 * Indicates whether the longitude of the destination point is east or west
	 * longitude. ASCII 'E' indicates east longitude, and 'W' is west longitude.
	 */
	GpsDestLongitudeRef(21, ASCII),

	/**
	 * Indicates whether differential correction is applied to the GPS receiver.
	 */
	GpsDifferential(30, SHORT),

	/**
	 * Indicates the GPS DOP (data degree of precision). An HDOP value is
	 * written during two-dimensional measurement, and PDOP during
	 * three-dimensional measurement.
	 */
	GpsDop(11, RATIONAL),

	/**
	 * Indicates the direction of the image when it was captured. The range of
	 * values is from 0.00 to 359.99.
	 */
	GpsImgDirection(17, RATIONAL),

	/**
	 * Indicates the reference for giving the direction of the image when it is
	 * captured. 'T' denotes true direction and 'M' is magnetic direction.
	 */
	GpsImgDirectionRef(16, ASCII),

	/**
	 * GPS IFD is a set of tags for recording GPS information. It is pointed to
	 * by the offset from the TIFF header (Value Offset) indicated by a GPS
	 * private tag value.
	 * <p>
	 * A pointer to the GPS Info IFD. The Interoperability structure of the GPS
	 * Info IFD, like that of Exif IFD, has no image data.
	 * </p>
	 */
	GpsInfoIfdPointer(34853, LONG),

	/**
	 * Indicates the latitude. The latitude is expressed as three RATIONAL
	 * values giving the degrees, minutes, and seconds, respectively. If
	 * latitude is expressed as degrees, minutes and seconds, a typical format
	 * would be dd/1,mm/1,ss/1. When degrees and minutes are used and, for
	 * example, fractions of minutes are given up to two decimal places, the
	 * format would be dd/1,mmmm/100,0/1.
	 */
	GpsLatitude(2, RATIONAL),

	/**
	 * Indicates whether the latitude is north or south latitude. The ASCII
	 * value 'N' indicates north latitude, and 'S' is south latitude.
	 */
	GpsLatitudeRef(1, ASCII),

	/**
	 * Indicates the longitude. The longitude is expressed as three RATIONAL
	 * values giving the degrees, minutes, and seconds, respectively. If
	 * longitude is expressed as degrees, minutes and seconds, a typical format
	 * would be ddd/1,mm/1,ss/1. When degrees and minutes are used and, for
	 * example, fractions of minutes are given up to two decimal places, the
	 * format would be ddd/1,mmmm/100,0/1.
	 */
	GpsLongitude(4, RATIONAL),

	/**
	 * Indicates whether the longitude is east or west longitude. ASCII 'E'
	 * indicates east longitude, and 'W' is west longitude.
	 */
	GpsLongitudeRef(3, ASCII),

	/**
	 * Indicates the geodetic survey data used by the GPS receiver. If the
	 * survey data is restricted to Japan, the value of this tag is 'TOKYO' or
	 * 'WGS-84'. If a GPS Info tag is recorded, it is strongly recommended that
	 * this tag be recorded.
	 */
	GpsMapDatum(18, ASCII),

	/**
	 * Indicates the GPS measurement mode. '2' means two-dimensional measurement
	 * and '3' means three-dimensional measurement is in progress.
	 */
	GpsMeasureMode(10, ASCII),

	/**
	 * A character string recording the name of the method used for location
	 * finding. The first byte indicates the character code used (Table 6､Table
	 * 7), and this is followed by the name of the method. Since the Type is not
	 * ASCII, NULL termination is not necessary.
	 */
	GpsProcessingMethod(27, UNDEFINED),

	/**
	 * Indicates the GPS satellites used for measurements. This tag can be used
	 * to describe the number of satellites, their ID number, angle of
	 * elevation, azimuth, SNR and other information in ASCII notation. The
	 * format is not specified. If the GPS receiver is incapable of taking
	 * measurements, value of the tag shall be set to NULL.
	 */
	GpsSatellites(8, ASCII),

	/** Indicates the speed of GPS receiver movement. */
	GpsSpeed(13, RATIONAL),

	/**
	 * Indicates the unit used to express the GPS receiver speed of movement.
	 * 'K' 'M' and 'N' represents kilometers per hour, miles per hour, and
	 * knots.
	 */
	GpsSpeedRef(12, ASCII),

	/**
	 * Indicates the status of the GPS receiver when the image is recorded. 'A'
	 * means measurement is in progress, and 'V' means the measurement is
	 * Interoperability.
	 */
	GpsStatus(9, ASCII),

	/**
	 * Indicates the time as UTC (Coordinated Universal Time). TimeStamp is
	 * expressed as three {@link Rational} values giving the hour, minute, and
	 * second.
	 */
	GpsTimeStamp(7, RATIONAL),

	/**
	 * Indicates the direction of GPS receiver movement. The range of values is
	 * from 0.00 to 359.99.
	 */
	GpsTrack(15, RATIONAL),

	/**
	 * Indicates the reference for giving the direction of GPS receiver
	 * movement. 'T' denotes true direction and 'M' is magnetic direction.
	 */
	GpsTrackRef(14, ASCII),

	/**
	 * Indicates the version of GPSInfoIFD. The version is given as 2.2.0.0.
	 * This tag is mandatory when GPSInfo tag is present. Note that the
	 * GPSVersionID tag is written as a different byte than the Exif Version
	 * tag.
	 */
	GpsVersionId(0, BYTE),

	/**
	 * A string that describes the subject of the image.
	 * <p>
	 * For example, a user may wish to attach a comment such as "1988 company
	 * picnic" to an image.
	 * </p>
	 */
	ImageDescription(270, ASCII),

	/**
	 * The number of rows of pixels in the image.
	 * <p>
	 * No default.
	 * </p>
	 *
	 * @see #ImageWidth
	 */
	ImageLength(257, SHORT, LONG),

	/**
	 * The number of columns in the image, i.e., the number of pixels per row.
	 * <p>
	 * No default.
	 * </p>
	 *
	 * @see #ImageLength
	 */
	ImageWidth(256, SHORT, LONG),

	/**
	 * This tag indicates an identifier assigned uniquely to each image. It is
	 * recorded as an ASCII string equivalent to hexadecimal notation and
	 * 128-bit fixed length.
	 */
	ImageUniqueId(42016, ASCII),

	/**
	 * Interoperability IFD is composed of tags which stores the information to
	 * ensure the Interoperability and pointed by the following tag located in
	 * Exif IFD.
	 * <p>
	 * The Interoperability structure of Interoperability IFD is same as TIFF
	 * defined IFD structure but does not contain the image data
	 * characteristically compared with normal TIFF IFD.
	 * </p>
	 */
	InteroperabilityIfdPointer(40965, LONG),

	/**
	 * Indicates the identification of the Interoperability rule. The following
	 * rules are defined. Four bytes used including the termination code (NULL).
	 */
	InteroperabilityIndex(1, ASCII),

	/**
	 * Indicates the ISO Speed and ISO Latitude of the camera or input device as
	 * specified in ISO 12232.
	 */
	ISOSpeedRatings(34855, SHORT),

	/**
	 * The offset to the start byte (SOI) of JPEG compressed thumbnail data.
	 * This is not used for primary image JPEG data.
	 */
	JPEGInterchangeFormat(513, LONG),

	/**
	 * The number of bytes of JPEG compressed thumbnail data. This is not used
	 * for primary image JPEG data. JPEG thumbnails are not divided but are
	 * recorded as a continuous JPEG bitstream from SOI to EOI. APPn and COM
	 * markers should not be recorded. Compressed thumbnails shall be recorded
	 * in no more than 64 Kbytes, including all other data to be recorded in
	 * APP1.
	 */
	JPEGInterchangeFormatLength(514, LONG),

	/** The kind of light source. */
	LightSource(37384, SHORT),

	/**
	 * The scanner manufacturer.
	 * <p>
	 * Manufacturer of the scanner, video digitizer, or other type of equipment
	 * used to generate the image. Synthetic images should not include this
	 * field.
	 * </p>
	 *
	 * @see #Model
	 * @see #Software
	 */
	Make(271, ASCII),

	/**
	 * A tag for manufacturers of Exif writers to record any desired
	 * information. The contents are up to the manufacturer, but this tag should
	 * not be used for any other than its intended purpose.
	 */
	MakerNote(37500, UNDEFINED),

	/**
	 * The smallest F number of the lens. The unit is the APEX value. Ordinarily
	 * it is given in the range of 00.00 to 99.99, but it is not limited to this
	 * range.
	 */
	MaxApertureValue(37381, RATIONAL),

	/** The metering mode. */
	MeteringMode(37383, SHORT),

	/**
	 * The scanner model name or number.
	 * <p>
	 * The model name or number of the scanner, video digitizer, or other type
	 * of equipment used to generate the image.
	 * </p>
	 *
	 * @see #Make
	 * @see #Software
	 */
	Model(272, ASCII),

	/**
	 * Indicates the Opto-Electric Conversion Function (OECF) specified in ISO
	 * 14524. OECF is the relationship between the camera optical input and the
	 * image values.
	 */
	OECF(34856, UNDEFINED),

	/**
	 * The orientation of the image with respect to the rows and columns.
	 * <ol type="1">
	 * <li>The 0th row represents the visual top of the image, and the 0th
	 * column represents the visual left-hand side.</li>
	 * <li>The 0th row represents the visual top of the image, and the 0th
	 * column represents the visual right-hand side.</li>
	 * <li>The 0th row represents the visual bottom of the image, and the 0th
	 * column represents the visual right-hand side.</li>
	 * <li>The 0th row represents the visual bottom of the image, and the 0th
	 * column represents the visual left-hand side.</li>
	 * <li>The 0th row represents the visual left-hand side of the image, and
	 * the 0th column represents the visual top.</li>
	 * <li>The 0th row represents the visual right-hand side of the image, and
	 * the 0th column represents the visual top.</li>
	 * <li>The 0th row represents the visual right-hand side of the image, and
	 * the 0th column represents the visual bottom.</li>
	 * <li>The 0th row represents the visual left-hand side of the image, and
	 * the 0th column represents the visual bottom.</li>
	 * </ol>
	 * <p>
	 * Default is 1.
	 * </p>
	 * <p>
	 * <i>Support for orientations other than 1 is not a Baseline TIFF
	 * requirement.</i>
	 * </p>
	 */
	Orientation(274, SHORT),

	/**
	 * This tag indicates the sensitivity of the camera or input device when the
	 * image was shot. More specifically, it indicates one of the following
	 * values that are parameters defined in ISO 12232: standard output
	 * sensitivity (SOS), recommended exposure index (REI), or ISO speed.
	 * Accordingly, if a tag corresponding to a parameter that is designated by
	 * a {@link SensitivityType} tag is recorded, the values of the tag and of
	 * this PhotographicSensitivity tag are the same. However, if the value is
	 * 65535 (the maximum value of SHORT) or higher, the value of this tag shall
	 * be 65535. When recording this tag, the SensitivityType tag should also be
	 * recorded. In addition, while Count = Any, only 1 count should be used
	 * when recording this tag.
	 */
	PhotographicSensitivity(34855, SHORT),

	/**
	 * The pixel composition. In JPEG compressed data a JPEG marker is used
	 * instead of this tag.
	 */
	PhotometricInterpretation(262, SHORT),

	/**
	 * Information specific to compressed data. When a compressed file is
	 * recorded, the valid width of the meaningful image shall be recorded in
	 * this tag, whether or not there is padding data or a restart marker. This
	 * tag should not exist in an uncompressed file. For details see section
	 * 2.8.1 and Annex F.
	 */
	PixelXDimension(40962, SHORT, LONG),

	/**
	 * Information specific to compressed data. When a compressed file is
	 * recorded, the valid height of the meaningful image shall be recorded in
	 * this tag, whether or not there is padding data or a restart marker. This
	 * tag should not exist in an uncompressed file. For details see section
	 * 2.8.1 and Annex F. Since data padding is unnecessary in the vertical
	 * direction, the number of lines recorded in this valid image height tag
	 * will in fact be the same as that recorded in the SOF.
	 */
	PixelYDimension(40963, SHORT, LONG),

	/**
	 * Indicates whether pixel components are recorded in chunky or planar
	 * format. In JPEG compressed files a JPEG marker is used instead of this
	 * tag. If this field does not exist, the TIFF default of 1 (chunky) is
	 * assumed.
	 */
	PlanarConfiguration(284, SHORT),

	/**
	 * The chromaticity of the three primary colors of the image. Normally this
	 * tag is not necessary, since color space is specified in the color space
	 * information tag (ColorSpace).
	 */
	PrimaryChromaticities(319, RATIONAL),

	/**
	 * This tag is used to record the name of an audio file related to the image
	 * data. The only relational information recorded here is the Exif audio
	 * file name and extension (an ASCII string consisting of 8 characters + '.'
	 * + 3 characters). The path is not recorded. Stipulations on audio are
	 * given in section 0. File naming conventions are given in section 0.
	 */
	RelatedSoundFile(40964, ASCII),

	/**
	 * The reference black point value and reference white point value. No
	 * defaults are given in TIFF, but the values below are given as defaults
	 * here. The color space is declared in a color space information tag, with
	 * the default being the value that gives the optimal image characteristics
	 * Interoperability these conditions.
	 */
	ReferenceBlackWhite(532, RATIONAL),

	/**
	 * <ol>
	 * <li>No absolute unit of measurement. Used for images that may have a
	 * non-square aspect ratio but no meaningful absolute dimensions.</li>
	 * <li>Inch.</li>
	 * <li>Centimeter.</li>
	 * </ol>
	 */
	ResolutionUnit(296, SHORT),

	/**
	 * The number of rows per strip. This is the number of rows in the image of
	 * one strip when an image is divided into strips. With JPEG compressed data
	 * this designation is not needed and is omitted.
	 *
	 * @see #StripByteCounts
	 */
	RowsPerStrip(278, SHORT, LONG),

	/**
	 * The number of components per pixel. Since this standard applies to RGB
	 * and YCbCr images, the value set for this tag is 3. In JPEG compressed
	 * data a JPEG marker is used instead of this tag.
	 */
	SamplesPerPixel(277, SHORT),

	/**
	 * This tag indicates the direction of saturation processing applied by the
	 * camera when the image was shot.
	 */
	Saturation(41993, SHORT),

	/**
	 * This tag indicates the type of scene that was shot. It can also be used
	 * to record the mode in which the image was shot. Note that this differs
	 * from the scene type ({@link SceneType}) tag.
	 */
	SceneCaptureType(41990, SHORT),

	/**
	 * Indicates the type of scene. If a DSC recorded the image, this tag value
	 * shall always be set to 1, indicating that the image was directly
	 * photographed.
	 */
	SceneType(41729, UNDEFINED),

	/**
	 * Indicates the image sensor type on the camera or input device. The values
	 * are as follows.
	 */
	SensingMethod(41495, SHORT),

	/**
	 * The SensitivityType tag indicates which one of the parameters of ISO12232
	 * is the {@link #PhotographicSensitivity} tag. Although it is an optional
	 * tag, it should be recorded when a PhotographicSensitivity tag is
	 * recorded. Value = 4, 5, 6, or 7 may be used in case that the values of
	 * plural parameters are the same. See Annex G for use of
	 * sensitivity-related tags.
	 */
	SensitivityType(34864, SHORT),

	/**
	 * This tag indicates the direction of sharpness processing applied by the
	 * camera when the image was shot.
	 */
	Sharpness(41994, SHORT),

	/**
	 * Shutter speed. The unit is the APEX (Additive System of Photographic
	 * Exposure) setting (see Annex C).
	 */
	ShutterSpeedValue(37377, SRATIONAL),

	/**
	 * This tag records the name and version of the software or firmware of the
	 * camera or image input device used to generate the image. The detailed
	 * format is not specified, but it is recommended that the example shown
	 * below be followed. When the field is left blank, it is treated as
	 * unknown.
	 */
	Software(305, ASCII),

	/**
	 * This tag records the camera or input device spatial frequency table and
	 * SFR values in the direction of image width, image height, and diagonal
	 * direction, as specified in ISO 12233.
	 */
	SpatialFrequencyResponse(41484, UNDEFINED),

	/**
	 * Indicates the spectral sensitivity of each channel of the camera used.
	 * The tag value is an ASCII string compatible with the standard developed
	 * by the ASTM Technical committee.
	 */
	SpectralSensitivity(34852, ASCII),

	/**
	 * The total number of bytes in each strip. With JPEG compressed data this
	 * designation is not needed and is omitted.
	 */
	StripByteCounts(279, SHORT, LONG),

	/**
	 * For each strip, the byte offset of that strip. It is recommended that
	 * this be selected so the number of strip bytes does not exceed 64 Kbytes.
	 * With JPEG compressed data this designation is not needed and is omitted.
	 *
	 * @see #RowsPerStrip
	 * @see #StripByteCounts
	 */
	StripOffsets(273, SHORT, LONG),

	/**
	 * This tag indicates the location and area of the main subject in the
	 * overall scene.
	 */
	SubjectArea(37396, SHORT),

	/**
	 * The distance to the subject, given in meters. Note that if the numerator
	 * of the recorded value is FFFFFFFF.H, Infinity shall be indicated; and if
	 * the numerator is 0, Distance unknown shall be indicated.
	 */
	SubjectDistance(37382, RATIONAL),

	/** This tag indicates the distance to the subject. */
	SubjectDistanceRange(41996, SHORT),

	/**
	 * Indicates the location of the main subject in the scene. The value of
	 * this tag represents the pixel at the center of the main subject relative
	 * to the left edge, prior to rotation processing as per the Rotation tag.
	 * The first value indicates the X column number and second indicates the Y
	 * row number.
	 */
	SubjectLocation(41492, SHORT),

	/**
	 * A tag used to record fractions of seconds for the {@link #DateTime} tag.
	 */
	SubsecTime(37520, ASCII),

	/**
	 * A tag used to record fractions of seconds for the
	 * {@link #DateTimeDigitized} tag.
	 */
	SubsecTimeDigitized(37522, ASCII),

	/**
	 * A tag used to record fractions of seconds for the
	 * {@link #DateTimeOriginal} tag.
	 */
	SubsecTimeOriginal(37521, ASCII),

	/**
	 * A transfer function for the image, described in tabular style. Normally
	 * this tag is not necessary, since color space is specified in the color
	 * space information tag ({@link #ColorSpace}).
	 */
	TransferFunction(301, SHORT),

	/**
	 * A tag for Exif users to write keywords or comments on the image besides
	 * those in ImageDescription, and without the character code limitations of
	 * the ImageDescription tag.
	 */
	UserComment(37510, UNDEFINED),

	/**
	 * This tag indicates the white balance mode set when the image was shot.
	 */
	WhiteBalance(41987, SHORT),

	/**
	 * The chromaticity of the white point of the image. Normally this tag is
	 * not necessary, since color space is specified in the color space
	 * information tag (ColorSpace).
	 */
	WhitePoint(318, RATIONAL),

	/**
	 * The number of pixels per ResolutionUnit in the ImageWidth direction.
	 * <p>
	 * It is not mandatory that the image be actually displayed or printed at
	 * the size implied by this parameter. It is up to the application to use
	 * this information as it wishes.
	 * </p>
	 * <p>
	 * No default.
	 * </p>
	 *
	 * @see #YResolution
	 * @see #ResolutionUnit
	 */
	XResolution(282, RATIONAL),

	/**
	 * The matrix coefficients for transformation from RGB to YCbCr image data.
	 * No default is given in TIFF; but here the characteristics given in Annex
	 * E, "Color Space Guidelines," is used as the default.
	 */
	YCbCrCoefficients(529, RATIONAL),

	/**
	 * Specifies the positioning of subsampled chrominance components relative
	 * to luminance samples.
	 */
	// TODO: Fill in the rest of the Javadoc
	YCbCrPositioning(531, SHORT),

	/**
	 * The sampling ratio of chrominance components in relation to the luminance
	 * component. In JPEG compressed data a JPEG marker is used instead of this
	 * tag.
	 */
	YCbCrSubSampling(530, SHORT),

	/**
	 * The number of pixels per ResolutionUnit in the ImageLength direction.
	 * <p>
	 * No default.
	 * </p>
	 *
	 * @see #XResolution
	 * @see #ResolutionUnit
	 */
	YResolution(283, RATIONAL),

	// Canon maker note
	// http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/Canon.html

	CameraSettings1(Proprietary.Canon, 1), //
	CanonFlashInfo(Proprietary.Canon, 3), //
	CameraSettings2(Proprietary.Canon, 4), //
	ImageType(Proprietary.Canon, 6, ASCII), //
	FirmwareVersion(Proprietary.Canon, 7, ASCII), //
	ImageNumber(Proprietary.Canon, 8), //
	OwnerName(Proprietary.Canon, 9), //
	CameraSerialNumber(Proprietary.Canon, 12), //
	CanonCameraInfo(Proprietary.Canon, 13), //
	CustomFunctions(Proprietary.Canon, 15), //
	CanonFileLength(Proprietary.Canon, 0xE), //
	CanonCustomFunctions(Proprietary.Canon, 0xF), //
	CanonModelId(Proprietary.Canon, 0x10), //
	ThumbnailImageValidArea(Proprietary.Canon, 0x13), //
	CanonTag0x18(Proprietary.Canon, 0x18), //
	CanonTag0x19(Proprietary.Canon, 0x19), //
	CanonTag0x1C(Proprietary.Canon, 0x1C), //
	CanonTag0x1D(Proprietary.Canon, 0x1D), //
	CanonTag0x1E(Proprietary.Canon, 0x1E), //
	CanonTag0x1F(Proprietary.Canon, 0x1F), //
	CanonTag0x22(Proprietary.Canon, 0x22), //
	CanonTag0x23(Proprietary.Canon, 0x23), //
	CanonAFInfo2(Proprietary.Canon, 0x26), //
	CanonTag0x27(Proprietary.Canon, 0x27), //
	CanonTag0x28(Proprietary.Canon, 0x28), //
	CanonTag0x2d(Proprietary.Canon, 0x2D), //
	CanonTag0x2E(Proprietary.Canon, 0x2E), //
	CanonTag0x2F(Proprietary.Canon, 0x2F), //
	CanonTag0x31(Proprietary.Canon, 0x31), //
	CanonTag0x32(Proprietary.Canon, 0x32), //
	CanonTag0x33(Proprietary.Canon, 0x33), //
	CanonTag0x9A(Proprietary.Canon, 0x9A), //
	VRDOffset(Proprietary.Canon, 0xD0);

	/**
	 * Qualifiers for vendor proprietary tags.
	 *
	 * @author Nick Radov
	 */
	public static enum Proprietary {
		Canon;
	}

	private final Proprietary proprietary;
	private final short tag;
	private final Set<FieldType> types;

	private FieldTag(final Proprietary proprietary, final int tag, final FieldType... type) {
		this.proprietary = proprietary;
		this.tag = (short) tag;
		types = new HashSet<>(type.length);
		for (final FieldType element : type) {
			types.add(element);
		}
	}

	private FieldTag(final int tag, final FieldType... type) {
		this(null, tag, type);
	}

	public short getTag() {
		return tag;
	}

	/** Map from tag numbers to objects. */
	@SuppressWarnings("serial")
	private static final Map<Proprietary, Map<java.lang.Short, FieldTag>> TAG_MAP = Collections
			.unmodifiableMap(new HashMap<Proprietary, Map<java.lang.Short, FieldTag>>() {
				{
					for (final FieldTag value : FieldTag.values()) {
						final Map<java.lang.Short, FieldTag> innerMap = getOrCreateInnerMap(value.proprietary);
						innerMap.put(value.getTag(), value);
					}
				}

				@Nonnull
				private Map<java.lang.Short, FieldTag> getOrCreateInnerMap(final Proprietary proprietary) {
					if (!containsKey(proprietary)) {
						final Map<java.lang.Short, FieldTag> newInnerMap = new HashMap<>();
						put(proprietary, newInnerMap);
					}
					return get(proprietary);
				}
			});

	static FieldTag valueOf(final Proprietary proprietary, final short tag) {
		if (TAG_MAP.containsKey(proprietary) && TAG_MAP.get(proprietary).containsKey(tag)) {
			return TAG_MAP.get(proprietary).get(tag);
		} else {
			throw new IllegalArgumentException("unsupported tag: " + formatShortAsUnsignedHex(tag));
		}
	}

	static FieldTag valueOf(final short tag) {
		return valueOf(null, tag);
	}

}
