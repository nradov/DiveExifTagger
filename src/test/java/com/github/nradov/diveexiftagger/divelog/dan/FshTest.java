package com.github.nradov.diveexiftagger.divelog.dan;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test the {@link Fsh} class.
 *
 * @author Nick Radov
 */
public class FshTest {

    @Test
    public void testFsh() {
        final Fsh fsh = new Fsh(null,
                "FSH|^~<>{}|ANST01^12X456^A|ZXU|19980206184224+0005|");
        assertEquals("|", fsh.getFieldSeparatorString());
        assertEquals("^~<>{}", fsh.getEncodingCharacters().toString());
        assertEquals("ANST01^12X456^A",
                fsh.getFileSendingApplication().toString());
        assertEquals("ZXU", fsh.getMessageType().getIdentifier().toString());
        assertEquals("19980206184224+0005",
                fsh.getFileCreationDateTime().toString());
    }

}
