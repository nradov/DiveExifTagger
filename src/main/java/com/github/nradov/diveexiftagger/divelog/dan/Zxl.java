package com.github.nradov.diveexiftagger.divelog.dan;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Diver demographics, Dive profile &amp; Dive log details.
 *
 * @author Nick Radov
 */
public final class Zxl extends Zxu {

	private Zpd zpd;
	private Zpa zpa;

	/**
	 * Create a new {@code Zxl} by parsing a message file.
	 *
	 * @param path
	 *            message file
	 * @throws IOException
	 *             if any error occurs while reading the message file
	 */
	public Zxl(final Path path) throws IOException {
		super(path);
	}

	/**
	 * Get the diver identification and demographics.
	 * 
	 * @return ZPD segment
	 */
	Zpd getZpd() {
		return zpd;
	}

	/**
	 * Get the diver's address.
	 * 
	 * @return ZPA segment
	 */
	Zpa getZpa() {
		return zpa;
	}

	@Override
	DiveGroupZxl getDiveGroup(final int index) {
		return (DiveGroupZxl) super.getDiveGroup(index);
	}

}
