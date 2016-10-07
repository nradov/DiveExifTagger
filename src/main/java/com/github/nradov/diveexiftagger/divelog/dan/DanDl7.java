package com.github.nradov.diveexiftagger.divelog.dan;

import java.time.Instant;

import com.github.nradov.diveexiftagger.divelog.DivesSource;

/**
 * Divers Alert Network Dive Log 7 format.
 *
 * @author Nick Radov
 * @see <a href=
 *      "https://github.com/johnstonskj/PyDL7/blob/master/docs/reference/dl7-specification.doc"
 *      title="Github" target="_">DL7 Standard</a>
 */
public class DanDl7 implements DivesSource {

    @Override
    public float getDepthMeters(final Instant instant) {
        throw new UnsupportedOperationException();
    }

}
