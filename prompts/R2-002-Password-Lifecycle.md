# R2-002 — Password & account lifecycle

**Goal.** Give users secure passwords and a safe lifecycle: argon2id hashing through an audited library,
the `invited`/`active`/`suspended`/`disabled` status machine, and single-use, hashed, expiring **activation**
and **reset** tokens. This is the credential engine; R2-003 exposes it over `/v1/auth/*` and R2-005 drives it
from user management. **No API operations are wired in this prompt** (it builds services the next prompts call).

**Dependency.** R2-001 (the `user`, `user_token` tables and the status column exist).

**Read first.**
- R2-001's completion report and the `ai.foresignal.security.user` package it created (`UserEntity`,
  `UserTokenEntity`, the status enum).
- `src/main/java/ai/foresignal/security/TokenHasher.java` — the existing SHA-256 token hashing + constant-time
  compare + `SecureRandom` pattern. Activation/reset tokens follow the **same** store-only-the-hash rule as
  API tokens: generate a high-entropy plaintext, return it once, persist only its SHA-256 and an 8-char prefix.
- `gradle/libs.versions.toml` (the comment noting bcprov/bcpkix are available as optional deps) and
  `build.gradle.kts` dependency block.

**Build.**

- **Password hashing (`PasswordHasher` in `ai.foresignal.security.user`).** Use **argon2id** via an audited
  library. **Decision (do not deliberate):** add Spring Security Crypto's `Argon2PasswordEncoder` (it is argon2
  from Bouncy Castle). Add `org.springframework.security:spring-security-crypto` and `org.bouncycastle:bcprov-jdk18on`
  if they are not already on the classpath; **verify each version exists on Maven Central and is compatible with
  Spring Boot 4.1 / Java 21** before pinning (record the versions in the report). Configure sensible argon2id
  parameters (for example saltLength 16, hashLength 32, parallelism 1, memory 1<<14 KiB, iterations 3) and keep
  them in one place so they can be tuned. Expose `hash(String raw)` and `matches(String raw, String encoded)`.
  Never log or echo a raw password; `password_hash` is the only thing stored.
- **Password policy.** A single `PasswordPolicy` validator (minimum length 12, reject empty/whitespace-only;
  no exotic composition rules). Used by activation, reset and admin-set-password. Return a structured validation
  error the controllers (R2-003/R2-005) can surface as a `Problem`.
- **Status machine (`UserStatus` + the transitions, in a `UserLifecycleService`).** Legal transitions:
  `invited → active` (activation consumed), `active ↔ suspended` (suspend/unsuspend), any → `disabled`
  (terminal for login, but the row is retained for audit/ownership). A `suspended` or `disabled` user cannot
  log in and all their sessions are revoked (the revocation wiring lands in R2-003; expose the hook here).
  Only an `active` user may authenticate.
- **Activation & reset tokens (`UserTokenService`).**
  - `issueActivation(user, createdBy, ttl)` and `issueReset(user, ttl)` → generate `fsv_`-prefixed (activation)
    / `fsp…` — pick a distinct, documented prefix per kind — high-entropy plaintext, store SHA-256 + 8-char
    prefix + `expires_at` in `user_token`, return the plaintext **once** (for the email link in R2-007 or an
    admin to copy when SMTP is down). Default TTLs: activation **72h**, reset **1h** (constants, documented).
  - `consume(plaintext, kind)` → constant-time hash lookup; reject if missing, wrong kind, expired, or already
    consumed (`consumed_at` set); on success mark consumed and return the user. **Single-use and single-active:**
    issuing a new token of a kind invalidates any prior unconsumed token of that kind for that user (consume or
    delete the old rows) so a reset link cannot be replayed after a re-request.
  - Setting a password (activation or reset success) revokes the user's existing sessions (hook for R2-003) and,
    on activation, moves `invited → active`.
- **Timing & enumeration safety.** Reset/activation lookups are constant-time on the hash; the *request* side
  (R2-003) must not reveal whether an email exists — bake that requirement into the service contract (methods
  return the same shape whether or not a user matched).

**Model / migration.** R2-001 already created `user_token` and the status column. If any column is missing for
the behavior above (e.g. a per-kind partial unique index to enforce single-active), add **`Vn__password_lifecycle.sql`** (next free migration number — read `src/main/resources/db/migration/`, never hard-code; Tier-1 builds in parallel; additive). Do not alter V14 in place.

**Contract / docs.** No operations and no version bump. Update `docs/RBAC-Design.md` (hashing choice + parameters,
token TTLs, the status machine, single-use/enumeration guarantees) and `docs/Invariants.md` (passwords stored only
as argon2id; activation/reset tokens stored only as hashes; only `active` users authenticate).

**Tests to write (do not run).** argon2id hash/verify round-trips and rejects a wrong password; the encoded hash
is argon2id (prefix `$argon2id$`); policy rejects short/blank passwords; activation token consumes once then
fails on replay; expired token rejected; issuing a new reset invalidates the prior one; consuming activation moves
`invited → active`; suspend/disable block authentication; setting a password triggers the session-revocation hook.
Target ≥ 90% line coverage on the new code.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish`
and keep it green. Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or `checkApiCompat`. Write the
tests; do not execute them. US English.

**Done when.** Passwords hash/verify with argon2id from an audited lib; the status machine and its transitions are
enforced; activation and reset tokens are hashed, single-use and expiring with the documented TTLs; setting a
password revokes sessions and (for activation) activates the account; docs updated; `checkUsEnglish` green;
compilation passes. The report records the exact dependency versions and argon2id parameters chosen.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-002: Password and account lifecycle` + `completion-reports/R2-002-CR-Password-Lifecycle.md`.
**Stage only this prompt's files.** End your reply with `Implementation commit: <hash>`.
