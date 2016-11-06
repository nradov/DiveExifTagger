package com.github.nradov.diveexiftagger;
/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org
 * (C) HydroloGIS - www.hydrologis.com
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

import static org.jgrasstools.gears.i18n.GearsMessages.EXIFGPSWRITER_doEast_DESCRIPTION;
import static org.jgrasstools.gears.i18n.GearsMessages.EXIFGPSWRITER_doNorth_DESCRIPTION;
import static org.jgrasstools.gears.i18n.GearsMessages.EXIFGPSWRITER_file_DESCRIPTION;
import static org.jgrasstools.gears.i18n.GearsMessages.EXIFGPSWRITER_pAltitude_DESCRIPTION;
import static org.jgrasstools.gears.i18n.GearsMessages.EXIFGPSWRITER_pLat_DESCRIPTION;
import static org.jgrasstools.gears.i18n.GearsMessages.EXIFGPSWRITER_pLon_DESCRIPTION;
import static org.jgrasstools.gears.i18n.GearsMessages.EXIFGPSWRITER_tTimestamp_DESCRIPTION;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.jgrasstools.gears.io.exif.ExifUtil;
import org.jgrasstools.gears.libs.exceptions.ModelsIOException;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.w3c.dom.NodeList;

import com.sun.media.imageio.plugins.tiff.EXIFGPSTagSet;
import com.sun.media.imageio.plugins.tiff.EXIFParentTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;
import com.sun.media.imageio.plugins.tiff.TIFFTag;
import com.sun.media.imageioimpl.plugins.tiff.TIFFIFD;

import oms3.annotations.Description;

/**
 *
 * @author Nick Radov
 * @see org.jgrasstools.gears.io.exif.ExifGpsWriter
 */
public class ExifGpsWriter extends JGTModel {

    @Description(EXIFGPSWRITER_file_DESCRIPTION)
    public String file = null;

    @Description(EXIFGPSWRITER_pLat_DESCRIPTION)
    public Double pLat = null;

    @Description(EXIFGPSWRITER_pLon_DESCRIPTION)
    public Double pLon = null;

    @Description(EXIFGPSWRITER_tTimestamp_DESCRIPTION)
    public String tTimestamp = null;

    @Description(EXIFGPSWRITER_pAltitude_DESCRIPTION)
    public Double pAltitude = null;

    @Description(EXIFGPSWRITER_doNorth_DESCRIPTION)
    public boolean doNorth = true;

    @Description(EXIFGPSWRITER_doEast_DESCRIPTION)
    public boolean doEast = true;

    private ImageReader jpegReader;

    private ImageWriter jpegWriter;

    private BufferedImage image;

    private File imageFile;

    private final String[] latRef = { "", "" };
    private final String[] longRef = { "", "" };
    private final byte[] altRef = new byte[1];
    private long[][] latitude;
    private long[][] longitude;
    private long[][] altitude;
    private final String[] imgDirectionRef = { "", "" };
    private long[][] imgDirection;
    private final String[] datum = { "W", "G", "S", "-", "8", "4", "" };
    private final String[] status = { "", "" };
    private long[][] timeStamp;
    private String[] dateStamp = new String[11];

    private final DecimalFormat latFormatter = new DecimalFormat("0000.0000");
    private final DecimalFormat lonFormatter = new DecimalFormat("00000.0000");

    public void writeGpsExif() throws IOException {

        checkNull(pLat, pLon, tTimestamp);

        final String latStr = latFormatter.format(pLat * 100);
        latitude = getLatitude(latStr);
        final String lonStr = lonFormatter.format(pLon * 100);
        longitude = getLongitude(lonStr);

        latRef[0] = doNorth ? EXIFGPSTagSet.LATITUDE_REF_NORTH
                : EXIFGPSTagSet.LATITUDE_REF_SOUTH;
        longRef[0] = doEast ? EXIFGPSTagSet.LONGITUDE_REF_EAST
                : EXIFGPSTagSet.LONGITUDE_REF_WEST;

        if (pAltitude != null) {
            final double alt = pAltitude * 10;
            altitude = new long[][] { { (long) alt, 10 } };
            altRef[0] = EXIFGPSTagSet.ALTITUDE_REF_SEA_LEVEL;
        }

        final String[] timeStampSplit = tTimestamp.trim().split("\\s+"); // yyyy-MM-dd
                                                                         // HH:mm:ss
        final String date = timeStampSplit[0].replaceAll("-", ":");
        dateStamp = getDate(date);
        final String time = timeStampSplit[1].replaceAll(":", "");
        timeStamp = getTime(time);

        imageFile = new File(file);
        final ImageInputStream is = ImageIO.createImageInputStream(imageFile);

        // Get core JPEG reader.
        jpegReader = ExifUtil.findReader();
        if (jpegReader == null) {
            throw new ModelsIOException("Cannot find JPEG reader.", this);
        }

        // Get core JPEG writer.
        jpegWriter = ExifUtil.findWriter();
        if (jpegWriter == null) {
            throw new ModelsIOException("Cannot find JPEG writer.", this);
        }

        jpegReader.setInput(is);
        image = jpegReader.read(0);

        writeExif();

    }

