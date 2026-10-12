# 031-CR-Web-Push

| | |
|---|---|
| Prompt | 031-Web-Push.md |
| Status | Complete |
| Implementation commit | 35290b3f816795f52d51927256b99fbca8c08b06 |
| Date | 2026-10-12 |

## Summary

Foresignal now pushes to browsers (PWA, operator console) and to native Android and Linux apps through
UnifiedPush, which uses the same RFC 8030/8291 wire protocol. It holds its own VAPID key, so no
third-party push account is needed. Delivery goes through a `PushTransport` SPI, so 032 adds APNs and
FCM as sibling beans.

The five operations the spec assigns to 031 are implemented and marked `implemented`, and their
lines are gone from `pending-operations.txt`:

- `getVapidPublicKey`, `listPushSubscriptions`, `createPushSubscription`, `verifyPushSubscription`,
  `deletePushSubscription`

`AccessPolicy` already had their rules: open, admin, and read with always-authenticated. These match
each operation's `x-foresignal-scope` and always-authenticated flag.

**Crypto (`ai.foresignal.notify.push`, JDK only).**

- `Hkdf`: HKDF-SHA-256 Extract and Expand on `HmacSHA256`. Java 21 has no HKDF API: `javax.crypto.KDF`
  is a preview in JDK 24 and final in JDK 25.
- `EcKeys`: P-256 key generation, the 65-byte uncompressed point (with an explicit on-curve check),
  raw scalars, PKCS#8, ECDH and base64url.
- `WebPushCrypto`: RFC 8291 encryption in the RFC 8188 `aes128gcm` coding, exactly as task 2 lays it
  out. Each message gets an ephemeral key and a random salt, and is sent as one record with the `0x02`
  delimiter. The plaintext is at most 3,993 bytes. `decrypt` is the receiver's side, which the tests
  use.
- `Vapid`: ES256 JWT `{aud, exp, sub}` with DER↔raw `R‖S` conversion, `verify`, `origin(endpoint)`
  and the `vapid t=…, k=…` header. `VapidTokens` caches one token per origin for 11 h, with `exp`
  12 h ahead.
- **Checked against the RFCs before committing** (a one-off `javac`/`java` run in the scratchpad, not
  a test run):
  - The RFC 8291 Appendix A body is reproduced byte for byte, and every intermediate value matches.
  - The RFC 5869 SHA-256 test cases 1 and 3 match.
  - RFC 8292 Figure 1's published JWT verifies with its published key, and our claim encoding of the
    same inputs is identical.
  - DER↔raw signatures interoperate with the JDK's `SHA256withECDSAinP1363Format`.

**VAPID keys.** `VapidKeyService` generates the key pair on first start (`PushBootstrap`,
`StartupOrder.PUSH = 17`):

- `system_secret` `push.vapid.public` holds the base64url point, 87 characters.
- `push.vapid.private` holds the PKCS#8 key encrypted with `CredentialCipher` (`nonce ‖ ciphertext`).
- The inserts use `on conflict do nothing`, so two instances starting at once agree on one pair.

**SPI.**

- `PushTarget` is sealed; `WebPushTarget` is its first kind.
- `PushTransport` (`id`, `supports`, `send`).
- `PushResult` (`Sent | Gone | Retry(after, counted) | Rejected(code, reason) | Failed(reason)`).
- `PushTargetSource` (`targets(message)`, `find(id)`, `onOutcome`).
- `PushFilters` (`events`, `ruleIds`, `minSeverity`, any-of `tags`), plus `PushMessage` (transport
  neutral), `PushOptions` and `Urgency`.
- **`WebPushTransport`**, for each send:
  - re-resolves the host and applies `PushAddressPolicy` (a refusal is `Rejected` with
    `URL_NOT_ALLOWED`);
  - takes a token from the per-host Bucket4j limit (`PushHostLimiter`, 20/s);
  - fits the payload to the budget, encrypts it and signs the VAPID token;
  - POSTs through `PushHttp` with the headers task 2 lists (TTL by event, Urgency by severity).
