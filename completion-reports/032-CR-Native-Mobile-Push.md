# 032-CR-Native-Mobile-Push

| | |
|---|---|
| Prompt | 032-Native-Mobile-Push.md |
| Status | Complete |
| Implementation commit | bd6bb2192c28ce589f73426ee3b54c468cdaf752 |
| Date | 2026-10-12 |

## Summary

Foresignal now delivers alerts to native apps through APNs (iOS, iPadOS, macOS) and FCM (Android).
Both are new `PushTransport` beans behind the 031 fan-out. Each push is a **visible** alert that
carries only ids, the severity and a minimal (or, per device, a short) text. Every operator enters
their own APNs key and Firebase service account at runtime. No credential is needed or committed,
and no real push was sent: tests use WireMock stand-ins and keys generated at test time.

The nine operations the spec assigns to 032 are implemented and marked `implemented`, and their
lines are gone from `pending-operations.txt`:

- admin: `listPushProviders`, `putPushProvider`, `deletePushProvider`, `testPushProvider`
- read, always authenticated: `registerPushDevice`, `listPushDevices`, `updatePushDevice`,
  `deletePushDevice`, `testPushDevice`

`AccessPolicy` gains `/v1/push/providers` and `/v1/push/providers/**` as admin, placed before
`/v1/push/devices` and `/v1/push/devices/**`, which are read and always authenticated. This matches
each operation's `x-foresignal-scope` and its always-authenticated flag.

**Request formats, checked against the official docs before coding:**
- Apple: "Sending notification requests to APNs", "Handling notification responses from APNs" and
  "Establishing a token-based connection to APNs". Read through `developer.apple.com/tutorials/data/…json`,
  because the HTML pages need JavaScript.
- Google: "FCM HTTP v1 send", "FCM error codes", "OAuth 2.0 for server-to-server applications", and
  the FCM v1 discovery document (`fcm.googleapis.com/$discovery/rest?version=v1`) for the
  `AndroidConfig` and `ApnsConfig` field names and the `NORMAL`/`HIGH` enum.

