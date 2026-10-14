# 082-CR-Radio-Sources

| | |
|---|---|
| Prompt | 082-Radio-Sources.md |
| Status | Complete |
| Implementation commit | d1c1a323eb152d4b267edd84d13150fcadcd1a8b |
| Date | 2026-10-13 |

## Summary

Five amateur-radio sources, all **disabled by default**, with the receiving station of every spot or packet stored as
an `Observer` and never in the payload. All six descriptors (`local-aprs-1` and `-2` included) pass
`DescriptorValidator` and `IngestPolicyValidator`, checked with a scratch `main` (see "Compile result").

**1. Shared pieces:**
- **`Maidenhead`** (`adapters.radio`): 4-, 6- and 8-character locators, case-insensitive, to the cell's center,
  envelope and half diagonal; malformed → `null`. `decodeLenient` reads PSK Reporter's 10-character locators
  (`CM88qg60fk`) to their 8-character prefix.
- **`AprsParser`** (`formats.aprs`, in-house, no library): TNC2 `SRC>DEST,PATH:info`.
  - Uncompressed positions (`! = / @`, with and without timestamp, ambiguity spaces read as zero), compressed
    positions (base-91, course/speed or altitude from `cs`, overlay tables).
  - Mic-E: latitude, message bits, N/S, W/E and the +100 offset from the destination; longitude, speed, course,
    symbol and the base-91 altitude from the info field.
  - Objects (killed → tombstone), items (killed → tombstone too), status, messages with ids and acks, position
    weather (`_`: wind, gust, temperature, rain, humidity, pressure in SI).
  - `speedMps` (knots), `altitudeM` (`/A=` feet), comment ≤ 256 characters with control characters stripped, the
    q-construct and igate from the path. Anything else is `type=unsupported` with a reason.
- **`AprsRecords`** (`formats.aprs`): the shared record shape, per connection. Position and status are merged per
  station (rule 7). Bounded to 20,000 stations.
- **`AprsPacketDedup`** (`formats.aprs`, singleton `@Component`): SHA-256 of `source:info`, 30 s window, 50,000 keys,
  LRU. `local-aprs-*` registers every packet; `aprs-is` drops a packet seen locally when `dedupLocal`, counting
  `foresignal.aprs.dedup{source}`.
- **`SocketTransport.tcpPublic(HostPort, SocketOptions, Framing, FrameHandler)`:**
  - accepts only a `host:port` in the new descriptor field `allowedHosts` (internal, `Builder.allowedHost`);
  - resolves through `OutboundUrlPolicy` `PUBLIC_ONLY` (or the system resolver with every address checked public);
  - refuses anything else **before** a budget unit is spent; otherwise exactly `tcp(...)`.
- **`SocketOptions.receiveOnly()` / `receiveOnlyAfterConnect()`** (new `WriteMode`):
  - `NONE` rejects an `onConnect` writer (at construction and in `withOnConnect`), and the transport never obtains
    the socket's output stream;
  - `ON_CONNECT_ONLY` writes the bytes once;
  - handles have no write method (as before); no half-close.
- **Codec `kiss-ax25`** (`source.codec.KissAx25Codec`): `formats.kiss.KissFrameDecoder` (FEND/FESC unescaping,
  reassembly across reads, port nibble, data frames only, 2 KB cap) and `formats.kiss.Ax25UiDecoder` (addresses,
  SSIDs, H bit, control 0x03, PID 0xF0 → TNC2), then `AprsParser` and `AprsRecords`. A `PacketListener` reports each
  packet and each dropped frame.

**2. `pskreporter-ft8`** (`PskReporterAdapter`, `PskReporterParser`): as prompted.
- One unfiltered request, or one per watched call sign, serial. `appcontact` is URL-encoded.
- Hardened StAX through the existing `CapParser.secureInputFactory()`; a DTD or entity reference throws.
- Records are capped at `rptlimit` per request.
- Observer `(receiverCallsign, frequencyHz, snr)`; `accuracyMeters` = half the cell diagonal.
- Coverage: `receivers` = `activeReceiver` count, `actual` = records.
- STANDARD, ALL (keyframe 19 min), TTL 30 min OBSERVED, retention 90 d (`bulk-event`).