    /**
     * Main method to write the gps data to the exif
     *
     * @param gps
     *            - gps position to be added
     * @throws IOException
     */
    private void writeExif() throws IOException {

        final IIOMetadata metadata = jpegReader.getImageMetadata(0);

        // names says which exif tree to get - 0 for jpeg 1 for the default
        final String[] names = metadata.getMetadataFormatNames();
        final IIOMetadataNode root = (IIOMetadataNode) metadata
                .getAsTree(names[0]);

        // EXIF is on the app1 node called unknown
        final NodeList nList = root.getElementsByTagName("unknown");
        final IIOMetadataNode app1EXIFNode = (IIOMetadataNode) nList.item(0);
        final List<IIOMetadata> md = readExif(app1EXIFNode);
        IIOMetadata exifMetadata = md.get(0);

        // insert the GPS data into the EXIF
        exifMetadata = insertGPSCoords(exifMetadata);

        // create a new EXIF node
        final IIOMetadataNode app1NodeNew = createNewExifNode(exifMetadata,
                null, null);

        // copy the user data accross
        app1EXIFNode.setUserObject(app1NodeNew.getUserObject());

        // write to a new image file
        final FileImageOutputStream out1 = new FileImageOutputStream(
                new File("GPS_" + imageFile.getName()));
        jpegWriter.setOutput(out1);
        metadata.setFromTree(names[0], root);

        final IIOImage image = new IIOImage(jpegReader.readAsRenderedImage(0,
                jpegReader.getDefaultReadParam()), null, metadata);

        // write out the new image
        jpegWriter.write(jpegReader.getStreamMetadata(), image,
                jpegWriter.getDefaultWriteParam());

    }

    /**
     * Private method - Reads the exif metadata for an image
     *
     * @param app1EXIFNode
     *            app1 Node of the image (where the exif data is stored)
     * @return the exif metadata
     * @throws IOException
     */
    private List<IIOMetadata> readExif(final IIOMetadataNode app1EXIFNode)
            throws IOException {
        // Set up input skipping EXIF ID 6-byte sequence.
        final byte[] app1Params = (byte[]) app1EXIFNode.getUserObject();

        final MemoryCacheImageInputStream app1EXIFInput = new MemoryCacheImageInputStream(
                new ByteArrayInputStream(app1Params, 6, app1Params.length - 6));

        // only the tiff reader knows how to interpret the exif metadata
        ImageReader tiffReader = null;
        final Iterator<ImageReader> readers = ImageIO
                .getImageReadersByFormatName("tiff");

        while (readers.hasNext()) {
            tiffReader = readers.next();
            if (tiffReader.getClass().getName().startsWith("com.sun.media")) {
                // Break on finding the core provider.
                break;
            }
        }
        if (tiffReader == null) {
            throw new RuntimeException("Cannot find core TIFF reader!");
        }

        final List<IIOMetadata> out = new ArrayList<>(1);

        tiffReader.setInput(app1EXIFInput);

        IIOMetadata tiffMetadata = null;

        tiffMetadata = tiffReader.getImageMetadata(0);
        // IIOMetadata meta = tiffReader.getImageMetadata(0);
        final TIFFImageReadParam rParam = (TIFFImageReadParam) tiffReader
                .getDefaultReadParam();
        rParam.setTIFFDecompressor(null);

        tiffReader.dispose();

        out.add(0, tiffMetadata);

        return out;
    }

