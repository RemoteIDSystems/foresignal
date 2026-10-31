# The Automated Newspaper

> **What this is.** The Automated Newspaper is Foresignal's personalized, scheduled, written-and-spoken
> edition system: a signed-in user follows the outlets and languages they care about, foreign news is
> translated as it arrives, and every morning (or on any cron they choose) an AI-written, AI-narrated
> edition is assembled for them and delivered by email, push, alert, a hosted web page and a podcast feed —
> with off-schedule "breaking" editions when a watched alert fires.
>
> **This document describes the code, not a plan.** Every claim below names the source file (and, where
> useful, the line) it was read from, against `info.version` **2.1.1** of `docs/OpenAPI.json`. If a line
> here and the code ever disagree, the code is right. Re-read the cited file before trusting a claim.

---

## 1. The two layers

The newspaper is built from two layers that already existed for intelligence work and were extended into a
consumer newspaper by the `NP-` campaign (migrations `V27`–`V33`):

| Layer | Package | Responsibility |
|---|---|---|
| **News intake** | `ai.foresignal.news`, `ai.foresignal.media.outlet`, `ai.foresignal.source.formats.news` | Pull articles, cluster them into stories, rate the outlets, analyze coverage balance, translate foreign news. |
| **Edition engine** | `ai.foresignal.briefing` | Turn a schedule + a scope into a stored, cited, narrated, delivered **edition** ("briefing" in the code and API). |

An "edition" is a `briefing` row (`src/main/java/ai/foresignal/briefing/BriefingEntity.java`). The API and the
database still use the word *briefing*; the user-facing word is *edition*. The self-service surface is spelled
`/v1/me/editions`, and the summary object is `EditionSummary` (schema in `docs/OpenAPI.json`).

The product idea maps onto the engine like this:

| Newspaper idea | Where it lives |
|---|---|
| Subscribe to outlets / languages | `user_news_subscription` (`V28`), `NewsSubscriptionService.java` |
| Auto-translate foreign news | `news_translation` (`V27`), `news/TranslationService.java`, `news/NewsTranslationJob.java` |
| AI-written, personalized edition | `briefing/LlmBriefingWriter.java`, `briefing/EditionResolver.java` |
| AI-narrated (spoken) | `briefing/BriefingAudioService.java`, `llm/TtsService.java`, `briefing/EditionVoice.java` |
| Delivered every morning | `briefing/BriefingScheduler.java` (cron), `briefing/EditionDelivery.java` |
| Delivered anywhere | `DeliveryTarget` → email / push / alert / website / podcast |
| A web page the user reads | `briefing/EditionSiteService.java` (`V30`), `GET /v1/editions/{id}/site` |
| Breaking-news editions | `briefing/BreakingNewsListener.java`, `briefing/BreakingTrigger.java` (`V31`) |
| "Why is this in my paper?" | `briefing/EditionProvenance.java` (`V32`) |
| Users own and run their own paper | `self.editions` permission (`V33`), `BriefingService.java` |

---

## 2. Data model (Flyway migrations `V27`–`V33`)

All seven migrations are **additive only** — an edition created before the feature keeps working unchanged.

- **`V27__news_translation.sql`** — `news_translation`: one row per `(object_type, object_id, target_language)`,
  `object_type` is `article` or `story`. Holds `translated_title`/`translated_excerpt`, a `source_hash`
  (SHA-256 of the source text, so unchanged content is never re-translated), a `status` of `ok`/`skipped`/`failed`
  (only `ok` is served), and `cost_usd` (summed per window for the translation budget). The original row is never
  touched.
- **`V28__edition_owner.sql`** — adds `briefing.owner_user_id` (FK to `"user"`, `ON DELETE CASCADE`; null for
  instance-token briefings) and `briefing.output_language`. Creates `user_news_subscription(user_id, type, value)`
  with `type ∈ {outlet, language}`.
- **`V29__edition_delivery.sql`** — adds `briefing.delivery_targets jsonb` and `briefing_run.delivery_results jsonb`.
- **`V30__edition_site.sql`** — adds `briefing.site_enabled` / `briefing.site_shareable` (both default false) and
  `edition_site_key(briefing_id, kind ∈ {feed, share}, …)` holding the **AES-256-GCM-encrypted** path key.
