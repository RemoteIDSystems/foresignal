# 149 — US-English guard + fix the backlog it flags

**Goal.** foresignal has **no** US-English enforcement — British spellings slip in and are only caught
by hand on touched files. Add a repo-wide guard that **fails the build** on a British spelling, with an
explicit allowlist for the legitimate keep-as-is cases, then fix every real violation it surfaces
(including the three known ones). This is the enforcement that ends the recurrence — a rule left to
memory recurs; a rule with a guard does not.

**Read first (do not modify any of them).**
- `~/Documents/GitLab/CLAUDE.md` — the **US English, always** section: the rule and the **five
  never-correct exceptions**. This defines the allowlist.
- `build.gradle.kts` — the existing guard tasks (`lintOpenApi`, `checkApiCompat`) for the pattern to
  match (a task wired into `check`).
- `~/Documents/GitLab/meshdtn/style/test_us_english.py` — reference for approach only; **do not modify
  it and do not wire foresignal to it** (separate repo). Build foresignal's own.

## Build the guard

- A **JVM/Gradle** verification task **`checkUsEnglish`** — keep it in the JVM/Gradle toolchain (no new
  language runtime; match the existing `lintOpenApi`/`checkApiCompat` guards) — wired into the `check`
  lifecycle so the build fails on any violation.
- It scans **project-authored** text for British spellings from a **comprehensive** British→American
  wordlist. Seed from the CLAUDE.md examples (artifact, behavior, initialize, license, color, catalog,
  canceled, neighbor, gray) and extend to the full families: `-our` (colour, favour, honour, labour,
  flavour, neighbour, rumour, armour, vapour), `-ise/-isation` (organise, optimise, realise, analyse,
  recognise, customise, serialise, normalise, categorise, synchronise, authorise, prioritise,
  standardise, visualise, finalise, specialise, generalise, centralise + their -isation forms), `-re`
  (centre, metre, litre, theatre, fibre, calibre, spectre), `-ce` (licence, defence, offence, pretence),
  `-ogue` (catalogue, dialogue, analogue), doubled-l (cancelled, labelled, travelled, modelled,
  signalled, marvellous), and the one-offs (grey, stylise/stylised, programme, aluminium, mould,
  smoulder, storey, manoeuvre, draught, sulphur). Flag the **British** form only; respect word
  boundaries and skip proper nouns (e.g. a surname "Grey", "Greyhound").
- Each finding reports `file:line` and the word.

**Scan scope.** `src/main/**/*.java`, `src/test/**/*.java` (comments, identifiers, string literals,
test names) and the authored guides `docs/*.md`. **Exclude** (data/contract, not authored prose):
`**/resources/**` embedded data and external fixtures, `docs/OpenAPI.json` and `docs/api/**` (the
contract, the frozen baseline, external field names and dated changelog history), generated/vendored
code, and `build/`.

## The allowlist (the five never-correct cases — the guard must not flag these, or must let them be suppressed)

1. license files and copyright-holder names;
2. vendored/generated code and a dependency's own API;
3. direct quotations from an external source;
4. translations / foreign-language text;
5. **anything that crosses a wire or persists** — JSON keys, preferences keys, protocol fields,
   published catalog entries, **external API field names** (e.g. `Attribution.licence`, `licenceUrl`,
   a UK/EU source's own field spelled `centre`/`licence`).

Plus foreign/external **data** embedded in the repo (place names, parser-test payloads for
CAP/EMWIN/FDSN/STIX/GeoNet, etc.) — legitimate non-US text; never change it.

**Mechanism.** Make the allowlist explicit and reviewable: narrow **directory/glob exclusions** only
for genuine data/vendored/fixture areas, plus a **line-level suppression marker carrying a required
reason** (e.g. a trailing `// us-english: <external API field>`), or a central allowlist file keyed by
`path + term + reason`. Prefer a narrow reasoned suppression over a broad directory exclusion so real
violations in those areas still get caught.

## Fix the backlog

Run the guard, then for every finding either **fix it** (project-authored prose / code / comment /
identifier / test name / log string / UI string → US English) or **allowlist it with a reason** (it is
one of the never-correct cases above). **Bias:** when genuinely unsure whether text is
external-data/wire/quotation, **allowlist it** (never risk changing wire or data) and note it. Iterate
until the guard passes clean. The three known fixes are included: `stylised` and `licence` in the
`SubmarineCablesAdapter`/`GpsjamAdapter` **comments**, and the `"The map colour"` **label** in
`SubmarineCablesAdapter.java:173`.

## Do not touch

Any `CLAUDE.md` or `CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal`
changelog history in `docs/OpenAPI.json`; the meshdtn guard; `Dockerfile`, `scripts/smoke.sh`, and the
untracked prompt files.

## What runs

`./gradlew compileJava compileTestJava` **and your new `checkUsEnglish`** — you must run the guard
you're building to calibrate the allowlist and confirm it passes clean (it is a fast, offline text
lint; a guard can't be built without running it). Do **not** run the test suite, `lintOpenApi`, or
`checkApiCompat` — those remain for the owner's code-complete run per `000-Run-Sequence`.

## Done when

`./gradlew checkUsEnglish` passes clean and is wired into `check` so the build fails on any new British
spelling; the allowlist is explicit and reasoned (external-data / wire / quotation / license / vendored
only); every real violation (including the three) is fixed; compilation passes. The **completion report
lists counts** — violations fixed vs entries allowlisted, broken down by category — so the judgment
calls are reviewable.

**Commit.** `149: US-English guard + backlog fix`, plus the completion report. **Stage only the files
this prompt changes.**
