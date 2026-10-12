# 035-CR-Access-Perimeter

| | |
|---|---|
| Prompt | 035-Access-Perimeter.md |
| Status | Complete |
| Implementation commit | 8fccc3800ca8b21a58dc23d424998af38df0211f |
| Date | 2026-10-12 |

## Summary

The perimeter is closed for remote and mobile use. All five tasks landed, plus the spec, Postman
and docs (task 6).

**1. Restrictions on every read path** (`ai.foresignal.security.restrict`)
- `ReadRestriction` is the effective restriction: sources, kinds, bbox and/or area (their
  intersection when both are set), endpoints, `excludePrivate` and `maxRequestsPerMinute`.
- `RequestPrincipal` is a request attribute: a token, a feed key or an anonymous caller.
- `RestrictionClamp` is the only place that turns a restriction into checks:
  - a SQL predicate: `= ANY` on source and kind, then `geom && envelope AND ST_Intersects(geom, region)`;
  - an in-memory `test()` for geometries, change-summary boxes, alert record snapshots and
    GeoJSON text.
- Semantics are intersect, never union: the predicate is ANDed into each query. An absent filter
  becomes the restriction, and a disjoint filter returns an empty list. A single record or alert
  outside the restriction is 404.
- 023's `FailClosedRestrictionEnforcer` is replaced by `ClampedRestrictionEnforcer`, so data
  restrictions no longer get 403.
- The restriction is applied to:
  - records, record versions, history and changes;
  - kinds and kind schemas;
  - every records format, and asOf;
  - tiles (inside the MVT SQL) and TileJSON;
  - the change, live and touch lanes, `/v1/events/stream` (a PATCHed filter keeps the restriction),
    the alerts stream and the notifications stream;
  - search, alerts and alert detail (alert writes check visibility too);
  - the inbox and badge, and the digest;
  - briefings: a new `briefing.restriction_snapshot` is used for runs, and a restricted caller sees
    only briefings created under a restriction within its own;
  - every MCP tool and resource, through `ToolContext.restriction()`;
  - Ask, in both the tool loop and retrieval;
  - push fan-out (`PushRestrictionGate`);
  - the source listings.
- Each of these paths carries `@RestrictedRead`. Responses to restricted callers are
  `Cache-Control: private`, and ETags and cursors include the restriction.

**2. Anonymous profile**
- `SecuritySettings.anonymous` and `metricsOpen` are accepted.
- `excludePrivate: false` is refused for the anonymous profile.
- `SystemInfo.security` gets `openReads`, `anonymousRestricted` and `warnings`, including the new
  `ANONYMOUS_UNRESTRICTED`, which is also logged at startup and whenever the setting changes.

**3. Feed keys**
- Keys are `api_token` rows with `type=feed_key`, prefix `fsf_`, scope `read`, a `prefixes`
  column, a mandatory restriction and an expiry of at most 365 days.
- `GET/POST /v1/feed-keys` and `DELETE /v1/feed-keys/{id}`. Creation returns the key and its URLs
  once.
- `PathKeyFilter` runs before `TokenFilter` and resolves `/v1/k/{key}/<rest>` through pluggable
  `PathKeyResolver`s chosen by key prefix (050 and 112 add theirs):
  - an invalid key is 401 `INVALID_FEED_KEY`; an expired or revoked one is 410 `FEED_KEY_GONE`;
  - methods other than GET and HEAD get 405; a path outside the prefixes gets 403 `RESTRICTED`;
  - unsafe paths get 400;
  - otherwise it forwards to the canonical path in the same filter chain.
- Hygiene: responses carry `Referrer-Policy: no-referrer`, `X-Robots-Tag: noindex` and
  `Cache-Control: private`. The key is redacted from the Tomcat access-log pattern and from the
  Caddy example log filter.
- The Home Assistant and Grafana cookbooks and the Agent Guide now use feed-key URLs.

