# Fixtures: `satdump-{station}` (prompt 049)

One set per pinned SatDump version, under `{version}/`. The set here stands for **SatDump 2.0.0-alpha**
(the local reference checkout, an import of commit `1aedd602`, October 2026).

**`synthetic: true` — every file here is hand-written**, not captured from a station: the owner's pinned
version and station are not known yet (Weather-Imagery-Design §1.5). The shapes and keys come from
reading the SatDump source for *format facts only* (GPL-3.0; no code, headers or structs were copied):

- `/api`: `src-cli/legacy/webserver.cpp` (the `/api`, `/polarplot.jpeg`, `/fft.jpeg`, `/schedule.jpeg`
  handlers), `src-cli/legacy/autotrack/autotrack_web.cpp` (the autotrack wrapper `live_pipeline`,
  `object_tracker`, `frequency`, `vfos`, `recording`), `src-core/common/tracking/obj_tracker/object_tracker_info.cpp`
  (the tracker keys) and the module statistics (`snr`, `peak_snr`, `viterbi_ber`, `viterbi_lock`,
  `deframer_state`, `rs_avg`; types differ per decoder, some are arrays).
- `dataset.json`: `src-core/products/dataset.cpp` (`satellite`, `timestamp`, `products`).
- `product.cbor`: `src-core/products/product.cpp` and `image_product.cpp` (`instrument`, `type`,
  `product_timestamp`, `product_source`, `images[]` with `file`, `name`, `abs_index`, `bit_depth`,
  `transform`, and `projection_cfg`).
- xRIT layout and the geostationary `projection_cfg` keys: `plugins/xrit_support/xrit/processor/xrit_channel_processor_saveimg.cpp`
  (`IMAGES/{satellite}/[{region}/]{YYYY-MM-DD_HH-MM-SS}/product.cbor`; `type: geos`, `lon0`, `sweep_x`,
  `scalar_x/y`, `offset_x/y`, `width`, `height`, `altitude`).

| File | What it is |
|---|---|
| `2.0.0-alpha/api/live.json` | `legacy live`: module statistics at top level, a locked LRPT decoder |
| `2.0.0-alpha/api/autotrack.json` | `legacy autotrack` during a Meteor-M2-3 pass; `snr` as a string, `peak_snr`, `viterbi_ber` and `rs_avg` as arrays |
| `2.0.0-alpha/api/autotrack-idle.json` | `legacy autotrack` between passes: no pipeline, target below the horizon |
| `2.0.0-alpha/pass/2026-10-12_18-02_meteor_m2-x_lrpt_137.9 MHz/` | A Meteor LRPT pass folder: `dataset.json` (timestamp 2026-10-12T18:02:10Z), `MSU-MR/product.cbor` (two channels, a TLE), two 48 × 32 channel PNGs, one 96 × 64 composite PNG, and a 1 KB `.cadu` that must never be copied |
| `2.0.0-alpha/xrit/IMAGES/GOES-19/Full Disk/2026-10-12_18-00-20/` | A GOES-19 HRIT full-disk folder with two 8-bit channels downscaled to 64 × 64; `projection_cfg` for a 2712-column disc (4008 m per column at 35,786,023 m, `sweep_x: true`, `lon0: -75.2`) |
| `2.0.0-alpha/xrit-truncated/product.cbor` | The same `product.cbor` cut at 40 bytes, as SatDump leaves it while rewriting |
| `2.0.0-alpha/xrit-fy4/IMAGES/FY-4A/Full Disk/2026-10-12_18-00-00/` | An FY-4A folder without a projection (refused) |
| `coastline-points.csv` | Coastline points with their visibility from GOES-19, GOES-18 and Elektro-L 4, checked against a great-circle limb of 81.3° |
| `generate-synthetic.py` | Writes every file above (Pillow for the PNGs, a minimal CBOR encoder): `python3 generate-synthetic.py src/test/resources/fixtures/satdump` |

## Capture procedure (replace the synthetic set)

Once the owner names the pinned version, capture a set into `{version}/` and keep this one until no
station runs it:

1. `/api` in each mode the station uses, during a pass and between passes:
   `curl -s http://station:8081/api > api/autotrack.json` (and `api/autotrack-idle.json`, `api/live.json`).
   Never request `/fft.jpeg` (it turns the FFT on).
2. One polar pass folder: `rsync -a station:/srv/output/<pass folder>/ pass/<pass folder>/`, then
   downscale every PNG to at most 128 px on the long edge (`mogrify -resize 128x128 *.png`) and replace
   the `.cadu` with a 1 KB stub, so the fixture stays small.
3. One GOES HRIT timestamp folder with two channels: `rsync -a station:/srv/output/IMAGES/GOES-19/<region>/<ts>/ xrit/IMAGES/GOES-19/<region>/<ts>/`,
   channels downscaled to 64 × 64 (the projection's `width`/`height` stay, and Foresignal scales the
   scalars with the image).
4. Record the sizes and SHA-256 here and drop the `synthetic` note for that version.
