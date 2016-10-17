package com.github.nradov.diveexiftagger.divelog.dan;

import java.util.ArrayList;
import java.util.List;

import com.github.nradov.diveexiftagger.divelog.DivesSource;

abstract class Message extends SegmentGroup implements DivesSource {

    /**
     * Split extended segments (primarily ZDP) into separate segments for easier
     * parsing.
     *
     * @param lines
     *            DL7 message broken up by segment terminators
     * @return each entry represents a single complete segment
     */
    List<String> splitExtendedSegments(final List<String> lines) {
        final List<String> segments = new ArrayList<>();
        boolean inExtension = false;
        final String segmentExtension = getSegmentExtensionString();
        if (segmentExtension.length() != 2) {
            throw new IllegalStateException(
                    "segment extension delimiter length is not 2");
        }
        final String segmentExtensionStart = segmentExtension.substring(0, 1);
        final String segmentExtensionEnd = segmentExtension.substring(1);
        String id = null;
        for (final String line : lines) {
            if (inExtension) {
                if (line.endsWith(segmentExtensionEnd)) {
                    inExtension = false;
                    id = null;
                } else {
                    segments.add(id + line);
                }
            } else {
                if (line.endsWith(segmentExtensionStart)) {
                    inExtension = true;
                    id = line.substring(0, 3);
                } else {
                    segments.add(line);
                }
            }
        }
        return segments;
    }

}
