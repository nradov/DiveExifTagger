# Dive Geotagger

Geotag underwater photos with **location, depth and water temperature** from your
dive computer's log, entirely in your web browser. No uploads, no server, no
install.

1. Drop in one or more dive logs (currently **Garmin FIT**, e.g. Descent series).
2. Drop in your JPEG photos.
3. Check the camera time zone / clock correction.
4. Download the geotagged copies as a ZIP, or save them straight to a folder.

Your original files are never modified.

## What gets written

| Data | EXIF tag(s) | Notes |
| --- | --- | --- |
| Position | `GPSLatitude`, `GPSLongitude` (+ refs), `GPSMapDatum` | Dive computers can't get a GPS fix underwater, so the position comes from the dive's entry point, exit point, a time-based interpolation between them, or a manual position. |
| Depth | `GPSAltitude` with `GPSAltitudeRef = 1` (below sea level) | Optional. Most photo managers (Lightroom, Apple Photos, digiKam…) display this. |
| Depth | `WaterDepth` (Exif 2.31, `0x9403`) | Optional. The standard tag for water depth, in metres. |
| Temperature | `Temperature` (Exif 2.31, `0x9400`) | Optional. Water temperature in °C at the photo's time. |
| Time | `GPSDateStamp`, `GPSTimeStamp` | The photo's corrected UTC time. |

Depth and temperature are linearly interpolated between the dive computer's
samples. Photos taken shortly before or after a dive (configurable tolerance)
are tagged as surface shots at the entry/exit point.

Existing metadata, including maker notes and embedded thumbnails, is preserved:
the original EXIF block is kept byte-for-byte and only new IFDs are appended
and re-pointed.

## Getting camera time right

Cameras store local wall-clock time with no time zone, and their clocks drift.
Dive Geotagger converts photo times to UTC using:

- the photo's `OffsetTimeOriginal` tag, if the camera wrote one, otherwise
- the **camera time zone** setting (defaulted to the zone the dive computer
  was using), plus
- a **clock correction**. The easiest way to find it: take a photo of your dive
  computer's screen, then use *"Work out the correction from a photo of your
  dive computer's screen"*.

## Running locally

There's no build step. Any static file server works:

```sh
npm start            # python3 -m http.server 8080
# or: npx serve .
```

Then open <http://localhost:8080>. (Opening `index.html` directly from disk
won't work because browsers block ES modules on `file://`.)

Run the tests (Node 20+, no dependencies to install):

```sh
npm test
```

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
src/match.js             photo ↔ dive matching and interpolation
src/formats/index.js     dive log parser registry
src/formats/garmin-fit.js   Garmin FIT → Dive adapter
src/formats/fit/decoder.js  generic FIT binary decoder
src/exif/jpeg-exif.js    JPEG EXIF reader/writer
src/zip.js               tiny ZIP writer
src/ui/profile.js        SVG depth profile
test/                    node:test suite and fixture builders
docs/ADDING_A_FORMAT.md  how to add another dive log format
```

Design principles: plain ES modules, zero runtime dependencies, and everything
runs client-side.

## Adding dive log formats

Parsers convert a file into the shared `Dive` model, so the rest of the app
doesn't care where the data came from. See
[docs/ADDING_A_FORMAT.md](docs/ADDING_A_FORMAT.md). Good candidates:
Subsurface XML (`.ssrf`), UDDF (`.uddf`), Shearwater, Suunto (`.json`/`.sml`),
MacDive and DL7.

## Roadmap

- More dive log formats (see above)
- XMP sidecars for RAW files, and HEIC support
- Map view for choosing / adjusting dive site positions
- Per-dive position overrides and per-photo manual adjustments
- ZIP64 for exports over 4 GB
- Offline support (service worker / installable PWA)

## License

[MIT](LICENSE)