- **`V31__breaking_run.sql`** — adds `briefing.breaking_news jsonb`, `briefing.last_breaking_at` (cooldown across
  instances) and `briefing_run.breaking jsonb` (the alert that fired an off-schedule run).
- **`V32__edition_provenance.sql`** — adds `briefing.include_provenance` / `briefing.include_corroboration`
  (both default false).
- **`V33__self_editions.sql`** — seeds the `self.editions` permission into the fixed catalog and grants it to the
  **Admin** and **Editor** built-in roles.

---

## 3. News intake

### 3.1 Articles and stories

Articles arrive as kind `news-article` from two source families (`source/formats/news/package-info.java`):
`gdelt-articles`, and operator-run outlet feeds of the `news-feed-*` family mapped by the 073 feed kit
(`source/kit/feed/NewsArticleItemMapper.java:11`). **Articles are metadata only — no body and no byline is ever
stored** (`NewsArticleEntity.java`, "NEWS-2"). An article's id is `{sourceId}/{externalId}`.

`NewsStoryResolver` joins articles into a **story** by title-embedding cosine similarity, or, without embeddings,
by normalized-title token Jaccard; wire reprints collapse into one evidence group (`NewsGrouper`, see
`news/package-info.java`). A story is a 116 canonical event of family `news-story` with a deterministic title,
title-embedding centroid, topics, regions and coverage statistics (`NewsStoryEntity.java`).

**Retention:** article rows older than 90 days are deleted (`NewsCoverageJob.java:135`); the story aggregates are
kept (an intelligence table, never retention-deleted).

### 3.2 Outlets and their ratings

`OutletRegistry` (`media/outlet/`) keys outlets by domain; an unknown domain is added as `unrated`. Ratings come
from declared providers — `wikidata`, `partisan-web`, `allsides`, `mbfc`, `operator` — each with its license,
egress rules and an admin license-acknowledgement gate (`MediaProviders`, `RatingImportService`). **Ratings are
imported, never invented** (`media/outlet/package-info.java`, "NEWS-1"): every `media_rating` row names its
provider, license, attribution and date, and nothing in Foresignal computes a rating. Outlet factuality consensus
maps to a 109 reliability grade via `media.reliabilityMap` (`NewsReliability`).

### 3.3 Coverage analysis

`CoverageAnalyzer` (version pinned in-file) describes how a story/topic/situation is covered — article and
evidence-group counts by lean bucket (`left`/`center`/`right`/`unrated`), by country, outlet type and factuality
band; `coverageBalance`, `stateAffiliatedShare`, `blindspot` and `skewed` findings. **It counts evidence groups,
never raw articles** (30 reprints of one wire story = one group), and it **describes reporting, never truth** —
balance never raises or lowers a claim's credibility (`CoverageAnalyzer.java`, `news/package-info.java`).

### 3.4 Translation at ingest (`NP-002`)

`NewsTranslationJob` runs once a minute while translation is active: it takes articles/stories seen in the last
window with no current translation in each target language and hands them to `TranslationService`, newest first,
batched per language and tick (`news/NewsTranslationJob.java`). It runs **beside** the story resolver, not inside
it, so a slow LLM never holds up ingest — a new article is translated within about a minute.

`TranslationService` (`news/TranslationService.java`):

- **What:** an article's title and excerpt, a story's title — each only if `TranslationSettings.fields` names it.
  There is no body to translate (metadata only).
- **When:** only when the detected source language differs from the target and is at least
  `minSourceLanguageConfidence` certain; otherwise a `skipped` row records why, with no LLM call.
- **How:** the 026 chat role with purpose `TRANSLATE`, low effort, no tools, a constant system prompt that treats
  the text as data and keeps proper nouns. URLs/code are masked (`TranslationMask`) and the output is checked like
  the 141 notification translator.
