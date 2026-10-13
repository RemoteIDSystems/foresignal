# 066-CR-GDELT-Events-Adapter

| | |
|---|---|
| Prompt | 066-GDELT-Events-Adapter.md |
| Status | Complete |
| Implementation commit | cdbf11bafa2fb9f571deb5f6ab72a987ff04673b |
| Date | 2026-10-13 |

## Summary

Foresignal now has `gdelt-events`, which ingests GDELT 2.0 events as `conflict-event` point records. Each run reads
the newest 15-minute export file, keeps CAMEO root codes 13–20 by default, and stores a deterministic text summary
for semantic search. It never fetches the linked articles.

**What was verified live** (2026-10-13, no key needed):
- `https://storage.googleapis.com/data.gdeltproject.org/gdeltv2/lastupdate.txt` has three lines of
  `<size> <md5> <url>` (export, mentions, gkg), and the URLs point at `http://data.gdeltproject.org/…` even on the
  mirror. It answers with an `ETag` and `Last-Modified`.
- The export `20261013143000.export.CSV.zip` was 90,443 bytes with the declared MD5. It holds one entry of 1,361
  tab-delimited lines, all with 61 columns. The column indices of the prompt match the live file and the Codebook
  V2.0 PDF.
- `Day` can lie far before `DATEADDED`: one captured row has Day `20251013` in a file of 2026-10-13.
- The CAMEO root labels come from the CAMEO Manual 1.1b3 PDF headings `01:` to `20:`.
- The terms page (`https://www.gdeltproject.org/about.html`, which redirects to `gdeltproject.org`) allows
  "unlimited and unrestricted use", and any use or redistribution "must include a citation to the GDELT Project and
  a link to this website".

**1. Descriptor** (`ai.foresignal.source.adapters.gdelt.GdeltEventsAdapter`).
- **Identity:** id `gdelt-events`, kinds `{conflict-event}`, no credential.
- **Params:**
  - `rootCodes`: a string list restricted to `01`–`20`, default `13`–`20`; `checkParams` refuses an empty list;
  - `minMentions`: an integer ≥ 1, default 1;
  - `requireGeo`: a boolean, default true;
  - `countries`: an optional string list of 2-letter FIPS 10-4 codes.
- **Schedule and budget:** interval 15 min (also the minimum); budget 4/min, 400/day; `maxResponseBytes` 64 MB, so a
  declared 50 MB file fits.
- **Policy and time:**
  - STANDARD, ALL with no keyframe (no heartbeats), `recordTtl` 24 h, `ttlAnchor` RECEIVED;
  - `observedAtMode` UPSTREAM.
  - Retention is 90 d (`defaultRetention` and `kindRetention conflict-event`), from Conventions `bulk-event`, which
    names "GDELT events". The prompt names no retention.
- **Text:** `embedFields [summary]`, `titleField summary`, `searchFields summary, actionGeoName, actor1Name,
  actor2Name`, `rankField numArticles`.
- **Tiles:** `eventRootCode, quadClass, goldstein, numArticles, geoPrecision`.
- **License:** "GDELT: unrestricted use and redistribution with citation and a link", terms
  `https://www.gdeltproject.org/about.html`, attribution "The GDELT Project (https://www.gdeltproject.org)".
- **`zeroResultsNormal`:** false.
- **Field specs** for `summary`, `eventRootCode`, `cameoRoot`, `quadClass`, `goldstein`, `numMentions`,
  `numSources`, `numArticles`, `avgTone`, `geoPrecision` and `sourceUrl`.

**2. Fetch.**
- **Host:** only `https://storage.googleapis.com/data.gdeltproject.org/gdeltv2/…`.
- **Index:** `getConditional(…/lastupdate.txt, "lastupdate")`. A 304 returns `FetchResult.unchanged()`.
- **File name** (`LastUpdateParser`):
  - only the last path segment of the export URL is kept, and it must match `^\d{14}\.export\.CSV\.zip$` with a real
    UTC timestamp;
  - the URL is rebuilt as `{base}/data.gdeltproject.org/gdeltv2/{file}` (`exportUri` checks the name again);
  - a declared size of 0 or above 50 MB is refused.
