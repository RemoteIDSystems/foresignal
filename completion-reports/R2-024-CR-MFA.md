# R2-024-CR-MFA

| | |
|---|---|
| Prompt | R2-024-MFA.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `c43231f664e23cd2d5fc65626f44aca805b12626` |
| Date | 2026-10-26 |

## What was added

- **Dependencies** (both added to `THIRD_PARTY_NOTICES.md`):
  - **`com.eatthepath:java-otp` 1.0.0** (MIT, no runtime dependencies).
    - Verified on Maven Central: `maven-metadata.xml` lists 1.0.0 as the latest release, last updated 2026-08-18.
      The POM declares MIT. The jar's SHA-1 matched the published `.sha1`.
    - It computes RFC 4226 and RFC 6238 codes over the JDK's HMAC. No HOTP truncation was hand-written.
  - **`commons-codec`**, version from the Spring Boot BOM, for Base32. It was already on the runtime classpath at
    1.21.0 and is now declared directly.
  - No checksum entry was needed: `verification-metadata.xml` trusts every group except `edu.ucar`.
- **`security/mfa/` package:**
  - `TotpService`: HMAC-SHA1, 6 digits, 30-second steps, a ±1-step window, constant-time comparison, and a rule that
    a step at or before the last accepted one is refused (replay protection). It also handles Base32.
  - `MfaService`:
    - enroll, verify (activation and step-up), `verifyLogin`, regenerate recovery codes, disable, `adminReset`, and
      `required`/`status`.
    - The TOTP secret is sealed with `CredentialCipher`, AES-GCM with the instance key.
    - There are 10 recovery codes, each 50 random bits (`xxxxx-xxxxx`). They are stored only as `TokenHasher` SHA-256
      hashes of the normalized code and consumed on use.
  - `RecoveryCodeEntity` and `RecoveryCodeRepository`.
- **`api/MfaController`:** the four operations.
  - They need a session; an API token gets 401.
  - `verifyMfa` and `disableMfa` are limited to 10 code checks a minute per user (429).
  - Every change and every refused code is audited as an account event. No secret or code is ever logged.
- **Login (R2-003 code):**
  - `SessionService.login` takes `mfaCode`.
  - With MFA active and no code, it audits `login_mfa_required` and issues **no session**. The answer has
    `mfaRequired: true`, an empty `token` and a null `expiresAt`.
  - A wrong code is the same `401 INVALID_CREDENTIALS`, audited with reason `wrong_mfa_code`.
  - A TOTP or a recovery code completes the sign-in.
- **Enforcement (R2-004 code):**
  - `AuthenticatedSession` gained `mfaEnrollmentRequired`, with an 8-argument constructor kept for existing callers.
  - `SessionService.authenticate` sets the flag on every request when the user holds the built-in Admin or Operator
    role, has not enrolled, and enforcement is on.
  - `TokenFilter.checkToken` then refuses every write except the following, with `403 MFA_ENROLLMENT_REQUIRED`:
    - enroll and verify;
    - `PUT /v1/me/preferences` and `PUT /v1/me/notification-preferences`;
    - logout;
    - `DELETE /v1/auth/sessions[/…]`.

    Reads are allowed.
- **Notices:** `AccountMailer.sendMfaChanged` is a default no-op method, so existing implementations still compile.
  `TransactionalEmailService` sends the notice when MFA is turned on, turned off, or reset by an administrator.
- **Error codes:** `MFA_ALREADY_ENABLED`, `MFA_NOT_ENABLED`, `MFA_REQUIRED`, `MFA_ENROLLMENT_REQUIRED` and
  `MFA_CODE_INVALID`.
- **Migration `V24__mfa.sql` (additive):**
  - `"user"` gained `mfa_secret_cipher`, `mfa_secret_nonce`, `mfa_enrolled_at` and `mfa_last_step`.
  - A new `user_recovery_code` table (id, user_id with `on delete cascade`, code_hash, created_at, used_at).
- **Configuration:** `foresignal.mfa.enforce-enrollment` (`FORESIGNAL_MFA_ENFORCE_ENROLLMENT`, default `true`). It is
  `false` in `application-test.yml`.