**3. `kiwisdr`** (`KiwiSdrAdapter`, `KiwiSdrParser`, `LenientJsLiteralParser`):
- plain HTTP, explained in the Javadoc;
- conditional GET (the host sends `ETag` and `Last-Modified`);
- the lenient parser removes comments and trailing commas outside strings and the `var` assignment, then Jackson
  with single quotes and unquoted names; 8 MB cap;
- `LiveIdSet` tombstones only when the list holds at least half the previous count;
- RECEIVED, `users` and `snr` volatile.

**4. `openmhz-systems`** (`OpenMhzAdapter`, `OpenMhzParser`):
- **Detection:** a `cf-mitigated: challenge` response (detected in `BoundSourceHttp` for every source), or an HTML
  body (403 or 200), is `UpstreamHttpException` with the new reason `BLOCKED_CHALLENGE`.
- **One request per run:** never another User-Agent, never a solved challenge.
- **If reachable:** JSON maps to `scanner` records.

**5. `aprs-is`** (`AprsIsAdapter`, `AprsIsFilter`):
- **Connection:** `tcpPublic` to `rotate.aprs2.net:14580`, `Framing.LINES`, idle watchdog 120 s.
- **Login:** `user {login} pass -1 vers Foresignal {version} filter {filter}\r\n` as the single `onConnect` write
  under `receiveOnlyAfterConnect()`.
- **Filter:** required; only `r/` (≤ 250 km), `b/`, `p/`, `t/types/call/km`, `f/call/km` (≤ 250 km); ≤ 5 terms; no
  bare wildcard.
- **Login rules:** `^[A-Z0-9]{3,9}(-\d{1,2})?$`, `N0CALL`/`NOCALL` refused, default `EL` + 6 install-id characters.
- **Records:** igate observer; messages only with `includeMessages`; unsupported formats counted
  (`foresignal.aprs.unsupported`).
- **Reconnect:** 30 s → 15 min, ±20% jitter, through the new descriptor field `reconnect` (`ReconnectPolicy`), which
  `StreamingSupervisor.nextDelay` now honors.
- **Descriptor:** STANDARD, ALL (keyframe 55 min), TTL 60 min, retention 90 d.

**6. `local-aprs-{n}`** (`LocalAprsFamily`, `LocalAprsAdapter`):
- slots on `foresignal.local-receivers.slots`; `endpoint` `tcp://` LAN URL; optional `stationCallsign` (observer)
  and `includeMessages`;
- `ctx.sockets().tcp(endpoint, SocketOptions.receiveOnly(), Framing.RAW, …)`: zero bytes are written;
- non-AX.25-UI frames counted in `foresignal.aprs.local.non_ax25`; every packet registered with the dedup filter;
- `LOCAL_ONLY`, overridable; reconnect 5 s → 5 min.

**Enabling:** `SourceService.update` now re-validates the stored parameters when a request enables a source without
supplying parameters (after the credentials check), so `aprs-is` cannot be enabled without a valid narrow filter
(`400 VALIDATION_FAILED`, `params.filter`).

**Docs:**
- `docs/Source-Catalog.md`: the new section "Amateur radio (prompt 082)" with all five rows (verified findings,
  observer mapping, OpenMHz blocked); the four idea rows point to it.
- `docs/Operator-Guide.md`: "Amateur radio sources" (PSK Reporter contact, APRS-IS filter etiquette, receive-only
  guarantees, Direwolf `KISSPORT`, MeshCore KISS is not AX.25).
