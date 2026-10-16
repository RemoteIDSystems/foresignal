# Foresignal SatDump hook (experimental)

This directory holds an **optional, separate** SatDump 2.0 AngelScript hook, `foresignal_hook.as`, that
makes a SatDump 2.0 station write products Foresignal can read without a version-specific parser.

It is **MIT-licensed** and is *not* bundled with Foresignal: the operator copies it into their SatDump
plugins directory. Foresignal never links it and its images never contain it. It copies no SatDump
source, headers or structs — it only calls SatDump's own public scripting API.

## Status

**Experimental and unverified.** As of 2026-10, SatDump 2.0 exists only as alpha/nightly builds; the
latest stable is **1.2.2**. The hook is gated: use it only when `foresignal.satdump.version` is `2.0`
(or a 2.0 stable release exists). It needs SatDump built with `PLUGIN_SCRIPTING` (off by default;
the official Dockerfile enables it).

The AngelScript API calls in `foresignal_hook.as` are the *intended* shape; verify the exact function
names against the installed build before enabling. If they do not match, ship only this description
and the manifest specification: Foresignal's manifest reader is version-agnostic and works with any
tool that writes the manifest.

## The manifest specification

Foresignal's inbox loop recognises a `foresignal-manifest.json` in a ready pass folder:

```json
{
  "schema": 1,
  "satellite": "METEOR-M2 3",
  "norad": 57166,
  "timestamp": "2026-10-15T18:02:00Z",
  "products": [
    {
      "name": "rgb",
      "instrument": "MSU-MR",
      "file": "MSU-MR/rgb_MSU-MR.tif",
      "projection": "equirec",
      "bounds": [-180, -90, 180, 90],
      "composite": "rgb_MSU-MR"
    }
  ]
}
```

- `schema` must be `1`; any other value is rejected with a health reason.
- `file` is relative to the manifest and must not be absolute or contain `..`.
- The listed GeoTIFFs (EPSG:4326/3857) and PNGs are read through the same pipeline as the polar
  overlays: `ImageGuard` first, then the GeoTIFF reader, and they become
  `satdump-{station}-{composite}` frames with real footprints. Because the polar glob (`**/*.tif`)
  already matches them, the manifest's main role is to declare the 2.0 output and validate the schema.

## Install

1. Build SatDump 2.0 with the scripting plugin enabled.
2. Copy `foresignal_hook.as` into the SatDump plugins directory.
3. Point the station's output directory at Foresignal's inbox (the `inboxPath` parameter), or sync it
   there (the 30-second rescan picks it up).
4. Set `foresignal.satdump.version = 2.0` in Foresignal's settings.