**4. Rate limits and backoff**
- `ClientRateLimitFilter` (Bucket4j, clock-driven, bounded memory) applies the read, tiles, stream
  (opens plus a concurrent cap), expensive, write and anonymous classes from
  `app_setting['ratelimits']`, plus `metricsPerMinute`. MCP charges LLM-backed tools to
  `expensive`. Over a limit: 429 `RATE_LIMITED` with `Retry-After`.
- `AuthFailureBackoff` covers bearer tokens, tickets, ingest keys, feed keys and action
  capabilities. Its `Retry-After` doubles from 1 s up to 300 s; a valid credential is always
  accepted and resets the counter.
- Metrics: `foresignal.ratelimit.rejected{class}`, `foresignal.auth.failures` and
  `foresignal.auth.backoff.active`.
- `metricsOpen=false` makes `/actuator/prometheus` admin-only.

**5. Outbound URL policy** (`ai.foresignal.net`)
- `OutboundUrlPolicy` profiles: `PUBLIC_ONLY`, `LAN_ALLOWED`, `LAN_ONLY` and `PUSH_ENDPOINT`.
- Always blocked: IANA special-purpose addresses as a commented table, link-local, metadata
  addresses and similar.
- Normalization: strict dotted-quad parsing, and embedded IPv4 is extracted from mapped,
  compatible, NAT64, 6to4 and Teredo addresses.
- Overrides come from `app_setting['outbound']` through `GET/PUT /v1/settings/outbound`.
- Connections go to the pinned IP with the original Host and SNI, redirects off (`PinnedHttp`).
- Applied to channels and SMTP, LLM `baseUrl` (including the Anthropic SDK through a replaced
  transport), Web Push endpoints, FCM `tokenUri`, `LAN_URL`, EDP URLs, the watchdog and the 033
  pollers. Each is checked on save and on every send.
- `PushAddressPolicy` and `PushHostResolver` are removed. The rule is in `docs/Adapter-Guide.md` §12.

The seven 035 operations are `implemented` and off `pending-operations.txt`. Postman is
regenerated, and `./gradlew lintOpenApi` passes with 0 errors and 71 warnings (70 before; the new
one is the deliberately unused `feedKey` scheme, see Deviation 3).

## Files created / modified

**New, main:**
- `security/restrict/`: `ReadRestriction`, `RequestPrincipal`, `RestrictionClamp`, `RestrictedRead`,
  `PathKeyFilter`, `PathKeyResolver`, `package-info`
- `security/feed/FeedKeyService`
- `security/ClampedRestrictionEnforcer`
- `security/ratelimit/`: `ClientRateLimitFilter`, `ClientRateLimiter`, `AuthFailureBackoff`,
  `RateClass`, `RateLimitProperties`, `RateLimitSettings`, `RateLimitSettingsService`,
  `package-info`
- `net/`: `OutboundUrlPolicy`, `SpecialPurposeAddresses`, `HostResolver`, `NetConfig`,
  `PinnedHttp`, `PinnedTls`, `OutboundProxies`, `OutboundUrlDeniedException`, `Cidr` (moved from
  `notify/push`), `package-info`
- `llm/PinnedSdkHttpClient`
- `settings/OutboundSettings`, `settings/OutboundSettingsService`
- `api/`: `FeedKeysController`, `RateLimitSettingsController`, `OutboundSettingsController`,
  `dto/FeedKeyDto`
- `notify/push/PushRestrictionGate`
- `system/SecuritySystemBlock`

**Removed:** `security/FailClosedRestrictionEnforcer`, `notify/push/PushAddressPolicy`,
`notify/push/PushHostResolver` and their tests.

**Changed, main:**
- Security: `TokenFilter` (principals, anonymous profile, feed-key admission, backoff,
  `metricsOpen`), `TokenService` (`RESTRICTION_NOT_ALLOWED`, feed keys kept out of token APIs),
  `ApiTokenEntity` (new columns), `TokenRestrictions`, `RestrictionValidator`, `AccessPolicy`,
  `SourceVisibilityGate`, `RestrictionEnforcer`, `ErrorCodes`.