    /**
     * Private method - creates a copy of the metadata that can be written to
     *
     * @param tiffMetadata
     *            - in metadata
     * @return new metadata node that can be written to
     */
    private IIOMetadataNode createNewExifNode(final IIOMetadata tiffMetadata,
            final IIOMetadata thumbMeta, final BufferedImage thumbnail) {

        IIOMetadataNode app1Node = null;
        ImageWriter tiffWriter = null;
        try {
            final Iterator<ImageWriter> writers = ImageIO
                    .getImageWritersByFormatName("tiff");
            while (writers.hasNext()) {
                tiffWriter = writers.next();
                if (tiffWriter.getClass().getName()
                        .startsWith("com.sun.media")) {
                    // Break on finding the core provider.
                    break;
                }
            }
            if (tiffWriter == null) {
                System.out.println("Cannot find core TIFF writer!");
                System.exit(0);
            }

            final ImageWriteParam writeParam = tiffWriter
                    .getDefaultWriteParam();
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionType("EXIF JPEG");

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final MemoryCacheImageOutputStream app1EXIFOutput = new MemoryCacheImageOutputStream(
                    baos);
            tiffWriter.setOutput(app1EXIFOutput);

            // escribir
            tiffWriter.prepareWriteEmpty(jpegReader.getStreamMetadata(),
                    new ImageTypeSpecifier(image), image.getWidth(),
                    image.getHeight(), tiffMetadata, null, writeParam);

            tiffWriter.endWriteEmpty();

            // Flush data into byte stream.
            app1EXIFOutput.flush();

            // Create APP1 parameter array.
            final byte[] app1Parameters = new byte[6 + baos.size()];

            // Add EXIF APP1 ID bytes.
            app1Parameters[0] = (byte) 'E';
            app1Parameters[1] = (byte) 'x';
            app1Parameters[2] = (byte) 'i';
            app1Parameters[3] = (byte) 'f';
            app1Parameters[4] = app1Parameters[5] = (byte) 0;

            // Append TIFF stream to APP1 parameters.
            System.arraycopy(baos.toByteArray(), 0, app1Parameters, 6,
                    baos.size());

            // Create the APP1 EXIF node to be added to native JPEG image
            // metadata.
            app1Node = new IIOMetadataNode("unknown");
            app1Node.setAttribute("MarkerTag", (new Integer(0xE1)).toString());
            app1Node.setUserObject(app1Parameters);
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (tiffWriter != null) {
                tiffWriter.dispose();
            }
        }

        return app1Node;

    }

    /**
     * Private method - adds gps information to the exif data
     *
     * @param pos
     *            a GPSPosition object containing the information to encode
     * @param exif
     *            the exif metadata to add the position to
     *
     */
    private IIOMetadata insertGPSCoords(final IIOMetadata exif) {

        IIOMetadata outExif = null;
        try {
            final TIFFDirectory ifd = TIFFDirectory.createFromMetadata(exif);
            TIFFField gpsInfoPointer = null;

            // first get the pointer from the directory if it's not there create
            // a new one
            if (ifd.containsTIFFField(
                    EXIFParentTIFFTagSet.TAG_GPS_INFO_IFD_POINTER)) {
                gpsInfoPointer = ifd.getTIFFField(
                        EXIFParentTIFFTagSet.TAG_GPS_INFO_IFD_POINTER);
                System.out.println("Already has GPS Metadata");
                return exif;
            } else {
                // this assumes that the EXIFParentTIFFTagSet is allowed on the
                // tiff image reader

                // first construct the directory to hold the GPS data
                final TIFFDirectory gpsData = createDirectory();

                // Create the pointer with the data
                final EXIFParentTIFFTagSet parentSet = EXIFParentTIFFTagSet
                        .getInstance();
                gpsInfoPointer = new TIFFField(
                        parentSet
                                .getTag(EXIFParentTIFFTagSet.TAG_GPS_INFO_IFD_POINTER),
                        TIFFTag.TIFF_LONG, 1, gpsData);
                System.out.println(
                        "is pointer =" + gpsInfoPointer.getTag().isIFDPointer()
                                + " data type is ok=" + gpsInfoPointer.getTag()
                                        .isDataTypeOK(TIFFTag.TIFF_LONG));
            }
            ifd.addTIFFField(gpsInfoPointer);
            outExif = ifd.getAsMetadata();

        } catch (final IIOInvalidTreeException e) {
            e.printStackTrace();
        }

        return outExif;

    }

