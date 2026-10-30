# NP-001 — Automated Newspaper: OpenAPI 2.1.0 contract

**Context.** v2.0 (R2) has shipped (cutover R2-027). This is the first prompt of the **automated
newspaper** feature — a per-user, personalized edition built by **extending the existing Briefing
engine** (`ai.foresignal.briefing`), not a parallel subsystem. That engine already gives us the LLM
writer (`LlmBriefingWriter`), TTS audio (`BriefingAudioService`/`TtsService`), the cron scheduler
(`BriefingScheduler`), HTML render (`BriefingHtmlRenderer`), podcast RSS (`BriefingPodcastController`),
sections (`BriefingSection`) and channel delivery (`notify/`). News is already ingested worldwide and
cross-linked (`ai.foresignal.news`, GDELT adapters). **This prompt authors the contract only; the
service and admin follow.** Owner-confirmed scope: extend briefings into user editions, plus four
extras — deliver in the user's language, breaking-news special editions, a shareable public edition
URL, and provenance + corroboration badges.

**Goal.** Author the 2.1.0 contract surface into `docs/OpenAPI.json` (the only contract) for: news
translation, per-user editions, per-user outlet/language subscriptions, output targets
(email/push/website/alerts), a hosted + shareable edition site, breaking-news triggers, and
provenance/corroboration on edition content. Bump the release version everywhere. No business logic.

## Operations (authored, status `planned`, `x-foresignal-prompt: NP-001`)

| operationId | method + path | scope |
|---|---|---|
| `getTranslationSettings` | `GET /v1/settings/translation` | admin (`system.settings`) |
| `updateTranslationSettings` | `PUT /v1/settings/translation` | admin (`system.settings`) |
| `getMyNewsSubscriptions` | `GET /v1/me/news-subscriptions` | read (self, `self.sources`) |
| `updateMyNewsSubscriptions` | `PUT /v1/me/news-subscriptions` | read (self, `self.sources`) |
| `listMyEditions` | `GET /v1/me/editions` | read (self) |
| `getEditionSite` | `GET /v1/editions/{briefingId}/site` | read (open while `security.openReads`; feed-key + share-link reachable) |

Schemas authored: `TranslationSettings`, `TranslationSettingsInput`, `NewsSubscription`,
`NewsSubscriptionView`, `NewsSubscriptionUpdate`, `EditionSummary`, `EditionSite`.

**Schema extensions** (additive fields on existing briefing/news schemas — editions ARE briefings):
- `BriefingInput`/`Briefing`: `ownerUserId` (NP-003 binds editions to a v2.0 user), `outputLanguage`
  (BCP 47; the language the edition is written and spoken in — NP-003), `deliveryTargets` (the
  output targets below — NP-004), `site` (hosting/share config — NP-005), `breakingNews`
  (trigger config — NP-006), `includeProvenance` and `includeCorroboration` (NP-007).
- `DeliveryTarget` (new): `kind` (`channel` | `website` | `podcast`), `channelId` (when `channel`),
  `format` (`email` | `push` | `alert` | `html` | `audio`), and per-kind options. Reuses `notify`
  channels; `website`/`podcast` map to the hosted render + RSS.
- `BreakingNewsConfig` (new): `enabled`, `ruleIds`, `minSeverity`, `cooldownMinutes`.
- `BriefingSection`/edition item schemas: optional `provenance` (cross-linked evidence refs) and
  `corroboration` (the `IntelCorroboration`/calibration signal) per item (NP-007).
- `NewsArticle`/`NewsStory`: `targetLanguage`, `translatedTitle`, `translatedExcerpt`,
  `translationProvenance` (engine, model, at) — original fields are never overwritten (NP-002).

## Read first

- `docs/OpenAPI.json` (the authored contract) and how existing briefing ops are authored there
  (`listBriefings`/`createBriefing`/`runBriefing`/`getBriefingPodcast`, `BriefingScope`,
  `BriefingInput`). `build.gradle.kts` lines on the contract + `scripts/generate-postman.py`.
- `security/AccessPolicy.java` (every new op needs an entry — the sync guard enforces it) and how
  `self.*`/`system.settings`/open-read ops are classified today.
- `briefing/`, `news/`, `notify/ChannelType.java`, `security` share-links (`ShareLinkService`) and
  feed keys (prompt 035, `/v1/k/{key}/…`) — so the authored shapes match what will implement them.
- `src/test/java/ai/foresignal/system/ApiVersionTest.java` (the version-agreement guard).

## Build

1. **Author the operations and schemas above** into `docs/OpenAPI.json`, each op
   `x-foresignal-status: planned` + `x-foresignal-prompt: NP-001`, consistent with the existing
   style (ProblemDetail errors, cursor paging, `operationId`s). Where an existing briefing schema is
   extended, add the fields as **optional/additive** so 2.0.0 clients stay valid. Anything the
   existing schemas do not already express, raise as an owner question — do not invent wire shapes.
2. **Add `AccessPolicy` entries** for the six new ops with the scopes in the table (the sync guard
   fails otherwise); `getEditionSite` is a `read` op reachable under open reads / feed key /
   share-link like the podcast.
3. **Bump the release version to `2.1.0` in all three places** the guard checks:
   `docs/OpenAPI.json` `info.version`, `ApiVersionTest.RELEASE`, and the Gradle project `version`.
   Leave the frozen `docs/api/v1.0.0/OpenAPI.json` untouched.
4. **Regenerate Postman** (`python3 scripts/generate-postman.py`). **Do not regenerate the SDKs** and
   **do not flip** `x-foresignal-status` — the final prompt of this set (NP-007) activates and
   regenerates, as R2-027 did for the cutover.

## Contract

`docs/OpenAPI.json` is the only contract. Ops authored `planned`. No new runtime dependency here.

## Tests to write (do not run)

- Extend the spec/version guard test(s) so `2.1.0` agreement is asserted (authored spec, `/v1/system`
  apiVersion, Gradle version).
- A contract test that each new `operationId` has an `AccessPolicy` entry (or extend the existing
  `AccessPolicySyncTest`).

## What runs

Compile only — `./gradlew compileJava compileTestJava`. **Do** run `./gradlew checkUsEnglish`. Do
**not** run the suite, `lintOpenApi`, `checkApiCompat` or SDK generation. US English — note that any
BCP 47 language codes and translated *content* are data, not identifiers, and wire keys stay as
authored.

## Done when

`docs/OpenAPI.json` is `2.1.0` with the six ops (`planned`) and the additive schema extensions, every
new op has an `AccessPolicy` entry, the version agrees in all three places, Postman is regenerated,
compile passes and `checkUsEnglish` is green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; `.gitlab-ci.yml`; other
prompts' files; the SDKs. **Stage only this prompt's files.**

## Commit

`NP-001: Automated Newspaper OpenAPI 2.1.0 contract`, then
`completion-reports/NP-001-CR-Newspaper-Contract.md` with the 40-character implementation hash and
any owner questions (e.g. whether editions get their own CRUD or reuse the briefing endpoints under
user ownership).
