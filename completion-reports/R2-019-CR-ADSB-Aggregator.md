# R2-019-CR-ADSB-Aggregator

| | |
|---|---|
| Prompt | R2-019-ADSB-Aggregator.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `ea5bb471156f4826c6b27b065c068172aae7b27f` |
| Date | 2026-10-26 |

## What was added

- **New package `ai.foresignal.source.aggregator`:**
  - `AggregatorSettings`: a record stored under `aggregator.<domain>`. The five keys are in `SettingKeys` and in
    `ALL`.
  - `DomainAggregator`: the SPI. Each implementation provides:
    - `domain()` and `name()`;
    - `implemented()`;
    - `sourceIds()`;
    - `supersededSourceIds()`;
    - `validate`, `apply` and `test`.
  - `PendingDomainAggregator`: serves ais, datalink, satellite and ground-sensors until their prompts add a bean.
    Their configuration can be stored, but only disabled.
  - `AggregatorService`:
    - dispatch, parsing (every problem in one 400) and the view;
    - the supersede and restore logic;
    - `SupersededSources`;
    - alerts through R2-014: `system.aggregator_down` and `system.aggregator_recovered`, edge-triggered, driven by
      `SourceHealthChangedEvent` on the aggregator's main source.
  - `AdsbAggregator`.
- **`api/AggregatorsController`:** the four operations.
- **New source `local-adsb-aggregator` (`LocalAdsbAggregatorAdapter`):**
  - It is a `LocalAdsbAdapter` with its own descriptor, using a new protected constructor.
  - Same parse, staleness rule and policy as the local slots, and `LOCAL_ONLY` (overridable).
  - Sized for a whole feed: 32 MB, 10 s timeout, every 5 s by default (1 s minimum), 60 per minute and 100,000 per
    day.
  - Its `url` and `band` parameters are written by the aggregator.
  - Its id carries the `local-adsb-` prefix, so every derived aircraft layer, `derived-gps-jamming` included, reads
    it without any change.
- **`SourceService`:**
  - New interface `source/SupersededSources` and an optional setter `setSupersededSources(ObjectProvider<…>)`.
  - `seedsEnabled` and `startsEnabled` now also require that no live aggregator supersedes the source. So seeding,
    `enable-core` and `enable-free` all leave a superseded source off.
- **`docker-compose.yml`:** an opt-in `readsb` profile:
  - It uses `ghcr.io/sdr-enthusiasts/docker-adsb-ultrafeeder`, overridable with `READSB_IMAGE`.
  - It is configured with `READSB_DEVICE_TYPE`, `READSB_LAT`/`READSB_LON` and `ULTRAFEEDER_CONFIG`, and maps USB
    with the cgroup rule.
  - Memory is capped and cannot swap (`READSB_MEM_LIMIT`, default 1g).
  - The tar1090 map is published on `127.0.0.1:8078`.
  - `docker compose --profile readsb config --services` lists `db`, `foresignal` and `readsb`.
- **Tests (written, not run):** `AggregatorSurfaceIT`, `AdsbAggregatorTest` and `SupersededSourcesIT`, with the
  shared fixture `AggregatorItSupport`.
- **Docs:**
  - `docs/Network-Sourcing.md` was replaced whole: a new section on the aggregator surface, and C2 rewritten to what
    was built.
  - `docs/Operator-Guide.md` gained a section, "First-party aggregators (R2-019)".
  - `docs/Source-Catalog.md` gained a parity row for `local-adsb-aggregator`.
  - `docs/Source-Onboarding.md` gained a note on superseded sources. Its `SourceService.java` line links were
    corrected; they were already stale before this prompt.
  - `docs/Invariants.md` registers EGR-6 and EGR-7.

## Contract

- **`Aggregator`** gains:
  - `config` and `disableSupersededSources`;
  - the read-only `implemented`, `statusReason` and `sourceIds`.

  The descriptions of `endpoint`, `status` and `lastSeenAt` now state their meaning.
- **`AggregatorConfig`:** the descriptions of `endpoint`, `config` and `disableSupersededSources` now state the
  rules.
- **Descriptions:** each of the four operations gained an R2-019 paragraph.
- **Postman:**
  - an example for `updateAggregator`;
  - the collection variable `{{domain}}`, defaulting to `adsb`;
  - the collection was regenerated.
- **Left alone:** the operations stay `planned`, and the SDKs were not regenerated.
- **Field name:** the prompt calls it `disableSuperseded`; the contract calls it `disableSupersededSources`. The
  contract name is used.

## Decisions and disagreements for the owner