    private TIFFDirectory createDirectory() {

        final EXIFGPSTagSet gpsTags = EXIFGPSTagSet.getInstance();

        final ArrayList<EXIFGPSTagSet> tags = new ArrayList<EXIFGPSTagSet>();
        tags.add(gpsTags);
        final TIFFDirectory directory = new TIFFIFD(tags,
                EXIFParentTIFFTagSet.getInstance()
                        .getTag(EXIFParentTIFFTagSet.TAG_GPS_INFO_IFD_POINTER));
        // TIFFDirectory directory = new TIFFDirectory(new
        // TIFFTagSet[]{gpsTags},EXIFParentTIFFTagSet.getInstance().getTag(EXIFParentTIFFTagSet.TAG_GPS_INFO_IFD_POINTER));

        // create the new fields

        // version field
        TIFFField field = new TIFFField(
                gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_VERSION_ID),
                TIFFTag.TIFF_BYTE, 4, EXIFGPSTagSet.GPS_VERSION_2_2);
        directory.addTIFFField(field);
        // lat reference
        field = new TIFFField(
                gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_LATITUDE_REF),
                TIFFTag.TIFF_ASCII, 2, latRef);
        directory.addTIFFField(field);
        // latitude
        field = new TIFFField(gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_LATITUDE),
                TIFFTag.TIFF_RATIONAL, 3, latitude);
        directory.addTIFFField(field);
        // long reference
        field = new TIFFField(
                gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_LONGITUDE_REF),
                TIFFTag.TIFF_ASCII, 2, longRef);
        directory.addTIFFField(field);
        // longitude
        field = new TIFFField(gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_LONGITUDE),
                TIFFTag.TIFF_RATIONAL, 3, longitude);
        directory.addTIFFField(field);
        // time stamp
        field = new TIFFField(gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_TIME_STAMP),
                TIFFTag.TIFF_RATIONAL, 3, timeStamp);
        directory.addTIFFField(field);
        // status
        field = new TIFFField(gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_STATUS),
                TIFFTag.TIFF_ASCII, 2, status);
        directory.addTIFFField(field);
        // date stamp
        field = new TIFFField(gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_DATE_STAMP),
                TIFFTag.TIFF_ASCII, 11, dateStamp);
        directory.addTIFFField(field);
        // datum
        field = new TIFFField(gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_MAP_DATUM),
                TIFFTag.TIFF_ASCII, 6, datum);
        directory.addTIFFField(field);
        // altitude reference
        field = new TIFFField(
                gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_ALTITUDE_REF),
                TIFFTag.TIFF_BYTE, 1, altRef);
        directory.addTIFFField(field);
        field = new TIFFField(gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_ALTITUDE),
                TIFFTag.TIFF_RATIONAL, 1, altitude);
        directory.addTIFFField(field);
        // add the direction
        imgDirectionRef[0] = EXIFGPSTagSet.DIRECTION_REF_TRUE;
        field = new TIFFField(
                gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_IMG_DIRECTION_REF),
                TIFFTag.TIFF_ASCII, 2, imgDirectionRef);
        directory.addTIFFField(field);
        if (imgDirection == null) {
            imgDirection = new long[][] { { 0, 100 } };
        }
        field = new TIFFField(
                gpsTags.getTag(EXIFGPSTagSet.TAG_GPS_IMG_DIRECTION),
                TIFFTag.TIFF_RATIONAL, 1, imgDirection);
        directory.addTIFFField(field);

        return directory;
    }

    // assumes the the format is HHMM.MMMM
    private long[][] getLatitude(final String lat) {

        final float secs = Float.parseFloat("0" + lat.substring(4)) * 60.f;
        final long nom = (long) (secs * 1000);

        final long[][] latl = new long[][] {
                { Long.parseLong(lat.substring(0, 2)), 1 },
                { Long.parseLong(lat.substring(2, 4)), 1 }, { nom, 1000 } };

        return latl;
    }

    // assumes the the format is HHHMM.MMMM
    private long[][] getLongitude(final String longi) {

        final float secs = Float.parseFloat("0" + longi.substring(5)) * 60.f;
        final long nom = (long) (secs * 1000);

        final long[][] longl = new long[][] {
                { Long.parseLong(longi.substring(0, 3)), 1 },
                { Long.parseLong(longi.substring(3, 5)), 1 }, { nom, 1000 } };

        return longl;
    }

    /**
     * Convert a time to exif format.
     *
     * @param time
     *            the time in format HHMMSS.
     * @return the exif time object.
     */
    private long[][] getTime(final String time) {
        final long[][] timel = new long[][] {
                { Long.parseLong(time.substring(0, 2)), 1 },
                { Long.parseLong(time.substring(2, 4)), 1 },
                { Long.parseLong(time.substring(4)), 1 } };
        return timel;
    }

    /**
     * Convert a date to exif date.
     *
     * @param date
     *            the date in format YYYY:MM:DD
     * @return the exif date object.
     */
    private String[] getDate(final String date) {

        final String dateStr = "20" + date.substring(4) + ":"
                + date.substring(2, 4) + ":" + date.substring(0, 2);

        final String[] dateArray = new String[11];

        for (int i = 0; i < dateStr.length(); i++) {
            dateArray[i] = dateStr.substring(i, i + 1);
        }
        dateArray[10] = "";

        return dateArray;
    }
}
