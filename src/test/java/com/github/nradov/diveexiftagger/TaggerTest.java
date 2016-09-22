package com.github.nradov.diveexiftagger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.ZipException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TaggerTest {

	@Test
	public void testTagFiles() throws ZipException, IOException, ParserConfigurationException, SAXException,
			ImageWriteException, ImageReadException, URISyntaxException {
		final File imageFile = new File(getClass().getResource("/IMG_8687.JPG").toURI());
		final File diveLogsFile = new File(getClass().getResource("/Divelogs.SDE").toURI());
		final Tagger tagger = new Tagger(imageFile, diveLogsFile);
		tagger.tagFiles();
	}

}
