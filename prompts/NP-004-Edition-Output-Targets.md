# NP-004 — Edition output targets (email, push, alerts)

**Context.** Automated-newspaper feature, after **NP-003**. The `notify/` layer already delivers
to email, webpush, webhook, telegram, discord, ntfy, pushover, matrix, mqtt and slack — and
briefings already deliver through it, **including synthesized audio** (prompt 141). `NP-001` authored
`DeliveryTarget` on the edition.

**Goal.** Deliver an edition run to each of its configured `deliveryTargets` in the right format —
**email**, **push**, and **alert** channels — reusing the existing notify channels and renders. (The
`website` target is NP-005; `podcast` already exists.)

## Operations

No new HTTP ops — delivery is driven by the edition's `deliveryTargets` (NP-001) at run time.

## Read first

- `briefing/BriefingRunner.java` (how a run currently delivers to `channelIds`),
  `BriefingHtmlRenderer`, `BriefingAudioService`.
- `notify/` — `ChannelType`, the send pipeline, image/audio attachment support; quotas.

## Build

1. In the run/delivery path, resolve each `DeliveryTarget` to a `notify` send:
   - **email** → the edition as an HTML body (`BriefingHtmlRenderer`) with the audio as an attachment
     and a link to the hosted site (NP-005);
   - **push**/webpush → a headline + summary + deep link to the hosted edition (not the full body);
   - **alert** → the configured channel, same payload shape as an alert notification.
2. Reuse the existing audio attachment handling (prompt 141) for channels that carry audio. Respect
   per-user quotas and the notification budget. A failed target never blocks the others, and each is
   recorded on the run.
3. Keep the default (no explicit targets) behaving exactly as today (every channel subscribed to the
   briefing) so existing briefings are unchanged.

## Tests to write (do not run)

- `EditionDeliveryIT` — an email target sends HTML + audio attachment + site link; a push target
  sends a headline + link, not the full body; an alert target uses the channel; one target failing
  does not block the rest.

## What runs

Compile only — `./gradlew compileJava compileTestJava`; run `./gradlew checkUsEnglish`. Write tests,
do not run. US English.

## Done when

An edition run delivers to its email/push/alert targets in the right per-format shape over the
existing channels, audio included, quotas respected, failures isolated, and the no-targets default
unchanged; compile and `checkUsEnglish` green.

## Do not touch

`CLAUDE.md`/`CONVENTIONS.md`; frozen v1.0.0 spec; `x-foresignal` changelog; `.gitlab-ci.yml`; other
prompts' files. Leave NP-001 ops `planned`. **Stage only this prompt's files.**

## Commit

`NP-004: Edition output targets`, then `completion-reports/NP-004-CR-Edition-Output-Targets.md` with
the 40-character implementation hash.
