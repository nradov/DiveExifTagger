package com.github.nradov.diveexiftagger.divelog;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.zip.ZipException;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.github.nradov.diveexiftagger.divelog.dan.Zxl;
import com.github.nradov.diveexiftagger.divelog.dan.Zxu;
import com.github.nradov.diveexiftagger.divelog.suunto.SuuntoSde;

/**
 * Utility class for creating {@link DivesSource} objects.
 *
 * @author Nick Radov
 */
public final class DiveSourceFactory {

    private DiveSourceFactory() {
        // not to be instantiated
    }

    /** Constants for supported dive log file extensions. */
    private static final class FileExtension {

        /** Suunto dive export. */
        final static String SUUNTO_DIVE_EXPORT = ".sde";

        /** DAN DL7 Level 1 (dive profile). */
        final static String DAN_DL7_LEVEL_1 = ".zxu";

        /**
         * DAN DL7 Level 3 (diver demographics, dive profile & dive log
         * details).
         */
        final static String DAN_DL7_LEVEL_3 = ".zxl";
    }

    /**
     * Factory method to automatically create the right source of dive profiles
     * based on the file extension. Currently the following file extensions are
     * supported.
     * <table>
     * <thead>
     * <tr>
     * <th>File Extension</th>
     * <th>Description</th>
     * </tr>
     * </thead><tbody>
     * <tr>
     * <td>.sde</td>
     * <td>Suunto Dive Export</td>
     * </tr>
     * <tr>
     * <td>.zxu</td>
     * <td>DAN DL7 Level 1</td>
     * </tr>
     * <tr>
     * <td>.zxl</td>
     * <td>DAN DL7 Level 3</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param file
     *            dive profiles
     * @return source of zero or more dive profiles
     * @throws ZipException
     *             if an error occurs while reading a compressed dive log file
     * @throws IOException
     *             if an error occurs while reading a dive log file
     * @throws ParserConfigurationException
     *             if an error occurs while reading an XML dive log file
     * @throws SAXException
     *             if an error occurs while reading an XML dive log file
     */
    @Nonnull
    public static DivesSource create(@Nonnull final Path file,
            @Nonnull final ZoneOffset zoneOffset) throws ZipException,
            IOException, ParserConfigurationException, SAXException {
        final String lowerCaseFile = file.toString().toLowerCase(Locale.US);
        if (lowerCaseFile.endsWith(FileExtension.SUUNTO_DIVE_EXPORT)) {
            return new SuuntoSde(file, zoneOffset);
        } else if (lowerCaseFile.endsWith(FileExtension.DAN_DL7_LEVEL_1)) {
            return new Zxu(file);
        } else if (lowerCaseFile.endsWith(FileExtension.DAN_DL7_LEVEL_3)) {
            return new Zxl(file);
        }
        // TODO: add support for other file formats

        throw new IllegalArgumentException(
                "unrecognized file format: \"" + file + "\"");
    }

    public static DivesSource create(@Nonnull final String file,
            @Nonnull final ZoneOffset zoneOffset) throws ZipException,
            IOException, ParserConfigurationException, SAXException {
        return create(Paths.get(file), zoneOffset);
    }

}
