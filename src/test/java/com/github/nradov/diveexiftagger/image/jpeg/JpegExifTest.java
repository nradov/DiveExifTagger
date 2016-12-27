package com.github.nradov.diveexiftagger.image.jpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        assertEquals(8, jpeg.getSegments().size());
        assertTrue(jpeg.getSegments().get(0) instanceof StartOfImage);
        assertEquals(2, jpeg.getSegments().get(0).getLength());
        assertTrue(jpeg.getSegments().get(1) instanceof ApplicationSpecific1);
        assertEquals(1216, jpeg.getSegments().get(1).getLength());
        assertTrue(jpeg.getSegments().get(2) instanceof ApplicationSpecific1);
        // assertEquals(, jpeg.getSegments().get(2).getLength());

        assertTrue(jpeg.getSegments().get(7) instanceof EndOfImage);
        assertEquals(2, jpeg.getSegments().get(7).getLength());

        jpeg.toString();
        assertEquals(0, new BigDecimal("6.1").compareTo(jpeg.getGpsAltitude()
                .get().toBigDecimal(RoundingMode.UNNECESSARY)));
        final Path temp = Files.createTempFile(null, null);
        jpeg.write(temp);

        assertEquals(Files.size(imageFile), Files.size(temp));
        Files.delete(temp);
    }

}