1. **The prompt contradicts itself on the ADS-B adapters, and the code agrees with neither half as written.**
   - The prompt says to point adsb.lol's world, mil and area adapters at the local readsb through
     `BASE_URL_PROPERTY`.
     - Those adapters request the mirror API's own paths (`/v2/lat/…/lon/…/dist/…`, `/v2/mil`, `/v2/point/…`; see
       `AdsbLolWorldAdapter.uri`, `AdsbLolMilAdapter.PATH`).
     - A readsb does not serve those paths. It writes `aircraft.json` (readsb README: `--write-json=<dir>`).
   - The same prompt also says to turn those adapters off as superseded.
   - `BASE_URL_PROPERTY` is also a test-only startup property ("Test-only override", `AdsbLol.java`), not a runtime
     setting.
   - So I built a dedicated read, `local-adsb-aggregator`, and superseded the mirrors.
   - Should the mirror adapters instead gain a runtime base URL? That would only help against a host that serves the
     adsb.lol API.
2. **Superseding is conditional; no descriptor's `autoEnable` was changed.**
   - The prompt asks to mark the mirrors `autoEnable=false` "so a fresh install seeds them off".
   - In the code, seeding follows `CORE_IDS`, not `autoEnable`. Turning the mirrors off for good would also leave a
     fresh install that has no readsb with no ADS-B at all.
   - Instead, the mirrors are held off only while the aggregator is live. That covers seeding, `enable-core` and
     `enable-free`.
3. **Turning the aggregator off restores sources.** Turning it off (or setting `disableSupersededSources` to false)
   turns back on exactly the sources it turned off, unless an operator has since turned them on. This is how "disabling
   restores the previous behavior" is met.
4. **The endpoint is the full `aircraft.json` URL** (for example `http://readsb/data/aircraft.json`), not
   `http://localhost` as the contract's example says.
   - It goes through `LAN_URL` validation and pinning. A readsb in the compose profile is reached by its service
     name, which resolves to a private address.
   - From inside the broker's container, `localhost` would not reach a readsb in another container.
5. **The read is `LOCAL_ONLY` (overridable)**, as the design's EGR-6 says.
   - This is stricter than `adsblol-world` (ODbL, allowed). Sinks, federation and share links will not carry it
     unless an operator sets `"egress": "OPEN"` on `local-adsb-aggregator`.
6. **The `feed` mode is documented generically.**
   - Verified: readsb takes `--net-connector=<ip,port,protocol>` inputs such as `beast_in`, and the ultrafeeder image
     takes `ULTRAFEEDER_CONFIG=adsb,<host>,<port>,beast_in`.
   - Not verified: any public "global feed" that may be aggregated, or on what terms. The docs say this is up to the
     feeds' operators.
7. **The image is `ghcr.io/sdr-enthusiasts/docker-adsb-ultrafeeder:latest`**, because its README says "Make sure to
   use …:latest". This repository otherwise pins tags (ollama `0.40.3`).
   - I did not invent a version. The comment tells you to pin a digest with `READSB_IMAGE`.
   - Your call whether to pin one in the file.
   - The image is GPL-3.0; nothing of it is bundled.
8. **Status mapping:** healthy → `ok`; degraded or not yet run → `degraded`; failing or disabled → `down`; not live →
   `unconfigured`.
9. **Domains without a backend:**
   - `enabled: true` is a 400 for the four domains without a backend, rather than accepting a configuration that does
     nothing.
   - R2-020…R2-023 each add a `DomainAggregator` bean, and the pending stand-in disappears.
10. **`docker-compose.yml` contains "honoured"** (the `FORESIGNAL_TRUSTED_PROXIES` comment, now line 83).
    That is British spelling in a file `checkUsEnglish` evidently does not scan. Not changed (another prompt's line);
    flagged for you.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- `docker compose config --quiet`: no output; `--profile readsb config --services` printed `db`, `foresignal` and
  `readsb`. No container was started.
- No test, `lintOpenApi` or `checkApiCompat` was run.
  - The guards this touches were reviewed by reading them, not run: `SourceCatalogParityIT`,
    `RegisteredSourcesHaveLicenceTest` and `InvariantRegistryTest`.

## Notes for the next prompt

- R2-020…R2-023 implement `DomainAggregator` as a `@Component` for their domain. `AggregatorService` picks the bean
  up and drops the pending stand-in; the constructor refuses a second bean for the same domain.
- To make a domain work:
  - put its first-party read sources in `sourceIds()`, main one first;
  - put its public sources in `supersededSourceIds()`, registered ids only;
  - write the endpoint into a source's `LAN_URL` parameter in `apply`, so pinning and the egress rule hold.
- The next Flyway migration is still **V24**.