- **`PushHttp`** connects to the checked IP with the original Host header and SNI. It verifies the
  certificate against the host name, reusing 024's `HostCheckingTrustManager`, and never follows
  redirects.
- **Status mapping:** 2xx → `Sent`; 404/410 → `Gone`; 429/503 → `Retry` after `Retry-After`; 413 is
  logged and `Rejected`; other 4xx → `Rejected`; 5xx and I/O errors → `Failed`. Reasons never contain
  the endpoint or its path.

**Fan-out and dispatch.**

- `PushChannelType` (`webpush`, fan-out, renders nothing) is the type of the built-in channel "Push".
  The channel has a fixed id (`UUID.nameUUIDFromBytes("foresignal:channel:webpush")`), events
  alert/briefing/system, and is created at startup when missing.
- `DeliveryWorker` hands a claimed delivery of a fan-out type to its `FanOutDelivery` (new notify
  interface). It does this after the pause and quiet-hours check, and does not apply the per-channel
  rate limit.
- `PushFanout` asks every `PushTargetSource` for its targets and inserts one `push_dispatch` row per
  target. Each row carries the `PushMessage` taken at fan-out time and a `downgraded` flag. The parent
  delivery stays pending but not due. With no targets, it is delivered at once.
- `PushDispatchWorker` (2 s poll, `FOR UPDATE SKIP LOCKED` with a lease, virtual threads) sends each row
  and applies the outcome to that row only. Failures follow the 018 ladder. A local rate-limit retry
  does not count as an attempt.
- `PushDispatchStore.settleParent` locks the parent and then settles it:
  - **delivered** when every dispatch is terminal and at least one was sent, or every target was gone;
  - **failed** when none was sent and at least one failed. A failed parent raises
    `system.delivery_failed`, which is never sent back to the Push channel.
- `WebPushTargetSource` lists `active` subscriptions only, and records the outcomes:
  - `last_success_at` on a send;
  - gone → the row is deleted;
  - other failures → failure counters;
  - three consecutive `URL_NOT_ALLOWED` rechecks → `disabled`.

**Subscriptions (`PushSubscriptionService`, `PushController`).**

- **Validation**, every problem in one 400:
  - an https endpoint of at most 1000 bytes, without user info or fragment;
  - `p256dh` a 65-byte point on the curve, and `auth` 16 bytes;
  - `filters`, `transportLabel`, `payloadFormat` and `lang`.
- **Then** the address policy (400 `URL_NOT_ALLOWED`), and the caps of 5 pending per token and 200 in
  total (409 `PUSH_LIMIT_REACHED`). A repeated endpoint replaces the earlier row.
- **Verification:** a new row is `pending`, and the verification push is sent right away with TTL 600
  and Urgency high. Only the SHA-256 of the code is stored. The creating token echoes the code: a match
  makes the row active, a mismatch is 400 `VERIFY_FAILED`, and the fifth mismatch deletes the row.
- **Sweeper** (60 s): deletes pending rows older than 10 minutes, rows of revoked, expired or missing
  tokens, and orphan dispatches.
- **Revoke cascade:** 023 published `ApiTokenRevokedEvent` but had no push listener, so one was added.
  It deletes the token's rows in its own statement, outside the revoking transaction (027's note).
- **Delete:** the creating token or an admin; anyone else gets 404.
- **Admin list:** `endpointHost` only, never the endpoint.

**Payloads and topics.**

- **`PushContentFactory`:**
  - `id` = notification id;
  - body: alert body ≤ 1000 characters, briefing excerpt of 200, system events only their code;
  - links: `api` = the message URL; `app` or `web` from 023's `clientLinks` template, by scheme;
  - `tag`: `foresignal-alert-<alertId>` or `foresignal-<event>-<id>`;
  - `Topic` from `PushTopic`: `base64url(SHA-256(key))[0:32]`.
