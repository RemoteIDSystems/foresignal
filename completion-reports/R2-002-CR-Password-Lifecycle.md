# R2-002-CR-Password-Lifecycle

| | |
|---|---|
| Prompt | R2-002-Password-Lifecycle.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `b3555cd1d9d32fb479f1a581fb76e5ca883ed166` |
| Date | 2026-10-26 |

## What was added (`ai.foresignal.security.user`; no operation wired, no API change)

- **`PasswordHasher`** — argon2id via Spring Security Crypto `Argon2PasswordEncoder`. Parameters (constants, one
  place): salt 16 B, hash 32 B, parallelism 1, memory 16,384 KiB, iterations 3. Encoded form
  `$argon2id$v=19$m=16384,t=3,p=1$…`. `matches` is false for nulls and non-argon2 strings.
- **`PasswordPolicy`** — 12 to 1,024 code points, not blank; `400 PASSWORD_POLICY` with one `FieldError`.
- **`UserLifecycleService`** — the transition table (`invited→active`, `active↔suspended`, any→`disabled`,
  else `409 USER_STATUS_CONFLICT`); `canAuthenticate` (only `active`); `verify` (always runs the hash check);
  `setPassword`; `activateWithToken` / `resetWithToken` (policy checked **before** the token is consumed, one
  transaction).
- **`UserSessionsRevokedEvent`** — the session-revocation hook, published on suspend, disable and every password
  set. Nobody listens yet; R2-003's session store must.
- **`UserTokenService`** + `IssuedUserToken` — activation `fsv_` (72 h default), reset `fsw_` (1 h default);
  prefix + 32 random bytes base64url; only SHA-256 hex + 8-char prefix stored; plaintext returned once
  (`toString` hides it). Single-use (`consumed_at`), single-active (issuing deletes earlier unconsumed tokens of
  that kind). One error, `400 USER_TOKEN_INVALID`, for unknown / wrong kind / expired / used.
  `requestReset(email)` is enumeration-safe: empty for no user and for a non-active user alike.
- **`ErrorCodes`**: `PASSWORD_POLICY`, `USER_TOKEN_INVALID`, `USER_STATUS_CONFLICT`.
- No migration was needed: single-active is enforced by the service (delete-on-issue), not by a partial index.

## Dependencies (verified on Maven Central 2026-10-26)

| Artifact | Version | Source |
|---|---|---|
| `org.springframework.security:spring-security-crypto` | 7.1.1 | Spring Boot 4.1.1 BOM (`spring-security.version`), no pin |
| `org.bouncycastle:bcprov-jdk18on` | 1.86 | pinned in `libs.versions.toml`; latest release, published 2026-09-11; Bouncy Castle License (MIT-style) |

Neither was on the runtime classpath before (`./gradlew dependencies --configuration runtimeClasspath` showed no
`bouncycastle` or `spring-security` entries). `gradle/verification-metadata.xml` checksums only `edu.ucar`,
so it needed no entry.

## Docs

- `docs/RBAC-Design.md`: new §A2 (hashing and parameters, policy, status machine, token prefixes and TTLs,
  single-use/single-active/enumeration guarantees).
- `docs/Invariants.md`: RBAC-5 extended to argon2id; new RBAC-7 (only `active` authenticates; suspend/disable/
  password change end sessions).
- `docs/Architecture.md` §15.5: credentials bullet.

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL. `./gradlew checkUsEnglish`: green.
- Not run (written only): `PasswordHasherTest`, `PasswordPolicyTest`, `UserLifecycleServiceTest`,
  `UserTokenServiceTest` (unit, in-memory store and `MutableClock`), `UserCredentialsIT` (database;
  `@RecordApplicationEvents` for the hook).

## Notes for the next prompt (R2-003)

- **Prefix collision:** `docs/RBAC-Design.md` §C names the session bearer `fss_`, but `fss_` is already the share
  link key (`ShareLinkService.KEY_PREFIX`, prompt 112). R2-003 must choose another prefix.
- Listen to `UserSessionsRevokedEvent` (synchronous, inside the changing transaction) to revoke sessions.
- Use `UserTokenService.requestReset` for `requestPasswordReset` and answer identically whether it returns a token.