- `docs/Adapter-Guide.md` §20: `tcpPublic`, the write rules, the per-source reconnect ladder, the `kiss-ax25` row and
  the observer rule for RF sources.
- `docs/OpenAPI.json`: kind notes and payload fields of `radio-spot`, `sdr-receiver`, `scanner`, `aprs-station`
  (adds `status, micEMessage, sender`) and `aprs-message`, plus a changelog entry. No operation changes.
- `docs/Invariants.md`: SRC-1 and EGR-2 test entries.

## Files created / modified

- **Created, main:**
  - `source/spi/{HostPort, ReconnectPolicy}`;
  - `source/formats/kiss/{package-info, KissFrameDecoder, Ax25UiDecoder}`;
  - `source/formats/aprs/{package-info, AprsPacket, AprsParser, AprsRecords, AprsPacketDedup}`;
  - `source/codec/KissAx25Codec`;
  - `source/adapters/radio/{Maidenhead, RadioSources, PskReporterAdapter, PskReporterParser, LenientJsLiteralParser,
    KiwiSdrAdapter, KiwiSdrParser, OpenMhzAdapter, OpenMhzParser, AprsIsAdapter, AprsIsFilter, AprsSourceFields,
    LocalAprsAdapter, LocalAprsFamily}`.
- **Modified, main:**
  - `source/spi/{SourceDescriptor (allowedHosts, reconnect), SocketOptions (WriteMode), SocketTransport (tcpPublic)}`;
  - `collect/socket/{DefaultSocketTransport, package-info}`;
  - `collect/{StreamingSupervisor, UpstreamHttpException (reason), BoundSourceHttp (cf-mitigated)}`;
  - `source/SourceService` (enable re-validation);
  - `source/adapters/radio/package-info`.
- **Created, test:**
  - `formats/kiss/{KissFrameDecoderTest, Ax25UiDecoderTest}`;
  - `formats/aprs/{AprsParserTest, AprsRecordsTest, AprsPacketDedupTest}`;
  - `codec/KissAx25CodecTest`;
  - `spi/ReconnectPolicyTest`;
  - `collect/socket/TcpPublicTest`;
  - `adapters/radio/{MaidenheadTest, PskReporterParserTest, LenientJsLiteralParserTest, PskReporterAdapterTest,
    KiwiSdrAdapterTest, OpenMhzAdapterTest, AprsIsAdapterTest, LocalAprsAdapterTest, RadioIngestIT,
    RadioTestAdapters, LoopbackPublicTransport}`;
  - `testsupport/stream/SocketStreamContext`.
- **Modified, test:**
  - `retention/FamilyDefaultsTest` (the five adapter classes: PSK and APRS `bulk-event`, KiwiSDR and OpenMHz
    `reference`);
  - `collect/StreamingSupervisorIT` (the descriptor ladder).
- **Created, fixtures** (each with a README):
  - `pskreporter-ft8/{callsign-N6AD.xml, active-receivers.xml}` (real) and `{edge.xml, doctype-xxe.xml}` (hand-made);
  - `kiwisdr/kiwisdr_com.js` (real) and `edge.js` (hand-made);
  - `openmhz-systems/challenge-403.html` (real);
  - `aprs-is/stream-r-sf-bay.txt` (real) and `formats.tsv` (mixed);
  - `local-aprs/direwolf-kiss.hex` (synthetic).
- **Docs:** `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Adapter-Guide.md`, `docs/OpenAPI.json`,
  `docs/Invariants.md`.

## Deviations from the prompt

1. **Package placement follows Conventions ("Shared parsers").** The KISS and AX.25 decoders are in
   `source.formats.kiss`. `AprsParser`, `AprsPacketDedup` and the shared record mapping are in `source.formats.aprs`,
   because both the `kiss-ax25` codec (`source.codec`) and two adapters use them. `Maidenhead` and the five adapters
   are in `adapters.radio`, as prompted, next to 075's `radio-browser`.
