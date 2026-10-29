# R2-032 — Hosted + shareable edition site

**Context.** Automated-newspaper feature, after **R2-031**. The backend already serves a briefing run
as sanitized HTML (`BriefingsController`, `?format=html`, `getLatestBriefingRun`), a podcast RSS
(`BriefingPodcastController`) and media, reachable unauthenticated via 035 **feed keys**
(`/v1/k/{key}/…`); revocable read-only **share links** exist (`ShareLinkService`). `R2-028` authored
`getEditionSite` + `EditionSite`.

**Goal.** A stable, always-current **per-user newspaper website** — the user's latest edition as a
standalone HTML page — plus a **shareable read-only public URL**. The user (or operator) builds
themselves an automated newspaper site; no new hosting is required, only this render + addressing.

## Operations

Implement `getEditionSite` (`GET /v1/editions/{briefingId}/site`, `read`): the edition's latest
successful run rendered as a complete standalone HTML page (`BriefingHtmlRenderer`), always current.
`EditionSite` carries the canonical site URL, the feed-key URL and the share URL.

## Read first

- `api/BriefingsController.java` (HTML render + conditional GET), `api/BriefingPodcastController.java`
  (feed-key + ETag/Last-Modified + visibility pattern to mirror), `briefing/BriefingHtmlRenderer.java`.
- `security` share links (`ShareLinkService`, `isShareLinkWrite`) and feed/tile keys (prompt 035,
  `PathKeyFilter`), and `api/support/PublicUrls`.

## Build

1. `getEditionSite` renders the latest successful run as a full HTML document (not a fragment),
   with strong ETag + `Last-Modified` and 304 handling, mirroring the podcast controller's caching
   and visibility (restriction + private gate). Reachable under open reads, via a feed key
   (`/v1/k/{key}/editions/{id}/site`, enclosure/asset URLs carrying the same key) and via a share
   link.
2. Make the edition shareable: a **read-only, revocable** share link (reuse `ShareLinkService`) that
   grants exactly `getEditionSite` (and its assets) for that edition — never writes, never other
   data. `EditionSite` returns the canonical, feed-key and share URLs.
3. Respect the edition's owner/visibility (R2-030): a private edition's site is only reachable with
   the owner's credential, its feed key, or its share link.

## Tests to write (do not run)

- `EditionSiteIT` — the site renders the latest run as standalone HTML; conditional GET returns 304;
  feed-key and share-link access work; a private edition is gated; a revoked share link 404s.

## What runs

Compile only — `./gradlew compileJava compileTestJava`; run `./gradlew checkUsEnglish`. Write tests,
do not run. US English.

## Done when

A user's edition is reachable as a stable, always-current HTML site, shareable via a revocable
read-only link, with the same caching and visibility guarantees as the podcast feed; compile and
`checkUsEnglish` green.

## Do not touch

`CLAUDE.md`/`CONVENTIONS.md`; frozen v1.0.0 spec; `x-foresignal` changelog; `.gitlab-ci.yml`; other
prompts' files. Leave R2-028 ops `planned`. **Stage only this prompt's files.**

## Commit

`R2-032: Hosted + shareable edition site`, then
`completion-reports/R2-032-CR-Hosted-Shareable-Edition-Site.md` with the 40-character hash.
