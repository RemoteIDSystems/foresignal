# 082 — Radio Sources

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/082-CR-Radio-Sources.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/081-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `pskreporter-ft8`, `kiwisdr`, `openmhz-systems`, `aprs-is` (Intelligence and specialist sources)
- `docs/Core-Data-Model.md` §2 (time contract), §3 (descriptor fields, adapter rules 7–13, limits), §5.3 ("Everything else" row), §7 (streaming sinks), §14 (observer and coverage)
- `docs/Gap-Analysis.md` §4a row "Direwolf APRS over RF" and §8.C item 41 ("Extend 046" = this prompt)
- `docs/Adapter-Guide.md` (prompt 011), and the Completion Reports of 063 (first streaming adapter) and 069 (socket transport, codec SPI, LAN validator)
- Upstream docs: https://pskreporter.info/pskdev.html, http://www.aprs-is.net/javAPRSFilter.aspx, http://www.aprs-is.net/Connecting.aspx, http://www.aprs.org/doc/APRS101.PDF, Direwolf User Guide (KISS TCP section)

## Objective

Add five amateur-radio sources: PSK Reporter FT8 spots, the public KiwiSDR receiver list, the OpenMHz scanner-system catalogue, a narrow, opt-in APRS-IS feed, and a local, provably receive-only APRS feed from Direwolf over KISS-TCP. The RF sources are the first to use the Core-Data-Model §14 observer model in earnest: the receiving station of every spot or packet is an `Observer`, never a payload field, so it stays gated behind `include=observer`.

## Tasks

### 1. Shared pieces (`ai.foresignal.source.adapters.radio`)