2. **Two new internal descriptor fields:**
   - `allowedHosts`, which the prompt asks for;
   - `reconnect`. The supervisor's ladder was global; the prompt's per-source 30 s → 15 min with jitter and
     5 s → 5 min needed it. Neither is in the OpenAPI `SourceDescriptor`.
3. **Budgets in per-minute and per-day units.** `Budget` has no hourly bucket:
   - "6/hour connects, 100/day" became 1/min and 100/day;
   - "60 connects/hour" became 1/min and 1,440/day.
4. **Enabling re-validates stored parameters** for every source (not just `aprs-is`). This applies only when a
   request enables a source without parameters, and comes after the credentials check. It is the general way to make
   "cannot be enabled without a valid filter" true; earlier tests that enable sources with required parameters set
   are unaffected.
5. **`BLOCKED_CHALLENGE` is a new reason on `UpstreamHttpException`** (a field, not an `ErrorCodes` API code). The
   `cf-mitigated: challenge` header is detected for every source in `BoundSourceHttp`. An HTML body is treated as a
   challenge only by `openmhz-systems`, because a plain HTML error page is common elsewhere.
6. **`rronly=1` suppresses `activeReceiver` elements** (verified 2026-10-14), so with the prompted URL `Coverage.receivers` is
   normally unset. The parser still counts the elements when present. The fixture with active receivers comes from a
   query without `rronly`.
7. **Payload additions:**
   - `aprs-station` gains `status` (the merged status report, rule 7), `micEMessage` (the Mic-E message bits the
     prompt asks to decode) and `sender` (who placed an object or item);
   - `aprs-message` payload is `from, addressee, text, msgId, path, raw`.

   The OpenAPI kind entries were amended, with a changelog entry.
8. **`local-aprs-{n}` also has `includeMessages`** (default false), so "same record shape as `aprs-is`" covers RF
   messages. Killed **items** are tombstoned like killed objects.
9. **Records keep `observedAt` null** (`observedAtMode` RECEIVED for `kiwisdr`, `openmhz-systems`, `aprs-is` and
   `local-aprs-*`); only tombstones carry the receive time.
10. **f/ ranges are capped at 250 km and `t/` needs a call and range.** The prompt caps only `r/`; an uncapped
    `f/call/5000` or a bare `t/p` would be worldwide, contrary to "narrow".
11. **The KISS fixture is synthetic.** No TNC was reachable during the build. It was generated with the same encoding
    as the tests' encoders, and includes the real Mic-E packet from the APRS-IS capture.
12. **The `local-aprs` slot constants are repeated** (`foresignal.local-receivers.slots`, 2, 16) instead of
    importing `adapters.local.LocalReceivers`, so the adapter packages do not import each other. 078's
    `AirqLocalFamily` imports it.

## Issues found / open questions

1. **OpenMHz** is registered but cannot collect anything until it admits API clients (2026-10-14: HTTP 403,
   `cf-mitigated: challenge`). Its JSON shape is unverified.
2. **PSK Reporter `rptlimit`** is honored only approximately (57 reports for 50), so the local cap matters.
   `noactive=1` was not tested without `rronly` (four queries, five minutes apart, were the etiquette budget).
3. **The APRS-IS login prefix `EL`** is as prompted. It looks like a remnant of the old product name (Elaro); `FS`
   might be intended.
4. **US English:** `check_us_english.py` on every new and changed file flags only the existing `licence` field
   (crosses the wire). It also flags two pre-existing words this prompt did not write: `initialiser` in
   `SourceDescriptor`'s Javadoc and `standardised` in the EGR-2 row of `docs/Invariants.md`. They were left
   unchanged.
5. **Instructions in data:** none. The OpenMHz challenge page's "Enable JavaScript and cookies to continue" is
   addressed to browsers and was not acted on.
6. **`docs/Source-Catalog.html`** was not regenerated (as in 070–081).

