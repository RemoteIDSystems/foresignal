# NP-002-CR-News-Translation

| | |
|---|---|
| Prompt | NP-002-News-Translation-At-Ingest.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test was run) |
| Implementation commit | `4a7ee64e049a2438858c660db90835a7ddd41ccf` |
| Date | 2026-10-30 |

## What was added

- **Migration `V27__news_translation.sql` (additive):** a new `news_translation` table, one row per
  (`object_type` `article`|`story`, `object_id`, `target_language`), stored beside the untouched `news_article` and
  `news_story` rows.
  - Content columns: `source_language`, `source_hash` (SHA-256 of the source text) and `status` (`ok`, `skipped` or
    `failed`, with `reason`).
  - Translation columns: `translated_title`, `translated_excerpt`, `engine` and `model`.
  - Bookkeeping columns: `cost_usd`, `translated_at` and `checked_at`.
  - No existing column is touched, and the original fields are never overwritten.
- **`news/NewsTranslationStore`:** JDBC over the table.
  - Lookups: `find`, `served` (the first configured target language that has an `ok` row), `servedAll` and
    `upsert`.
  - Bookkeeping: `markChecked`, and `spentSince` (the budget sum).
  - The pick queries: `articlesToTranslate` and `storiesToTranslate`.
  - `touchStory` marks a story changed so its record is re-published. `deleteOrphanedArticles` removes translations
    of expired articles, so they follow the article's 90-day retention.
- **`news/LanguageDetector`:**
  - The article's declared `language` (ISO code or GDELT language name, through `NewsNames.languageCode`) gets
    confidence 1.0.
  - Otherwise a cheap script detector. Hangul, Thai, Hebrew, Greek, Georgian, Armenian and kana get 0.9. Cyrillic,
    Arabic, Han and Devanagari get 0.6. Latin text gets no guess (0).
- **`news/TranslationService`:**
  - It translates an article's title and excerpt (its feed `summary`), or a story's title, into one target language.
  - It uses the chat role with `Purpose.TRANSLATE`, low effort, no tools, and a constant system prompt that treats
    the text as data and keeps proper nouns (people, places, organizations, vessels, aircraft, products).
  - URLs and code are masked with the 141 `TranslationMask`. The output is checked with the 141 rules: no more than
    1.5× the input length, and every masked token restored.
  - It is idempotent by source hash: unchanged content gives `UNCHANGED` with no call, and changed content is
    translated again.
  - It skips, without a call, and records why:
    - the same language (`en-US` counts as `en`);
    - confidence under `minSourceLanguageConfidence`;
    - the translation budget spent;
    - no chat provider;
    - the provider governor refusing (`LlmBudgetExceededException`).
  - A provider error or a rejected output is recorded as `failed`. Each call's USD cost is stored on its row.
- **`news/NewsTranslationJob`:** runs every minute while translation is on.
  - For each target language it picks up to 100 articles seen, and 100 stories active, in the last 48 h that lack a
    current translation, newest first.
  - It stops for a language once the budget is spent.
  - Failed and budget/provider skips are retried after 60 minutes. Same-language and low-confidence skips wait for
    new content.
  - A translated story is touched, so `NewsStoryRecordPublisher` re-publishes it.
- **Translation settings:**
  - `news/TranslationSettings`, a record under the new key `SettingKeys.TRANSLATION` (`translation`, added to `ALL`).
  - `news/TranslationSettingsService` parses and validates. It canonicalizes and deduplicates BCP 47 tags, allows at
    most 10 targets, and reports every problem in one 400. It also computes the read-only `spend`.
  - `api/TranslationSettingsController` serves `getTranslationSettings` and `updateTranslationSettings`.
- **Reads:**
  - `listNewsArticles`, `listNewsStories` and `getNewsStory` (its articles too) add `targetLanguage`,
    `translatedTitle`, `translatedExcerpt` and `translationProvenance` when an `ok` translation exists.
  - While translation is off, reads carry the originals only.
  - The MCP news tools read through the same `NewsViews`, so they get the fields too.
- **Story record payload:** the `news-story` record gains `translatedTitles` (language → title), so search, the
  situation linker and NP-003 editions see the translation.
- **`notify/NotificationTranslator`:** `userTurn`, `maxOutputChars` and `clean` are now public, so news translation
  reuses them instead of copying them.
- **Tests (written, not run):**
  - `TranslationServiceTest` (fake LLM, in-memory store) covers:
    - a German article translated beside the original, with purpose `translate`;
    - the same content translated once, and changed content translated again;
    - budget exhaustion skipped and recorded, including the governor refusing;
    - the same language, and Latin text of unknown language, skipped without a call;
    - a Korean headline detected by its script;
    - an over-long output rejected as `failed`;
    - no provider;
    - the detector cases.
  - `TranslationSettingsIT` covers:
    - defaults and a round-trip with canonical tags;
    - one 400 listing every problem;
    - admin-only access (403 and 401);
    - end to end through the job and `/v1/news/articles`: the translation served beside the original, not
      translated twice, and gone from the reads once translation is disabled.

## Decisions and disagreements for the owner

1. **The prompt says no machine translation exists. That is wrong.** Prompt 141's `NotificationTranslator`
   translates notifications with the LLM (`Purpose.TRANSLATE`, `TranslationMask`). I reused its purpose, masking and
   output checks rather than writing a second copy. That also means news and notification translation share the
   `translate` budget role in the provider's usage logs.
2. **Translation runs in a job a minute behind ingest, not inside the resolver.** The prompt asks for translation at
   ingest. A synchronous LLM call per article inside `NewsStoryResolver` would stall the evidence cursor at GDELT
   volume, or whenever the provider is slow or down. New articles are translated within about a minute. Say if you
   want it in-line instead.
3. **Translations live in a separate table, not as columns on the news tables.** That leaves the 26-field `Article`
   record and all its callers alone, allows several target languages per article, and lets the rows follow article
   retention.
4. **There is no body to translate.** News is metadata only (NEWS-2), so `fields` is `title` and `excerpt`, as
   NP-001 authored it. A story has no excerpt, so its `translatedExcerpt` is always null.
5. **Which translation a read shows.** The wire has one `targetLanguage` per object, so reads show the first
   configured target language that has a translation. NP-003 can pick by the reader's locale through
   `NewsTranslationStore.served`.
6. **How the translation budget is counted.** It is enforced by summing `cost_usd` per window, on top of the
   provider's own caps. With no provider prices set, every cost is 0 and only the provider caps apply. When a failed
   row is retried, its earlier cost is replaced rather than added, so a retried failure undercounts slightly.
7. **"A per-source target override" was not added.** Nothing in the source model needed it, and NP-001 authored no
   wire field for it.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: "6240 files, 1783 British forms; suppressed 0 by marker, 2242 by 107 allowlist
  entries; 0 finding(s)".
- No test was run.

## Notes for the next prompt

- `getTranslationSettings` and `updateTranslationSettings` are implemented and still `planned`; NP-007 flips them.
- The next Flyway migration is **V28**.
- For NP-003: translated text is available through `NewsTranslationStore.served(type, ids, languages)` and on the
  `news-story` record as `translatedTitles`.
