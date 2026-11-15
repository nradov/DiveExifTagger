# Contributing

Thanks for helping! Bug reports, sample dive logs and new format parsers are all welcome.

## Ground rules

- **Client-side only.** Everything must run in the browser; no servers, no uploads.
- **No build step.** Plain ES modules served as-is. Third-party modules are
  loaded through the import map in `index.html` (currently only Garmin's FIT
  SDK) and listed in `package.json` so tests can use them. Discuss new
  dependencies in an issue first.
- **Never damage photos.** Changes to `src/exif/` must keep existing metadata
  intact and leave image data untouched. Add a test for any new tag.

## Development

```sh
npm start   # serve at http://localhost:8080
npm install # once: installs the FIT SDK for tests
npm test    # node --test, Node 20+
```

## Reporting a bug with a dive log

If a log doesn't parse correctly, attach the file to an issue if you're
comfortable doing so (logs can contain GPS positions of where you dive), and say
which dive computer and firmware recorded it.

## Adding a format

See [docs/ADDING_A_FORMAT.md](docs/ADDING_A_FORMAT.md).

## Pull requests

- Keep PRs focused; one format or feature per PR.
- Include tests, and make sure `npm test` passes.
- Match the existing style (2-space indent, single quotes, semicolons).
