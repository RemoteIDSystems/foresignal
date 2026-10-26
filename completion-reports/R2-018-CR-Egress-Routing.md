# R2-018-CR-Egress-Routing

| | |
|---|---|
| Prompt | R2-018-Egress-Routing.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `675b138d6e94e90894862f2cf899aff271a8fefc` |
| Date | 2026-10-26 |

## What was added

- **Migration `V23__egress_route.sql`:**
  - `egress_route`: id, name (unique on `lower(name)`), type, enabled, url, bound_interface, the sealed secrets
    (`secrets_cipher`, `secrets_nonce`), `exit_ip` and `exit_ip_checked_at`, and timestamps.
  - `egress_backoff`: per-(source, route) holds, with the route as a foreign key on delete cascade.
  - `source_settings` gains `egress_route_id` and `egress_excluded` (default false).
- **New package `ai.foresignal.egress`:**
  - `EgressRoute` (record) and `EgressRouteStore` (JDBC; pages by name with a cursor; deleting a route clears the
    pins to it).
  - `EgressRouteService`: CRUD and validation, every problem in one 400.
    - A proxy URL must be only `scheme://host:port`, and its host must pass the `LAN_ALLOWED` outbound policy.
    - The secrets are sealed together with `CredentialCipher`. An omitted secret keeps its value on a replace; an
      empty string or null clears it.
  - `EgressSettings` and `EgressSettingsService`: settings key `egress`; default `direct`; `defaultRoute` must
    exist.
  - `RouteSelector`: the pure policy (the rules are listed under Decisions).
  - `EgressBackoff`: per-(source, route) holds, persisted, with the same arithmetic as `BackoffRegistry`. Its new
    static `throttleDelay` is shared with `BackoffRegistry`.
  - `EgressHealthRegistry`: in-memory health per route. Three consecutive connection failures mark a route
    unreachable and hold it off on a growing delay. Alerts `system.egress_unhealthy` and `system.egress_recovered`
    go through `OperationalAlertService`, edge-triggered.
  - `EgressTunnel`: a loopback `CONNECT` bridge for SOCKS5 routes and authenticated HTTP routes.
  - `EgressRouter`: the collector's entry point. `decide` returns direct, a route, a wait or a refusal. Each route
    gets its own pair of clients (following and not following redirects), built from the new
    `UpstreamClient.clientBuilder`.
  - `EgressProbe`: `testEgressRoute`.
- **`api/EgressController`:** all ten operations.
- **Collector wiring (`UpstreamClient`, `BoundSourceHttp`):**
  - `UpstreamClient` gets `setEgressRouter` as an optional setter, so the unit tests that construct it by hand keep
    sending direct.
  - Every `BoundSourceHttp` path chooses its route after the backoff check and before pacing and the budget:
    `execute` (so GET, HEAD, ranges and the S3 listing), `post`, `download`, `openStream` and `openWebSocket`.
  - Pacing is keyed by `host@egress:{routeId}`.
  - Through a route, a 429 or any `Retry-After` goes to `EgressBackoff` instead of the source backoff.
  - A connection failure counts against the route's health; any answer counts as a success.
- **`SourceService.setEgressAssignment`**, and the two new columns on `SourceSettingsEntity`.
- **Tests (written, not run):**
  - `RouteSelectorTest` (unit);
  - `EgressRouteServiceTest`, `EgressFetchIT` and `EgressHealthIT`, with the shared fixture `EgressItSupport`.
- **Guard change:** `UpstreamClientIsTheOnlyHttpClientTest` now sanctions `egress/EgressTunnel.java` by name for
  `new ServerSocket(` and `new Socket(` only, and asserts that it still opens both, in the same exact-list style as
  the SERVE listener.
  - The tunnel builds no `HttpClient`. The route clients are built through `UpstreamClient.clientBuilder`, so the
    HttpClient half of that guard is untouched.
- **Docs:**
  - `docs/Network-Sourcing.md` was replaced whole, with C1 now describing what was built.
  - `docs/Operator-Guide.md` gained a section, "Egress routing (R2-018)".
  - `docs/Invariants.md` registers EGR-4 and EGR-5 with their tests.

## Contract

- **`EgressRoute`** gains the read-only fields `available` and `unavailableReason`.
- **`EgressHealth`** gains `lastError` and `sources`. `sources` appears only on `getEgressRouteHealth` and lists the
  per-source holds.
- **`EgressSettings`, `SourceEgressAssignment`:** the descriptions now say what the code does (see the first
  disagreement below).
- **`listEgressRoutes`** gains a 400 for a bad cursor or limit.
- **Descriptions:** each of the ten operations gained an R2-018 paragraph (validation, write-only secrets, fail
  closed, the probe URL, health semantics).