- **`PushPayloads`** renders the `foresignal` and `declarative` formats.
- **`PushPayloadBudget`** shrinks in the stated order and falls back to the minimal payload. Body cuts
  use 018's `Truncator`, which respects graphemes and UTF-8.

**Quiet hours.** The Push channel's `config.quietHoursMode` is `defer` (default), `downgrade` or
`drop` (Deviation 2). `downgrade` sends with `Urgency: very-low` and `silent: true`. Critical messages
bypass quiet hours through the quiet hours' `bypassSeverity`, which defaults to critical.

**Settings.**

- `ServerSettings.push` (`PushSettings`: `contact`, `allowedPrivateHosts`, `defaultLang`) is parsed and
  validated in `ServerSettingsService`. Allowlist entries are exact host names or CIDRs: no wildcards,
  IPv4 prefix ≥ /8, IPv6 ≥ /32; a bare address counts as a /32 or /128.
- The settings export and import carry `push` automatically.

**Spec, Postman, docs.**

- `docs/OpenAPI.json`: the 5 operations implemented; `PushSubscription.status` adds `disabled`;
  `Problem` lists `PUSH_LIMIT_REACHED`, `VERIFY_FAILED` and `URL_NOT_ALLOWED`; descriptions for
  replace-on-same-endpoint, idempotent verify, the VAPID key, the built-in webpush channel and the
  declarative member check; a changelog entry. `lintOpenApi` passes.
- Postman was regenerated with a `verifyPushSubscription` body, a fuller `createPushSubscription` body
  and `push` in `updateServerSettings`.
- `docs/Architecture.md`: §24 is now "Push" with subsections 24.1 Web Push, 24.2 UnifiedPush,
  24.3 Native push (032) and 24.4 SPI and delivery. The §7.1 `push_subscription` columns and the new
  `push_dispatch` table, the §17 `webpush` row and the §2 stack row are updated.
- `docs/Product-Charter.md` FR-53 now covers UnifiedPush, tags and verification.
- `docs/Operator-Guide.md` has a new section "Web Push and UnifiedPush": the Push channel and its
  quiet-hours modes, subscribing and verification, the service-worker snippet, the payload, the
  declarative format, the Flutter UnifiedPush snippet, the LAN distributor via `allowedPrivateHosts`,
  reach limits and delivery.

Tests were written but not run (feature prompt).

## Files created / modified

**New: `ai.foresignal.notify.push`**
- `Hkdf`, `EcKeys`, `WebPushCrypto`, `Vapid`, `VapidKeyService`, `VapidTokens`, `Cidr`
- `PushAddressPolicy`, `PushHostResolver`, `PushTls`, `PushHttp`, `PushHostLimiter`, `PushConfig`,
  `PushProperties`
- `PushTarget`, `WebPushTarget`, `PushTransport`, `PushResult`, `PushMessage`, `PushOptions`, `Urgency`,
  `PushFilters`, `PushTargetSource`
- `PushTopic`, `PushPayloads`, `PushPayloadBudget`, `PushContentFactory`
- `WebPushTransport`, `WebPushTargetSource`, `PushChannelType`, `PushFanout`, `PushDispatchStore`,
  `PushDispatchWorker`, `PushSubscriptionService`, `PushBootstrap`, `package-info`

**New: other**
- `api/PushController`, `notify/FanOutDelivery`, `notify/QuietHoursMode`, `settings/PushSettings`
- `store/PushSubscriptionEntity`, `store/PushSubscriptionRepository`, `store/PushDispatchEntity`,
  `store/PushDispatchRepository`

**Changed**
- `notify/DeliveryWorker`: the fan-out hand-off and the quiet-hours mode.
- `notify/NotificationService`: no quiet-hours deferral at enqueue for a fan-out channel in
  downgrade or drop mode.
