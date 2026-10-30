# NP-003 — Per-user editions (written & spoken in the user's language)

**Context.** Automated-newspaper feature, after **NP-001** (contract) and **NP-002** (translation).
Editions are **user-owned briefings**: the briefing engine already writes (`LlmBriefingWriter`),
speaks (`BriefingAudioService`/`TtsService`), schedules (`BriefingScheduler`, cron) and sections
(`BriefingSection`). Today a briefing is owned by a token + scope (`BriefingEntity.owner_token_id`/
`owner_scope`); the v2.0 user identity (R2-001/R2-003) and per-user source views / preferences exist.

**Goal.** Bind an edition to a v2.0 **user**, scope it to that user's saved view / per-user source
view / preferences, and write **and speak it in the user's language** (`outputLanguage`) — the
deliver-in-the-user's-language extra. A user gets their own auto-scoped daily edition.

## Operations

Implement `listMyEditions` (`GET /v1/me/editions`, self) — the caller's editions. Edition CRUD reuses
the existing briefing endpoints under user ownership (note any gap as an owner question).

## Read first

- `briefing/` — `BriefingEntity`, `BriefingService`, `BriefingRunner`, `BriefingScope`,
  `BriefingInput`, `LlmBriefingWriter`, `BriefingPrompts`, `BriefingAudioService`.
- The v2.0 identity + per-user model: the `user` entity, `security/AccessPolicy.java`, the per-user
  source view and `UserPreferences` (R2-010 of the backend set / the `me/*` endpoints).

## Build

1. `BriefingEntity` gains `owner_user_id` (beside the existing owner token/scope). `BriefingService`
   resolves a user-owned edition's effective `BriefingScope` from the user's saved view + per-user
   source view + region/preferences, so "my edition" follows "my data" automatically.
2. `outputLanguage` (NP-001 field; default the user's `UserPreferences.locale`): `LlmBriefingWriter`/
   `BriefingPrompts` write the edition in that language, and `BriefingAudioService`/`TtsService`
   synthesize in it (pick a matching voice). Where source items were translated (NP-002), the edition
   consumes the translated text so a foreign story reads natively.
3. `listMyEditions` returns only the caller's editions; ownership is enforced in `AccessPolicy`/the
   service (a user sees and runs only their own).

**Migration.** Next free additive Flyway `V{n}__edition_owner.sql`: `owner_user_id` on the briefing
table (nullable; existing token/scope briefings keep working). Additive only.

## Tests to write (do not run)

- `EditionOwnershipIT` — a user-owned edition is scoped to that user's view; `listMyEditions` returns
  only the caller's; another user cannot see or run it.
- `EditionLanguageTest` — the writer and audio produce `outputLanguage`; a translated source item is
  rendered in the user's language.

## What runs

Compile only — `./gradlew compileJava compileTestJava`; run `./gradlew checkUsEnglish`. Write tests,
do not run. US English in code/identifiers (edition *content* may be any language).

## Done when

A user owns editions scoped to their own data, written and spoken in their language, listed via
`listMyEditions` and isolated from other users; compile and `checkUsEnglish` green.

## Do not touch

`CLAUDE.md`/`CONVENTIONS.md`; frozen v1.0.0 spec; `x-foresignal` changelog; `.gitlab-ci.yml`; other
prompts' files. Leave NP-001 ops `planned`. **Stage only this prompt's files.**

## Commit

`NP-003: Per-user editions`, then `completion-reports/NP-003-CR-Per-User-Editions.md` with the
40-character implementation hash.
