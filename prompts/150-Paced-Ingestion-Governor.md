# 150 — Paced ingestion: global pull governor + configurable concurrency + curated core default

**Goal.** Reshape how an install starts and sustains collection. Today every enabled source runs on
its own thread with only **per-host** (`HostPacer`) and **per-source** (`BudgetGovernor`) limits and a
100 ms launch `STAGGER` — **nothing bounds the total number of simultaneous pulls**, so enabling many
sources stampedes the machine. Add three things:

1. a **global pull governor** that caps total simultaneous pulls;
2. a **user-configurable max** (default **2**, range **1–16**) as a runtime setting, enforced live;
3. a **curated core default-enabled set** — the Shadowbroker-equivalent live-tracking sources —
   **replacing 145's "all free on."**

Net effect: a fresh install comes up pulling the small core set a couple at a time (gentle on a laptop),
tunable up on a strong machine. **This revises 145/146:** the fresh-install default is now the core set;
`enable-free` stays as the "turn on everything free" control.

**Read first.** `collect/CollectorScheduler.java` (the timer thread, per-source single-thread executors,
`STAGGER`, the `inFlight` *counter* — confirm there is **no** global cap today), `collect/FetchExecutor.java`
and the `launch`→run path, `collect/HostPacer.java` (per-host), `collect/BudgetGovernor.java` (per-source),
`collect/CollectorProperties.java`, `collect/ManualFetchService.java`, `collect/StreamingSupervisor.java`;
the settings pattern — `settings/` (`ServerSettingsService`, `WatchdogSettingsService`, `AppSettingService`,
`AppSettingChangedEvent`, `SettingKeys`) and `api/SettingsController.java` (`/v1/settings/*`);
`source/SourceService.java` (`defaultRow`, `seedDefaults`) and the 145/146 `runsWithoutOperatorInput`/
`autoEnable`; prompts 145–146 and their CRs. **Core-set mapping — read `docs/Source-Catalog.md` as the authority, do not trust any list here.**
The catalog's aircraft sections ("Aircraft decisions (prompt 062)", "Rail and supplemental aircraft
(prompt 080)") describe exactly which sources make up Shadowbroker's set. For **aircraft** the core is
`adsblol-world` (the worldwide civilian+military feed — **Shadowbroker's main aircraft feed**),
`adsblol-mil`, and the blindspot gap-fill (`adsbfi-blindspots`) — **NOT** the `/mil`-only feeds alone
(a `/mil` feed gives *military-only*, which is the bug this prompt must avoid). For the other chosen
categories pick the keyless, param-free source(s) that give that coverage: satellites = CelesTrak TLE
group(s); earthquakes = `usgs-earthquakes`; weather alerts = the NWS `api.weather.gov` alerts source;
wildfires = the NASA FIRMS global source; mesh = the Meshtastic source(s). Verify each id against its
descriptor (keyless, no required param).

## Part A — the global pull governor

- Add a **global concurrency limiter** (a resizable permit pool) that every **polling fetch run**
  acquires immediately before the upstream pull and releases in a `finally`. Put it at the single
  fetch-execution chokepoint (`FetchExecutor` / the `launch`→run path) so it bounds **total**
  simultaneous pulls across all sources and hosts — the limit that does not exist today. Manual fetch
  (`ManualFetchService`) and any backfill pull acquire it too.
- **Streams are not gated by it.** A streaming source connects once and persists; it is not a repeated
  pull. Only polling / manual / backfill pulls acquire a permit. Keep per-source threads, `STAGGER`,
  `HostPacer` and `BudgetGovernor` underneath, unchanged.
- **Resizable at runtime:** a `Semaphore` subclass (or equivalent) whose permit count can grow
  (`release`) and shrink (`reducePermits`) so a setting change takes effect **without a restart**.
- **No separate cold-start ramp is needed:** with the governor in place the initial burst of
  due-at-startup sources proceeds at most `cap` at a time; the full enabled set fills a couple at a
  time, then settles into normal interval scheduling — all under the same cap.

## Part B — the configurable setting (the slider's backend)

- Add a persisted runtime setting **`maxConcurrentPulls`** (name to fit the codebase), **default 2**,
  **clamped to 1–16**, via the existing settings pattern: a typed setting + service + **admin GET/PUT
  under `/v1/settings`** (model it on a simple existing settings service). Read at startup; on change
  (`AppSettingChangedEvent` / the setting's event) **resize the governor live**.
- Add it to the **authored** `docs/OpenAPI.json` (the setting schema + endpoint shape), regenerate
  Postman (the generate-postman task), and keep redocly lint + the additive compat gate green.
- **Bump the API to 1.2.0** (additive: the new setting field + the `enable-core` endpoint below),
  updating the three `ApiVersionTest` pins and `info.version`, consistent with the 1.1.0 bump in 146.

## Part C — the curated core default-enabled set (revises 145)

- Add a **`core`** concept to the source descriptor (a boolean flag on `SourceDescriptor`, or a curated
  id set in `SourceRegistry` — pick the cleaner one) marking the keyless, param-free sources that make
  up the Shadowbroker live-tracking set **as the catalog describes them** (see the core-set mapping in
  "Read first"). For aircraft this is `adsblol-world` + `adsblol-mil` + `adsbfi-blindspots` — the
  worldwide feed included, **not** a `/mil`-only subset (that would make the map military-only). A core
  source must itself be runnable with no operator input.
- Change `SourceService.defaultRow` so a source seeds **enabled iff `isCore(d) &&
  runsWithoutOperatorInput(d)`**. Everything else — free non-core, keyed, required-param, opt-in
  (`wiki-edit-surge`) — seeds **disabled**. Update the `seedDefaults`/`defaultRow` javadoc.
- `POST /v1/sources/enable-free` is **unchanged** (still enables all free + runnable + `autoEnable`,
  skipping `wiki-edit-surge`). Add a symmetric **`POST /v1/sources/enable-core`** (admin) that enables
  the core set — the one call by which an **existing** install adopts the new default without wiping its
  DB. Route both through the settings-update path so the `SourceSettingsChangedEvent` fires.

## Part D — docs

- `docs/Source-Onboarding.md` "The guarantee": the default-enabled set is now the **core set** (name
  them), pulled a couple at a time under the governor; explain `enable-core` / `enable-free` and the
  `maxConcurrentPulls` setting.
- `docs/Operator-Guide.md`: the `maxConcurrentPulls` setting and the core default.
- `collect/package-info.java`: document the governor as the global bound beneath the per-host and
  per-source limits.
- `src/main/resources/application.yml`: update the `sources.default-enable` comment — it now seeds the
  **core** set, not all free.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. Per `000-Run-Sequence`, do **not** run the test
suite, `lintOpenApi` or `checkApiCompat`. **Do** run `./gradlew checkUsEnglish` (the 149 guard; keep it
green). Write — but do not run — tests covering: the governor bounds concurrent pulls to the cap; a
setting change resizes it live; the cold-start fill never exceeds the cap; streams are **not** gated;
`seedDefaults` seeds only the core set enabled; `enable-core` enables the core set; `enable-free` still
enables all free.

## Done when

A fresh DB seeds only the core set enabled and fills a couple at a time; total simultaneous pulls never
exceed `maxConcurrentPulls`; the setting is admin GET/PUT and resizes the governor **without a restart**;
`enable-core` and `enable-free` behave as specified; API is 1.2.0; docs + spec + Postman updated;
`checkUsEnglish` green; compilation passes. The completion report notes the exact core source ids chosen.

**Do not touch:** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the
`x-foresignal` changelog history; `Dockerfile`, `scripts/smoke.sh`, the untracked prompt files.

**Commit.** `150: Paced ingestion — pull governor, concurrency setting, core default set`, plus the
completion report. **Stage only the files this prompt changes.**