- **`Maidenhead`**: locator (4, 6 or 8 characters, case-insensitive) → centre point and cell envelope; reject malformed locators with `null`. Pure, unit-tested.
- **`AprsParser`** (pure, in-house; do not add a GPL or unlicensed APRS library): `AprsPacket parse(String tnc2Line)` for TNC2 text `SRC>DEST,PATH:info`. Support uncompressed positions (`! = / @`, with and without timestamp), compressed positions, Mic-E (latitude and message bits from the destination field), objects (`;`, killed objects become tombstones), items (`)`), status (`>`), messages (`:ADDRESSEE:text{id`), and position weather (`_` after the symbol). Return symbol table/code, course/speed (knots → m/s in payload `speedMps`), altitude (`/A=` feet → `altitudeM`), comment (≤ 256 chars, control characters stripped) and the q-construct/igate from the path. Unknown formats return a packet with `type=unsupported`, which adapters count and drop.
- **`AprsPacketDedup`** (singleton bean): key = SHA-256 of `source + ":" + info`, window 30 s, bounded (50k entries, LRU). `local-aprs-*` registers every packet it ingests; `aprs-is` drops a packet already seen from `local-aprs-*` within the window when `dedupLocal=true` (default), counting `foresignal.aprs.dedup{source}`. RF is first-hand, so the local copy always wins.
- **Raw TCP to fixed public hosts.** 069's `SocketTransport` (`ctx.sockets()`) is LAN-only by design. Add `SocketTransport.tcpPublic(HostPort, SocketOptions, Framing, FrameHandler)` that accepts **only** a host:port listed in the descriptor's new `allowedHosts` field (never operator input), resolves it normally, and otherwise behaves like `tcp(...)` (one budget unit per connect attempt, idle watchdog, `Framing.LINES`). 085 reuses it.
- **`SocketOptions.receiveOnly()`** (add to 069's options if absent): the transport rejects an `onConnect` writer and its handle exposes no write method, so a receive-only source has no code path that can transmit. Do not half-close the socket (`shutdownOutput`): Direwolf and aprsc treat EOF from the client as a disconnect. `receiveOnlyAfterConnect()` is the same except that the `onConnect` writer runs once first.

### 2. `pskreporter-ft8` (POLLING)

- URL: `https://retrieve.pskreporter.info/query?mode={mode}&flowStartSeconds=-{window}&rronly=1&noactive=1&rptlimit={rptlimit}&appcontact={contact}` plus `callsign={call}` per watched callsign (one request each, serial).
- Params: `mode` (string, default `FT8`, allowed FT8, FT4, WSPR, JS8, CW), `callsigns` (string-list, default empty), `windowSeconds` (integer 300–3600, default 900), `rptlimit` (integer ≤ 5000, default 2000), `appContact` (string, required: operator email, sent as `appcontact`).
- **Verified 2026-10:** an unfiltered 5-minute FT8 query returns about 1.8 MB, and `noactive=1` did not suppress `activeReceiver` elements. During fixture capture, confirm which of `rronly`, `noactive` and `rptlimit` are honoured and record it in the fixture README. Without `callsigns` the adapter must still cap records at `rptlimit`.
- Parse with a hardened StAX parser (`XMLInputFactory` with `SUPPORT_DTD=false`, `IS_SUPPORTING_EXTERNAL_ENTITIES=false`); a document containing a DOCTYPE throws `UpstreamFormatException`. Read `receptionReport` attributes `senderCallsign, senderLocator, receiverCallsign, receiverLocator, frequency, flowStartSeconds, mode, sNR, senderDXCC`. `activeReceiver` count goes to `Coverage.receivers`.
- Record: kind `radio-spot`, externalId = `sender|receiver|frequency/1000|flowStartSeconds`, `eventTime = observedAt = flowStartSeconds`, Point = sender locator centre (null if absent). Payload `sender, senderLocator, mode, frequencyHz, band, snr, dxcc, receiverLocator`. Observer: `receiverId=receiverCallsign, frequencyHz, snr`. Quality `accuracyMeters` = half the locator cell diagonal.
- Descriptor: interval 10 min, minInterval 5 min (PSK Reporter asks clients to stay at or above five minutes), budget 2/min, 300/day, STANDARD, ALL, `recordTtl` 30 min, `ttlAnchor` OBSERVED, `zeroResultsNormal` true, tileProperties `sender, mode, band, snr`, searchFields `sender`, licence "PSK Reporter terms (non-commercial, attribution)", termsUrl the pskreporter.info page you verify.

### 3. `kiwisdr` (POLLING)

- URL `http://rx.linkfanel.net/kiwisdr_com.js` (generated from `kiwisdr.com/public`). **Verified 2026-10:** HTTPS to this host times out; plain HTTP returns about 930 KB of `var kiwisdr_com = [ {...}, ... ];` with string-valued fields (`id, status, offline, name, sdr_hw, bands, users, users_max, gps, loc, antenna, snr, url, updated`). Use plain HTTP (public, non-sensitive data) and record why in the adapter Javadoc.
- **`LenientJsLiteralParser`**: strip the `var x =` prefix and trailing `;`, `//` and `/* */` comments (outside strings), and trailing commas, then parse with Jackson (`ALLOW_SINGLE_QUOTES`, `ALLOW_UNQUOTED_FIELD_NAMES`). Cap input at 8 MB.
- Record: kind `sdr-receiver`, externalId `id`, geometry from `gps` (`"(lat, lon)"`), `observedAtMode` RECEIVED. Payload: `name, url, antenna, bandsHz` (parsed range), `usersMax, sdrHw, online` (`offline=="no"`); `users` and `snr` are volatile. Tombstone receivers absent from a complete list only when the list has ≥ 50% of the previous count.
- Interval 24 h, minInterval 6 h, budget 1/min, 4/day, conditional GET.

### 4. `openmhz-systems` (POLLING)

- `https://api.openmhz.com/systems`, JSON. **Verified 2026-10:** our identifiable UA receives a Cloudflare challenge (HTTP 403, HTML). Detect an HTML body or `cf-mitigated` header, throw `UpstreamHttpException` with reason `BLOCKED_CHALLENGE`, and let health go `failing`. **Never spoof a browser UA or solve the challenge.** Document in Source-Catalog that the source stays failing until OpenMHz allows API clients.
- If reachable: kind `scanner`, externalId `shortName`, geometry from system lat/lon when present, payload `name, shortName, location, state, county, lastActive, clientCount` (`clientCount` volatile). Interval 1 h, budget 1/min, 30/day.

### 5. `aprs-is` (STREAMING, opt-in)

- `rotate.aprs2.net:14580` (descriptor `allowedHosts`) via `ctx.sockets().tcpPublic(…, Framing.LINES, …)`. The login line is the `onConnect` writer under `SocketOptions.receiveOnlyAfterConnect()`: `user {login} pass -1 vers Foresignal {version} filter {filter}\r\n`. **Passcode is always -1** (receive-only); the adapter never writes anything after login.
- Params: `filter` (string, **required**, no default, must contain only `r/`, `b/`, `p/`, `t/` or `f/` terms; reject `r/` radius > 250 km, more than 5 terms, and any bare wildcard), `login` (string, default `EL` + first 6 install-id characters uppercased; `^[A-Z0-9]{3,9}(-\d{1,2})?$`; `N0CALL`/`NOCALL` rejected; **verified 2026-10:** aprsc refuses `N0CALL` with "Login by user not allowed" and accepts an unverified non-callsign login), `includeMessages` (boolean, default false), `dedupLocal` (boolean, default true), `maxSubscriptions` fixed at 1.
- Reconnect 30 s → 15 min, exponential with ±20% jitter; idle watchdog 120 s (the server sends `#` comments about every 20 s). Disabled by default.
- Record: kind `aprs-station` (objects and items use the object name), externalId = source call with SSID, `observedAtMode` RECEIVED (APRS timestamps are optional and unreliable). Payload `symbol, comment, speedMps, courseDeg, altitudeM, type, weather` (weather keys only when present), plus `path` and `raw` (the TNC2 line, ≤ 512 chars, which 110's APRS-IS serve sink re-emits) as volatile fields. Observer: `receiverId` = igate from the q-construct. Messages become kind `aprs-message` only when `includeMessages=true`; they are never embedded.
- STANDARD, ALL, `recordTtl` 60 min, budget 6/hour connects, 100/day. The stream sink uses the Core-Data-Model §7 ALL list.

### 6. `local-aprs-{n}` (STREAMING, LAN, receive-only)

- Slot family on 069's pattern (`foresignal.local-receivers.slots`, disabled by default, `egress` LOCAL_ONLY with `egressOverridable=true`). Param `endpoint` (`LAN_URL tcp://`, e.g. `tcp://192.168.1.20:8001`; Direwolf's conventional KISS port is 8001 but none is assumed). Optional `stationCallsign` (Observer `receiverId`).
- Connect with `ctx.sockets().tcp(endpoint, SocketOptions.receiveOnly(), Framing.RAW, …)` and the codec `kiss-ax25` (069's codec SPI, registered here).
- **`KissFrameDecoder`**: FEND/FESC/TFEND/TFESC unescaping, port nibble, command 0 data frames only; others ignored. **`Ax25UiDecoder`**: address fields (shifted ASCII, SSID, H-bit), control 0x03, PID 0xF0, then the info field rendered as TNC2 text and passed to `AprsParser`. Frames that are not AX.25 UI (for example a MeshCore KISS modem, which carries raw MeshCore packets, not AX.25) are counted `foresignal.aprs.local.non_ax25` and dropped; say so in the Operator Guide.
- **Provably receive-only:** under `receiveOnly()` there is no write path; the adapter sends no KISS commands, and Foresignal never igates.
- Same record shape as `aprs-is`; registers with `AprsPacketDedup`. The operator may mark the source private (039); then 008's precision cap applies. Budget 60 connects/hour; reconnect 5 s → 5 min.

### 7. Fixtures

Capture with an identifiable UA and record URL, date and size in each `README.md`: a PSK Reporter response for one busy callsign (trim to 50 reports, keep `activeReceiver` elements), `kiwisdr_com.js` trimmed to 30 receivers (keep its header comments), the OpenMHz challenge page as served, 200 lines of APRS-IS output for a narrow `r/` filter (strip nothing: it is public, but trim personal messages), and a KISS byte capture from Direwolf (hex file). Hand-make: an XML document with a DOCTYPE and external entity, a JS literal with comments and trailing commas, and one APRS line per supported format.

### 8. Spec, Postman, docs

- No new operations. If `docs/OpenAPI.json` enumerates kinds anywhere, add `radio-spot, sdr-receiver, scanner, aprs-station, aprs-message`.
- `docs/Source-Catalog.md`: update the four rows and add `local-aprs-*` (verified findings above, the observer mapping, OpenMHz blocked status). `docs/Operator-Guide.md`: "Amateur radio sources" (APRS-IS filter etiquette, receive-only guarantees, Direwolf `KISSPORT`, MeshCore KISS is not AX.25). `docs/Adapter-Guide.md`: the observer rule for RF sources.

## Tests to write (do not run)

- `MaidenheadTest`, `AprsParserTest` (one fixture line per format including Mic-E, compressed, killed object, message, weather, malformed), `KissFrameDecoderTest` (escapes, split frames, non-zero command ignored), `Ax25UiDecoderTest` (non-UI and non-AX.25 frames dropped).
- `PskReporterParserTest` (real fixture; DOCTYPE/XXE payload throws; observer populated; payload has no receiver callsign), `LenientJsLiteralParserTest` (comments, trailing commas, real fixture), `OpenMhzAdapterTest` (WireMock challenge page → `BLOCKED_CHALLENGE`, no retry with another UA).
- `PskReporterAdapterTest`, `KiwiSdrAdapterTest` (WireMock: URL, `appcontact`, conditional GET, 304).
- `AprsIsAdapterTest` (embedded TCP server: exact login line with `pass -1`, nothing written after login, filter validation errors, reconnect jitter bounds, dedup against a local packet), `LocalAprsAdapterTest` (embedded KISS server: zero bytes received from Foresignal, the handle has no write method, `onConnect` writer rejected), `TcpPublicTest` (host not in `allowedHosts` refused).
- `RadioIngestIT` (`@IntegrationTest`): PSK spots ingest with observers only under `include=observer` for an admin token; APRS station re-beacon with the same content only touches.

## Acceptance criteria

- Five sources registered with valid descriptors; `aprs-is` and `local-aprs-*` disabled by default; `aprs-is` cannot be enabled without a valid narrow filter.
- No code path writes to the APRS-IS socket after login or to the KISS socket at all.
- Receiver identities appear only as observers.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `082: Radio Sources`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/082-CR-Radio-Sources.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `082: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