**Owner questions (non-blocking):**
1. Should the default APRS-IS login prefix be `EL` (as prompted) or `FS` (Issue 3)?
2. Accept re-validating stored parameters on every enable (Deviation 4)?
3. Accept the 250 km cap on `f/` and the required call and range on `t/` (Deviation 10)?
4. Accept the hourly budgets mapped to per-minute and per-day ones (Deviation 3)?

## Tests written (not run)

- **`KissFrameDecoderTest`:** escapes; a frame split byte by byte; non-zero commands ignored and counted; junk before
  the first FEND; a bad escape; oversize; `encode`.
- **`Ax25UiDecoderTest`:**
  - a UI frame to TNC2 with SSIDs and the last repeated digipeater marked, and the hand-checked bytes;
  - SABM and a wrong PID are `NotUi`;
  - a MeshCore payload, no extension bit, short and null frames are `NotAx25`.
- **`AprsParserTest`:** one fixture line per format.
  - uncompressed, timestamped, ambiguous;
  - compressed with course/speed, with altitude and with an overlay;
  - Mic-E with and without altitude, and the message table;
  - weather and its value rules; objects and a killed object; items and a killed item;
  - status, message and acknowledgement;
  - telemetry, malformed and header-less lines;
  - comment cleaning and the 256 cap;
  - the real capture: 181 packets, 6 unsupported.
- **`AprsRecordsTest`:**
  - the position snapshot and observer;
  - status merge both ways, and a status-only station;
  - objects with sender, and a killed object as a tombstone;
  - messages only when included, never acknowledgements, with a hash id without `msgId`;
  - the raw cap; a re-beacon differing only in volatile fields.
- **`AprsPacketDedupTest`:** the 30 s window, the key and the 50,000 bound.
- **`KissAx25CodecTest`:**
  - the fixture fed in 7-byte reads: 5 records, 2 non-AX.25 frames, observers;
  - `stationCallsign` and `includeMessages`;
  - an unsupported payload reported.
- **`ReconnectPolicyTest`:** the base ladder, the ±20% jitter at the extremes, bounds, and `HostPort` parsing and
  `allowsHost`.
- **`TcpPublicTest`:**
  - a host not in `allowedHosts`, or an allowed name resolving to loopback, refused before any unit;
  - `isPublic`;
  - receive-only sends zero bytes, then EOF; receive-only after connect sends exactly the login;
  - a writer is rejected; no handle has a write method.
- **`MaidenheadTest`, `PskReporterParserTest`:**
  - the real capture: 50 spots, observer populated, no receiver call sign in any payload value, `FieldSpecContract`;
  - active receivers; the cap; edge cases;
  - DOCTYPE/XXE throws; format errors; bands; a stable hash.
- **`LenientJsLiteralParserTest`:**
  - comments inside and outside strings, trailing commas, assignments, errors and the cap;
  - the real 30-receiver fixture and the hand-made list;
  - a stable hash when only `users` and `snr` change.
- **`PskReporterAdapterTest`** (WireMock):
  - the URL with `appcontact`, `rronly`, `noactive`, `rptlimit` and the User-Agent;
  - one request per call sign in order; coverage; errors; `checkParams`; the descriptor.
- **`KiwiSdrAdapterTest`** (WireMock):
  - the path; the `ETag` round trip with a 304; the User-Agent;
  - a tombstone after a plausible list, none after a shrunk one;
  - a format error forgets the validators; the descriptor.
- **`OpenMhzAdapterTest`** (WireMock):
  - the real challenge page → `BLOCKED_CHALLENGE`, one request, the Foresignal User-Agent;
  - HTML without the header and an HTML 200; a JSON 404 without a reason;
  - a catalog maps; the descriptor.
- **`AprsIsAdapterTest`** (embedded TCP server):
  - the exact login line with `pass -1`, then nothing written;
  - the 200-line capture → 175 records with igate observers and 6 unsupported counted;
  - public TCP to the allowed host with `ON_CONNECT_ONLY` and a 120 s watchdog;
  - filter and login validation; no connection without a filter;
  - reconnect jitter bounds; dedup against a local packet; comments and messages.
