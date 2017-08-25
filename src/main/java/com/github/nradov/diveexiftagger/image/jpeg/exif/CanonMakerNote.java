package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.nio.ByteOrder;

/**
 * Canon proprietary MakerNote directory entry.
 *
 * @author Nick Radov
 * @see <a target="_" href=
 *      "http://www.ozhiker.com/electronics/pjmt/jpeg_info/canon_mn.html">Canon
 *      Makernote Format Specification</a>
 * @see <a target="_" href=
 *      "http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/Canon.html">Canon
 *      Tags</a>
 * @see <a target="_" href="http://www.burren.cx/david/canon.html">EXIF
 *      MakerNote of Canon</a>
 */
class CanonMakerNote extends ImageFileDirectory {

	CanonMakerNote(final byte[] b, final int offset, final ByteOrder byteOrder) {
		super(b, offset, byteOrder, FieldTag.Proprietary.Canon);
	}

	@Override
	int getLength() {
		// add 2 bytes for the entry count
		return FieldType.SHORT.getLength() + super.getLength();
	}
}
