# NP-005-CR-Hosted-Shareable-Edition-Site

| | |
|---|---|
| Prompt | NP-005-Hosted-Shareable-Edition-Site.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test was run) |
| Implementation commit | `386e5038d8ac569d3bc16fa887fa8971afd7d0d9` |
| Date | 2026-10-30 |

## What was added

- **Migration `V30__edition_site.sql` (additive):**
  - `briefing.site_enabled` and `briefing.site_shareable`, both `boolean not null default false`, so no existing
    briefing serves anything.
  - A table `edition_site_key (briefing_id, kind feed|share, key_id, key_ciphertext, key_nonce, expires_at,
    created_at)`, deleted with the briefing.
- **`site {enabled, shareable}` input:** `BriefingInput.Site`, validated with unknown members a 400. `shareable`
  without `enabled` is a warning. It is returned as `Briefing.site`.
- **`getEditionSite`** (`EditionSitesController`, `GET /v1/editions/{briefingId}/site`):
  - **HTML by default:** the latest `succeeded` or `fallback` run as a complete document from
    `BriefingHtmlRenderer.page`, in the edition's language (`lang`). The headers are the strict CSP, `nosniff` and
    `no-referrer`.
  - **Caching:** a strong ETag over run, status, finish time and language, `Last-Modified` set to when the run
    finished, and `304` on `If-None-Match` or, without one, on `If-Modified-Since`. The response is publicly cacheable
    only under open reads, for a non-owned edition, and without the private gate.
  - **JSON** (`?format=json` or an `Accept` ranking JSON first): the `EditionSite`, with `url`, `feedKeyUrl`,
    `shareUrl`, `latestRunId` and `updatedAt`. The key URLs are shown only to an authenticated caller that is not
    itself using a path key, so a key URL never reaches anonymous readers or key holders.
  - **Visibility, as for the podcast:**
    - the 035 restriction, and the NP-003 ownership (owner or instance credential; otherwise `404`);
    - runs citing private sources only behind the private gate;
    - `404` while the site is off, and before the first successful run.
- **The edition's feed key** (`EditionSiteService`, kind `feed`):
  - A 035 `fsf_` key whose only prefix is `/v1/editions/{id}/site`, with read scope, no private grant and a 365-day
    lifetime.
  - `/v1/k/{key}/editions/{id}/site` serves the page. Any other path under the key is `403`, and anything but
    GET/HEAD is `405`, both from `PathKeyFilter`.
- **The edition's share link** (kind `share`):
  - A 112 `fss_` link of the new target type `edition-site` (`EditionSiteShareTarget`, an `ExtraShareTarget`),
    served by `openEditionSiteShareLink` at `GET /v1/k/{key}/site`. `/site` was added to `ShareKeyResolver`'s
    sub-paths.
  - It grants that page only. The target is checked again on every use: a deleted edition or a site turned off gives
    `404`. A run citing private or non-redistributable sources is never served on the link.
- **Stable addresses:**
  - Both keys are minted when the edition is saved with its site on, and renewed when less than 30 days are left
    (on save, and after each run through `BriefingService.execute`).
  - The plaintext keys are stored AES-256-GCM encrypted (`CredentialCipher`), because `EditionSite` returns the URLs
    on every read. The keys survive saves unchanged.
  - Turning sharing off revokes the link, turning the site off revokes both keys, and deleting the edition revokes
    both before the row goes.
  - A key revoked from outside (an operator's `revokeShareLink` or `revokeFeedKey`) is **not** silently replaced. Its
    URL reads as null until the owner turns the setting off and on again.
- **Share-link lifetime per target:** `ExtraShareTarget.maxTtl()` is a new default method that returns `MAX_TTL`
  (30 days). `ShareLinkService.create` now honors it, and `edition-site` allows 365 days, so the shared address stays
  put. All other targets are unchanged.
- **`EditionSummary.siteUrl`** is now set (the canonical URL) while the site is enabled.
- **Spec additions** (`docs/OpenAPI.json`):
  - the operation `openEditionSiteShareLink` (`GET /v1/k/{key}/site`, open, `planned`, `x-foresignal-prompt:
    NP-005`), so that NP-007 flips it with the NP-001 operations;
  - `edition-site` in the `ShareLink.targetType` enum. `ShareLinkCreate` is unchanged, following the
    `workspace`/`report` precedent of a dedicated create path; here the link comes from `site.shareable`.
  - `AccessPolicy`: `GET /v1/k/*/site` is OPEN. `AccessPermissions` was regenerated (907 rows) and Postman
    regenerated.
- **Test (written, not run):** `EditionSiteIT`:
  - the standalone page, ETag, `Last-Modified`, both kinds of `304`, and a new run replacing the page;
  - the `EditionSite` URLs, with the feed key and share link reaching only the site (403, 404, 405 elsewhere), no key
    URLs for a path-key caller, and stable keys across saves;
  - a private edition, gated to its owner and its own keys;
  - revocation: sharing off gives 410, site off gives 404 and 410, deletion gives 410;
  - an operator's revocation is not undone.

## Decisions and disagreements for the owner

1. **A revoked share link answers `410 SHARE_EXPIRED`, not the `404` the prompt's test line asks for.**
   `PathKeyFilter`'s `ShareKeyResolver` refuses revoked, expired and exhausted `fss_` keys with 410 before any
   controller runs. Every share link, the report link included, behaves this way, and `openShareLink` documents it.
   `404` is what a link gets when its edition is deleted or its site is off. Should edition links alone answer 404,
   which needs a special case in the filter?
2. **The plaintext keys are stored encrypted.** Elsewhere a feed key's value is shown once and only its hash is kept.
   The contract's always-present `feedKeyUrl`/`shareUrl` needs the value on every read, and encrypting with the
   instance key is the least exposure that still allows it. The alternative is to show the URLs once, when minted.
3. **Keys last 365 days and renew in the last 30.** On renewal the URL changes once a year; the old key is revoked when
   the new one is stored. A share link of type `edition-site` may now live 365 days, longer than the 30-day cap of
   other targets.
4. **"Asset URLs carrying the same key":** the page has no assets. Its styles are inline, and the CSP
   (`default-src 'none'; img-src 'self' data:`) loads nothing else. The edition's audio is therefore not embedded; the
   podcast feed carries it. Embedding an `<audio>` element would need `media-src`, plus a key covering
   `/v1/media/{id}` for each run.
5. **Generic `createShareLink` also accepts `edition-site`**, because extra target types are always accepted. It is
   checked against the NP-003 ownership of the current request, so only the owner or an instance credential can
   create one. Its 365-day cap applies too.
6. **Both new operations stay `planned`** until NP-007, as the campaign rules say for the NP-001 operations. The new
   `x-foresignal-prompt: NP-005` has the same `\d{3}` pattern problem in `SpecAnnotationsTest` that NP-001 reported.
7. The 2.1.0 changelog was not touched. NP-007 should mention `openEditionSiteShareLink` and the `edition-site`
   share type there.

## What runs

- `./gradlew compileJava compileTestJava checkUsEnglish` → `BUILD SUCCESSFUL`.
- `python3 scripts/generate-access-permissions.py …` → "907 rows (555 with a permission)". Postman was regenerated.
- No test, `build`, `check`, `lintOpenApi`, `checkApiCompat` or SDK generation was run.

## Notes for the next prompt

- NP-006 (breaking news): a breaking run is an ordinary run of the edition, so the site shows it as soon as it
  succeeds.
- The next free migration is `V31`.