- **Idempotent:** the `source_hash` means the same content in the same language is never translated twice.
- **Budget:** `TranslationSettings.budget` caps translation spend alone, inside the provider's own caps; cost is
  stored per row and summed per window. Once spent, objects are recorded `skipped` until the window resets.

Settings are served by `GET`/`PUT /v1/settings/translation` (permission `system.settings`), schema
`TranslationSettings`; the only engine is `llm` (`news/TranslationSettings.java`).

---

## 4. Anatomy of an edition

An edition is configured by `BriefingInput` (schema in `docs/OpenAPI.json`). The important fields:

| Field | Meaning |
|---|---|
| `name` | Display name (required). |
| `schedule` | 5-field cron (required). |
| `timezone` | IANA zone for the cron. |
| `enabled` | Whether the scheduler runs it. |
| `scope` | What the edition covers — see §5. |
| `lookback` | ISO-8601 duration or shorthand (`7d`, `15m`, `12h`) for the period. |
| `mode` | `digest` (deterministic) or `llm` (AI narrative). |
| `instructions` | Free-text steering for LLM mode (also drives a scope-filtered hybrid search). |
| `channelIds` | Notify channels this edition goes to (legacy path; see delivery §8). |
| `maxItemsPerKind` | Cap per record kind in the digest. |
| `audio` | Synthesize spoken audio after each run (§6). |
| `outputLanguage` | BCP 47 language the edition is written and spoken in (`NP-003`). |
| `deliveryTargets` | Where each run goes and in what shape (`NP-004`, §8). |
| `site` | `{enabled, shareable}` — the hosted web page (`NP-005`, §9). |
| `breakingNews` | Off-schedule breaking-edition config (`NP-006`, §7). |
| `includeProvenance` / `includeCorroboration` | Per-item "why / how-supported" annotations (`NP-007`, §6.3). |
| `ownerUserId` | The user who owns the edition (`2.1.1`); assignable only with `users.manage` (§7). |

---

## 5. Scope — what an edition covers

`BriefingScope` (`briefing/BriefingScope.java`) — empty lists mean "all":

- `sources`, `kinds` — source ids and record kinds.
- `bbox` — `minLon,minLat,maxLon,maxLat`.
- `ruleIds` — trigger rules whose alerts appear in the alert section (empty = all in scope).
- `includeHealth` — the source-health section (default true).
- `placeIds` / `viewId` — places whose areas bound the scope, or a saved view's predicate (prompt 036).
- `country` / `admin1` / `county` — ISO2, ISO 3166-2, US FIPS5 region filters (prompt 040).
- `places` / `weatherProducts` — the "Weather at places" section (prompt 056).
- `includeAlertQuality` — the weekly alert-quality section (prompt 115; default false).

For a **user-owned** edition the scope is narrowed further at run time (§7): sources the owner turned off are
hidden, and news is limited to the owner's subscriptions.

---

## 6. The run pipeline

### 6.1 Scheduling

`BriefingScheduler` (`briefing/BriefingScheduler.java`) ticks every `foresignal.briefing.poll-interval`
(**30 s**, `application.yml:393`). One transaction selects enabled briefings with `next_run_at <= now` and no run
in progress `FOR UPDATE SKIP LOCKED`, advances each one's `next_run_at` to its next fire time **after now**, marks
it running, and commits. Each claimed edition then runs on its own virtual thread. Because the next fire time is
computed from *now*, **runs missed while Foresignal was down run once and are never replayed**, and two schedulers
never claim the same edition. The scheduler is left out in edge mode (prompt 113).

### 6.2 Executing a run

`BriefingRunner` (`briefing/BriefingRunner.java`):

1. Write the `briefing_run` row as `running` in its own transaction (so state is visible while it runs and survives
   the thread).
2. Compute the **period** (previous run → now; a scheduled run ignores breaking runs when picking its previous
   period).
3. Compute the **digest statistics** (`DigestStatsService`, shared with `GET /v1/digest`).
4. Render the body: **`digest` mode** → deterministic markdown (`DigestRenderer`); **`llm` mode** → a cited
   narrative (`LlmBriefingWriter`), falling back to the digest on any failure.
