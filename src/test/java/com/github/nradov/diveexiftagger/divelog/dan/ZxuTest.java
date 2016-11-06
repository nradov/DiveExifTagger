package com.github.nradov.diveexiftagger.divelog.dan;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * Test the {@link Zxu} class.
 * 
 * @author Nick Radov
 */
public class ZxuTest {

    @Test
    public void testZxu() throws URISyntaxException, IOException {
        final Path example = Paths
                .get(getClass().getResource("/example.ZXU").toURI());
        final Zxu zxu = new Zxu(example);
        assertEquals("|", zxu.getFieldSeparatorString());
    }

}
