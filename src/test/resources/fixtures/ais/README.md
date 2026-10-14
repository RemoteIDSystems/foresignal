# AIS fixtures (prompt 085)

| File | Source | Real or synthetic | Contents |
|---|---|---|---|
| `digitraffic-mqtt.jsonl.gz` | `ais-digitraffic` | **real**, captured 2026-10-13 (UTC 2026-10-14 03:01–03:12) | 10 minutes of `wss://meri.digitraffic.fi:443/mqtt`, subscriptions `vessels-v2/+/location`, `vessels-v2/+/metadata`, `vessels-v2/status`, one JSON object per line: `{"receivedAt": <ms>, "topic": "…", "payload": {…}}` (the payload compacted from Digitraffic's pretty-printed JSON). 21,551 messages: 20,269 locations, 1,162 metadata, 120 status, 599 MMSIs, no fix older than 24 h. Gzipped (5.1 MB plain). |
| `digitraffic-vessels.json` | `ais-digitraffic` | **real**, fetched 2026-10-13 | `GET https://meri.digitraffic.fi/api/ais/v1/vessels` (908 vessels) trimmed to the first 100. The API answered 406 without `Accept-Encoding: gzip`. |
| `kystverket-stream.txt` | `ais-kystverket` | `synthetic: true` | 290 NMEA lines with TAG blocks (`\s:<station>,c:<unix s or ms>*hh\`) over 10 minutes: four Class A vessels (type 1 every 10 s, two-part type 5 with TAG `g` groups), one Class B vessel (type 18 every 30 s, type 24 A/B), base station type 4 (`!BSVDM`). Every line carries `c`; one line carries `c` in milliseconds. |
| `aiscatcher-stream.txt` | `local-ais-{n}` | `synthetic: true` | The same 290 sentences without TAG blocks, as AIS-catcher's `-S` TCP server sends them. |
| `golden-sentences.tsv` | codec tests | hand-made | One message per line, its sentences tab-separated: types 1, 2, 3, 5 (two parts), 18, 19, 24A, 24B, 27, 4 and 21; sentinel variants of 1, 5 and 27; the two public samples from the gpsd AIVDM/AIVDO protocol notes; a two-part type 5 out of order; a bad checksum. |

**Kystverket was unreachable at capture time.** On 2026-10-13 a TCP connect from the build host to `153.44.253.27:5631`
timed out after 25 s (as the prompt's 2026-10 check from a US host did), although Kystverket's access page
(`https://www.kystverket.no/en/sea-transport-and-ports/ais/access-to-ais-data/`) still lists it as open. The
Kystverket and AIS-catcher streams are therefore generated sentences in Kystverket's documented line format (TAG block
with `s` and `c`, then `!AIVDM`/`!BSVDM`), not captures. No AIS-catcher receiver was available either.

**How the hand-made and synthetic sentences were produced.** They were generated in a scratch environment outside the
repository with pyais 3.3.1 (MIT), from the field values in the tests, and decoded back with it as an independent
oracle: the expected values in `AisMessageDecoderTest` are what pyais reads from the same sentences. No pyais code is in
this repository. pyais takes a turn rate in degrees per minute and writes ROT<sub>AIS</sub>, so the type 1 golden
written with −10 °/min carries the raw value −15, which is what Foresignal reports.

**Recapture (prompt 143).**
- Kystverket, from a host that can reach it: `timeout 600 nc 153.44.253.27 5631 > kystverket-stream.txt`, then check
  that every line starts with a TAG block carrying `c` (if not, the descriptor must switch to RECEIVED for the whole
  source) and update `KystverketAdapterTest.FIXTURE_RECORDS`.
- AIS-catcher: start it with `-S 5012` and run `timeout 600 nc <host> 5012 > aiscatcher-stream.txt`.
- Digitraffic: 10 minutes of the three subscriptions with client id `Foresignal; <installId>-<random6>` and the
  `Digitraffic-User` header, written as above, then `gzip -9`; update the counts in `DigitrafficAdapterTest`.

No instructions directed at an agent were found in any captured data.
