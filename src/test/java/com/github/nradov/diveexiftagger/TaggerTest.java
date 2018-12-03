package com.github.nradov.diveexiftagger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class TaggerTest {

    @Test
    public void testTagFiles() throws ZipException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final Path diveLogsFile = Paths
                .get(getClass().getResource("/Divelogs.SDE").toURI());
    }

}
