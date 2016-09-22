package com.github.nradov.diveexiftagger.divelog;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.zip.ZipException;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

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

	private static final class FileExtension {
		final static String SUUNTO_DIVE_EXPORT = ".sde";
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
	 * </tbody>
	 * </table>
	 * 
	 * @param file
	 *            dive profiles
	 * @return source of zero or more dive profiles
	 * @throws ZipException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	@Nonnull
	public static DivesSource create(final File file, final ZoneOffset zoneOffset)
			throws ZipException, IOException, ParserConfigurationException, SAXException {
		if (file.getCanonicalPath().toLowerCase(Locale.US).endsWith(FileExtension.SUUNTO_DIVE_EXPORT)) {
			return new SuuntoSde(file, zoneOffset);
		}
		// TODO: add support for other file formats

		throw new IllegalArgumentException("unrecognized file format: \"" + file.getCanonicalPath() + "\"");
	}

	public static DivesSource create(final String file, final ZoneOffset zoneOffset)
			throws ZipException, IOException, ParserConfigurationException, SAXException {
		return create(new File(file), zoneOffset);
	}

}
