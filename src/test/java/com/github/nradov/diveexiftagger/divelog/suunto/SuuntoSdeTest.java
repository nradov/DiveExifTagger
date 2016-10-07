package com.github.nradov.diveexiftagger.divelog.suunto;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.ZipException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.github.nradov.diveexiftagger.divelog.DivesSource;

/**
 * Test the {@link SuuntoSde} class.
 *
 * @author Nick Radov
 *
 */
public class SuuntoSdeTest {

    private static final String SDE_RESOURCE_NAME = "/Divelogs.SDE";

    @Test
    public void testSuuntoSdeFileZoneOffset() throws ZipException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        assertNotNull(new SuuntoSde(
                new File(getClass().getResource(SDE_RESOURCE_NAME).toURI()),
                null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDepthMetersNull() throws ZipException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final DivesSource source = new SuuntoSde(
                new File(getClass().getResource(SDE_RESOURCE_NAME).toURI()),
                null);
        source.getDepthMeters(null);
    }

}
