package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.nio.ByteOrder;

/**
 * Canon proprietary MakerNote directory entry.
 *
 * @author Nick Radov
 * @see <a target="_" href=
 *      "http://www.ozhiker.com/electronics/pjmt/jpeg_info/canon_mn.html">Canon
 *      Makernote Format Specification</a>
 */
class CanonMakerNote extends ImageFileDirectory {

	CanonMakerNote(final byte[] b, final int offset, final ByteOrder byteOrder) {
		super(b, offset, byteOrder, FieldTag.Proprietary.Canon);
	}

}