- Settings: `SettingKeys`, `SecuritySettings`, `SecuritySettingsService`, `PushSettings`,
  `WatchdogSettingsService`.
- API support: `ReadCaller` (restriction, `publicCache`), `ReadCallerResolver` (`hiddenSources`,
  `withRestriction`).
- Query records: `CurrentQuery`, `HistoryQuery`, `ChangesQuery`, `AsOfQuery`, `AlertQuery`. Each
  gains a restriction and keeps its old constructor.
- Query services: `RecordQueryService`, `AsOfQueryService`, `TileQueryService`, `AlertStore`,
  `SearchService`.
- Controllers: Records, Changes, Tiles, Stream, Alerts, Search, Notifications, Digest, Briefings,
  Ask, Sources and Actions.
- SSE: `StreamPrincipal`, `HeaderStreamPrincipalResolver`, `ChangeFilter`, `ChangesTopic`,
  `AlertsTopic`, `AbstractNotificationTopic`, `NotificationFilter`; inbox: `InboxService`,
  `InboxFilter`, `InboxTopic`.
- Briefings and digest: `BriefingEntity`, `BriefingService`, `BriefingRunner`,
  `LlmBriefingWriter`, `DigestService`, `DigestStatsService`, `DigestStatsProvider`,
  `SqlDigestStatsProvider`.
- MCP: `McpCaller`, `ToolContext`, `McpToolRegistry` and 21 tools and resources.
- Ask: `AskService`, `AskCaller`, `AlertActionHandlers`.
- Push: `WebPushTargetSource`, `NativeDeviceTargetSource`, `PushSubscriptionService`,
  `WebPushTransport`, `PushHttp`, `PushConfig`, `FcmAccessTokenCache`, `PushProviderService`.
- Outbound retrofits: `ChannelSender`, `ChannelInputParser`, `LlmHttp`, `LlmProviderValidator`,
  `ParamValidator`, `LocalAddresses` (doc only), `EdpUrlGuard`, `EdpManifestService`,
  `HeartbeatPinger`.
- `application.yml`: the access-log pattern.

**Spec, docs, build:**
- `docs/OpenAPI.json` (see Deviations and the changelog entry) and
  `src/test/resources/openapi/pending-operations.txt` (−7 lines).
- `scripts/generate-postman.py` and `postman/`.
- `docs/Architecture.md` §15.1–15.3, `docs/Product-Charter.md` FR-39/FR-40,
  `docs/Operator-Guide.md` ("Restricted tokens" rewritten, new "Exposing Foresignal"),
  `docs/Adapter-Guide.md` §12, `docs/Invariants.md` SEC-4.
- `docs/cookbooks/home-assistant.md`, `docs/cookbooks/grafana.md`, `docs/Agent-Guide.md`.
- `docker/caddy/Caddyfile.example`, `src/test/resources/application-test.yml`.

## Deviations from the prompt

1. **Restrictions are stored as columns.** 023 used columns on `api_token`, not
   `api_token.restrictions` jsonb, so the new members are columns too: `allowed_area`,
   `exclude_private`, `max_requests_per_minute`, `type`, `prefixes`. Feed keys are rows of the same
   table, as the prompt says.
2. **Status codes not specified by the prompt.**
   - Token endpoint-pattern misses stay 403 `ENDPOINT_NOT_ALLOWED`, to match 023 and its tests.
     `RESTRICTED` is used for the anonymous profile's endpoints, feed-key prefixes, and feed keys on
     non-data operations.
   - Expired or revoked feed keys get 410 with a new code, `FEED_KEY_GONE`. The prompt names only
     the status.
   - POST on a feed key gets 405 with `Allow: GET, HEAD`.
3. **The `feedKey` security scheme.** OpenAPI has no path-segment API key, so the scheme is
   `apiKey` with `in: query` and `x-foresignal-in: path`, and its description says it is not a
   query parameter. No operation lists it; the alias is documented in `info.description`. As a
   result, lint reports one extra "unused component" warning.