- `notify/ChannelInputParser`: `webpush` is built in, so it can be updated but not created, and it is
  removed from `LATER_TYPES`.
- `notify/ChannelService`: `ensureBuiltIn`; testing a fan-out channel returns a failure that explains
  why.
- `settings/ServerSettings` (`push`, `effectivePush`), `settings/ServerSettingsService` (parses
  `push`; `View.push`).
- `api/error/ErrorCodes` (3 codes), `config/StartupOrder` (`PUSH`).
- `inbound/edp/EdpHttpClient`: `HostCheckingTrustManager` and `platformTrustManager()` are now public
  for reuse; the certificate message is neutral.
- `application.yml` (`foresignal.push.*`), `application-test.yml` (`push.scheduled: false`,
  `builtin-channel: false`).
- `docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`, `scripts/generate-postman.py`,
  `docs/Architecture.md`, `docs/Product-Charter.md`, `docs/Operator-Guide.md` (edited, not replaced).
- `src/test/resources/openapi/pending-operations.txt` (5 lines removed).
- Tests:
  - `api/ChannelsControllerIT`: 16 types, the `webpush` refusal message, an `mqtt` later-type check;
  - `settings/ServerSettingsServiceTest`: `push` accepted and validated;
  - `collect/UpstreamClientIsTheOnlyHttpClientTest`: `PushHttp` is a sanctioned outbound client.