- **Postman:**
  - examples for `createEgressRoute`, `updateEgressRoute`, `updateEgressSettings` and `assignSourceEgress`;
  - the path variable `{{egressRouteId}}`, captured from `createEgressRoute`;
  - the collection was regenerated.
- **Left alone:** every R2 operation stays `x-foresignal-status: planned`; the SDKs were not regenerated; the frozen
  v1.0.0 contract and the `x-foresignal` changelog are untouched.

## Decisions and disagreements for the owner

1. **The contract and the code disagree about `defaultRoute`. Which should change?**
   - The authored `EgressSettings.defaultRoute` said "Route used when rotation is off or a source has no assignment".
   - The code treats `direct` as the master off-switch: nothing is routed, pins and the default route included. So
     `direct` "keeps today's behavior exactly", as the prompt requires.
   - With rotation on, the default route is used only by a source that is excluded from rotation. An unassigned
     source rotates.
   - I rewrote the schema descriptions to match the code.
2. **Fail closed.**
   - With rotation on and no usable route, a pull fails; it does not go direct.
   - A pinned route that is unusable fails the pull rather than moving it to another exit.
   - An excluded source with no default route goes direct. That is the only way a source leaves direct while rotation
     is on, and it needs that explicit assignment.
3. **SOCKS5 and authenticated HTTP go through a loopback bridge (`EgressTunnel`).**
   - Verified in the JDK source: `HttpRequestImpl.retrieveProxy` uses a proxy only when its type is `HTTP`. A SOCKS
     `ProxySelector` would therefore be ignored silently, and the request would go direct.
   - The bridge carries `CONNECT` only, so plain-http requests through these routes fail with a 502 and are never
     sent direct. An unauthenticated HTTP route carries both http and https.
   - The bridge listener is unauthenticated and bound to the loopback address. Any process on the host can use it
     while the route exists.
   - This needed the socket-guard change above, which is yours to accept.
4. **WireGuard is recorded and sealed, but always unavailable.** The server does not bring tunnels up.
   - Bringing tunnels up would need `NET_ADMIN` and kernel or userspace WireGuard in the container.
   - The documented path is to bring the tunnel up on the host and add its interface as an `interface` route.
5. **What is per egress, and what stays per source.**
   - Per egress: a 429, or any `Retry-After`, received through a route.
   - Per source:
     - a refusal (451, or 403 where access denied is terminal), so no other egress is tried for a refused client;
     - a 503 without `Retry-After`, which is an upstream outage.
6. **Pinned LAN hosts are never routed.** Re-resolving them through a proxy would undo the DNS pinning.
7. **A permessage-deflate WebSocket cannot be proxied** (`DeflateWebSocket` opens its own socket). While the source
   would be routed, it is refused rather than opened direct.
8. **Health is in memory** and starts empty after a restart. Only the exit IP from a test is persisted.
9. **The exit-IP service** is `https://checkip.amazonaws.com/`, run by Amazon Web Services.
   - It is overridable with `foresignal.egress.probe-url` and is checked against `PUBLIC_ONLY`.
   - This is an outbound call to a third party, but only when an admin presses Test.
10. **`UNKNOWN_SOURCE`** is used for `assignSourceEgress` only, because the authored contract names it. Elsewhere an
    unknown source is `NOT_FOUND`.
11. **The prompt names `FetchExecutor` as a wiring point; it was not changed.** It sends nothing itself: every source
    request goes through `BoundSourceHttp`.
    - The search that established this:
      `grep -rln "parent.client(\|.client(true)\|.client(false)\|HttpClient.newBuilder\|clientBuilder(" src/main/java`
    - Outside `collect/`, it found only `egress/`, `net/PinnedHttp`, `inbound/edp/EdpHttpClient` and the two
      `notify/push` senders.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff (the ten rules were already
  present).
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.
  - The guard tests (`UpstreamClientIsTheOnlyHttpClientTest`, `InvariantRegistryTest`) were edited or extended but
    not executed.
  - Nothing has been sent through a real proxy.
  - `EgressFetchIT` assumes that WireMock serves a stub for an absolute-form (proxied) request line. This has not
    been run.

## Notes for the next prompt

- R2-019…R2-023 extend `docs/Network-Sourcing.md` (sections C2 and C3). EGR-6 and EGR-7 are still to be registered
  in `docs/Invariants.md`.
- The next Flyway migration is **V24**.
- A first-party aggregator read from `localhost` or the LAN should go through a pinned `LAN_URL` parameter. Requests
  to pinned hosts are never routed, so rotation cannot send a local read out through a proxy.