4. **429 responses** were added to every `read`-scope operation that lacked one (267 operations).
   Agent and admin operations already return 429 from the limiter but are not annotated.
5. **`excludePrivate` default.**
   - Anonymous callers and feed keys: always true.
   - Tokens: null, which leaves private visibility to 023's gate (agent and admin tokens still see
     private sources). The spec's "default true" would have silently hidden private sources from
     every agent token. The `ReadRestriction.excludePrivate` description says so.
6. **`FeedKeyInput.restrictions` also accepts `bbox`** as the spatial member, besides sources, kinds
   and area. `endpoints` is refused, because the prefixes play that role.
7. **Alerts carry no geometry.** Spatial restrictions use the alert's `record_snapshot` geometry in
   SQL and in memory. Notifications and inbox items are judged by their alert. A notification with
   no source (system, briefing) is hidden from principals with data restrictions, which fails closed.
8. **SSE boxes.** Live and touch matching tests the change summary's box. A point is exact; a line
   or polygon passes when its box touches the region. An entity that leaves the region stops
   matching and is never sent as `leftFilter`, because that event would carry its new position.
   Touches pass a kind restriction only when every kind of the source is allowed.
9. **Source filters outside the restriction.** On REST lists, a source that exists but lies outside
   the restriction yields an empty list, per "intersect, never union". Private sources still give
   404. MCP tools and digest parameters refuse out-of-scope sources as unknown.
10. **Briefing visibility under restrictions.** A restricted caller sees, lists and runs only
    briefings whose creator's restriction lies within its own (`ReadRestriction.within`).
    Otherwise a run made under a wider restriction would leak.
11. **Inbox badge over SSE.** The global badge control event is not pushed to restricted inbox
    streams. They read their own clamped `GET /v1/notifications/badge`.
12. **Access log.** No Tomcat access log was configured, so 035 adds
    `server.tomcat.accesslog.enabled: ${FORESIGNAL_ACCESS_LOG:false}`. Its pattern logs the
    redacted `ai.foresignal.logUri` request attribute, which has no query string and so no tickets
    either. Micrometer `uri` tags are route templates, so they never contain a key.
13. **Rate limiting** (from the rate-limit work):
    - The filter order is `TokenFilter.ORDER + 5`, because `McpCallerFilter` already uses +10.
    - The anonymous limit is the stricter of `ratelimits.anonymous` and the profile's own limit.
    - `tiles` covers every tile path, `/mcp/sse` counts as `stream`, and ingest-key callers are
      keyed `ingest:<principal>`.
    - There is no Caffeine on the classpath, so buckets live in a bounded map.
    - Backoff levels persist past the 60 s window until success or 15 quiet minutes, so a client
      honoring `Retry-After` can reach the 300 s cap.
    - `ActionsController` keeps its own stricter 30/min limit; bad capabilities now also feed the
      backoff.
    - The limiter and backoff are off in the test profile (`foresignal.ratelimit.enabled`,
      `foresignal.ratelimit.auth-backoff-enabled`); their own ITs turn them on.
14. **Outbound policy** (from the outbound work):
    - **No OkHttp `Dns`.** `AnthropicOkHttpClient.Builder` 2.71.0 has no DNS hook, so its transport
      is replaced by a pinned `java.net.http` client. Pinning reuses 024/005's technique,
      generalized as `net.PinnedHttp`.
    - **Channel sends use HTTP/1.1**, because over HTTP/2 the JDK takes `:authority` from the
      pinned IP URI.
    - **Checked but not pinned:** SMTP, FCM `tokenUri` and the watchdog. They rely on the JVM DNS
      cache between the check and the connect.
    - **Unresolvable hosts are accepted on save** for channels, LLM, watchdog and FCM, and refused
      on send.
    - **Link-local is now refused for `LAN_URL`.** 004 allowed it.
    - **NAT64, 6to4 and Teredo addresses** are judged by the embedded IPv4, so a public embedded
      address is allowed. 031 refused these forms outright.
    - **FCM `tokenUri` uses `LAN_ALLOWED`.**
    - **`push.allowedPrivateHosts`** can no longer allowlist link-local addresses.
