# Adding a dive log format

Each dive log format is a small module in `src/formats/` that turns file bytes
into an array of `Dive` objects (see `src/model.js`). Matching, EXIF writing and
the UI only ever see `Dive`s.

## 1. Write the parser

```js
// src/formats/uddf.js
import { makeDive } from '../model.js';

export default {
  id: 'uddf',
  name: 'UDDF',
  extensions: ['.uddf'],

  // Return true only if you're confident the bytes are your format.
  // Called before the extension check, so keep it cheap and strict.
  sniff(bytes) {
    const head = new TextDecoder().decode(bytes.subarray(0, 512));
    return head.includes('<uddf');
  },

  parse(bytes, { fileName }) {
    const xml = new DOMParser().parseFromString(new TextDecoder().decode(bytes), 'application/xml');
    // ...walk the document...
    return dives.map((d) =>
      makeDive({
        source: 'uddf',
        fileName,
        device: d.computerName ?? null,
        startTime: d.startUtcMs,       // UTC milliseconds
        endTime: d.endUtcMs,
        utcOffsetMinutes: d.tzOffset ?? null,   // if the format records it
        entry: d.site ? { lat: d.site.lat, lon: d.site.lon } : null,
        exit: null,
        samples: d.waypoints.map((w) => ({
          time: d.startUtcMs + w.divetime * 1000,
          depth: w.depth,              // metres, positive down
          temperature: w.temperature != null ? w.temperature - 273.15 : null, // °C
          lat: null,
          lon: null,
        })),
      }),
    );
  },
};
```

Conventions:

- **Times are UTC milliseconds.** If the format stores local time only, convert
  using whatever offset it provides and set `utcOffsetMinutes`; if there is none,
  document that the user must set the time zone.
- **Units are metric:** metres, °C, decimal degrees (WGS-84).
- Use `null` for missing values, never `0` (zero is a valid depth).
- `makeDive` sorts samples and fills in `maxDepth` / `minTemperature`.
- Throw an `Error` with a helpful message for corrupt files; the UI shows it.
- Note that `DOMParser` exists in browsers but not in Node. If you want your
  parser tested under `node --test`, keep XML handling isolated or inject it.

## 2. Register it

Add it to `PARSERS` in `src/formats/index.js`:

```js
import uddf from './uddf.js';
export const PARSERS = [garminFit, uddf];
```

The file picker's accepted extensions and the "Supported:" hint update
automatically.

## 3. Test it

Add a test in `test/` (see `test/core.test.js`). If your parser needs a
third-party module, add it to the import map in `index.html` and to
`devDependencies` in `package.json` so the browser and the tests resolve the
same bare specifier. Prefer generating small
fixtures in code (like `test/helpers/fit-writer.js`) or committing a tiny,
anonymised sample file under `test/fixtures/`. Please strip or fuzz real GPS
coordinates from shared sample logs if they are private.

```sh
npm test
```