- **Same file:** if the name equals adapter state `lastFile`, the run returns `unchanged()` with no ZIP request.
- **Checks:** the ZIP's byte length must equal the declared size (a truncated body fails too), and its MD5 must
  equal the declared hash. Either failure throws `UpstreamFormatException`.
- **Unzip** (`ExportZip`): exactly one entry, named `{ts}.export.CSV`, and no directory entry. The uncompressed bytes
  are counted while streaming and refused past 200 MB.
- **Any failure after `lastupdate.txt` was read** forgets its validators (`SourceHttp.forgetValidators`), so the next
  run gets a 200 and retries the same file rather than a 304.
- **Newest file only:**
  - when `lastFile` is known, `Coverage(expected = files due, actual = 1)` is attached, with files due = whole
    15-minute steps between the two file timestamps, at least 1;
  - skipped files (due − 1) are added to `foresignal.gdelt.files_skipped` and logged at INFO;
  - on the first run no coverage is attached.
- **Hand-off:** `lastFile` advances only after the batch was ingested, with the FIRMS pattern of 065 (Deviation 1).

**3. Parser** (`GdeltExportParser`, pure, streaming).
- **Reading:** tab-delimited, UTF-8. Lines are read without ever buffering more than 64 KB, so a longer line is
  counted as bad rather than held in memory.
- **Bad lines:** a column count other than 61, an unreadable `GlobalEventID`, `Day` or `DATEADDED`, or an overlong
  line. More than 5% bad lines throws `UpstreamFormatException` with the first bad line as the snippet.
- **Codes:** event codes stay strings. A one-digit `rootCodes` entry is padded (`2` → `02`).
- **Filters:** root code; `NumMentions ≥ minMentions`; with `requireGeo`, a numeric in-range lat/lon and
  `ActionGeo_Type ≥ 1`; optional `countries`.
- **Record:**
  - `externalId = GlobalEventID`;
  - `observedAt = DATEADDED` (UTC), `eventTime = Day` at 00:00 UTC;
  - geometry `Point(ActionGeo_Long, ActionGeo_Lat)`, or null when `requireGeo=false` and there is no point;
  - `geoPrecision` 1 → `country`, 2 and 5 → `adm1`, 3 and 4 → `city`;
  - `geometryApproximate` is true for `country` and `adm1` and false for `city`. It is present only with a point.
- **Payload:** every field the prompt lists; `avgTone` is rounded to 2 dp.
  - Control characters are removed, whitespace is collapsed, and empty strings are left out.
  - `sourceUrl` is kept verbatim (it can be a citation rather than a URL; Codebook) when at most 2,048 characters.
- **`actionGeoCountryIso`:** filled through 040's crosswalk (`CountryCentroids.byFips`). FIPS codes the country table
  lacks, such as `GZ` (Gaza Strip), leave it out.
- **`summary`:** `"{actor1Name|Unknown actor} — {cameoRoot} — {actor2Name|} in {actionGeoName}"`.
  - Whitespace is collapsed and the text trimmed. Past 200 characters it is cut to 199 plus `…`, never splitting a
    surrogate pair.
  - It is built only from codes and names.
- **CAMEO roots:** a bundled 20-row table (`CameoRoots`).

**4. Fixtures** (`src/test/resources/fixtures/gdelt-events/`, README with date, file names and commands):
- **Captured:** `lastupdate.txt` and the unmodified export ZIP. The ZIP is kept, at 90 KB, because the adapter and
  ingest tests need its real size and MD5.
- **`export-40.tsv`:** 40 captured lines: 28 kept by the default filter (two country-level and three ADM1-level), one
  root-18 line with no location, and 11 other roots with leading-zero codes.
- **Hand-made:** `lastupdate-foreign-host.txt`, `bad-md5.txt`, `zip-two-entries.zip` and `short-rows.tsv`. The
  300 MB zip bomb is built in the test.

**5. Docs.**
- **Source Catalog:** the `gdelt-events` row is rewritten. It covers the URLs, tab-delimited text, verified facts,
  checks, newest-file-only with coverage, the params, policy, TTL, retention, time base, payload rules,
  `embedFields` and the corrected license.
- **Agent Guide:** a new section, "Conflict events from news: GDELT (prompt 066)". It says records are
  machine-coded from news, may be duplicated and do not confirm that an event happened, and that corroboration
  (101) is preferred. It also covers precision, times, `summary` and the untrusted `sourceUrl`.