No new dependencies. No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` change. Nothing was copied
from AGPL/GPL references.

## Deviations from the prompt

1. **`PushSubscriptionEntity` did not exist.** The prompt says "extend" it, but 002 never created it,
   so it was created with the Architecture §7.1 columns plus every task 4 column. Two extra columns:
   - `verify_attempts`, for the five tries;
   - `recheck_failures`, for "disabled after 3 consecutive rechecks".
2. **The quiet-hours mode is the Push channel's `config.quietHoursMode`.** 018's `QuietHours` has no
   `mode`; it always defers, and the spec's `QuietHours` schema has none either. Rather than change
   every channel's schema, the webpush type declares an enum config field `quietHoursMode` (`defer`
   default, `downgrade`, `drop`), served by `GET /v1/channels/types`. "drop" settles the delivery with
   the note "dropped during quiet hours". Critical bypass uses the quiet hours' `bypassSeverity`.
3. **`PushSubscription.status` adds `disabled`.** The prompt says "the subscription is disabled" after
   three failed rechecks, and the spec enum only had `pending|active`. Additive, with a changelog
   entry.
4. **`WebPushTarget` carries `lang` too.** It is needed for the payload's language (subscription,
   else `push.defaultLang`). 032 must add `NativeDeviceTarget` to the sealed `PushTarget`'s `permits`
   clause: a sealed interface needs that one-line edit, alongside a transport and a source.
5. **The "all gone" outcome.** A parent whose every dispatch is `gone` is `delivered`, with the note
   `push: 0 sent, N gone, 0 failed`. The prompt only defines all-failed and at-least-one-sent.
   `Rejected` results (413, other 4xx, `URL_NOT_ALLOWED`) are terminal `failed`, never retried.
6. **The verification push is sent synchronously** during `POST /v1/push/subscriptions`, after the row
   commits. If the push service refuses it, the row stays pending and expires.
   - Declarative subscriptions get a declarative notification: the body carries the code, `navigate`
     is `publicBaseUrl`, and `data` is the verify payload. A Safari client without a service worker
     cannot echo the code automatically, but the user can read it.
7. **Declarative members, checked against WebKit's "Meet Declarative Web Push" post.** The 16574 post
   the prompt links has no schema; it points to the "Meet" post.
   - Required: `web_push: 8030`, `notification.title`, `notification.navigate`.
   - Optional: `body`, `lang`, `dir`, `silent`, other NotificationOptions members, and `app_badge`.
   - Differences from the prompt: `app_badge` sits inside `notification` there, not at the top level;
     `dir` exists. Foresignal sends neither.
8. **`ServerSettingsService.View` gained `push`** (the settings in force), so `getServerSettings`
   returns it.
9. **The built-in channel is not created in the test profile** (`foresignal.push.builtin-channel:
   false`). Otherwise every notification IT would see an extra channel and extra deliveries; the push
   ITs create it with `PushBootstrap.ensureChannel()`. Deleting the Push channel through the API is
   possible; it comes back at the next start.
10. **Test TLS.** The WireMock push service is https with its self-signed certificate. The push ITs
    replace the `PushTls` bean with a trust-all context (test only) and DNS with `MutableResolver`.
    WireMock 3.13.2's Jetty is configured with `new SecureRequestCustomizer(false)` (checked in the
    jar's bytecode), so the `push.test` SNI is accepted.

## Issues found / open questions

1. **Unverified at run time.** No test has run. These are first exercised at checkpoint 041:
   - the JDK `HttpClient` with SNI set on a pinned IP against WireMock https;
   - `settleParent` under concurrency;
   - the Hibernate-created `push_*` tables.
2. **A guard was already red before this prompt.** `UpstreamClientIsTheOnlyHttpClientTest` does not
   list `inbound/edp/EdpHttpClient.java` (024), which builds its own `HttpClient`, so the guard should
   already fail. 031 added only `PushHttp` to the sanctioned list. **Owner/041:** sanction
   `EdpHttpClient` too.
3. **The verify push is synchronous.** A slow push service delays `POST /v1/push/subscriptions` by up
   to the 5 s connect plus 15 s request timeout.
4. **Rate buckets are in memory**, per process, as in 018. A restart can burst up to 20 per host.
5. **Never sent to a real push service.** Payloads, headers and crypto follow the RFCs and match their
   vectors. A manual check with a real browser and a UnifiedPush distributor would confirm interop.
6. **Data.** No instructions directed at an agent were found in data, docs, web pages or tool output.
   Fetched for verification:
   - RFCs 8291, 5869, 8292 and 8188 (rfc-editor.org);
   - WebKit blog posts 16574 and 16535;
   - pub.dev docs of `unifiedpush` and `unifiedpush_platform_interface`.
7. Owner decisions carried from 012–030 are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `notify/push/WebPushCryptoTest` | • the RFC 8291 Appendix A body byte for byte, and its header<br>• intermediates: ecdh_secret (both sides), PRK_key, IKM, PRK, CEK, NONCE<br>• decryption of the published body; a max-size round trip, exactly 4096 bytes<br>• refusals: plaintext of 3994 bytes, bad auth, off-curve or short key, tampered body<br>• RFC 5869 cases 1, 2 and 3, and the expand length limit<br>• RFC 8292 Figure 1: the JWT verifies, our header and claims encode identically, sign/verify round trip, wrong key and tampered signature<br>• `origin`<br>• DER↔raw over 50 signatures, cross-checked with P1363; leading-zero padding<br>• point and scalar encodings, base64 variants |
| `notify/push/PushPayloadBudgetTest` | • untouched small payload<br>• multibyte body cut first (no lone surrogates; later steps unused)<br>• shrink order: body → links.api/url → tags 5 → title 120<br>• minimal fallback with a small cap<br>• declarative shape and cap<br>• many character classes (emoji, ZWJ, quotes, control characters) always ≤ 3993<br>• verify payload, both formats |
| `notify/push/TopicDerivationTest` | • ≤ 32 characters in the base64url alphabet<br>• stable per entity, distinct across entities<br>• the key formats<br>• the known SHA-256("") prefix |
| `notify/push/PushAddressPolicyTest` | • 40 blocked addresses: loopback, RFC 1918, CGNAT, link-local, metadata, 0/8, multicast, 240/4, broadcast, IETF, benchmarking, documentation, ULA, site-local, IPv4-mapped and -compatible, NAT64 (both), 6to4, Teredo, discard<br>• global addresses allowed and pinned<br>• mixed public and private DNS answers refused<br>• allowlist by host, by CIDR and by bare address; every address must match<br>• metadata always refused<br>• unresolvable hosts<br>• entry validation (wildcards, prefixes)<br>• CIDR math |
| `notify/push/PushSubscriptionsIT` | MockMvc and WireMock https:<br>• the open VAPID key<br>• 14 validation failures, nothing sent<br>• private and mixed hosts → `URL_NOT_ALLOWED`<br>• 401 without a token, 403 list for a read token<br>• pending → verify push (TTL 600, Urgency high, decrypted code) → active, idempotent<br>• wrong code ×5 deletes; another token gets 404<br>• expiry and sweep<br>• 5-pending and 200-total caps<br>• same endpoint replaces<br>• delete ownership<br>• the admin list hides endpoints<br>• revoke cascade and expired-token sweep<br>• `PUT /v1/settings/server` push round trip and validation |
| `notify/push/WebPushDeliveryIT` | WireMock https push service:<br>• headers (Content-Encoding, Content-Type, TTL, Urgency, Topic, VAPID with a verified JWT, aud/sub/exp) and a body that decrypts with every member<br>• urgency by severity<br>• quiet hours downgrade (very-low and silent), critical bypass, defer, drop<br>• filters: events, ruleIds, minSeverity, tags<br>• only active rows, and delivered with no targets<br>• 410 deletes<br>• 429 `Retry-After`<br>• a retry re-sends only to the failed target<br>• ladder exhaustion fails the parent, with the error redacted<br>• declarative shape<br>• DNS rebinding blocked, with `disabled` after 3 rechecks<br>• system and briefing TTL, topic and body<br>• the built-in channel: fixed id, not creatable, not testable |

Test support:
- `testsupport/push/PushTestConfig`: `MutableClock`, `MutableResolver` and trust-all `PushTls`.
- `testsupport/push/MutableResolver`
- `testsupport/push/TestSubscriber`: keys generated at test time, request bodies, decryption.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → `Woohoo! Your API description is valid.`, 70 warnings (unchanged), 0
errors.

`python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 825 requests, all operations
covered.

