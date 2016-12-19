package com.github.nradov.diveexiftagger.image.jpeg;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.adobe.internal.xmp.XMPException;

public class JpegExifTest {

    @Test
    public void testJpegExif()
            throws URISyntaxException, IOException, XMPException {
        final Path imageFile = Paths
                .get(getClass().getResource("/IMG_8687.JPG").toURI());
        final JpegExif jpeg = new JpegExif(imageFile);
        jpeg.toString();
        assertEquals(0, new BigDecimal("6.1").compareTo(jpeg.getGpsAltitude()
                .get().toBigDecimal(RoundingMode.UNNECESSARY)));
        final Path temp = Files.createTempFile(null, null);
        jpeg.write(temp);

        assertEquals(Files.size(imageFile), Files.size(temp));
        Files.delete(temp);
    }

}
