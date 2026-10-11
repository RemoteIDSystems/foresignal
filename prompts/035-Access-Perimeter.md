# 035 — Access Perimeter

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/035-CR-Access-Perimeter.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/034-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §1 item 5, §4c (Outbound URL policy row), §6 (Stream tickets + feed keys, Restricted tokens + anonymous profile, Public-exposure hardening rows), §8 item 20 ("028a")
- `docs/Architecture.md` §15 (scopes, `AccessPolicy`), §17 (channel security), §19 (LLM providers), §4 adapter rules; `docs/Weather-Imagery-Design.md` §6.7 (tile keys at `/v1/k/{key}/…`, built in 050 on this prompt's mechanism)
- Completion Reports of 023 (restriction columns on `api_token`, stream tickets, trusted proxies, `alwaysAuthenticated`, audit log, private gate), 004/005 (`LocalAddresses`, `LAN_URL` pinned-IP connect), 015 (SSE filters), 016 (tile SQL), 025 (MCP tool context), 027/028/029 (search, Ask, briefing scopes), 031/032/033 (push targets, inbox)
- IANA IPv4 and IPv6 Special-Purpose Address Registries (RFC 6890 as updated by RFC 8190); OWASP API4:2023 Unrestricted Resource Consumption (https://api-security.owasp.org/editions/2023/en/0xa4-unrestricted-resource-consumption)

## Objective

Close Foresignal's perimeter before remote and mobile use. One enforcement point applies token and anonymous restrictions to every read path; header-less clients get path-bound feed keys; every client is rate-limited, and failed authentication backs off instead of locking out; every operator-supplied outbound URL passes one SSRF policy.

## Tasks

### 1. Restriction model and the single enforcement point (`ai.foresignal.security.restrict`)

- `ReadRestriction` (immutable): `sources` (set, null = any), `kinds`, `bbox` and/or `area` (MultiPolygon 4326, ≤ 2,000 vertices; 036 adds `placeIds` resolved to an area), `endpoints` (path-prefix allowlist), `excludePrivate` (default true for anonymous and feed keys), `maxRequestsPerMinute`. Check that 023 stored `api_token.restrictions` jsonb with this shape; extend it if not. Restrictions are rejected on admin tokens (400 `RESTRICTION_NOT_ALLOWED`: admin can mint tokens anyway).
- `RequestPrincipal` (request-scoped): token id/scope or anonymous or feed key, plus the effective `ReadRestriction`. `TokenFilter` builds it; endpoint allowlists are checked there (403 `RESTRICTED`).
- `RestrictionClamp`: the one place that turns a restriction into query predicates. **Intersect, never union:** an absent request filter becomes the restriction; a disjoint explicit filter yields an empty list (single-record GETs outside the restriction return 404, never 403, so existence does not leak). Provide `clamp(RecordQuery)`, `sqlPredicate(alias)` (source/kind `= ANY`, `ST_Intersects(alias.geom, :area)` with a bbox `&&` prefilter) and an in-memory `test(sourceId, kind, geometry)` for streams.
- Apply it everywhere a record or alert is read. Inventory every read path from the code (grep controllers, MCP tools and services) and add a `@RestrictedRead` marker to each so a test can assert coverage:
  - records, history, versions, changes, kinds and kind schemas, records formats (ndjson, csv, geojsonseq)
  - tiles and TileJSON (predicate inside the MVT SQL, not post-filtered; restricted responses get `Cache-Control: private`)
  - SSE: change, live and touch lanes, multiplexed `/v1/events/stream`, `/v1/alerts/stream`, `/v1/notifications/stream` (filter with `test()` at publish time; a client's PATCH filter is clamped too)
  - search (`embedding_document` filter by source/kind/geom), alerts and alert detail, notifications inbox and badge, digest
  - MCP tools (the token's principal flows into the tool context, so service-layer clamping applies; also clamp tool arguments)
  - Ask and briefings: the LLM tool loop runs with the caller's principal; briefing runs use the creating token's restriction (store `restriction_snapshot` on `briefing`); citations outside it are dropped
  - push fan-out (031/032): subscriptions and devices whose token is restricted only receive alerts that pass `test()`
  - exports and any future read path: document in `docs/Architecture.md` that new read endpoints must use `RestrictionClamp` and carry `@RestrictedRead`.
- The 023 private gate stays orthogonal; `excludePrivate` adds private-source exclusion to restricted principals.

### 2. Anonymous restriction profile

- `SecuritySettings.anonymous`: `{sources, kinds, bbox, area, endpoints, excludePrivate: true, maxRequestsPerMinute: 120}` (all optional). When `openReads=true`, tokenless reads run with this restriction. Default: no source/kind/area limits, private excluded, endpoints = data GETs only (never inbox, places, watchlists or anything `alwaysAuthenticated`).
- Startup warning (log + `/v1/system` `security.warnings`) when `openReads=true`, `publicBaseUrl` is not loopback/private, and the anonymous profile is unrestricted.

### 3. Feed keys for header-less clients

- Feed keys are `api_token` rows with `type = feed_key`, prefix `fsf_`, scope `read`, `prefixes text[]` (allowed canonical path prefixes, e.g. `/v1/records`, `/v1/changes/stream`, `/v1/tiles/`), mandatory `restrictions` (at least one of sources/kinds/area), `expires_at` ≤ 365 d. Hashed, listed and revoked like tokens; `last_used_at` throttled. 050 adds tile keys (`type = tile_key`, `fsk_`) on the same filter.
- API (admin): `GET/POST /v1/feed-keys`, `DELETE /v1/feed-keys/{id}`; creation returns the key and ready-made URLs once.
- `PathKeyFilter` (before `TokenFilter`): `/v1/k/{key}/<rest>` → validate key (constant time), method GET/HEAD only, `/v1/<rest>` starts with one of `prefixes`, then forward to the canonical path with the feed-key principal. Works for KML NetworkLinks, iCal subscriptions, feed readers, HA `geo_json_events` and browser `EventSource` (`/v1/k/{key}/changes/stream`). Invalid → 401 `INVALID_FEED_KEY`; expired/revoked → 410.
- Hygiene: responses carry `Referrer-Policy: no-referrer`, `X-Robots-Tag: noindex`, `Cache-Control: private` (tiles may keep `max-age` but never `public`); redact `/v1/k/{key}` in Tomcat access logs, Micrometer tags and the Caddy example; never echo the key in response bodies other than creation.
- Update the 034 Home Assistant and Grafana cookbooks to use feed-key URLs when `openReads=false`.

### 4. Per-client rate limits and auth-failure backoff

- `ClientRateLimitFilter` (Bucket4j already on the classpath; no starter): key = token id, else feed-key id, else client IP resolved through 023's trusted-proxy settings (never raw `X-Forwarded-For`). Classes and defaults in `app_setting['ratelimits']`: `read` 600/min, `tiles` 1,200/min, `stream` 30 opens/min + 8 concurrent per client, `expensive` 20/min (search, Ask, LLM-backed MCP tools, `/v1/sample` later), `write` 120/min, `anonymous` 120/min per IP. Token `maxRequestsPerMinute` overrides `read`. Over limit → 429 `RATE_LIMITED` with `Retry-After`. Admin tokens are exempt from `read`/`tiles` only.
- Optional limit for `/actuator/prometheus` by IP and a setting `security.metricsOpen` (default true, LAN assumption); when false, metrics require an admin token.
- `AuthFailureBackoff`: per client IP, after 5 failed credential checks within 60 s, each further **failed** attempt receives 429 with `Retry-After` doubling from 1 s to a 300 s cap; a correct credential is always evaluated and accepted (no lockout of the owner behind NAT); a success resets the counter. Count failures for bearer tokens, ingest keys, feed keys and action capabilities (033).
- Metrics: `foresignal.ratelimit.rejected{class}`, `foresignal.auth.failures`, `foresignal.auth.backoff.active`.

### 5. Outbound URL policy (`ai.foresignal.net.OutboundUrlPolicy`)

- `check(URI, Profile)` → `Allowed(pinnedAddresses)` or `URL_NOT_ALLOWED` (400 at validation, delivery failure at send). Profiles:
  - `PUBLIC_ONLY`: globally routable addresses only (LLM frontier providers, public webhooks)
  - `LAN_ALLOWED`: public plus RFC 1918, ULA, CGNAT/Tailscale 100.64/10 and loopback (channels, LLM `baseUrl` such as `http://ollama:11434`)
  - `LAN_ONLY`: the 004 `LAN_URL` rule (replace `LocalAddresses` checks with this profile)
  - `PUSH_ENDPOINT`: `PUBLIC_ONLY` + `push.allowedPrivateHosts` (replace 031's check)
- Always blocked in every profile: link-local 169.254/16 and fe80::/10, cloud metadata (169.254.169.254, fd00:ec2::254, 100.100.100.200), 0.0.0.0/8, multicast, broadcast, and the rest of the IANA special-purpose "not forwardable" entries. Normalise first: reject non-dotted-quad IPv4 literals (decimal, octal, hex), extract embedded IPv4 from IPv4-mapped/compatible, NAT64 (64:ff9b::/96), 6to4 (2002::/16) and Teredo and check it; schemes `https` (and `http` only in LAN profiles or when the operator allows it per field); no userinfo.
- Admin overrides `app_setting['outbound']`: `allowHosts`, `allowCidrs`, `denyHosts`, `denyCidrs` (deny wins).
- **Connect-time pinning:** resolve once, check every address, connect to a checked address with the original Host/SNI, redirects off; callers that allow redirects re-check each hop (max 3). Reuse the pinned-IP connector from 005 if it exists; otherwise implement it with OkHttp's per-client `Dns` (OkHttp arrives with the Anthropic SDK; verify and add it explicitly to `gradle/libs.versions.toml` if used directly). When a JVM or env HTTP proxy is configured, refuse `LAN_ONLY` and log that address checks are bypassed for proxied profiles.
- **Retrofit** every operator-supplied URL: 018 channel URLs (webhook `url`, ntfy/gotify `serverUrl`, matrix `homeserverUrl`, slack/mattermost/discord/teams/google-chat webhook URLs, apprise, homeassistant; SMTP host via `LAN_ALLOWED` host check), 026 LLM `baseUrl` (`LAN_ALLOWED`; `PUBLIC_ONLY` is not forced because Ollama is local), 031 push endpoints (`PUSH_ENDPOINT`), 004 `LAN_URL` params (`LAN_ONLY`), 024 any URL fields. Validate on save (400 with `field`) and on every send. Write the rule into `docs/Adapter-Guide.md`: later prompts with operator URLs (generic feeds, dataset-by-URL, kits) must call `OutboundUrlPolicy`.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: `ApiTokenInput`/`ApiToken` `restrictions` (`ReadRestriction` schema); `SecuritySettings.anonymous`, `metricsOpen`; `listFeedKeys`/`createFeedKey`/`revokeFeedKey` (`x-foresignal-scope: admin`); a `feedKey` path-segment security scheme and a documented `/v1/k/{key}/…` alias (note it in `info.description`, not as duplicated operations); `RateLimitSettings` + `getRateLimits`/`updateRateLimits` (admin); `OutboundSettings` + get/update (admin); codes `RESTRICTED`, `RESTRICTION_NOT_ALLOWED`, `INVALID_FEED_KEY`, `RATE_LIMITED`, `URL_NOT_ALLOWED`; 429 responses on data operations. Update `AccessPolicy`, BODIES examples, regenerate Postman.
- `docs/Architecture.md` §15 (restrictions, anonymous profile, feed keys, rate limits) and a new "Outbound URL policy" section; Charter FR-39/FR-40. `docs/Operator-Guide.md` "Exposing Foresignal": restricted tokens for kiosks and family phones, feed keys, limits, log redaction in Caddy.

## Tests to write (do not run)

- `RestrictionLeakMatrixIT`: a fixture with two sources, two kinds, records inside/outside an area and one private source; for {anonymous with profile, restricted read token, feed key} × every `@RestrictedRead` path (records, history, versions, changes, formats, tiles, TileJSON, each SSE lane, alerts, inbox, search, digest, MCP `query_records`/`search`/`list_alerts`, Ask tool loop via `FakeLlmClient`, push fan-out) assert nothing outside the restriction is returned or streamed.
- `RestrictedReadCoverageTest`: every controller method and MCP tool that returns records or alerts carries `@RestrictedRead`.
- `RestrictionClampTest`: intersection semantics, empty request ⇒ restriction, disjoint ⇒ empty, 404 for out-of-scope single reads.
- `PathKeyFilterIT`: valid prefix, wrong prefix, POST rejected, expired 410, revoked, EventSource stream, log redaction, `Referrer-Policy`.
- `ClientRateLimitFilterIT` (`MutableClock`): per-class limits, concurrent SSE cap, trusted-proxy keying vs spoofed `X-Forwarded-For`, admin exemptions.
- `AuthFailureBackoffTest`: doubling, cap, valid credential never blocked, reset on success.
- `OutboundUrlPolicyTest`: table over every blocked range and literal form, embedded-IPv4 cases, profile differences, overrides, DNS answers mixing public and private.
- `OutboundRetrofitIT`: channel, LLM provider and push validation reject metadata/link-local; DNS rebinding between save and send is blocked; redirects not followed.

## Acceptance criteria

- A restricted token, a feed key and the anonymous profile see exactly the same subset on every read path, including tiles, streams, search, MCP and Ask.
- KML, iCal, feed readers and `EventSource` can read through a path-bound feed key; nothing else works with it.
- Floods get 429 with `Retry-After`; repeated bad credentials back off; the owner's valid token always works.
- No outbound request reaches link-local or metadata addresses, and LAN targets are allowed only where the profile permits.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `035: Access Perimeter`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/035-CR-Access-Perimeter.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `035: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
