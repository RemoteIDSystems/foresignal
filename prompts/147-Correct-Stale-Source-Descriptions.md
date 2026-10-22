# 147 — Correct the now-stale "disabled by default" source descriptions

**Goal.** After 145/146 flipped the seed default, some adapter description strings — returned by
`GET /v1/sources` — still say "disabled by default" / "off by default" for sources that now start
**enabled**. Correct **only** those; leave every description that is still accurate. No behavior
change — this is a truthfulness pass over the API-visible strings and a stale comment.

Owner decisions this rests on: free, runnable sources start enabled (145); `wiki-edit-surge` is the
one opt-in exception (146); **keyed sources stay disabled until configured** (so their "disabled by
default" descriptions remain correct).

**Read first.** `prompts/145-*.md`, `prompts/146-*.md` and their completion reports;
`SourceService` (`runsWithoutOperatorInput`, the 146 `autoEnable` flag, `defaultRow`);
`SourceDescriptor` (`requiresCredentials()`, `params()`); `adapters/bluesky/BlueskySearchAdapter.java`.

**The rule.** For each adapter under `src/main/java/ai/foresignal/source/adapters/**`, determine the
source's **actual** seed default under the current logic — it seeds **enabled** iff
`runsWithoutOperatorInput(descriptor) && descriptor.autoEnable()` (no required credential, no required
param, not opt-in-flagged). Then:
- **If it now seeds enabled** and its description still claims "disabled/off by default" → **correct
  it** (describe the source's behavior; drop or fix the stale default claim). US English.
- **If it still seeds disabled** → **leave the description unchanged.**

**Do not touch** (these claims are still true or mean something else):
- Any source with a **required `CredentialField`** or **required `ParamSpec`** — the keyed and
  operator-configured sources: `ebird`, `mastodon`, `instagram`, `x`, `youtube`, `reddit`, `threads`,
  `ercot`, `fingrid`, `ris-live`, `spacetrack`, and the LAN / `local-*` receivers (`local-ais`,
  `local-acars`, `local-aprs`, `local-sonde`, `local-remoteid`, `airq-local`, `local-receivers`,
  `cctv`, …). Their "disabled by default" is accurate.
- **`wiki-edit-surge`** — opt-in-flagged, stays off.
- The **"off by default on the small profile"** lines (`ecmwf` AIFS, `sst`, and the ~6 like them) —
  that is Spring-profile registration, a different thing. Leave them verbatim.

**bluesky-search (decision from the 146 report).** Correct the stale `"Off by default"` text in
`BlueskySearchAdapter` (around L46 and L334) — the source now starts enabled. **No functional change:**
the social-correlation "platform enabled" flag governs whether the correlation engine *uses* a
platform for cues/comparisons (`ComparisonService`, `CueTriggers`, `SocialCueService`), not whether
the source *collects* — the two are separate subsystems, so there is nothing contradictory in the
code, only the comment. Leave `BlueskyJetstreamAdapter` ("on by default") as is.

**If a test pins one of the edited description strings,** update that expectation in the same pass
(write it; do not run it).

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Per `000-Run-Sequence`, do not
run tests, `lintOpenApi` or `checkApiCompat`. Report the US-English/style guard on the touched files.

**Done when.** No `GET /v1/sources` description claims "disabled by default" for a source that now
starts enabled; every still-disabled source's description is unchanged; the "small profile" lines are
untouched; `bluesky-search`'s wording is corrected; compilation passes.

**Commit.** `147: Correct stale 'disabled by default' source descriptions`, plus the completion
report. **Stage only the files this prompt changes** — leave `Dockerfile`, `scripts/smoke.sh` and the
untracked prompt files alone.
