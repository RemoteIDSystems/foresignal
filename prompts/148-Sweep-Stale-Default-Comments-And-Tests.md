# 148 — Final cleanup of the source-default flip (comments, tests, docs, spellings)

**Goal.** The **last** prompt of the 145–147 arc. Fix every remaining thing the source seed-default
flip made stale — but **only** within the source-enablement domain. No behavior change. After this, the
arc is closed; if you find more staleness *inside this domain*, fix it in this same pass rather than
deferring.

Context: 145 made a source seed **enabled** when it needs no operator input; 146 added the `autoEnable`
opt-in flag and held `wiki-edit-surge` off; 147 corrected the API **description** strings for the five
that flipped (amtraker, gpsjam-daily, submarine-cables, meshtastic-mqtt, bluesky-search). What's left is
comments, test names, a few doc spots, and two spellings.

**The determination rule (same as 147).** A source now seeds **enabled** iff
`runsWithoutOperatorInput(descriptor) && descriptor.autoEnable()` — no required credential, no required
param, not opt-in-flagged. Edit wording **only** where it now misstates that; leave wording that is
still true.

## In scope — fix these

1. **Source-domain code comments** under `src/main/java/ai/foresignal/source/**` (adapters, families,
   `package-info.java`, `kit/**`) that state or imply a source is "disabled/off by default" when that
   source now seeds **enabled**. Correct the wording (describe behavior; drop the stale default claim).
   The now-enabled families to check include the ones the 147 report named — CAP, FDSN, CWFIS, road,
   CCTV — plus the adapter-level comments of the five already description-fixed (amtraker, gpsjam,
   meshtastic-mqtt, submarine-cables; bluesky-search's comment was done in 147). Apply the rule
   per-member: these families are **mixed** (e.g. some CCTV and road members need a key or a required
   param) — leave those.

2. **Source-domain tests** under `src/test/java/ai/foresignal/source/**` whose **name or display
   string** says "disabled by default" for a source that now seeds enabled (e.g. in the cctv / roads /
   fire / osm / cyber / stac ingest tests, and `AutoEnableDefaultsTest` / `SourceServiceIT` where
   applicable). For each: if it asserts the **test-profile** behavior (everything seeded disabled —
   `application-test.yml` sets `sources.default-enable: false`), rename it to say so; if it was meant to
   assert the **production** default and no longer does, update it to assert the real per-source default.
   Keep every test passing **by inspection** (do not run them).

3. **The three source docs** — `docs/Operator-Guide.md`, `docs/Source-Catalog.md`,
   `docs/Source-Onboarding.md`: any remaining claim about the **source enablement default** that is now
   wrong. (These were touched in 145–147; this is a verification pass for leftovers. Ignore unrelated
   "disabled by default" prose about other features.)

4. **Two description spellings (owner-approved):** `licence` → `license` in the **gpsjam-daily**
   description, `stylised` → `stylized` in the **submarine-cables** description. These two are the only
   API-description edits in this prompt.

5. **Verify the `enable-free` endpoint's OpenAPI prose** (the `POST /v1/sources/enable-free` summary /
   description added in 145) accurately says it enables the free, runnable sources and skips the opt-in
   (`wiki-edit-surge`) and credential/param-gated ones. Correct it only if it misstates that.

## Out of scope — do NOT touch

- **"disabled by default" for non-source features** — notification channels (`notify/**`,
  `store/NotificationChannelEntity`), plugins (`plugin/**`), glance (`stats/glance`), feature specs
  (`source/kit/feature/**`, `FeatureSpecValidatorTest`), the **websub kit** gate
  (`foresignal.kits.websub.enabled=false`, `WebSubDisabledIT` — a feature flag, not source seeding),
  raster/basemap (`raster/**`), and the various `*Notifications` classes. These features are genuinely
  off by default and are unrelated.
- **`wikistream` / `wiki-edit-surge`** — opt-in-flagged, correctly off; its "disabled by default" is
  true.
- **Keyed, required-param, and "small profile" sources** — their default claims are still accurate
  (acars/ais/ebird/ercot/fingrid/instagram/mastodon/reddit/threads/x/youtube/spacetrack/ris-live, the
  `local-*` receivers, airq-local, stac's aoi-gated sources, AIFS/SST small-profile lines, etc.).
- **The OpenAPI.json `x-foresignal` changelog `note` entries** (dated, prompt-numbered history like
  "081:…", "092:…" saying sources were "disabled by default" when added) — these are historical and
  accurate as of their version; leave them, do not rewrite history.
- **Every `1.0.0`** that is a forecaster/scorer/template/component version, a test fixture, or the
  frozen `docs/api/v1.0.0/OpenAPI.json` baseline — the API version work (→ 1.1.0) is already complete.
- **Config** — `application.yml` `sources.default-enable: true` is already present and commented, and
  `application-test.yml` already sets it `false` in both profiles. Leave it.
- **`Dockerfile`, `scripts/smoke.sh`, and the untracked prompt files** — unrelated uncommitted edits.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. Per `000-Run-Sequence`, do not run tests,
`lintOpenApi` or `checkApiCompat`. Report the US-English/style guard on the touched files.

## Done when

No comment, test name, or source-doc line claims a source is "disabled by default" when it now seeds
enabled; every still-off source (keyed, required-param, opt-in, small-profile) is unchanged; the two
spellings are corrected; the `enable-free` prose is accurate; compilation passes.

**Commit.** `148: Final source-default cleanup — comments, tests, docs, spellings`, plus the completion
report. **Stage only the files this prompt changes.**
