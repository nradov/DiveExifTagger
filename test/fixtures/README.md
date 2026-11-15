# Test fixtures

Real files from one dive, used by `test/real-data.test.js`:

| File | What it is |
| --- | --- |
| `24921353436.zip` | Garmin Connect export of a Garmin Descent Mk2 dive (2026-10-31, 74 min, max 43.6 m), containing `24921353436_ACTIVITY.fit` |
| `PA310402.JPG` … `PA310471.JPG` | Five photos from an OM System TG-7 in an underwater housing, taken 12–28 minutes into that dive |

Things they exercise:

- **Garmin Connect ZIP:** the FIT file is DEFLATE-compressed with a data descriptor.
- **Time zone tag:** the photos carry `OffsetTimeOriginal` (`-07:00`), which matches
  the dive computer's offset.
- **Wrong camera depth/temperature:** the TG-7 writes its own `WaterDepth`,
  `Temperature` and `Pressure` tags, but in a housing it measures the inside of
  the housing (≈0.6 m, 21–25 °C). The app overwrites depth and temperature with
  the dive computer's values.
- **Compass heading:** the TG-7 stores `GPSImgDirection` in the GPS IFD, which
  must survive geotagging.
- **Maker note:** a large Olympus maker note that must be preserved byte-for-byte.

The photos are © the project owner and included for testing this project.
