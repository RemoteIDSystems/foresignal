# 145 — Free sources enabled by default

**Goal.** A fresh Foresignal install should be useful immediately: every source that needs **no
credential and no operator-supplied parameter** starts **enabled** and pulling at its default
interval. Sources that need a credential or a required parameter still start **disabled** until
configured. The operator can disable any source at any time (unchanged). Also provide a one-time
control to bring an **existing** install — whose rows were already seeded disabled — to the same
state.

This **reverses** the prior "every source starts disabled" default (see `SourceService.seedDefaults`
javadoc and `docs/Source-Onboarding.md` "The guarantee"). It is a deliberate owner decision: on this
self-hosted tool a new install should light up with all free data flowing, not sit empty.

**Read first.**
- `SourceService.java` — `seedDefaults` / `defaultRow` (~L132–172, the single `setEnabled(false)` at
  L161) and the `update()` enable guard (~L415–420: `requireCredentials` + `requireValidParams`).
- `SourceDescriptor.java` — `requiresCredentials()` (L365), `params()` → `List<ParamSpec>` (L149).
- `spi/CredentialField.java` (`required`) and `spi/ParamSpec.java` (`required`; a required spec has
  `defaultValue == null`).
- `CollectorScheduler.java:357–382` — the work set is `… where enabled` then skip when
  `requiresCredentials() && !credentialsSet`. Confirms an enabled, keyless, param-complete source
  runs and a gated one is simply skipped, never an error.
- `docs/Source-Onboarding.md` (the guarantee section), `api/CotBackfillController.java` (the pattern
  for an admin `POST /v1/sources/...` endpoint).

**The predicate — "runs with zero operator input".** A built-in source seeds **enabled** iff both:
- `!descriptor.requiresCredentials()`, AND
- `descriptor.params().stream().noneMatch(ParamSpec::required)`.

Otherwise it seeds **disabled**. This is exactly the set the existing `update()` enable guard accepts
with default settings (both `requireCredentials` and `requireValidParams` pass on empty params), so
no source is ever seeded enabled-but-broken. Add one private helper, e.g.
`boolean runsWithoutOperatorInput(SourceDescriptor d)`, and use it in both changes below.

DERIVED / PUSH / STREAM free sources are included — a push/derived source waits or computes, a stream
connects; none needs a credential. Each seeded row already carries the descriptor's **default
interval and budget**, so enabling the whole free set fetches at each source's own cadence and does
not hammer any upstream.

**Change 1 — the seed default.** In `SourceService.defaultRow(descriptor)`, replace
`entity.setEnabled(false)` with `entity.setEnabled(runsWithoutOperatorInput(descriptor))`. Rewrite the
`seedDefaults` / `defaultRow` javadoc to state the new rule (free-and-runnable → enabled;
credential- or param-gated → disabled; the operator opts out). `seedDefaults` still only creates rows
for sources that have none, so this governs fresh installs and any newly-registered source.

**Change 2 — one-time bulk enable for an existing install (admin).** Add
`POST /v1/sources/enable-free`, admin-token-gated exactly like the other source writes (model it on
`POST /v1/sources/cftc-cot/backfill` in `CotBackfillController`). It enables every **currently-
disabled** source for which `runsWithoutOperatorInput` is true, leaves every other row untouched, is
**idempotent**, and returns `{ "enabled": [<ids>], "count": N, "skipped": M }`. Route it through the
existing settings-update path so the same `SourceSettingsChangedEvent` fires and the collector picks
the sources up within ~2 s. This is the operator's "turn on everything free" control and the way an
existing install adopts the new default without re-seeding (which would discard existing config).

**Change 3 — docs + spec.**
- Rewrite `docs/Source-Onboarding.md` "The guarantee" section to the new behavior: free-and-runnable
  sources start **enabled and pulling**; credential/param-gated sources start **disabled** until
  configured; the operator can disable any source; and `POST /v1/sources/enable-free` brings an
  existing install up to the free default in one call. Keep the "any subset runs cleanly" guarantee —
  it still holds.
- Correct any "starts disabled" / "opt each source in" wording in `docs/Operator-Guide.md` and
  `docs/Source-Catalog.md`.
- Add `POST /v1/sources/enable-free` to the **authored** `docs/OpenAPI.json` (that file is the
  contract; the springdoc document is only for drift detection). Then regenerate Postman from it
  (the `scripts/generate-postman.py` gradle task) and keep the redocly lint and the additive-only
  API-compat gate green — a new path is additive.

**Tests (write in the same pass; run the touched tests + the guards, per CLAUDE.md "run the tests a
change touches").**
- `seedDefaults` / `defaultRow`: a no-credential, no-required-param source (e.g. `usgs-earthquakes`)
  seeds **enabled**; a credentialed source (an AIS / AISHub source) seeds **disabled**; a keyless
  source with a required param seeds **disabled**.
- `POST /v1/sources/enable-free`: enables only the free-and-runnable currently-disabled sources,
  leaves credentialed / param-gated / already-enabled rows unchanged, is idempotent on a second call,
  and is admin-gated (401/403 without the admin token).
- Run the touched tests, the US-English / style guard, the OpenAPI lint and the API-compat gate;
  report each result.

**Done when.** A fresh DB comes up with every free-and-runnable source enabled and fetching at its
default cadence; credential/param sources stay disabled; `POST /v1/sources/enable-free` brings an
existing DB to the same state idempotently; the operator can still disable any source; docs, spec and
Postman updated; touched tests and guards green.

**Commit.** `145: Free sources enabled by default`. **Stage only the files this prompt changes** — the
working tree has unrelated uncommitted edits (`Dockerfile`, `scripts/smoke.sh`); do not stage, revert
or touch them.