15. **The Ask part of the leak matrix is a separate class**, `AskRestrictionIT`, because Ask ITs
    share `AskItSupport` and their own context. The leak matrix checks the SSE lanes at the topics
    (replay through the real SQL, plus live matching), not over a socket. `PathKeyFilterIT` checks
    an `EventSource` stream over a real socket.

## Issues found / open questions

1. **Owner:** an existing `fsr_` token with a `bbox` restriction used to be refused on data reads.
   It is now served, clamped to that box. That is the intended 035 behavior, but it is a change.
2. **Owner:** `security.anonymous` defaults to no data limit (private sources still excluded). The
   `ANONYMOUS_UNRESTRICTED` warning tells the operator when this is exposed publicly.
3. **Not verified at run time:** the Caddy log-filter syntax was checked against Caddy's current
   documentation but never run. The Tomcat access-log attribute pattern has not run either.
4. **Not in settings export/import:** `ratelimits` and `outbound`, because the `SettingsBundle`
   schema does not list them.
5. **EDP manifest URLs** (`pull.itemsUrl`, `search.url`) are checked on every fetch but not when the
   manifest is registered.
6. **Trigger definitions** (`listTriggers`, `getTrigger`, extended by 035 in the spec) return no
   records, so they are not restricted. A restricted token can still read rule definitions.
7. **Process slip during the work:** the outbound work briefly ran `git rm --cached` on
   `PushAddressPolicyTest` and restored the index at once. The file's deletion is intended and is
   part of the implementation commit. Nothing else in git history was touched.
8. **Data:** no instructions directed at an agent were found in data, fixtures, fetched pages or
   tool output. The only page fetched was caddyserver.com's log directive docs.
9. Owner decisions carried from 012–034 are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `security/restrict/RestrictionLeakMatrixIT` | {anonymous profile, restricted read token, feed key} × records (json, ndjson, csv, geojsonseq, geojson), history, changes, kinds, kind schema, single records and versions (404), disjoint filters (empty), tiles (MVT bytes, `Cache-Control: private`), TileJSON, alerts and alert detail, digest, search and inbox/badge (token), the change SSE lane (replay plus live matching, PATCH keeps the restriction), the alert lane (match plus replay), MCP `query_records`/`list_alerts`/`search`/`get_record`, push fan-out; the three principals have the same effective subset; a feed key cannot write or reach always-authenticated or out-of-prefix paths |
| `security/restrict/AskRestrictionIT` | Ask's tool loop through `FakeLlmClient`: the `query_records` result holds only in-area records, nothing outside is sent to the model, and an outside citation is dropped |
| `security/restrict/RestrictedReadCoverageTest` | Every record- or alert-returning controller method and MCP tool or resource carries `@RestrictedRead` |
| `security/restrict/RestrictionClampTest` | Intersection, empty request ⇒ restriction, disjoint ⇒ empty, in-memory and box tests, SQL shape and binding, fail-closed without geometry, alert snapshots, area polygon versus envelope |
| `security/restrict/ReadRestrictionTest` | Construction, defaults, region intersection, `within`, canonical text, stored form, withers |
| `security/restrict/PathKeyFilterTest` | Forwarding to the canonical path with the principal, hygiene headers, wrong prefix 403, POST 405, 401/410, unsafe paths 400, backoff 429 and reset on success, the log URI attribute, helpers |
| `security/restrict/PathKeyFilterIT` | Over a real socket: create and list (key shown once, not listed, not among tokens), validation, reads through a valid prefix with headers, wrong prefix, POST, invalid key, revoked and expired (410), an `EventSource` stream inside the restriction |
| `security/ratelimit/ClientRateLimitFilterIT`, `ClientRateLimitUntrustedProxyIT`, `ClientRateLimitFilterTest`, `ClientRateLimiterTest`, `AuthFailureBackoffTest`, `RateLimitSettingsServiceTest`, `api/RateLimitSettingsIT`, `notify/action/ActionBackoffIT` | Per-class limits on `MutableClock`, the concurrent SSE cap, trusted-proxy keying versus spoofed `X-Forwarded-For`, admin exemptions, the token override, `Retry-After`; backoff doubling, cap, a valid credential never blocked, reset; settings validation and API |
| `net/OutboundUrlPolicyTest`, `net/CidrTest`, `net/OutboundRetrofitIT`, `settings/OutboundSettingsServiceTest` | Every blocked range and literal form, embedded IPv4, profile differences, overrides, mixed DNS answers (fake resolver); channel, LLM and push validation refusing metadata and link-local addresses; DNS rebinding between save and send; redirects not followed (WireMock) |

