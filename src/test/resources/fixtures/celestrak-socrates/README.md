# `celestrak-socrates` fixtures

| File | What it is |
|---|---|
| `sort-minRange-head.csv` | The header and the first 30 rows of `GET https://celestrak.org/SOCRATES/sort-minRange.csv`, captured 2026-10-13 with a `Range: bytes=0-8191` request (206; the full file was 23,424,045 bytes, `content-type: application/octet-stream`, `ETag: "596be990a05add1:0"`, `Last-Modified: Mon, 12 Oct 2026 23:22:36 GMT`). CRLF line endings; `TCA` is UTC `yyyy-MM-dd HH:mm:ss.SSS` |

The 12 MB file of `SocratesParserTest` is synthetic, generated in the test from these rows.
