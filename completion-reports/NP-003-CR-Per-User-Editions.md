# NP-003-CR-Per-User-Editions

| | |
|---|---|
| Prompt | NP-003-Per-User-Editions.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test was run) |
| Implementation commit | `e7775f69f6f100fc5ffc812e226e093dd416d473` |
| Date | 2026-10-30 |

## What was added

- **Migration `V28__edition_owner.sql` (additive):**
  - `briefing.owner_user_id`: nullable, with a foreign key to `"user"` that cascades on delete, and the index
    `briefing_owner_user_idx (owner_user_id, created_at)`.
  - `briefing.output_language` (`varchar(35)`).
  - A new table `user_news_subscription (user_id, type outlet|language, value, updated_at)`, deleted with its user.
  - Existing token/scope briefings keep `owner_user_id` null and behave exactly as before.
- **Ownership.**
  - A briefing created by a signed-in session, or by a user's own API token, gets `ownerUserId`. That makes it the
    user's edition.
  - `BriefingService.EditionViewer` and `visibleTo(entity, viewer)` decide access to an edition. Its owner and any
    instance (non-user) token may see it, run it, list its runs, read its feeds and podcast, update it and delete it.
    Any other user, and an anonymous caller, gets `404`, the same answer as for a briefing that does not exist.
  - The user is resolved from the caller's principal (`UserRepository.findByPrincipalId`), so REST and MCP use the
    same rule.
  - Gated call sites:
    - REST: `listBriefings`, `getBriefing`, `updateBriefing`, `deleteBriefing`, `runBriefing`, `listBriefingRuns`,
      `getLatestBriefingRun` and `getBriefingRun`;
    - the Atom/JSON run feeds and the podcast;
    - the MCP tools `list_briefings`, `get_briefing_run` and `run_briefing`.
  - `updateBriefing` never changes the owner.
- **`listMyEditions`** (`EditionsController`, `GET /v1/me/editions`):
  - It returns the session user's editions, newest first, with a cursor and `limit` 1–200 (default 50).
  - Each item is an `EditionSummary`, including `latestRunId`. `siteUrl` is null until NP-005.
  - An API token gets `401`, as the NP-001 contract says.
- **News subscriptions** (`NewsSubscriptionsController`, `NewsSubscriptionService`): `getMyNewsSubscriptions` and
  `updateMyNewsSubscriptions` are implemented here, because the edition's news follows them.
  - An outlet is stored by the canonical id `OutletRegistry.find` resolves (an id or a domain). A language is stored
    as a canonical BCP 47 tag.
  - Validation errors are listed per entry. The limit is 500 subscriptions. An API token gets `401`.
- **Effective scope: "my edition follows my data"** (`EditionResolver`, `EditionContext`):
  - At every run, the sources the owner turned off in their source view (`UserSourceViewService.hiddenFor`) are added
    to the hidden sources. They are counted but never listed, like private sources.
  - The read restriction snapshot taken when the edition is created already carries the session's narrowing.
  - A **"Top stories"** section (`EditionNews`, at most 10 stories) is added to owned editions.
    - It keeps stories that have at least one article in the period from a visible source.
    - When the owner has outlet or language subscriptions, the article must also come from a subscribed outlet, and
      be in a subscribed language or have an `ok` NP-002 translation into it.
    - Stories rank by distinct outlets, then articles, then recency.
  - The section reaches the digest, the LLM writer and the audio like any other `BriefingSection`.