5. If an `audio` option is set, synthesize the run (§6.2) before delivery.
6. Store the completed run, embed it, and publish/deliver (§8).

The run is completed in a second transaction; **notifications are published only after the completed run is
committed.** A failed run is stored `failed` and **not** delivered. A run's status is one of
`running` / `succeeded` / `fallback` / `failed` (`BriefingRun` schema).

**One run at a time:** a run claims `running_since` with a conditional update, so a second run of the same edition
(scheduler or manual, on any instance) is refused `409 BRIEFING_RUNNING` until the first finishes or the mark goes
stale (`BriefingService.java`). `runBriefing`/`runMyEdition` waits up to `run-timeout` (**120 s**,
`application.yml:395`) and returns the run; a run still going is returned `running` and finishes in the background.
Previews (`POST /v1/briefings/preview`) render **without** storing, delivering or touching the schedule.

### 6.2 LLM narrative

`LlmBriefingWriter` (`briefing/LlmBriefingWriter.java`): the context is the digest statistics, the notable records
in short form, and — when semantic search is available and the edition has `instructions` — a scope-filtered hybrid
search on those instructions. **Every record passes `EgressPolicy` for the chat provider first:** a private
source's records/counts/alerts never reach a non-local provider, and sources hidden from the edition's owner never
reach any provider. The call uses purpose `briefing`, effort `medium`, a constant system prompt
(`BriefingPrompts.SYSTEM`); the answer is checked by the 028 `CitationVerifier` and its warnings are kept. Any
failure (no provider, budget, provider error, refusal, empty answer) is a `Fallback` and the digest is rendered
instead.

### 6.2 Audio and voice (prompt 141 + `NP-003`)

After a run completes, an edition with an `audio` option is synthesized (`briefing/BriefingAudioService.java`):

- The plain-text rendition (`SpeechText.of`) is spoken through the governed `tts` role (`llm/TtsService.java`).
- Each request carries at most **4,000 characters**; longer text is split at paragraph/sentence boundaries and the
  MP3 parts concatenated (`Mp3Concat`), capped at **25 requests**. Opus (whose Ogg streams don't concatenate) is
  truncated with a spoken note.
- The MP3 is stored in the 043 media store with `role=audio`, expires with the briefing-run retention, is linked
  from the run's `audio.mediaId`, and is signed with a 109 detached JWS (`audio.signature`).
- Outcome is `BriefingRun.audio` = `ok` / `failed` / `skipped`. **Nothing here throws:** an audio failure never
  blocks the run or its text delivery.

`TtsService` reserves all chunks' characters up front with `TtsBudget` (a long edition is refused whole rather than
cut off), then spends one request/minute from the budget governor; the text is never logged. Nothing runs without a
`tts` provider — an `openai-compatible` endpoint that is OpenAI itself or a local server (Kokoro-FastAPI,
openedai-speech).

`EditionVoice` (`briefing/EditionVoice.java`) picks a voice that **speaks the edition's language**: Kokoro voice
names encode language in the first letter and gender in the second (`af_bella` = American-English female); if the
configured voice speaks another language, the same-gender voice for the edition's language is used; a language
Kokoro has no voice for keeps the configured one; OpenAI voices (`alloy`, which speak any language) are kept.

### 6.3 Provenance and corroboration (`NP-007`)