- **OpenAPI:** no operation changes.
  - The `conflict-event` kind note documents `gdelt-events`; the payload field list was already complete.
  - There is a changelog entry. `lintOpenApi` is valid (71 warnings, unchanged). Postman was regenerated and did
    not change.

## Files created / modified

**Created**
- `src/main/java/ai/foresignal/source/adapters/gdelt/`: `package-info.java`, `GdeltEventsAdapter.java`,
  `LastUpdateParser.java`, `GdeltExportParser.java`, `ExportZip.java`, `CameoRoots.java`.
- `src/test/java/ai/foresignal/source/adapters/gdelt/`: `GdeltExportParserTest.java`, `LastUpdateParserTest.java`,
  `GdeltEventsAdapterTest.java`, `GdeltIngestIT.java`, `GdeltEmbeddingTest.java`.
- `src/test/resources/fixtures/gdelt-events/`: `README.md`, `lastupdate.txt`, `20261013143000.export.CSV.zip`,
  `export-40.tsv`, `lastupdate-foreign-host.txt`, `bad-md5.txt`, `zip-two-entries.zip`, `short-rows.tsv`.

**Modified**
- `src/test/java/ai/foresignal/testsupport/weather/WeatherIngestSupport.java`: the GDELT base URL and source id, so
  `GdeltIngestIT` shares the existing context.
- `src/test/java/ai/foresignal/retention/FamilyDefaultsTest.java`: `GdeltEventsAdapter` in the `bulk-event` class.
- `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Agent-Guide.md`.

## Deviations from the prompt

1. **"Store `lastFile` after a successful hand-off."** The SPI has no hook that runs after the writer. As in 065, a
   run stores `lastFilePending = {file}@{run time}`. The next run promotes it to `lastFile` when `lastSuccessAt` is
   no earlier than that run time. Otherwise it discards it and forgets the `lastupdate.txt` validators, so the file
   is fetched again. A failed or interrupted run therefore never advances `lastFile`.
2. **Retention of 90 d.** The prompt names none, so the Conventions `bulk-event` class ("GDELT events") applies.
3. **No place name.** When `requireGeo=false` and there is no ActionGeo name, `" in {actionGeoName}"` is left out
   instead of leaving a dangling "in".
4. **The summary is cut** to 199 characters plus `…` (200 in all), so a cut summary is visibly cut.
5. **Coverage when nothing was skipped.** When `lastFile` is known and the new file is the next one,
   `Coverage(1, 1)` is attached, so a healthy run reports full coverage. Only the first run has none. A file older
   than `lastFile` counts as 1 due.
6. **Bad lines also include** an unreadable `GlobalEventID`, `Day` or `DATEADDED`, and lines over 64 KB, not only
   wrong column counts. Such a line cannot become a record, and the 64 KB cap keeps a zip bomb of long lines from
   being buffered.
7. **`sourceUrl` is capped at 2,048 characters** (left out above that).

## Issues found / open questions

1. **FIPS codes missing from 040's country table:** `GZ` (Gaza Strip) appears in the captured file and has no ISO
   mapping, so its `actionGeoCountryIso` is absent (`WE`, the West Bank, maps to `PS`). Other GDELT-only FIPS codes
   may also be missing; this was not surveyed. `actionGeoCountryIso` is present only when the table knows the code.
2. **A guard is still red since 062** (carried from 065 Issue 4, not fixed here). `FamilyDefaultsTest` lacks the
   062–064 adapters, and most of them set no `defaultRetention`. Classifying them means choosing a class and changing
   those adapters' descriptors, which is outside this prompt. `GdeltEventsAdapter` was added.
3. **The `ParamValidator` BBOX antimeridian defect** (065 Issue 3) does not affect this source, which has no bbox
   parameter.
4. **Volume:** a 15-minute file had 191 root 13–20 events on 2026-10-13, 183 kept, so about 17,600 records a day
   (about 12 a minute), well inside §5.3's "standard < 0.5k/min".
5. **Instructions in data:** none. `lastupdate.txt`, the export lines read, the terms page and the two PDFs contained
   no instructions to an agent. The export rows hold third-party article URLs, which are stored as data and never
   fetched.

