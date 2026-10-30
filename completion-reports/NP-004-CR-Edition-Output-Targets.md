# NP-004-CR-Edition-Output-Targets

| | |
|---|---|
| Prompt | NP-004-Edition-Output-Targets.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test was run) |
| Implementation commit | `2cc27ac1dec3a143ca8e13065b0f23c00d169ee2` |
| Date | 2026-10-30 |

## What was added

- **Migration `V29__edition_delivery.sql` (additive):** `briefing.delivery_targets jsonb` and
  `briefing_run.delivery_results jsonb`. Both are null for existing rows.
- **`briefing/DeliveryTarget`:** the record for `{kind, channelId, format, options}`, with stored and wire forms.
  `BriefingInput.Edition` gains `deliveryTargets`, and its one-argument constructor from NP-003 is kept.
  `Briefing.deliveryTargets` is returned on reads (an empty array when there are none).
- **Validation** (`BriefingValidator.deliveryTargets`):
  - Null, or at most 20 targets; duplicates are dropped.
  - A `channel` target needs a `channelId` and the format `email`, `push` or `alert`.
  - `website` takes `html` and `podcast` takes `audio`, neither with a channel.
  - `options` must be an object; unknown members are a 400 for each field.
  - A missing or disabled target channel is a warning, as for `channelIds`.
- **`briefing/EditionDelivery`:** delivers each target in its own shape over the existing notify pipeline.
  - **email:** a `briefing` notification with the whole edition. The email's HTML body is the sanitized
    `BriefingHtmlRenderer` page, carried as `data.editionHtml`; `EmailChannel` uses it when present and otherwise
    renders as before. The page's `lang` is the edition's language (a new `page(…, language)` overload). The text
    part is the markdown with a "Read online" link to the site. The audio is attached by the existing 141
    `AudioAttachments`, because the run view carries `audio.mediaId`.
  - **push:** a `briefing` notification with the title (headline) and a summary. The summary is the first paragraph
    that is not a heading, as plain text, at most 280 characters, cut at a word. It carries the site URL and link.
    `data` is cut down to the run's id, briefing id, status, period, mode and `siteUrl`, so the payload has no body,
    audio, citations, statistics or HTML.
  - **alert:** an `alert` notification with subtype `edition.alert` and severity from `options.severity`
    (`info`|`warning`|`critical`, default `info`). It carries the headline, the summary and the link, and the same
    cut-down `data`.
  - **website** and **podcast** are pulled, not pushed. Their result is `available`, with the URL:
    `/v1/editions/{id}/site` (NP-005) or `/v1/briefings/{id}/podcast.rss`. A podcast target on a run without `ok`
    audio is `skipped`.
- **Isolation and budgets:**
  - Each channel target is its own `NotificationService.publish(message, c -> false, [channelId])`, which reaches
    that channel alone. The channel's rate limit, quiet hours, backlog cap and the notification pause apply as for
    any send.
  - Each target sits in its own try/catch. A missing, disabled or foreign channel is `skipped` with the reason, an
    exception is `failed`, and the remaining targets still run.
  - An owned edition (NP-003) may use only its owner's personal channels, plus the global ones when the owner holds
    `channels.global`. This is checked at run time, so a later permission change applies.
- **On the run:** `BriefingRun.deliveryResults` lists each target in order:
  `{kind, channelId, format, status queued|available|skipped|failed, notificationId, url, error}`.
  `deliveries` counts the channel deliveries that were enqueued.
- **The default is unchanged:** a briefing without `deliveryTargets` goes through the existing `deliver(…)`, which
  uses `channelIds`, else every channel subscribed to `briefing` (owned editions keep the NP-003 default of the
  owner's channels). Its `deliveryResults` is null.
- **Spec addition** (`docs/OpenAPI.json`): `BriefingRun.deliveryResults` (read-only), and `NP-004` added to
  `BriefingRun.x-foresignal-extended-by`. NP-001 left per-target results open. The 2.1.0 changelog entry was not
  touched, as this prompt forbids; NP-007 should mention the addition there. The Postman and `AccessPermissions`
  generators were re-run and produced no change.
- **`BriefingLinks`:** `editionSite(id)` and `podcast(id)`.
- **Test (written, not run):** `EditionDeliveryIT`:
  - email over GreenMail: the HTML page, the `briefing.mp3` attachment and the site link;
  - push over a WireMock webhook: no body, `mediaId`, citations or HTML;
  - alert: the `alert` event, `edition.alert` and the severity;
  - isolation: a deleted channel is skipped while the others are queued, and every result is stored in order;
  - the no-targets default;
  - validation.

## Decisions and disagreements for the owner

1. **Alert targets use the `alert` event** (subtype `edition.alert`), so every channel renders them in its alert
   shape: MQTT `alertFired`, severity-based quiet hours, and the backlog cap.
   - They have no `alertId` or `ruleId`. Channels with a `ruleIds` filter skip nothing, because the target is
     explicit.
   - The subtype is neither `alert.fired` nor `alert.*`, so enrichment waits, rule grouping and lifecycle edits do
     not apply.
   - The other choice was the `briefing` event with an alert-like severity. Which do you want?
2. **There is no per-user send quota.** The R2-026 quotas count owned objects (channels, rules, views, storage), not
   sends. "Respect quotas and the notification budget" is met by the channel rate limiter, the pause, quiet hours,
   the backlog cap, and the restriction to the owner's channels. Should edition sends count against a new quota?
3. **The email HTML replaces the channel's own rendering only for edition emails.** Other briefing emails and alert
   emails still use the `MarkdownRenderer` path. `plainTextOnly` channels still get only the text part, with the
   audio attached.
4. **`deliveryTargets` replaces `channelIds`** when present, as NP-001's description says. Listing both is not an
   error, and `channelIds` is then ignored for delivery. Should that be a warning?
5. **The site link points at `/v1/editions/{id}/site` before NP-005 exists.** NP-005 implements it in this campaign,
   so the link is correct once the campaign is complete.
6. **Targets are delivered on scheduled and manual runs alike.** Previews deliver nothing.
7. **Push summary:** the first paragraph that is not a heading. For a digest edition that is usually the counts line.
   For an LLM edition it is the lead.

## What runs

- `./gradlew compileJava compileTestJava checkUsEnglish` → `BUILD SUCCESSFUL`.
- No test, `build`, `check`, `lintOpenApi`, `checkApiCompat` or SDK generation was run.
- The six NP-001 operations stay `planned`, and the changelog was not touched.

## Notes for the next prompt

- NP-005: `BriefingLinks.editionSite(id)` is the URL already sent in emails and push. `BriefingViews.editionSummary`
  should set `siteUrl` from it when `site.enabled`. The `website` target result is `available` with that URL.
- The next free migration is `V30`.