**Providers (`notify.push.provider`).**
- `PushProviderEntity` (`push_provider`): the columns the prompt lists.
- `PushProviderService`:
  - Validates every problem in one 400: the slug id, `type`, 1–50 `appIds`, and the config of each
    type. APNs: `teamId` and `keyId` are 10 characters A–Z/0–9, `keyEnvironment` is
    `sandbox|production|both`, plus `timeSensitive`. FCM: `projectId`, `clientEmail`, and `tokenUri`
    (default Google's endpoint).
  - `ProviderKeys` checks keys with the JDK only. The `.p8` must be a PKCS#8 EC key whose curve
    and order equal P-256. The service account's `private_key` must be PKCS#8 RSA of at least 2048
    bits.
  - Accepts the whole service-account JSON (as a string or an object). Its `project_id`,
    `client_email` and `token_uri` fill config members left out. Only the PEM key is stored,
    encrypted with `CredentialCipher`.
  - Omitted `secrets` keep the stored key, unless the type changes.
  - Responses list `secretFields` and never a secret.
- Settings export carries `pushProviders` without keys. Import upserts them: an existing provider
  keeps its key, and a new one is stored disabled with a warning.
- The self-hosting constraint is documented in the class Javadoc, Architecture §24.3, the Operator
  Guide and the Client Guide.

**Devices (`notify.push.device`).**
- `PushDeviceEntity` (`push_device`): the prompt's columns plus `token_suffix`, `device_name` and
  `app_version` (Deviation 4). `(transport, token_hash)` is unique.
- `PushDeviceService.register` validates the input:
  - APNs: a token of 64–200 hex characters (stored lower case), a required `environment`, and an
    Apple platform.
  - FCM: a token of 1–4096 characters.
  - For both: filters (031 `PushFilters.parse`), `contentLevel` and `lang`.
- Registration then requires an enabled provider with a key that serves the app and, for APNs,
  covers the environment. Otherwise it returns 409 `PUSH_PROVIDER_NOT_CONFIGURED`, whose detail says
  whether the app or the environment is the problem.
- Upsert by `(transport, SHA-256(token))`: re-registering moves the device to the calling token
  (200; a new token is 201). A token may hold 20 devices (409 `PUSH_LIMIT_REACHED`). `deviceName`
  and `appVersion` are copied from the token's 023 device metadata.
- Visibility: admin sees all; other tokens see only their own, and anything else is 404. Updates
  change `name`, `filters`, `contentLevel` and `lang`. Delete is for the owner or an admin.
- Revoke cascade: an `ApiTokenRevokedEvent` listener deletes the token's devices in its own
  statement (027's note). An hourly sweep (`foresignal.push.native.expiry-sweep-interval`) deletes
  devices of expired, revoked or deleted tokens.
- `security.ClientDeviceResolver.currentDevice()`: the device identity is the API token id when the
  token carries device metadata. It is shared with 033.
- `NativeDeviceTargetSource` (031 SPI) lists active devices whose filters match and whose app an
  enabled provider serves. Outcomes:
  - sent → `last_success_at` and `failure_count` reset to 0;
  - gone → the device is deleted;
  - other failures → `failure_count` and `last_failure_at`.
- `NativePushTester`:
  - Provider test: mints the credential and records `last_test_at` and `last_error`. Given a
    `deviceId`, it also sends that device a test notice through that provider.
  - Device test: owner only, 5 per hour per device (429 `RATE_LIMITED` with `Retry-After`).

**SPI changes (`notify.push`).**
- `NativeDeviceTarget` is added to the sealed `PushTarget`'s `permits`, as 031's notes asked.
- `PushOptions` gains `downgraded` and `dispatchId`, keeping the old 4-argument constructor. The
  dispatch worker passes the row id.
- `PushMessage` gains nullable `badge` (filled by 033) and `category` (`FORESIGNAL_<EVENT>`, set by
  `PushContentFactory`). The 18-argument constructor stays, and both new fields survive `toMap` and
  `fromMap`.
- `NativeContent` holds the shared text rules:
  - `minimal`: title "Foresignal"; body "&lt;Severity&gt; alert", "Briefing ready" or "System notice";
  - `short`: title "Foresignal · &lt;severity&gt;"; body the `shortTitle`, cut to 40 characters with
    `Truncator`.
  It also builds the collapse id, the thread id, the category, and the link: the 023 `clientLinks`
  app link, else the web link.
- `NativePushHttp` (one `HttpClient` per key, `Version.HTTP_2`, no redirects), `NativePushTls`
  (production: the JDK default, no pinning) and `NativePushEndpoints` / `NativePushProperties`
  (`foresignal.push.native.*`; the defaults are Apple's and Google's hosts).

**APNs (`notify.push.apns`).**
- `ApnsJwtCache`:
  - The token is an ES256 JWT with header `{"alg":"ES256","kid"}` and claims `{"iss","iat"}`. The
    DER signature is converted to raw `R‖S` with 031's `Vapid.derToRaw`.
  - One token per provider, reused for 40 minutes. A changed key or config (`id:updatedAt`
    fingerprint) gets a new one.
  - `forceRefresh` only replaces the caller's stale token, so a burst of `ExpiredProviderToken`
    answers produces one new token.
- `ApnsPayloads`:
  - Body and headers as task 3 specifies, kept within 4096 bytes. If it is too large, the link is
    dropped first, then the text falls back to minimal.
  - `interruption-level` by severity: `time-sensitive` only when the provider sets it, never
    `critical`. A quiet-hours downgrade is `passive`, with no sound and priority 5.
  - `apns-id` is `UUID.nameUUIDFromBytes("foresignal:push_dispatch:<id>")`, so retries of one row
    reuse it. No background push is ever sent.
- `ApnsTransport` posts to `<host>/3/device/<token>`. The host is production or sandbox by the
  device's environment. There is one connection per provider and environment, because APNs refuses
  tokens of several teams on one connection. Status mapping:
  - 200 → sent;
  - 410, `BadDeviceToken`, `DeviceTokenNotForTopic` → gone;
  - 403 `ExpiredProviderToken` → one refresh and a retry;
  - other 403, and 429 `TooManyProviderTokenUpdates` → provider error;
  - 429 `TooManyRequests` → retry after `Retry-After`, or 1 minute;
  - 5xx → the 018 ladder;
  - 413 and other 4xx → failed for good.

**FCM (`notify.push.fcm`).**
- `FcmAccessTokenCache`:
  - The assertion is an RS256 JWT `{iss, scope, aud: tokenUri, iat, exp: iat+3600}`, POSTed as the
    jwt-bearer grant.
  - The access token is cached until `expires_in − 300 s`.
  - JDK crypto only; `firebase-admin` was not added, as the prompt decided.
- `FcmPayloads`:
  - A data message with string values `v, id, a, s, ev, l, t, c`, and `android.priority`
    `HIGH|NORMAL`, `ttl` `"<s>s"` and `collapse_key`.
  - An Apple device registered through FCM also gets `apns: {headers, payload}`.
- `FcmTransport` posts to `<host>/v1/projects/<projectId>/messages:send`. It reads the error code
  from the `FcmError` detail, falling back to `status`:
  - `UNREGISTERED`, `SENDER_ID_MISMATCH`, and an `INVALID_ARGUMENT` whose `BadRequest` field
    violation names the token → gone;
  - 401 → drop the token and retry once, then provider error; 403 and `THIRD_PARTY_AUTH_ERROR` →
    provider error;
  - 429 → `Retry-After`, at least 1 minute;
  - 503 with `Retry-After` → that wait; other 5xx → the ladder.

**Provider errors.**
- `PushProviderService.providerError` writes `last_error` as a reason code only (for example
  `APNs 403 InvalidProviderToken`).
- A conditional update (`where last_error is null`) makes exactly one caller raise
  `NotificationRouter.pushProviderFailed` (`system.push_provider_failed`). Its filter excludes the
  `webpush` channel.
- The dispatch retries after `provider-retry` (15 minutes), and a success clears the error.
- Responses, logs and `last_error` never contain a key, a device token or alert text: reasons are
  matched against `^[A-Za-z]{1,64}$` and `^[A-Z_]{1,64}$`.

**Spec, Postman, docs.**
- `docs/OpenAPI.json`:
  - 9 operations implemented, and `Problem` lists `PUSH_PROVIDER_NOT_CONFIGURED` (409).
  - Additive reconciliations, listed in a changelog entry: the descriptions of registerPushDevice,
    testPushDevice, putPushProvider and deletePushProvider; FcmProviderConfig and `tokenUri`;
    PushDevice.status; `NativePushPayload` `c`, `token`, `category` and `id`;
    SettingsBundle.pushProviders; NotificationMessage.subtype.
  - `lintOpenApi` passes.
- Postman, regenerated (827 requests, all operations covered):
  - bodies for `putPushProvider` (APNs, plus an FCM variant), `testPushProvider`,
    `registerPushDevice` (APNs, plus an Android variant) and `updatePushDevice`;
  - variables `pushProviderId`, so push providers no longer share `{{providerId}}` with the LLM
    providers, and `pushDeviceId`, captured by `registerPushDevice`.
- `docs/Architecture.md`: §24.3 rewritten as "Native push"; §7.1 gains the `push_provider` and
  `push_device` rows; the §17 `webpush` row is updated.
- `docs/Product-Charter.md`: FR-35 and FR-53 updated.
- `docs/Operator-Guide.md` has a new section "Native push (APNs and FCM)":
  - the self-hosting constraint and the content-level privacy trade-off;
  - how the extension reaches Foresignal (LAN, VPN or Tailscale) and the fallback text;
  - creating an environment-specific APNs key, creating a Firebase service account;
  - testing and monitoring, and devices.
  The settings export paragraph and the Web Push reach paragraph were edited, not replaced.
- `docs/Client-Guide.md` (new) has a section "Native push for the Flutter app":
  - the payloads, APNs registration and environments;
  - a Swift Notification Service Extension that reads `e`, fetches `/v1/alerts/{id}` (until 033)
    with the Keychain token on a 5 s budget, and keeps the fallback text otherwise;
  - the `FORESIGNAL_*` categories, with actions arriving in 033;
  - Android data messages, channels per severity, and the rule that every HIGH message must be shown;
  - `foresignal://` deep links; Live Activities stay on the client roadmap.

Tests were written but not run (feature prompt).

## Files created / modified

**New: `ai.foresignal.notify.push`**
- `NativeDeviceTarget`, `NativePushTransport`, `NativeContent`
- `NativePushHttp`, `NativePushTls`, `NativePushEndpoints`, `NativePushProperties`

**New: `notify.push.provider`**
- `PushProviderEntity`, `PushProviderRepository`, `PushProviderService`
- `ProviderKeys`, `ProviderCredentials`, `package-info`

**New: `notify.push.device`**
- `PushDeviceEntity`, `PushDeviceRepository`, `PushDeviceService`
- `NativeDeviceTargetSource`, `NativePushTester`, `package-info`

**New: transports**
- `notify.push.apns`: `ApnsJwtCache`, `ApnsPayloads`, `ApnsTransport`, `package-info`
- `notify.push.fcm`: `FcmAccessTokenCache`, `FcmPayloads`, `FcmTransport`, `package-info`

**New: other**
- `api/NativePushController`, `security/ClientDeviceResolver`, `docs/Client-Guide.md`

**Changed**
- `notify/push/PushTarget` (`permits`), `PushOptions`, `PushMessage`, `PushContentFactory`,
  `PushDispatchWorker`, `PushConfig`, `package-info`
- `notify/NotificationRouter` (`pushProviderFailed`)
- `api/error/ErrorCodes` (`PUSH_PROVIDER_NOT_CONFIGURED`)
- `security/AccessPolicy`
- `settings/SettingsTransferService` (`pushProviders`)
- `application.yml` (`foresignal.push.native.*`)
- `docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`, `scripts/generate-postman.py`
- `docs/Architecture.md`, `docs/Product-Charter.md`, `docs/Operator-Guide.md` (all edited)
- `src/test/resources/openapi/pending-operations.txt` (9 lines removed)
- Tests:
  - `collect/UpstreamClientIsTheOnlyHttpClientTest`: `NativePushHttp` is sanctioned;
  - `openapi/PostmanCollectionContractTest`: validators for `registerPushDevice` and
    `updatePushDevice`, which call the production static parsers; `putPushProvider` and
    `testPushProvider` are schema-only, with the reason;
  - `testsupport/push/PushTestConfig`: trust-all `NativePushTls` and `MutableNativeEndpoints`.

No new dependencies. No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` change. Nothing was copied
from AGPL/GPL references. No key, token or credential is committed: test keys are generated at run
time, and Postman bodies carry placeholders.

## Deviations from the prompt

1. **`TokenRevokedEvent` is `ApiTokenRevokedEvent`.** That is the 023 class; the listener follows
   031's pattern.
2. **APNs `TooManyProviderTokenUpdates` is 429, not 403.** Apple's response table says so. Both it
   and the 403 auth errors (`InvalidProviderToken`, `MissingProviderToken`,
   `BadEnvironmentKeyIdInToken`, `UnrelatedKeyIdInToken`, …) are provider errors. 410
   `ExpiredToken` is gone, like `Unregistered`. 413 is terminal `failed` (`Rejected`), not retried.
3. **Collapse id.** It is `a-` plus the 031 32-character Topic, which is the first 32 base64url
   characters of the key's SHA-256. The prompt asked for the first 40, but `push_dispatch` stores only
   the Topic. The result is 34 bytes, within the 64-byte limit.
4. **Extra `push_device` columns.**
   - `token_suffix`: listing never decrypts tokens.
   - `device_name` and `app_version`: the spec's `PushDevice` returns them.
   `platform` comes from the request, not the 023 metadata, because `DeviceInfo.platform` has no
   `ipados` and the spec requires `platform`.
5. **The default deep link is `foresignal://alerts/{id}`.** That is 023's `ClientLinks.DEFAULT_ALERT`;
   the prompt says `foresignal://alert/{id}`.
6. **The 031 budget helper.** `PushPayloadBudget` renders the Web Push format, so APNs uses its own
   4096-byte step in `ApnsPayloads.fit`: drop the link, then fall back to minimal text. A native
   payload is tiny by construction, so this never triggers in practice.
7. **Testable endpoints.** The APNs and FCM hosts come from `NativePushEndpoints`, whose default reads
   `foresignal.push.native.*` with the official hosts. Tests swap in a mutable bean in the shared
   `PushTestConfig`, so the push ITs keep one cached context. FCM's `tokenUri` also accepts `http`,
   so a local stand-in works; it is admin-set, and 035 can tighten this.
8. **FCM data carries `c` (category).** The spec defines it, and 032 now has a category. Errors
   without a recognised code fall back to the HTTP mapping. `collapse_key` is sent in the snake-case
   form the prompt and spec use; proto3 JSON accepts both spellings.
9. **The provider-error retry waits 15 minutes** (`provider-retry`). The prompt says "retry later".
   The retry counts as an attempt, so a key that stays broken fails the dispatch on the 018 ladder.
10. **`PushDevice.status = disabled` is never set by 032.** Dead tokens are deleted, and no rule for
    disabling a device is specified. The value is documented as reserved.
11. **The settings import is defined here.** The prompt only asks for export. An import without keys
    keeps an existing key and stores a new provider disabled, mirroring 026's LLM providers.

## Issues found / open questions

1. **Unverified at run time.** No test has run. These are first exercised at checkpoint 041:
   - the JDK `HttpClient` asking for HTTP/2 against WireMock https: WireMock may negotiate HTTP/1.1,
     and the tests do not depend on it;
   - the trust-all `NativePushTls`;
   - the Hibernate-created `push_provider` and `push_device` tables;
   - the conditional `last_error` update under concurrent dispatches.
2. **Never sent to Apple or Google.** Headers, bodies and error handling follow the official
   documentation listed above. Sending one push from a real signed app with the operator's own key
   would confirm interop.
3. **FCM `message.token` is marked deprecated** in the v1 discovery document, "use `fid` instead".
   It is still accepted during the transition. A later prompt may switch registration to Firebase
   Installation IDs.
4. **A guard was already red before this prompt.** `PostmanCollectionContractTest
   .validatorTableCoversTheParsedBodies` lists 10 implemented operations from 026–031 with neither a
   validator nor a schema-only entry. They were found by evaluating the test's own filter in a
   script:
   - `createLlmProvider`, `updateLlmProvider`, `updateLlmSettings`
   - `ask`, `askStream`
   - `createBriefing`, `previewBriefing`, `updateBriefing`
   - `createPushSubscription`, `verifyPushSubscription`
   032 covered its own four operations. **Owner/041:** add entries for the 10.
5. **Device-test counters are kept in memory**, per process, as in 018 and 031.
6. **Data.** No instructions directed at an agent were found in data, docs, web pages or tool output.
   Fetched for verification: Apple's three APNs documents (JSON form), Google's FCM v1 send and
   error-code pages, Google's service-account OAuth page, and the FCM v1 discovery document.
7. Owner decisions carried from 012–031 are still open, including 031 Issue 2 (`EdpHttpClient` and
   the HTTP-client guard).

## Tests written (not run)

| Test | What it covers |
|---|---|
| `notify/push/apns/ApnsJwtCacheTest` | • header `{alg, kid}` and claims `{iss, iat}` only<br>• a 64-byte raw signature that verifies with the generated public key and fails with another<br>• reuse at 20 and 39:59 minutes, refresh at 40 (new `iat`)<br>• a single forced refresh for a shared stale token<br>• a fingerprint change |
| `notify/push/apns/ApnsPayloadTest` | • minimal text, `e` ids and link, no full title, body or rule name<br>• short title ≤ 40<br>• briefing and system texts, thread ids and categories<br>• interruption levels ×6 (never `critical`)<br>• downgrade: passive, silent, priority 5<br>• headers: topic, push type, expiration by TTL (24 h, 12 h, 1 h), collapse id ≤ 64 bytes, stable canonical `apns-id`<br>• the 4096-byte budget drops a huge link and keeps the badge |
| `notify/push/fcm/FcmPayloadTest` | • string-only data with `v, id, a, s, ev, l, t, c`; no `notification`<br>• `HIGH` priority, `86400s` TTL, collapse key ≤ 64<br>• no full text, short text in `t`<br>• priority ×5<br>• the `apns` block for iOS via FCM<br>• briefing TTL, no `a`/`l` |
| `notify/push/fcm/FcmAccessTokenCacheTest` | WireMock token endpoint:<br>• form grant type<br>• assertion header and claims (`iss`, `scope`, `aud`, `iat`, `exp`), RS256 signature verified<br>• caching until `expires_in − 300`, refresh after<br>• `invalidate`, fingerprint change<br>• a refusal reports only `invalid_grant` |
| `notify/push/provider/ProviderKeysTest` | • P-256 parse and normalization<br>• P-384, RSA, empty, non-PEM and bad base64 refused without echoing the key<br>• RSA 2048 accepted, EC refused as RSA |
| `notify/push/NativeContentTest` | • texts by level and fallback<br>• collapse id from the Topic or the tag, capped at 64<br>• links, thread id, category<br>• `badge` and `category` survive `toMap`/`fromMap` |
| `notify/push/provider/PushProvidersIT` | • PUT 201/200; replace without secrets keeps the key; never returned (`secretFields` only)<br>• FCM JSON fills the config; the ciphertext holds no PEM<br>• refused: RSA as `.p8`, garbage, bad base64, JSON without `private_key`, non-JSON, an EC key as the service account, a missing secret, a bad config, a bad slug<br>• admin only, 401 anonymous<br>• test: APNs mint, FCM mint via WireMock, refusal recorded in `lastError`, 404s<br>• export without secrets; import keeps the key and stores a new provider disabled |
| `notify/push/device/PushDevicesIT` | • validation ×9, nothing stored, 401<br>• 409 with no provider, a disabled provider, and the wrong environment; the FCM app not served<br>• 201 with suffix, metadata copied, no token in the response or the ciphertext; re-register moves ownership (200)<br>• visibility: own, admin and others 404; update, delete, test 404 for admin<br>• cap 20 and re-register at the cap<br>• revoke cascade, expired-token sweep |
| `notify/push/NativePushDeliveryIT` | WireMock: APNs over https, FCM and OAuth over http:<br>• APNs path, JWT verified, all headers, `apns-id` = dispatch, minimal body<br>• FCM bearer, path, data, HIGH<br>• 410 and `UNREGISTERED` delete devices; `BadDeviceToken` and `INVALID_ARGUMENT` on the token delete them<br>• 429 retried after 1 minute, then sent<br>• `ExpiredProviderToken` → one new JWT, then success<br>• `InvalidProviderToken` → `last_error` and exactly one `system.push_provider_failed`<br>• filters honoured<br>• an APNs 500 does not block FCM<br>• a disabled provider gets nothing<br>• device test: minimal notice, id 0, 5/hour limit, reset after an hour<br>• provider test with a device |

Test support:
- `testsupport/push/NativeTestKeys`: EC, RSA and service-account JSON generated at run time.
- `testsupport/push/MutableNativeEndpoints`
- `PushTestConfig`: the native TLS and endpoints beans.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → `Woohoo! Your API description is valid.`, 70 warnings (unchanged), 0
errors.

`python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 827 requests, all operations
covered.

No new dependencies.

## Notes for the next prompt

1. **033 (actionable notifications and inbox):**
   - `PushMessage.badge` and `category` exist. Set `badge` from the unread count, and use
     `FORESIGNAL_ALERT_ACTIONABLE` for firing alerts with actions. `ApnsPayloads.aps` already emits
     `badge` when it is set, and FCM sends `c`.
   - `ClientDeviceResolver.currentDevice()` gives the device id (the API token id) for per-device
     read cursors.
   - The Client Guide's NSE snippet reads `/v1/alerts/{id}`; switch it to `/v1/notifications/{id}`.
2. **035 (access perimeter):** FCM `tokenUri` is admin-set and accepts http or https. Consider an
   `OutboundUrlPolicy` profile for it. The APNs and FCM hosts are fixed by config.
3. **041 (checkpoint):**
   - the native push ITs share `PushTestConfig`'s cached context;
   - see Issue 4 (the Postman validator table) and 031 Issue 2.
4. **115 (exercise):** `NativePushPayload.exercise` is in the spec. Exercises must never be
   `time-sensitive`; set that in `ApnsPayloads.interruptionLevel`.