- **Tests (written, not run):** `TotpServiceTest`, `MfaEnrollmentIT`, `RecoveryCodeIT`, `MfaDisableIT`,
  `MfaEnforcementIT`, and the shared `MfaItSupport`.
  - `TotpServiceTest` uses RFC 6238 Appendix B vectors. These are the last 6 digits of the RFC's SHA-1 8-digit codes:
    `287082`, `081804`, `005924`, `279037`.
- **Docs:** `docs/Operator-Guide.md` gained two bullets under "Signing in and sessions". `THIRD_PARTY_NOTICES.md` gained
  two entries.

## Contract

- **`SessionToken`:**
  - `expiresAt` is now nullable.
  - The `token`, `expiresAt` and `mfaRequired` descriptions say what happens when no session is issued.
- **`MfaStatus`:** gained an optional, read-only `recoveryCodes` (`MfaRecoveryCodes`).
- **Descriptions:**
  - `MfaEnrollment`, `MfaVerifyRequest` and `MfaRecoveryCodes` have sharper descriptions.
  - `login` describes the MFA step and the enrollment gate.
  - The four MFA operations have the rules and codes.
  - `verifyMfa` gained a `409` response.
- **Postman:** regenerated (936 requests). Statuses stay `planned`, and the SDKs were not regenerated.

## Decisions and disagreements for the owner

1. **`verifyMfa` returns `MfaStatus`, but the prompt says it returns `MfaRecoveryCodes`.**
   - The contract's `200` is `MfaStatus`; the prompt says to return the recovery codes "as `MfaRecoveryCodes`".
   - I kept `MfaStatus` and added an optional `recoveryCodes` member, present only on the call that activates MFA.
     That satisfies both.
2. **The MFA operations are not gated on `self.preferences`.**
   - The contract's descriptions said "Requires `self.preferences`", but the operations carry no
     `x-foresignal-permission`.
   - The built-in **Operator role does not hold `self.preferences`** (Platform plus Data only). Gating on it would have
     made MFA required for Operators but impossible for them to enroll.
   - I left the operations ungated and corrected the four descriptions. Say if you want a permission, for example
     `self.preferences` added to Operator.
3. **A sign-in that needs a code returns `200` with an empty `token` and a null `expiresAt`.**
   - The contract requires `token` and `expiresAt` and says "issues no usable session".
   - I made `expiresAt` nullable. The alternative is a short-lived "MFA pending" token or a separate status code, and
     that is your call.
   - The client signs in again with `mfaCode`. No intermediate state exists on the server.
4. **There is a test-profile switch, `foresignal.mfa.enforce-enrollment`.**
   - 16 existing test files sign in Admins or Operators and write without MFA. With enforcement on, all of them would
     get 403.
   - The property defaults to `true`, the test profile sets `false`, and `MfaEnforcementIT` turns it back on through a
     package-private setter.
   - **The property is a production off switch for required MFA.** Say if you would rather it did not exist and the
     existing tests enroll instead.
5. **The secret columns are `mfa_secret_cipher` and `mfa_secret_nonce`, not one `mfa_secret_encrypted`.** This matches
   every other sealed secret (`(ciphertext, nonce)` pairs). `mfa_last_step` was added for replay protection.
6. **The issuer is `ForeSignal`, spelled as the prompt has it.** The rest of the code says `Foresignal`. It is one
   constant, `MfaService.ISSUER`. Say which spelling you want.
7. **Admin reset is a service method only.** The user-management API (R2-005) does not expose it yet. `adminReset`
   clears MFA whatever the role. A user who must have MFA is then gated to enrollment again.
8. **There is no separate rate limit for login codes.** A wrong `mfaCode` counts as a failed sign-in in the existing
   per-address limit and backoff.
9. **Replay protection is per user, not transactional across concurrent requests.** Two requests carrying the same
   code at the same instant could both pass before either saves `mfa_last_step`. A row lock on `"user"` would close
   this. I left it, because the code check is also rate-limited.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- `./gradlew dependencies --configuration runtimeClasspath` shows `com.eatthepath:java-otp:1.0.0` and
  `commons-codec:commons-codec -> 1.21.0`.
- No test, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next prompt

- `MfaService.verify` (step-up) and `MfaService.adminReset` are available for the session and user-management work.
- `AuthenticatedSession.mfaEnrollmentRequired()` is recomputed on every request.
- The next Flyway migration is **V25**.
