# R2-007 — Transactional email

**Goal.** Global SMTP settings and templated, US-English transactional emails: **welcome** (full name + role
names + activation link), **password reset**, **role-changed** and **suspended**. Reuse the existing SMTP delivery
path. The from-address defaults to `info@remoteidsystems.com`. The whole thing **degrades gracefully with no SMTP**:
nothing fails, the admin is warned, and activation/reset links stay retrievable by an admin.

**Dependency.** R2-002 (activation/reset token plaintext to embed), R2-003 (reset/activation request flows that
ask for an email), R2-005 (create/suspend/role-change emit the welcome/suspended/role-changed emails). R2-004's
`system.settings` gate.

**Operations (authored in `docs/OpenAPI.json`, `x-foresignal-status: planned`) — implement to the authored schemas and leave the status `planned`; R2-027 flips the whole v2.0 surface to `implemented` at cutover:** `getSmtpSettings`, `updateSmtpSettings`,
`testSmtpSettings`. All `scope: admin`; add `x-foresignal-permission: system.settings`. Schemas already authored:
`SmtpSettings{enabled,host,port,security,username,fromAddress,fromName,secretsSet}`, `SmtpTestRequest`,
`SmtpTestResult`.

**Read first.**
- `docs/OpenAPI.json` — the three `/v1/settings/smtp*` operations and the SMTP schemas. Note `secretsSet` is the
  **write-only secret** pattern: the password is never returned; `secretsSet` reports whether one is stored (mirror
  how other settings handle secrets, e.g. the LLM provider keys).
- `src/main/java/ai/foresignal/notify/types/EmailChannel.java` — the existing per-channel `JavaMailSender` build
  (host, port, `tls` mode `starttls`/`ssl`/`none`, auth, multipart/alternative plain+HTML, `X-Foresignal-Origin`
  header). Reuse this delivery mechanism; do not hand-roll SMTP.
- `src/main/java/ai/foresignal/settings/` — the settings service pattern (`ServerSettingsService`,
  `SecuritySettingsService`, `SettingKeys`, `AppSettingChangedEvent`) and how a setting with an encrypted secret is
  stored. `security/CredentialCipher.java` / `Encrypted.java` / `SecretKeyStore.java` for encrypting the SMTP
  password at rest (never in the app, never logged).
- `src/main/java/ai/foresignal/system/` — how `/v1/system` surfaces warnings (R2 needs a `SMTP_NOT_CONFIGURED`
  warning when email is wanted but SMTP is off).
- `server.contactEmail` / `OutboundSettings` — the contact email already in the project; keep the from-address and
  contact consistent.

**Build.**

- **`SmtpSettingsService` + settings model.** Persist global SMTP config: `enabled`, `host`, `port` (default 587),
  `security` (`starttls`/`ssl`/`none`), `username`, encrypted `password` (write-only; `secretsSet` reflects
  presence), `fromAddress` (**default `info@remoteidsystems.com`**), `fromName` (default a sensible product name).
  `getSmtpSettings` returns everything except the password (with `secretsSet`); `updateSmtpSettings` validates and
  stores, encrypting the password; omitting the password leaves the stored one intact; an explicit empty clears it.
- **`testSmtpSettings`.** Send a test message to the address in `SmtpTestRequest` (or the from-address) using the
  **provided or stored** settings and return `SmtpTestResult{ok, message}` — a clear success or the SMTP error,
  without leaking the password. This is a safe, explicit admin action.
- **`TransactionalEmailService`.** One place that renders and sends the four templates via a `JavaMailSender` built
  from the global SMTP settings (factor the builder out of `EmailChannel` or reuse it). Templates (US English,
  plain + HTML, no tracking, no third-party assets):
  - **welcome / account-added:** greets by full name, lists the assigned role names, and carries the **activation
    link** built from the public base URL (`ServerSettings.publicBaseUrl` + the activation path the client uses) and
    the R2-002 activation token plaintext. States the link's expiry.
  - **password reset:** the reset link (public base URL + reset token plaintext), its expiry, and a "ignore if you
    didn't request this" line.
  - **role-changed:** the user's new effective roles.
  - **suspended:** a brief, factual notice that the account was suspended.
  Build links from `clientLinks`/`publicBaseUrl` (R2-003/existing server settings); never embed a raw token anywhere
  but the single-use link.
- **Graceful degradation.** If SMTP is disabled or unconfigured when an email is requested: do **not** throw into the
  caller (user creation, reset request, etc. still succeed). Instead log a warning, raise the `SMTP_NOT_CONFIGURED`
  `/v1/system` warning, and keep the activation/reset link retrievable by an admin (R2-002 already stores the token;
  expose/log the admin-retrievable link). The outcome of create/suspend/reset must never depend on SMTP being up.
- **Wire the senders.** R2-005's create/suspend/role-change and R2-003's reset/activation call
  `TransactionalEmailService`. Because R2-003/R2-005 may already be merged, implement the service here and connect
  their call sites (they left the hooks); if a hook is a TODO stub from an earlier prompt, complete it.

**Model / migration.** Add **`Vn__smtp_settings.sql`** (next free migration number — read the directory, never hard-code; Tier-1 builds in parallel; additive) if the SMTP config needs its own table/columns
rather than the generic app-setting store; otherwise store it as a typed app setting with the encrypted secret and
add no migration. Record which approach was taken.

**Contract / docs.** Implement the three ops to the authored schemas but **leave their `x-foresignal-status: planned`** (R2-027 flips the surface and regenerates the SDKs once at cutover; regenerate Postman only). Add `x-foresignal-permission: system.settings` to them (additive metadata, allowed mid-release). Keep
`AccessPolicy`/spec-sync in agreement. Regenerate Postman (bodies for `updateSmtpSettings`, `testSmtpSettings`).
Update `docs/Operator-Guide.md` (configuring SMTP, the from-address default, what each email says, the no-SMTP
fallback) and `docs/RBAC-Design.md` (where welcome/reset/role-changed/suspended fire).

**Tests to write (do not run).** get returns settings without the password and with `secretsSet`; update stores and
encrypts the password, and omitting it preserves the stored one; the from-address defaults to
`info@remoteidsystems.com`; `testSmtpSettings` returns ok/error without leaking the secret; the welcome email
renders the full name, role names and a working activation link with expiry; reset/role-changed/suspended render;
with SMTP off, creating a user still succeeds, the `SMTP_NOT_CONFIGURED` warning appears, and the activation link is
admin-retrievable. Use a fake/intercepting mail transport in tests (no real send). Target ≥ 90% line coverage.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Also run `./gradlew checkUsEnglish` and keep
it green (the email templates are US English). Per `000-Run-Sequence.md` do **not** run tests, `lintOpenApi` or
`checkApiCompat`. Write the tests; do not execute them.

**Done when.** SMTP settings get/update/test work (secret write-only, default from-address
`info@remoteidsystems.com`); the four templates render and send via the existing SMTP path; create/suspend/
role-change/reset trigger the right email; with no SMTP everything still succeeds, warns, and keeps links
retrievable; ops built to their authored schemas (status stays `planned` until R2-027); Postman regenerated; docs updated; `checkUsEnglish` green; compilation passes.

**Do not touch.** any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog history; `.gitlab-ci.yml`; and any other prompt's files.

**Commit.** `R2-007: Transactional email` + `completion-reports/R2-007-CR-Transactional-Email.md`. **Stage only
this prompt's files.** End your reply with `Implementation commit: <hash>`.