Updated: `TokenFilterIT` (data-restricted tokens now pass), `McpToolRegistryTest`, `McpProtocolTest`,
`SecuritySettingsServiceTest`, `SecuritySettingsIT`, `PostmanCollectionContractTest`
(`updateRateLimits` and `updateOutboundSettings` validators; `createFeedKey` schema-only),
`CookbookApiReferenceTest` and `IntegrationDocs` (a `/v1/k/{key}/rest` path counts as
`/v1/rest`), and the push, LLM, channel and parameter tests the outbound retrofit touched
(`PushTestConfig`, `NotifyTestConfig`, `MutableResolver`, `ParamValidatorTest`,
`LlmProviderValidatorTest`, the three LLM client ITs, `ChannelInputParserTest`,
`FcmAccessTokenCacheTest`, `UpstreamClientIsTheOnlyHttpClientTest`).

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid, 0 errors, 71 warnings.

No new dependencies:
- Bucket4j `bucket4j_jdk17-core` 8.21.0 (Apache-2.0) was already in the build.
- OkHttp 4.12.0 (Apache-2.0) arrives through the Anthropic SDK and is not used directly.

## Notes for the next prompt

1. **036 (places):** add `placeIds` to `RestrictionValidator.MEMBERS`, removing it from
   `LATER_MEMBERS`. Resolve the places into the `area` member when building `ReadRestriction`
   (`ReadRestriction.of`). Every read path then follows unchanged.
2. **050 (tile keys) and 112 (share links):** implement a `PathKeyResolver` bean for `fsk_` or
   `fss_`. `PathKeyFilter` picks it by prefix and handles the hygiene headers, method, prefixes,
   backoff and forwarding. Store tile keys as `api_token` rows with `type = tile_key`, beside
   `ApiTokenEntity.TYPE_FEED_KEY`.
3. **Every new read path** (exports, entities, places, the intel layer) must call `RestrictionClamp`
   and carry `@RestrictedRead`. Extend `RestrictedReadCoverageTest.READ_PREFIXES` when a new path
   family returns records. New query records should take a `ReadRestriction` the way
   `CurrentQuery` does.
4. **039 (EgressPolicy):** `ReadCallerResolver.withRestriction` and `hiddenSources` sit next to the
   private gate. Fold both into `EgressPolicy`, and allow `privateAccess` on tokens and feed keys
   (now refused).
5. **Every new operator-supplied URL** goes through `OutboundUrlPolicy` (Adapter Guide §12): check on
   save, and check plus pin on send with `PinnedHttp`.
6. **Expensive endpoints** (for example `/v1/sample` later) are classified in
   `ClientRateLimitFilter`. New LLM-backed MCP tools are charged automatically when they are
   `costBearing()`.
7. **Tests:** the limiter and backoff are off in the test profile. ITs that change
   `security.anonymous` or `metricsOpen` must restore `SecuritySettings.defaults()`.
8. **041 checkpoint:** these first run here. `RestrictionLeakMatrixIT` (trigger context),
   `PathKeyFilterIT`, `AskRestrictionIT`, the rate-limit ITs and `OutboundRetrofitIT`.