- **Language** (`outputLanguage`, otherwise the owner's `UserPreferences.locale`):
  - **LLM mode:** `BriefingPrompts.userTurn(…, language)` adds an "Output language" instruction to the user turn,
    with ids, citations, numbers and names kept. The system prompt stays constant, so it can still be cached.
    `LlmBriefingWriter.Request` gains `language`, and the old constructors are kept.
  - **Digest mode, and the LLM fallback:** the rendered markdown goes through the new
    `NotificationTranslator.translateMarkdown`, which uses the 141 prompt, masking and checks without the
    notification deadline. When there is no provider, or the output is rejected, the English is kept and the run
    carries the warning "the edition could not be translated into …; it is in English".
  - **Translated news:** a story NP-002 translated is listed under its translated title, with `originalTitle` kept in
    the section data.
  - **Audio:** `EditionVoice.match` picks a voice of the edition's language.
    - A Kokoro voice whose language prefix differs from the edition's language is swapped for the voice of the same
      gender in that language.
    - The voices: `af_heart`/`am_michael`, `bf_emma`/`bm_george` (`en-GB`), `jf_alpha`/`jm_kumo`,
      `zf_xiaobei`/`zm_yunjian`, `ef_dora`/`em_alex`, `ff_siwis`, `hf_alpha`/`hm_omega`, `if_sara`/`im_nicola` and
      `pf_dora`/`pm_alex`. The names were checked against Kokoro-82M `VOICES.md` (fetched in this session).
    - A language Kokoro has no voice for, and any non-Kokoro voice (OpenAI's voices speak any language), keeps the
      configured voice.
    - The voice actually used is recorded in `BriefingRun.audio.voice`.
- **Isolation of a run's outputs:**
  - An owned edition's runs are **not embedded** in the shared search index, because Ask and search would otherwise
    show one user's edition to everyone.
  - An owned edition that lists no `channelIds` is delivered **only to its owner's personal channels** (R2-009
    `owner_principal_id`). Before this change it went to every channel subscribed to `briefing`. With no personal
    channel, nothing is delivered.
- **Views and validation:** `Briefing.ownerUserId` (output only; ignored on input) and `Briefing.outputLanguage`.
  `BriefingValidator` accepts `outputLanguage` and canonicalizes it; a malformed tag is a `400`.
- **Tests (written, not run):**
  - `EditionOwnershipIT`:
    - ownership and listing for two signed-in editors and an instance token;
    - `404` for the other user on get, run, runs, latest, update, delete, the feed and the podcast;
    - `401` for a token on `listMyEditions` and on news subscriptions;
    - paging;
    - the edition following the owner's source view;
    - news-subscription validation and isolation.
  - `EditionLanguageTest`:
    - the language instruction in the LLM user turn, with the system prompt constant;
    - the digest translation and its fallback;
    - the voice choice;
    - a translated story rendered in the edition's language;
    - language resolution.

## Decisions and disagreements for the owner

1. **Who counts as the owner, and who else sees an edition.**
   - Any briefing created by a signed-in user, or by a user's own token, becomes that user's edition and is hidden
     from every other user. This includes admins, unless they use an instance token.
   - Instance tokens (the operator's machine path) still see everything.
   - Should admin sessions (`users.manage`) also see every user's editions?
2. **Edition CRUD reuses the briefing endpoints**, so creating one needs `agent` scope.
   - A Viewer-role user, whose session scope is `read`, cannot create an edition. "A user gets their own daily
     edition" therefore holds for Editor-and-above roles only.
   - No edition is created automatically for each user.
   - Should there be a self-service create path at `read` scope, or an automatic edition per user? Either needs a
     contract change.
3. **`listMyEditions` and news subscriptions are session-only**, as NP-001 wrote them; a user's own API token gets
   `401`. The ownership checks themselves do recognize user tokens. Should the three `me` operations also accept a
   user's token, as `getMyQuota` does?
4. **Edition runs are kept out of the search index**, to protect isolation, so an owner cannot find their edition
   through Ask or search. A per-user filter on briefing documents in search would allow both; it is not built here.
5. **Delivery default changed for owned editions only**: with no `channelIds`, an edition goes to the owner's
   personal channels instead of every `briefing` channel. NP-004 builds the `deliveryTargets` model on top of this.
6. **Share links:** `createShareLink` for a `briefing-run` of another user's edition is still allowed by the share
   service. Its `Caller` has no user identity, and resolving an existing link is an explicit share. NP-005 (edition
   sharing) is the place to gate creation. Flagged so it is not missed.
7. **Ask citations:** `CitationSet` can still title a cited `briefing` document by id. With embedding off for
   editions, no edition document reaches Ask.
8. **The digest is translated after rendering** (one extra translation call per run), not written natively. The
   digest renderer's English labels are therefore translated together with the data. The original English is not
   stored beside the translation.
9. **The prompt asks to resolve the edition's `BriefingScope` from the saved view and region preferences.** That was
   implemented as a run-time narrowing (source view, news subscriptions, and the session restriction captured at
   creation). The briefing's own `scope` (kinds, places, bbox) is still what the user sets. Applying a saved view's
   filter automatically would need a link from edition to saved view, which the contract has no field for.
10. **The `self.sources` permission** guards news subscriptions, as NP-001 annotated. Viewer-role users lack it, so
    they cannot set subscriptions.

## What runs

- `./gradlew checkUsEnglish compileJava compileTestJava` → `BUILD SUCCESSFUL` (exit 0).
- No test, `build`, `check`, `lintOpenApi`, `checkApiCompat` or SDK generation was run, per the campaign rules.
- The six NP-001 operations stay `planned`. The OpenAPI changelog was not touched.

## Notes for the next prompt

- NP-004 (delivery targets): `BriefingRunner.deliver(briefing, input, run, edition)` is the hook. The owned-edition
  default (the owner's channels) lives in it, and `EditionResolver.ownerChannels(userId)` lists them.
- NP-005 (site): `BriefingViews.editionSummary` sets `siteUrl` to null. Fill it there, and gate `getEditionSite` with
  `BriefingService.visibleTo(entity, EditionViewer.of(request, anonymous))` for private editions.
- NP-006 (breaking news): run an owned edition through `BriefingRunner.run(entity)`, which resolves the edition
  context itself.
- The next free migration is `V29`.
