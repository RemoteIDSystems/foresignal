# `celestrak-gp` fixtures

| File | What it is |
|---|---|
| `stations.csv` | `GET https://celestrak.org/NORAD/elements/gp.php?GROUP=stations&FORMAT=csv`, captured 2026-10-13 (200, `text/plain; charset=UTF-8`, 3,426 bytes, 21 objects). Includes six-digit catalog numbers (100057, 100712, 100882) and a CelesTrak-style `EPOCH` with no zone |
| `stations.json` | **Derived, not captured:** the same 21 element sets converted to CelesTrak's `FORMAT=JSON` shape (an array of OMM objects, numbers as JSON numbers). Built locally from `stations.csv` so the CSV and JSON parsers can be compared without requesting the same group twice inside one CelesTrak update (which earns a 403) |
| `iss-tle.txt` | `GET https://celestrak.org/NORAD/elements/gp.php?CATNR=25544&FORMAT=tle`, captured 2026-10-13 right after `stations.csv` (same element set: epoch 26286.11204827). CRLF line endings, name padded to 24 characters. The reference for TLE column positions and checksums |

**Capture command:**

```sh
curl -sA "Foresignal/0.1 (+https://foresignal.ai; one-off fixture capture)" \
  "https://celestrak.org/NORAD/elements/gp.php?GROUP=stations&FORMAT=csv"
```

Usage policy of 2026-05-22: do not repeat a capture of the same query inside one 2-hour update.
