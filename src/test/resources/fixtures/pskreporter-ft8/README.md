# `pskreporter-ft8` fixtures

| File | What it is |
|---|---|
| `callsign-N6AD.xml` | Real. `GET https://retrieve.pskreporter.info/query?mode=FT8&flowStartSeconds=-900&rronly=1&noactive=1&rptlimit=2000&callsign=N6AD`, captured 2026-10-14T00:31:13Z: 243,754 bytes, 581 `receptionReport` elements, no `activeReceiver`. Trimmed to the first 50 reports (header elements kept) |
| `active-receivers.xml` | Real. `GET https://retrieve.pskreporter.info/query?mode=FT8&flowStartSeconds=-300&rptlimit=50&callsign=N6AD` (no `rronly`, no `noactive`), captured 2026-10-14T00:41:58Z: 1,260,677 bytes, 49 reports, 5,930 `activeReceiver` and 20 `activeCallsign` elements and one `senderSearch`. Trimmed to the first 25 `activeReceiver` and 5 `activeCallsign` elements; every report kept |
| `edge.xml` | Hand-made. Two `activeReceiver` elements; a report repeating an id (same kHz and time slot), a report without a locator, one with an invalid locator, one without `flowStartSeconds` and one with a non-numeric frequency |
| `doctype-xxe.xml` | Hand-made. A DOCTYPE declaring an external entity (`file:///etc/passwd`) used in an attribute: must be refused, never resolved |

**Capture commands** (prompt 082; no `appcontact` was sent for the captures, the operator's address being the
adapter's to send):

```sh
UA="Foresignal-fixture-capture (+https://foresignal.ai)"
curl -s -A "$UA" "https://retrieve.pskreporter.info/query?mode=FT8&flowStartSeconds=-300&rronly=1&noactive=1&rptlimit=50"
curl -s -A "$UA" "https://retrieve.pskreporter.info/query?mode=FT8&flowStartSeconds=-900&rronly=1&noactive=1&rptlimit=2000&callsign=N6AD"
curl -s -A "$UA" "https://retrieve.pskreporter.info/query?mode=FT8&flowStartSeconds=-900&rronly=1&rptlimit=2000&callsign=N6AD"
curl -s -A "$UA" "https://retrieve.pskreporter.info/query?mode=FT8&flowStartSeconds=-300&rptlimit=50&callsign=N6AD"
```

The four queries were at least five minutes apart (00:25:45, 00:31:13, 00:36:24 and 00:41:58 UTC), as PSK Reporter
asks. Responses: HTTP 200, `content-type: text/xml; charset=utf-8`, `cache-control: public,max-age=90`, served by
Cloudflare, no `ETag` or `Last-Modified`.

**Which parameters are honored** (2026-10-14):
- `rptlimit`: approximately. `rptlimit=50` returned 57 reports unfiltered and 49 for a call sign; the adapter caps
  locally as well.
- `rronly=1`: honored, and it **suppresses every `activeReceiver` element** (none in the first three captures, with or
  without `noactive=1`); without it the fourth query returned 5,930 of them.
- `noactive=1`: not distinguishable here, because `rronly=1` already removes the active receivers. It was not tested
  without `rronly` (the prompt's 2026-10 note says it did not suppress them).
- `callsign`: matches reports where the call is the sender or the receiver (the N6AD captures are mostly N6AD's own
  receptions).