- **`LocalAprsAdapterTest`** (embedded KISS server):
  - zero bytes from Foresignal, then EOF on close; the options are receive-only;
  - 5 records with the station observer; 2 non-AX.25 frames counted; dedup registration; one unit;
  - no write method, and the writer rejected;
  - the descriptor and family; `checkParams`.
- **`RadioIngestIT`** (`WeatherIngestSupport`):
  - six sources registered, the streaming ones disabled and `local-aprs-*` `LOCAL_ONLY`;
  - `aprs-is` refuses enabling without a filter, with a 900 km range and with `b/*`;
  - PSK spots ingest, and the observer is returned only with `include=observer` to an admin token;
  - an APRS re-beacon through another igate only touches.
- **Updated:** `FamilyDefaultsTest`, `StreamingSupervisorIT` (descriptor ladder; credentials and budget waits kept).

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (the existing deprecation and unchecked notes).
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 832 requests, covers all ops: True. The
  collection is unchanged.
- No project tests were run (feature prompt).
- **Scratch check:** throwaway `main` classes in the scratchpad, outside the repository, run with Gradle's JDK 21:
  - `DescriptorValidator.problems` and `IngestPolicyValidator.problems` are empty for all six descriptors (after
    adding the keyframes and replacing an icon missing from the sprite);
  - every fixture was parsed to confirm the counts and values used in the assertions: PSK 50/49/3, KiwiSDR 30/2,
    the APRS formats, 175 records from the capture, KISS 5 records and 2 non-AX.25 frames, the login line, the
    filter messages and the query URL.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK.

## Notes for the next prompt

1. **083 onward:**
   - `SocketTransport.tcpPublic` plus the descriptor's `allowedHosts` is the way to fixed public TCP hosts; 085 should
     reuse it.
   - `SocketOptions.receiveOnly()` is the provable no-write mode.
   - A streaming descriptor may set `reconnect(ReconnectPolicy)`.
   - `formats.aprs` and `formats.kiss` are shared format code.
   - `UpstreamHttpException.REASON_BLOCKED_CHALLENGE` marks bot-mitigation pages.
   - Enabling a source now re-validates its stored parameters.
2. **097 checkpoint:**
   - first run of every 082 test;
   - `RadioIngestIT` uses `WeatherIngestSupport` (no new property: the radio adapters share 075's
     `foresignal.test.adapters.radio.base-url`);
   - the socket tests bind loopback ports;
   - check that no existing IT enables a source whose required parameters are unset (Deviation 4);
   - `FamilyDefaultsTest` now lists the five adapter classes.
3. **110 (serve sinks):** `aprs-station` and `aprs-message` keep the TNC2 line in the volatile `raw` (≤ 512
   characters) for the APRS-IS serve sink. `local-aprs-*` is `LOCAL_ONLY`.
4. **109:** license strings:
   - "PSK Reporter terms (non-commercial, attribution)";
   - "unspecified (public receiver list)" (KiwiSDR);
   - "unspecified (OpenMHz)";
   - "APRS-IS network data (amateur radio, public; no license stated)";
   - "Operator's own data (no upstream license)" (`local-aprs-*`).
5. **143:**
   - recapture per the fixture READMEs, including a real Direwolf KISS capture (`nc -d`, never half-close);
   - retry OpenMHz;
   - re-check `noactive` without `rronly` and the `rptlimit` behavior.
6. **144 (Flyway):** no schema change. Adapter state:
   - `kiwisdr`: `etag:kiwisdr`, `lastmod:kiwisdr`, `live:kiwisdr`, `gone:kiwisdr`;
   - the other 082 sources keep no state.
7. **Owner decisions pending:** the four questions above, plus those carried from 012–081.