No new dependencies.

## Notes for the next prompt

1. **032 (native push):**
   - add `NativeDeviceTarget` to `PushTarget`'s `permits` clause;
   - declare `PushTransport` beans (APNs, FCM) and a `NativeDeviceTargetSource`.
   `PushFanout` and `PushDispatchWorker` pick them up through `List` injection. They also pick up
   `push_dispatch.target_type`, `PushFilters` (reuse `parse`, `matches` and `toMap`), `PushOptions`
   (TTL, urgency, downgrade) and `PushMessage` (`shortTitle`, `tag`, `topic`). Devices of a revoked
   token: listen for `ApiTokenRevokedEvent` as `PushSubscriptionService.onTokenRevoked` does.
2. **033 (actionable, inbox):** `PushPayload.actions` and `confirmUrl` are in the spec but not sent
   yet. Add them in `PushPayloads.foresignal`, and keep them within `PushPayloadBudget`.
3. **035 (access perimeter):** replace `PushAddressPolicy` with `OutboundUrlPolicy` profile
   `PUSH_ENDPOINT`. It is used in `PushSubscriptionService.create` and `WebPushTransport.sendOnce`.
4. **041 (checkpoint):**
   - the push ITs share one cached context (`PushTestConfig`);
   - `push.builtin-channel` is off in the test profile;
   - see Issue 2 about `EdpHttpClient` and the HTTP-client guard.
5. **060 (MQTT):** implement `notify.FanOutDelivery` for a fan-out type; `DeliveryWorker` hands
   deliveries to it.