**Owner questions (non-blocking):**
1. Keep `gdelt-events` at the `bulk-event` 90 d retention (Deviation 2)?
2. Should 040's country table gain the GDELT-only FIPS codes (`GZ` and any others) for the crosswalk?

## Tests written (not run)

- **`GdeltExportParserTest`:**
  - the default filter (28 of 40, 12 filtered, 0 bad);
  - every mapped index of one row (time base, point, all payload fields, the summary, no ISO for `GZ`); `IS` → `IL`
    and `KS` → `KR`;
  - no crosswalk;
  - leading zeros (`0356`, `035`, `03`; a padded `2`);
  - geoPrecision for 1–5 and the approximate flag;
  - `requireGeo=false` (29 records, no geometry);
  - `minMentions` (12) and `countries` (10 and 4);
  - summary determinism under line reversal, whitespace, the 200-character cap and surrogates;
  - malformed counting: `short-rows.tsv` refused at 2 of 20, 2 of 42 accepted, an unreadable time and id, an
    overlong line;
  - the CAMEO table.
- **`LastUpdateParserTest`:** the three-line parse; the export entry and timestamp; the foreign host ignored; the
  file-name regex (digits, case, traversal, query, impossible date); 50 MB accepted and 50 MB + 1 refused, and 0
  refused; malformed, empty and no-export files.
- **`GdeltEventsAdapterTest`** (`@IntegrationTest`, WireMock, real `UpstreamClient` through `AdapterHttpHarness`):
  - the first file (183 records, pending hand-off, only bucket paths);
  - production URLs on `https://storage.googleapis.com`; the foreign host;
  - the same `lastFile` → not modified with no ZIP request; a 304 → not modified;
  - pending promoted, then not fetched again; pending discarded → fetched again without `If-None-Match`;
  - MD5 mismatch (`lastFile` unchanged, validators forgotten); size mismatch;
  - two entries; a wrong entry name, a directory entry, a non-ZIP; the 300 MB zip bomb refused at 200 MB;
  - skipped-file coverage (2 due, counter 1) and `filesDue`; the next file (1 of 1);
  - params reaching the parser; `SOURCEURL` never requested (2 requests, all under the bucket path);
  - descriptor validity and values; `checkParams`.
- **`GdeltIngestIT`** (`WeatherIngestSupport`, real executor and writer):
  - the first file → 183 FIRST versions, `event_time` = start of `Day`, `observed_at` = `DATEADDED`,
    `valid_until = last_received_at + 24 h`;
  - the same file 15 minutes later → no new versions; the ZIP requested once and `lastupdate.txt` twice;
    `lastFile` promoted;
  - retention resolves to 90 d.
- **`GdeltEmbeddingTest`:** the embedded text of every record is `conflict-event | GDELT events | {summary}`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (one unchecked-operations note from
  `GdeltExportParserTest`).
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "832 requests across 80 tag folders … covers all
  ops: True" (no change to the collection).

## Notes for the next prompt

1. **067 and later adapters:**
   - for upstreams that publish one file per interval, reuse the `lastFile`/`lastFilePending` hand-off of
     `GdeltEventsAdapter.promotePending`;
   - forget `getConditional` validators whenever a 200 index could not be used (`SourceHttp.forgetValidators`);
   - `ExportZip.CappedInputStream` shows streaming zip-bomb protection.
2. **Country-level sources** (IODA, advisories): `CountryCentroids.byFips` lacks at least `GZ`.
3. **097 checkpoint:**
   - first run of the GDELT tests;
   - `GdeltIngestIT` shares `WeatherIngestSupport`'s context;
   - `GdeltEventsAdapterTest.zipBomb` deflates 300 MB in the test (a few seconds of CPU);
   - `FamilyDefaultsTest` still fails on the unclassified 062–064 adapters (Issue 2).
4. **101 (intel events):** GDELT records are uncorroborated news coding. Count evidence groups, not records, and
   treat `sourceUrl` as untrusted (028).
5. **143:** no key is needed. Optionally recapture the fixtures per the README and confirm the counts.
6. **144 (Flyway):** no schema change. Adapter state of `gdelt-events`: `lastFile`, `lastFilePending`
   (`{file}@{instant}`), `etag:lastupdate`, `lastmod:lastupdate`.
7. **Owner decisions pending:** the two questions above, plus those carried from 012–065.
