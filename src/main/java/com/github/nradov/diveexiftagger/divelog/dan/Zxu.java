package com.github.nradov.diveexiftagger.divelog.dan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.nradov.diveexiftagger.divelog.Dive;

/**
 * Level 1 dive log export.
 *
 * @author Nick Radov
 */
public class Zxu extends Message {

	private final Fsh fsh;
	private final Zrh zrh;
	private final Zar zar;
	final List<DiveGroupZxu> diveGroup = new ArrayList<>();

	/**
	 * Create a new {@code Zxu} object by parsing a message file.
	 *
	 * @param path
	 *            message file
	 * @throws IOException
	 *             if any error occurs while reading the message file
	 */
	public Zxu(final Path path) throws IOException {
		try (final Stream<String> stream = Files.lines(path)) {
			final List<String> lines = stream.collect(Collectors.toList());
			fsh = new Fsh(this, lines.get(0));
			zrh = new Zrh(this, lines.get(1));
			final List<String> segmentLines = splitExtendedSegments(lines.subList(2, lines.size()));
			int index = 0;
			zar = segmentLines.get(index).startsWith(Zar.ID) ? new Zar(this, segmentLines.get(index++)) : null;
			while (index < segmentLines.size() && segmentLines.get(index).startsWith(Zdh.ID)) {
				int toIndex = index;
				// find the end of the segment group
				for (int i = 0; i < DiveGroupZxu.SEGMENT_IDS.size(); i++) {
					while (toIndex < segmentLines.size()
							&& DiveGroupZxu.SEGMENT_IDS.get(i).equals(segmentLines.get(toIndex).substring(0, 3))) {
						toIndex++;
					}
				}
				diveGroup.add(new DiveGroupZxu(this, segmentLines.subList(index, toIndex)));

			}
		}
		throw new UnsupportedOperationException();
	}

	int getDiveGroupSize() {
		return diveGroup.size();
	}

	DiveGroupZxu getDiveGroup(final int index) {
		return diveGroup.get(index);
	}

	/**
	 * Get the file segment header.
	 *
	 * @return the FSH segment.
	 */
	public Fsh getFsh() {
		return fsh;
	}

	/**
	 * Get the application reserved segment.
	 *
	 * @return the ZAR segment
	 */
	public Zar getZar() {
		return zar;
	}

	/**
	 * Get the record header.
	 *
	 * @return the ZRH segment.
	 */
	public Zrh getZrh() {
		return zrh;
	}

	@Override
	St getFieldSeparator() {
		return fsh.getFieldSeparator();
	}

	@Override
	St getEncodingCharacters() {
		return fsh.getEncodingCharacters();
	}

	@Override
	public float getDepthMeters(final Instant instant) {
		for (final Dive dive : diveGroup) {
			if (dive.isDuringDive(instant)) {
				return dive.getDepthMeters(instant);
			}
		}
		throw new IllegalArgumentException("no dive at " + instant);
	}

}
