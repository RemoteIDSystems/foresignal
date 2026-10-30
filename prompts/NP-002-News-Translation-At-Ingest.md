# NP-002 — News translation at ingest

**Context.** Automated-newspaper feature, after the **NP-001** contract. News is already ingested
worldwide and cross-linked (`ai.foresignal.news`: `NewsArticleEntity` with a `language` column,
`NewsStoryStore`, `NewsStoryRecordPublisher`; GDELT adapters) — but there is **no machine
translation** today (verified: the only "translate" code is geometry/NAT64/transit-variant
selection). The LLM layer with budget governance already exists (`llm/LlmService`,
`LlmBudgetGovernor`, `LlmUserBudgetService`, `LlmFeature`).

**Goal.** Translate foreign-language news into the configured target language(s) at ingest —
title, excerpt, and (optionally) body — storing the translation **beside** the untouched original,
with provenance. Operator-configurable, budget-governed.

## Operations

Implement the two authored in NP-001: `getTranslationSettings` / `updateTranslationSettings`
(`/v1/settings/translation`, `system.settings`) over `AppSettingService` + a new `SettingKeys`
entry, shaped as `TranslationSettings` (target languages, which fields to translate, engine,
per-window budget cap, min source-language confidence).

## Read first

- `news/` — `NewsArticleEntity` (`language`), `NewsStoryStore`, `NewsStoryRecordPublisher`,
  `NewsStoryResolver`; `source/formats/news/NewsNames.java` (language-name → ISO).
- `llm/LlmService.java`, `LlmFeature`, `LlmBudgetGovernor`, `LlmUsageMeter` — how a feature calls the
  LLM under a budget; add a `translation` `LlmFeature`.
- `settings/AppSettingService.java`, `SettingKeys.java` — typed settings with defaults.

## Build

1. A `TranslationService` (new, in `ai.foresignal.news` or a sibling `ai.foresignal.translate`): given
   an article/story and its detected source language, when it differs from a target language and
   `TranslationSettings` enables it, translate the configured fields via `LlmService` under a
   `translation` `LlmFeature` (budget-governed; skip and record when the budget is exhausted).
   Detect the source language from `NewsArticleEntity.language`/`NewsNames`, else a cheap detector.
2. Store `targetLanguage`, `translatedTitle`, `translatedExcerpt` (and `translatedBody` if enabled) and
   `translationProvenance` (engine, model, at) on the article/story — **never overwrite** the
   original fields. Idempotent: do not re-translate unchanged content (hash the source text).
3. Wire it into the ingest path (where `NewsStoryRecordPublisher` publishes), so the translation is
   available to cross-linking and downstream editions. Proper nouns/entity names are preserved.
4. Implement the settings endpoints over `AppSettingService`.

**Migration.** Next free additive Flyway `V{n}__news_translation.sql` (one past the highest `V*` —
read the directory): translated columns + provenance on the news tables (and a per-source target
override if the source model needs one). Additive only.

## Tests to write (do not run)

- `TranslationServiceTest` — a known foreign article yields a translation; the original is preserved;
  same content twice does not re-translate; budget exhaustion skips and is recorded.
- `TranslationSettingsIT` — settings round-trip; disabling translation leaves originals only.

## What runs

Compile only — `./gradlew compileJava compileTestJava`; run `./gradlew checkUsEnglish`. Write tests,
do not run them. US English (translated *content* and BCP 47 codes are data, not identifiers).

## Done when

Foreign news is translated at ingest into the configured target language(s), stored beside the
original with provenance, budget-governed and idempotent; the settings endpoints work; compile and
`checkUsEnglish` green.

## Do not touch

`CLAUDE.md`/`CONVENTIONS.md`; the frozen v1.0.0 spec; the `x-foresignal` changelog; `.gitlab-ci.yml`;
other prompts' files. Leave the NP-001 ops `planned`. **Stage only this prompt's files.**

## Commit

`NP-002: News translation at ingest`, then `completion-reports/NP-002-CR-News-Translation.md` with
the 40-character implementation hash.
