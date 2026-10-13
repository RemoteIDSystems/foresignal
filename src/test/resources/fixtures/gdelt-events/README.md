# gdelt-events fixtures (prompt 066)

Captured on 2026-10-13 at 14:33 UTC from the GDELT 2.0 mirror on `storage.googleapis.com` (no key needed;
`synthetic: false` for the captured files).

```sh
curl -s https://storage.googleapis.com/data.gdeltproject.org/gdeltv2/lastupdate.txt > lastupdate.txt
curl -s -O https://storage.googleapis.com/data.gdeltproject.org/gdeltv2/20261013143000.export.CSV.zip
```

| File | What it is |
|---|---|
| `lastupdate.txt` | Captured. Three lines `<size> <md5> <url>` (export, mentions, gkg); the URLs point at `http://data.gdeltproject.org/…`. |
| `20261013143000.export.CSV.zip` | Captured, unmodified: 90,443 bytes, MD5 `8768bad35cf2f5e245598013a24497d7`, one entry `20261013143000.export.CSV` of 1,361 tab-delimited lines with 61 columns. 191 lines have CAMEO root 13–20; 183 of them have an ActionGeo point (the default filter). |
| `export-40.tsv` | 40 lines of the captured export, unchanged: 28 root 13–20 lines with a point (two country-level, three ADM1-level), one root 18 line with no location (`ActionGeo_Type` 0), and 11 lines of other roots, including `02` and `03` with the leading-zero codes `026`, `020` and `0356`. |
| `lastupdate-foreign-host.txt` | Hand-made: the captured file with the export URL moved to `https://downloads.example.net/mirror/payload/…`. Only the validated file name may be used. |
| `bad-md5.txt` | Hand-made: the captured file with the export MD5 replaced by zeros (the size still matches). |
| `zip-two-entries.zip` | Hand-made (Python `zipfile`): `20261013143000.export.CSV` (the 40 lines) plus a second entry `20261013143000.extra.CSV`. The test serves it with a `lastupdate.txt` it computes. |
| `short-rows.tsv` | Hand-made: the first 20 lines of `export-40.tsv`, line 5 cut to 30 columns and line 12 given a 62nd column (2 of 20 malformed, above the 5% limit). |

The zip bomb (a highly compressible 300 MB entry) is built by `GdeltEventsAdapterTest.zipBomb` and not committed.

The export lines are GDELT data: machine-coded events with actor names, place names and the URL of the first
report. Foresignal never fetches those URLs.