`EditionProvenance` (`briefing/EditionProvenance.java`) annotates a run's citation maps when the flags are on
(neither member is added while its flag is off, so other editions' citations are unchanged):

- **`provenance`** — the record's `record:` ref plus the `canonical-event:` refs its observation links point to
  (and an `alert:` ref for alert citations), with a one-line `reason` ("why included").
- **`corroboration`** — `independentSources` (the larger of the record's corroboration cluster and its canonical
  events' independent evidence groups), the Admiralty `credibility`/`reliability` grade, and an `evidenceQuality`
  of `strong` / `moderate` / `weak` / `insufficient` following the 154 corroboration policy.

Only record and alert citations are annotated; briefing and weather citations are left alone.

---

## 7. Per-user editions, ownership and self-service

### 7.1 Who owns an edition

`BriefingService` (`briefing/BriefingService.java`): a briefing created by a signed-in user, or by that user's own
API token, becomes **that user's edition** (`owner_user_id`). A briefing created with an **instance** (non-user)
token has a null owner and behaves exactly as before (the operator's shared briefing). **Only an edition's owner
sees, runs, changes or deletes it**, plus an instance token as the operator's credential; any other user or an
anonymous caller gets `404` — indistinguishable from an edition that does not exist (`visibleTo(...)`).

### 7.2 Assignment (`NP-008`, `2.1.1`)

On `createBriefing` / `updateBriefing`, a caller holding **`users.manage`** may set `ownerUserId` to stand an
edition up on another user's behalf. Otherwise the owner is the creating user (create) or unchanged (update). The
`BriefingInput.ownerUserId` field is writable; the response `Briefing.ownerUserId` is read-only.

### 7.3 Self-service surface

The `/v1/me/editions` operations (`EditionsController`) let a normal signed-in user own and manage their own
edition **without agent scope**, gated by the `self.editions` permission (`Permissions.java:34`, granted to Admin +
Editor in `V33`):

| Operation | Method / path |
|---|---|
| `listMyEditions` | `GET /v1/me/editions` |
| `createMyEdition` | `POST /v1/me/editions` |
| `updateMyEdition` | `PUT /v1/me/editions/{briefingId}` |
| `deleteMyEdition` | `DELETE /v1/me/editions/{briefingId}` |
| `runMyEdition` | `POST /v1/me/editions/{briefingId}/run` |

These reuse `BriefingService`, **forcing the owner to the caller**. Each returns `EditionSummary`
(name, schedule, `nextRunAt`/`lastRunAt`/`lastRunStatus`, `latestRunId`, `siteUrl`).

### 7.4 How an owned edition is personalized at run time

`EditionResolver` → `EditionContext` (`briefing/EditionResolver.java`, `briefing/EditionContext.java`):

- **Language** — the edition's `outputLanguage`, else the owner's `UserPreferences.locale`, else the source
  language. The LLM writer is told to write in it; a digest is translated after rendering (English kept with a
  warning if that fails); audio is spoken by a voice of that language (§6.2).
- **Data** — an owned edition **follows the owner's data**: sources the owner turned off (`UserSourceViewService`,
  R2-010) are counted but never listed, exactly like private sources.
- **News** — limited to the owner's **news subscriptions**: with ≥1 `outlet` entry, only those outlets; with ≥1
  `language` entry, only news in (or translated into) those languages (`NewsSubscriptionService.java`,
  `self.sources`, `GET`/`PUT /v1/me/news-subscriptions`).
- **Top stories** — a `topStories` section (`EditionResolver.java:42`, `EditionNews`) lists the period's top
  stories, titled in the edition's language where `NP-002` translated them. It reaches the digest, the LLM writer
  and the audio like any other section.
- **Delivery** — an owned edition that lists no channels goes to the **owner's personal channels**, never to every
  channel subscribed to `briefing` (which would hand one user's edition to everyone).

---

## 8. Delivery — channels and formats (`NP-004`)

`DeliveryTarget` (schema + `briefing/EditionDelivery.java`) — each target is `{kind, channelId, format, options}`:

- **`kind: channel`** — send through an 018 notify channel (`channelId` required). Formats:
  - **`email`** — the whole edition: the sanitized HTML page is the body, the markdown is the text part, the run's
    audio is attached, and a "Read online" link points to the hosted site.
  - **`push`** — headline + a summary of at most **280 characters** (`EditionDelivery.java:59`) + the site link;
    **never** the whole body, the audio, the citations or the statistics.
  - **`alert`** — an `alert` notification, subtype `edition.alert` (`EditionDelivery.java:62`), with severity
    (`info`/`warning`/`critical`; defaults to the firing alert's severity for a breaking run, else `info`),
    headline, summary and link. It is not an alert-lifecycle event, so no enrichment wait or rule grouping applies.
- **`kind: website`** and **`kind: podcast`** are **pulled, not pushed**: the result just records the URL where the
  run is available (`getEditionSite`, `getBriefingPodcast`).

Each channel target is published on its own, to that channel alone, so the channel's **rate limit, quiet hours,
backlog cap and the notification pause apply exactly as to any send**. An owned edition may use only its owner's
personal channels (and the global ones if the owner holds `channels.global`); any other channel is skipped. **One
target failing never stops the others**; every target's outcome is stored on `BriefingRun.deliveryResults`.

With no `deliveryTargets`, an edition delivers as before: to its `channelIds`, else every channel subscribed to
`briefing` (an *instance* edition), or the owner's personal channels (an *owned* edition).

---

## 9. The hosted edition site (`NP-005`)

`EditionSiteService` (`briefing/EditionSiteService.java`) — the "a website the user reads" piece. The page is the
latest `succeeded` or `fallback` run, rendered as a standalone sanitized HTML page (`BriefingHtmlRenderer`, §12).

- **Canonical URL:** `GET /v1/editions/{briefingId}/site` (`getEditionSite`, permission `intel.read`).
- **Feed-key URL:** a 035 `fsf_` key whose only prefix is `/v1/editions/{id}/site` (read scope, no private grant),
  reachable at `/v1/k/{key}/editions/{id}/site` and nothing else — for a browser without headers.
- **Share URL:** a 112 `fss_` share link (`/v1/k/{key}/site`), kept while `site.shareable` is on.
- Keys are minted when the edition is saved with its site on and renewed after a run; the plaintext keys are stored
  **AES-256-GCM-encrypted** (`CredentialCipher`) in `edition_site_key`, because `EditionSite` returns the URLs on
  every read. Turning the site off revokes both keys; turning sharing off revokes the share link; deleting the
  edition revokes both. A key revoked from outside (an operator's `revokeShareLink`/`revokeFeedKey`) **stays
  revoked** — its URL reads null until the owner toggles the setting off and on.

The `EditionSite` object returns `url`, `feedKeyUrl`, `shareUrl`, `latestRunId` and `updatedAt`.

---

## 10. Breaking-news editions (`NP-006`)

`BreakingNewsListener` (`briefing/BreakingNewsListener.java`) listens to the `AlertBus`. An alert fires an edition
off-schedule when **all** hold:

1. it is a new alert (`fired`), not suppressed by a snooze or an exercise replay;
2. the edition is enabled and `breakingNews.enabled` is true;
3. the alert's rule is in `breakingNews.ruleIds` (or, when empty, the scope's `ruleIds`);
4. the severity is at or above `breakingNews.minSeverity`;
5. for an owned edition, **the rule is shared or the owner's own** — one user's alert never fires another user's
   edition.

The `cooldownMinutes` is claimed with one conditional update (via `briefing.last_breaking_at`), so a burst of
alerts — on one instance or several — **coalesces into one breaking run per window**. A breaking run stores its
trigger as `BriefingRun.breaking` (`BreakingTrigger`: alert id, rule id/name, severity, firedAt, message); its
heading/title say "Breaking", its markdown leads with the alert, the LLM is told to lead with it, and its
notifications carry the alert's severity. Its period runs from the previous run of any kind, and a later scheduled
run ignores it when choosing its own period. Left out in edge mode.

---

## 11. Feeds and podcast

Besides HTML and the delivery channels, each edition's runs are published as standard feeds (all under the
`Briefings` tag):

| Feed | Operation / path | Permission |
|---|---|---|
| Atom | `getBriefingRunsAtom` — `GET /v1/briefings/{id}/runs/feed.atom` | `data.read` |
| JSON Feed | `getBriefingRunsJsonFeed` — `GET /v1/briefings/{id}/runs/feed.json` | `data.read` |
| **Podcast (RSS 2.0)** | `getBriefingPodcast` — `GET /v1/briefings/{id}/podcast.rss` | `intel.read` |

The podcast feed (`api/BriefingPodcastController.java`) lists the runs that have synthesized audio, newest first,
each with an `<enclosure>` to its 043 media. Podcast apps without headers use a 035 feed key
(`/v1/k/{key}/briefings/{id}/podcast.rss`), whose prefixes should also cover `/v1/media/` so the enclosure URLs
carry the same key. Conditional GET is supported (strong ETag + `Last-Modified`, answered `304`).

---

## 12. Security and privacy invariants

These are enforced in code, not policy:

- **Egress on the narrative.** Every record handed to the LLM writer passes `EgressPolicy` for the chat provider;
  a private source's data never leaves to a non-local provider, and a source hidden from the owner never reaches
  any provider (`LlmBriefingWriter.java`).
- **Metadata-only news.** No article body or byline is ever stored (`NewsArticleEntity.java`,
  `source/formats/news/package-info.java`), so there is nothing sensitive to translate or narrate beyond titles and
  feed summaries.
- **Ratings are imported, never invented** (`media/outlet/package-info.java`); **coverage describes reporting,
  never truth** (`news/package-info.java`).
- **Inert HTML.** The hosted page and the email body are built by `BriefingHtmlRenderer`
  (`briefing/BriefingHtmlRenderer.java`): CommonMark escapes raw HTML and drops unsafe links, then an OWASP
  allowlist keeps only headings/paragraphs/lists/tables/links/code/quotes/rules — no script, event handler, `style`
  attribute or iframe survives — inside a page whose only asset is an inline stylesheet, permitted by a strict CSP.
- **Signed runs.** A stored run carries a detached Ed25519 JWS over its markdown bytes (`BriefingRun.signature`);
  synthesized audio is signed too (`BriefingRun.audio.signature`).
- **Owner isolation.** A non-owner sees `404`, not `403` (`BriefingService.visibleTo`); a user's alert can't fire
  another user's breaking edition; an owned edition can't deliver to channels the owner doesn't hold.
- **Translation treats text as data** — a constant system prompt, URLs/code masked, output checked
  (`TranslationService.java`).

---

## 13. Configuration surface (who sets what)

| What | Operation | Permission | Who |
|---|---|---|---|
| Which sources the instance collects (incl. news feeds) | source/collection ops | `collection.manage` | Operator / Admin |
| Outlet ratings & merges | `updateMediaOutlet` — `PATCH /v1/media/outlets/{id}` | `system.settings` | Admin |
| News translation policy | `updateTranslationSettings` — `PUT /v1/settings/translation` | `system.settings` | Admin |
| Create/assign an edition for a user | `createBriefing` / `updateBriefing` (+ `ownerUserId`) | agent scope; `users.manage` to assign | Operator / Admin |
| A user's own edition | `/v1/me/editions/*` | `self.editions` | Any signed-in user (Admin, Editor) |
| A user's outlet/language follows | `updateMyNewsSubscriptions` — `PUT /v1/me/news-subscriptions` | `self.sources` | The user |

The Admin app (`foresignal-admin`, `NP-001`…`NP-005`) is the UI over this surface — selecting news sources,
acknowledging rating-provider licenses and editing outlet ratings, setting the translation policy, and standing up
or assigning editions on a user's behalf. The scheduler mechanics (`foresignal.briefing.*`: `poll-interval` 30 s,
`run-timeout` 120 s, `stale-run`, `batch-size`) are process config, **not** operator settings
(`briefing/BriefingProperties.java`, `application.yml:393`).

---

## 14. Retention and lifecycle

- **News articles:** 90-day `bulk-event` retention (`NewsCoverageJob.java:135`); story aggregates are kept.
- **Translations:** an article's translation rows are deleted with the article; a story's stay with the story
  (`V27` comment, `NewsTranslationStore`).
- **Runs & audio:** `briefing_run` rows and their audio media expire with the briefing-run retention
  (`BriefingRetentionStep`, `BriefingAudioService.java`).
- **Owned editions:** deleted with their user (`briefing.owner_user_id … ON DELETE CASCADE`, `V28`);
  `user_news_subscription` and `edition_site_key` cascade too.

---

## 15. API reference (version 2.1.1)

All operations are `implemented` in `docs/OpenAPI.json`.

| Operation | Method / path | Permission |
|---|---|---|
| `listBriefings` | `GET /v1/briefings` | `intel.read` |
| `createBriefing` | `POST /v1/briefings` | agent scope |
| `previewBriefing` | `POST /v1/briefings/preview` | agent scope |
| `getBriefing` | `GET /v1/briefings/{id}` | `intel.read` |
| `updateBriefing` | `PUT /v1/briefings/{id}` | agent scope |
| `deleteBriefing` | `DELETE /v1/briefings/{id}` | agent scope |
| `runBriefing` | `POST /v1/briefings/{id}/run` | agent scope |
| `listBriefingRuns` | `GET /v1/briefings/{id}/runs` | `intel.read` |
| `getLatestBriefingRun` | `GET /v1/briefings/{id}/runs/latest` | `intel.read` |
| `getBriefingRun` | `GET /v1/briefings/{id}/runs/{runId}` | `intel.read` |
| `getBriefingRunsAtom` | `GET /v1/briefings/{id}/runs/feed.atom` | `data.read` |
| `getBriefingRunsJsonFeed` | `GET /v1/briefings/{id}/runs/feed.json` | `data.read` |
| `getBriefingPodcast` | `GET /v1/briefings/{id}/podcast.rss` | `intel.read` |
| `getDigest` | `GET /v1/digest` | `intel.read` |
| `getEditionSite` | `GET /v1/editions/{id}/site` | `intel.read` |
| `listMyEditions` | `GET /v1/me/editions` | `self.editions` |
| `createMyEdition` | `POST /v1/me/editions` | `self.editions` |
| `updateMyEdition` | `PUT /v1/me/editions/{id}` | `self.editions` |
| `deleteMyEdition` | `DELETE /v1/me/editions/{id}` | `self.editions` |
| `runMyEdition` | `POST /v1/me/editions/{id}/run` | `self.editions` |
| `getMyNewsSubscriptions` | `GET /v1/me/news-subscriptions` | `self.sources` |
| `updateMyNewsSubscriptions` | `PUT /v1/me/news-subscriptions` | `self.sources` |
| `listMediaOutlets` | `GET /v1/media/outlets` | `intel.read` |
| `getMediaOutlet` | `GET /v1/media/outlets/{id}` | `intel.read` |
| `updateMediaOutlet` | `PATCH /v1/media/outlets/{id}` | `system.settings` |
| `listNewsArticles` | `GET /v1/news/articles` | `intel.read` |
| `listNewsStories` | `GET /v1/news/stories` | `intel.read` |
| `getNewsStory` | `GET /v1/news/stories/{id}` | `intel.read` |
| `getTranslationSettings` | `GET /v1/settings/translation` | `system.settings` |
| `updateTranslationSettings` | `PUT /v1/settings/translation` | `system.settings` |

> The agent-scope operations show no `x-foresignal-permission` because they are gated by token scope, not an RBAC
> permission key; verify each against `docs/OpenAPI.json` and `security/AccessPolicy.java`.

---

## 16. Source map

| Package / file | Responsibility |
|---|---|
| `ai/foresignal/briefing/` | The edition engine: scheduling, run pipeline, scope, modes, sections, audio, provenance, delivery, hosted site, breaking editions, ownership. |
| `ai/foresignal/news/` | Articles, stories, subscriptions, translation, coverage. |
| `ai/foresignal/media/outlet/` | Outlet registry and imported ratings. |
| `ai/foresignal/source/formats/news/`, `source/kit/feed/` | The `news-article` record shape and the `news-feed-*` feed-kit mapper. |
| `ai/foresignal/llm/TtsService.java`, `TtsBudget.java` | The governed `tts` role for narration. |
| `ai/foresignal/api/` | `BriefingsController`, `EditionsController`, `EditionSitesController`, `BriefingPodcastController`, `NewsController`, `NewsSubscriptionsController`, `MediaOutletsController`, `TranslationSettingsController`, `DigestController`. |
| `src/main/resources/db/migration/V27…V33` | The data model (§2). |

---

*Written against the working tree on 2026-10-30; `docs/OpenAPI.json` `info.version` 2.1.1. Re-run the greps and
re-read the cited files before relying on any line here — the code is the source of truth.*
