# 146 — Hold high-bandwidth free sources off by default; API 1.1.0; reconcile 145's fallout

**Goal.** Refine prompt 145. Free, runnable sources still start **enabled** — with one carve-out: a
source may be marked **opt-in** so it starts **disabled** despite needing no credential or parameter,
for sources whose cost is bandwidth or terms rather than a key. Apply that flag to
**`wiki-edit-surge`** (~3 GB/day) only. Bump the API to **1.1.0** for the `enable-free` endpoint 145
added. Fix the doc and test fallout 145's new default created.

Owner decisions driving this: "follow Shadowbroker except for `wiki-edit-surge` — leave that one off
by default until the user chooses" and "bump to 1.1.0". So **every other** free, runnable source stays
enabled as 145 built it — including `openmhz-systems` (if Cloudflare keeps blocking it, it simply
shows failing health; that is visible, not an error) and the non-commercial feeds.

**Read first.** `prompts/145-Free-Sources-Enabled-By-Default.md` and its completion report;
`SourceService.java` (the `runsWithoutOperatorInput` helper, `defaultRow`, and the
`POST /v1/sources/enable-free` handler added in 145); the `wiki-edit-surge` adapter/descriptor;
`SourceDescriptor.java` (builder); `ApiVersionTest`; `docs/Operator-Guide.md`;
`CollectorSchedulerIT` and the social source-seeding tests.

**Change 1 — opt-in flag.** Add one boolean to `SourceDescriptor` (default **true**), meaning "when
this source needs no operator input, it is eligible to start enabled." Name it to fit the codebase
(e.g. `autoEnable`), with a javadoc: a free, runnable source may still be held off by default for
bandwidth or licensing reasons; the operator opts in. Set it **false** on `wiki-edit-surge`'s
descriptor (reason in a comment: ~3 GB/day).

Gate **both** the seed default and the bulk endpoint on the combined predicate
`runsWithoutOperatorInput(d) && d.autoEnable()`:
- `SourceService.defaultRow(...)` seeds enabled only when both hold.
- `POST /v1/sources/enable-free` enables only sources where both hold.

So `wiki-edit-surge` seeds **disabled** and `enable-free` **skips** it; the operator turns it on with
the normal per-source `PUT /v1/sources/wiki-edit-surge {"enabled":true}`. Every other free, runnable
source is unaffected. The flag is the general mechanism if the owner later holds more sources back.

**Change 2 — API version 1.1.0.** Set the version to **1.1.0** everywhere it is declared — the three
`ApiVersionTest` pins and `docs/OpenAPI.json` `info.version` (search for others; do not guess the
set). If `docs/OpenAPI.json` changes, regenerate Postman via its gradle task (a generation step, not
a check). The new path is additive, so the frozen-baseline compat gate stays satisfied.

**Change 3 — doc reconcile.** In `docs/Operator-Guide.md`, the line stating no social platform
collects except Bluesky's Jetstream is now wrong — `bluesky-search` is free and starts enabled.
Correct it. Re-scan `Operator-Guide.md`, `Source-Catalog.md` and `Source-Onboarding.md` for any claim
the new default contradicts, and note `wiki-edit-surge` as the opt-in exception (free but off by
default). US English throughout.

**Change 4 — test reconcile (write/adjust in this pass; see "What runs" — do not run them).** Make the
suite consistent with the new default by inspection:
- Any test asserting "sources seed disabled", or counting seeded enabled/disabled sources, updates to
  the new default.
- `CollectorSchedulerIT` must stay **offline and deterministic** — it must not reach real upstreams now
  that free sources seed enabled. Use the test's existing fixture mechanism (test-only descriptors, or
  seed disabled in the test setup/profile) so no live network call happens; assert the scheduler picks
  up the fixtures.
- The social source-seeding tests: update expectations for `bluesky-search` now enabled.
- Add a unit test: `wiki-edit-surge` seeds **disabled** and is **skipped** by `enable-free` even though
  it needs no credential or param; a plain free source (`usgs-earthquakes`) seeds enabled and is
  included.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Per
`prompts/000-Run-Sequence.md`, do **not** run tests, `lintOpenApi` or `checkApiCompat`; the owner runs
the full suite and the Gradle checks when the project is declared code-complete. Postman regeneration
is a generation step and is fine. Report the US-English/style guard result on the touched files.

**Done when.** `wiki-edit-surge` (and anything else later flagged) starts disabled and is excluded from
`enable-free`; all other free, runnable sources start enabled as in 145; the API reads 1.1.0 across the
pins and the spec; the Operator Guide no longer contradicts the code; the tests are consistent with the
new default by inspection. Compilation passes.

**Commit.** `146: Hold high-bandwidth free sources off by default; API 1.1.0`, plus the completion
report. **Stage only the files this prompt changes** — the working tree has unrelated uncommitted edits
(`Dockerfile`, `scripts/smoke.sh`) and untracked prompt files; do not stage, revert or touch them.
