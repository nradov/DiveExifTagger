# DiveExifTagger

Geotag underwater photos with **location, depth and water temperature** from your
dive computer's log, entirely in your web browser. No uploads, no server, no
install.

**▶ Use it here: <https://nradov.github.io/DiveExifTagger/>**

1. Drop in one or more dive logs: **FIT** files from **Garmin** (e.g. Descent
   series) or **Suunto** (e.g. Ocean), as `.fit` files or as the `.zip` you get
   when exporting from Garmin Connect. See [Supported dive computers](#supported-dive-computers).
2. Drop in your JPEG photos.
3. Check the times. Time zones are detected automatically.
4. Download the geotagged copies as a ZIP, or save them straight to a folder.

Your original files are never modified.

## Supported dive computers

| Brand | How to get the file | Where the position comes from |
| --- | --- | --- |
| **Garmin** (Descent Mk2/Mk3, G1, other Garmin dive watches) | Export from the Garmin Connect website (see [below](#exporting-a-dive-from-garmin-connect)) | Entry/exit points recorded by the watch |
| **Suunto** (Ocean, Nautic and other models that sync to the Suunto app) | Suunto app → dive → *Export* / download as a `.fit` file | GPS fixes the watch recorded at the surface before and after the dive |

### Exporting a dive from Garmin Connect

1. Sign in to [Garmin Connect](https://connect.garmin.com/) in a web browser.
2. Open the dive activity.
3. Click the **More** button: the gear icon in the upper right corner of the activity page.
4. Click **Export File**.

Your browser downloads a `.zip` file containing the dive's `.fit` file. Drop
the `.zip` into DiveExifTagger as is; there's no need to unzip it.

**Positions come only from the dive computer's own GPS fixes.** If you change
or add a dive location afterwards on the Garmin Connect website or in the Suunto
app, the exported FIT file still contains the coordinates the dive computer
recorded (or none). Use the manual position option for those dives.

Suunto support was tested against Suunto Ocean exports (scuba and freediving).

### Other activities: GPS tracks (e.g. a watch in a towed surface buoy)

For underwater survey work you can put a GPS watch (for example a Garmin
Forerunner 935) in a towed surface buoy and record any non-diving activity,
such as an open water swim. Export it the same way and drop it in like a dive log.

DiveExifTagger reads the activity type from the FIT file. Scuba and freediving
activities are handled as dives. Any other activity is handled as a **GPS track**:

- **Position:** each photo's position is interpolated between the GPS fixes
  recorded immediately before and after the photo was taken. Gaps in the GPS
  track are bridged in a straight line, and the review table flags photos
  taken in a gap longer than two minutes.
- **No depth or temperature:** the watch has no depth data, and its temperature
  isn't the water temperature where the photo was taken, so neither is written.
  When only GPS tracks are loaded, those options are switched off.
- **The chart** shows where the track has GPS coverage and where the photos fall,
  instead of a depth profile.

### Combining a dive computer log with a surface GPS track

On survey dives you'll typically have two FIT files: one from the dive
computer (depth, temperature) and one from the watch in the towed buoy
(position). Run your photos through the page twice, once with each file, in
either order. Load the photos downloaded from the first pass into the second.

- The **GPS track pass** writes the position measured at the photo's time.
- The **dive log pass** writes depth and temperature. It does not replace a
  position measured at the photo's time (marked `GPSStatus = A`, below), so the
  track's position survives even if the dive log is applied second. Choosing
  *Manual position* still overrides it.
- Each pass only replaces the tags it owns, so the result is identical in
  either order. All other metadata, including maker notes, thumbnails and
  the camera's own GPS tags such as compass heading, is kept, and the image
  data is never re-encoded. This is checked by automated tests and was
  verified with ExifTool (`-validate`).
Other dive log formats can be added; see [Adding dive log formats](#adding-dive-log-formats).

## What gets written

Tags follow the Exif standard, published by CIPA as
[CIPA DC-008](https://www.cipa.jp/e/std/std-sec.html); the latest edition is
[Exif 3.1 (DC-008-2026)](https://www.cipa.jp/std/documents/download_e.html?CIPA_DC-008-2026-E).
`WaterDepth` and `Temperature` were added in Exif 2.31.

| Data | EXIF tag(s) | Notes |
| --- | --- | --- |
| Position | `GPSLatitude`, `GPSLongitude` (+ refs), `GPSMapDatum` | Dive computers can't get a GPS fix underwater. **Automatic** (the default) uses the entry point for dives and interpolates along the GPS track for other activities. You can also choose the exit point, a time-based interpolation between entry and exit, interpolation along the GPS track, or a manual position. |
| Depth | `GPSAltitude` with `GPSAltitudeRef = 1` (below sea level) | Optional, dives only. Most photo managers (Lightroom, Apple Photos, digiKam…) display this. |
| Depth | `WaterDepth` (Exif 2.31, `0x9403`) | Optional, dives only. The standard tag for water depth, in metres. |
| Temperature | `Temperature` (Exif 2.31, `0x9400`) | Optional, dives only. Water temperature in °C at the photo's time. |
| Time | `GPSDateStamp`, `GPSTimeStamp` | The photo's corrected UTC time. |
| Position source | `GPSProcessingMethod`, `GPSStatus` | Standard values only: `GPS` (or `MANUAL`), and `GPSStatus` `A` when the position was measured at the photo's time (GPS track) or `V` when it wasn't (a dive's entry/exit point, or a manual position). |

EXIF values are always metric (metres, °C), as the standard requires. The web page
can *display* depths and temperatures in metric or statute units (feet, °F); the
default follows your browser's locale.

Depth and temperature are linearly interpolated between the dive computer's
samples. Photos taken shortly before or after a dive (configurable tolerance)
are tagged as surface shots at the entry/exit point.

Existing metadata, including maker notes and embedded thumbnails, is preserved:
the original EXIF block is kept byte-for-byte and only new IFDs are appended
and re-pointed.

## Getting camera time right

Cameras record local wall-clock time, and their clocks drift. With the camera
time zone left on **Automatic**, DiveExifTagger works out each photo's time zone
from:

1. the time zone saved in the photo (`OffsetTimeOriginal`, or `OffsetTime`).
   Many recent cameras write this, including the OM System / Olympus TG-7;
2. otherwise, the dive computer's time zone for that dive, assuming the camera
   and dive computer were set to the same zone. With dives in several zones,
   each photo gets the zone of the dive it falls in.

Choosing a specific zone overrides both. The review table shows which zone was
used for each photo and where it came from.

If the camera clock was off by a few minutes, open *"Camera clock was a few
minutes off?"* and enter a correction. The easiest way to find it is to take a
photo of your dive computer's screen and let the app calculate it.

## Running locally

There's no build step. Any static file server works:

```sh
npm start            # python3 -m http.server 8080
# or: npx serve .
```

Then open <http://localhost:8080> (needs internet access the first time to load
the FIT SDK from jsDelivr). (Opening `index.html` directly from disk
won't work because browsers block ES modules on `file://`.)

Run the tests (Node 22+):

```sh
npm install   # installs the dependencies used by the tests
npm test
```

Besides generated test data, the tests run against a real Garmin Descent dive
log and photos from an OM System TG-7 in [`test/fixtures/`](test/fixtures/).

## Deploying

The included GitHub Actions workflow (`.github/workflows/pages.yml`) publishes
the site to GitHub Pages on every push to `main`. In your repository, go to
**Settings → Pages** and set **Source** to **GitHub Actions**.

## Project layout

```
index.html               UI markup
css/style.css            styles (light & dark)
src/app.js               UI controller
src/model.js             format-neutral Dive / Sample model
src/match.js             photo ↔ dive matching and interpolation (incl. GPS tracks)
src/tags.js              which EXIF values to write for a photo
src/formats/index.js     dive log parser registry
src/formats/fit.js       FIT (Garmin, Suunto) → Dive adapter (uses @garmin/fitsdk)
src/exif/jpeg-exif.js    JPEG EXIF reader/writer
src/ui/profile.js        SVG depth profile
test/                    node:test suite and fixture builders
docs/ADDING_A_FORMAT.md  how to add another dive log format
```

Design principles: plain ES modules, no build step, and everything runs
client-side.

## Dependencies

| Library | Used for | License |
| --- | --- | --- |
| [`@garmin/fitsdk`](https://www.npmjs.com/package/@garmin/fitsdk) | Decoding FIT dive logs | Garmin FIT Protocol License |
| [`fflate`](https://github.com/101arrowz/fflate) | Unpacking `.zip` dive logs, building the `.zip` download | MIT |

Both are loaded from jsDelivr through the import map in `index.html`, and
installed from npm (`npm install`) for the tests. Photos and logs never leave
the device; only library code is downloaded.

JPEG/EXIF reading and writing is this project's own code (`src/exif/`): the
available JavaScript EXIF writers can't write the Exif 2.31 `WaterDepth` and
`Temperature` tags and drop camera tags they don't recognise.

### Garmin FIT SDK

FIT files are decoded with Garmin's official
[FIT JavaScript SDK](https://developer.garmin.com/fit/overview/)
([`@garmin/fitsdk`](https://www.npmjs.com/package/@garmin/fitsdk)).

The SDK is distributed under Garmin's
[FIT Protocol License](https://developer.garmin.com/fit/download/), not under
this project's license, and is not included in this repository. Anyone
redistributing or self-hosting it is responsible for complying with that
license.

## Adding dive log formats

Parsers convert a file into the shared `Dive` model, so the rest of the app
doesn't care where the data came from. See
[docs/ADDING_A_FORMAT.md](docs/ADDING_A_FORMAT.md). Good candidates:
Subsurface XML (`.ssrf`), UDDF (`.uddf`), Shearwater, Suunto's older formats
(`.sml`/`.sde`) and its newer JSON export, MacDive and DL7.

**Shearwater:** Shearwater computers can record dive entry/exit coordinates with
the Swift GPS transmitter, and libdivecomputer documents how they are stored.
What's missing is a sample export that contains them. If you dive with a Swift
GPS, please open an issue and attach a Shearwater export of a dive with GPS
(UDDF or CSV) plus a screenshot of the coordinates the Shearwater app shows.

## Roadmap

- More dive log formats (see above)
- XMP sidecars for RAW files, and HEIC support
- Map view for choosing / adjusting dive site positions
- Per-dive position overrides and per-photo manual adjustments
- ZIP64 for exports over 4 GB
- Offline support (service worker / installable PWA), optionally self-hosting the FIT SDK

## License

DiveExifTagger is free software: you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version. See [COPYING.LESSER](COPYING.LESSER) and [COPYING](COPYING)
(the GNU General Public License v3, which the LGPL builds on).

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.

The Garmin FIT SDK is not part of this project and is licensed separately by
Garmin.
