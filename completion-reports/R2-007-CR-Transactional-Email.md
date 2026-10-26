# R2-007-CR-Transactional-Email

| | |
|---|---|
| Prompt | R2-007-Transactional-Email.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `cd0175fd0e3c1f91cc8eb7ec14693e470cea0d90` |
| Date | 2026-10-26 |

## What was added

- **`settings/SmtpSettings` + `settings/SmtpSettingsService`** — global SMTP stored as a typed app setting under the
  new key `SettingKeys.SMTP = "smtp"` (**no migration**): `enabled`, `host`, `port` (587), `security`
  (`starttls`/`tls`/`none`, default `starttls`), `username`, the password sealed with `CredentialCipher`
  (base64 ciphertext + nonce, the `ArchiveSettingsService` pattern), `fromAddress` (default
  `info@remoteidsystems.com`), `fromName` (default `Foresignal`). Omitted password keeps it; `null`/`""` clears it;
  `enabled` without `host` is refused; every problem in one `400 VALIDATION_FAILED`.
- **`api/SmtpSettingsController`** — `getSmtpSettings`, `updateSmtpSettings` (both return the view: no password,
  `secretsSet`), `testSmtpSettings` (`SmtpTestResult{ok, message, sentAt}`; the message is the exception text, never
  the password).
- **`notify/MailSenders`** — the JavaMail builder factored out of `ChannelSender.sendMail` (the email channel now uses
  it); **`notify/TransactionalMailTransport`** / **`SmtpTransactionalMailTransport`** send multipart plain + HTML
  through it (`tls` maps to the channel's `ssl` mode; 10 s connect, 30 s read).
- **`security/user/TransactionalEmailService`** — the `AccountMailer` (replaces the deleted `LoggingAccountMailer`)
  and a `SystemWarningContributor`. Four US-English templates, plain + HTML, no tracking or remote assets: welcome
  (full name, role names, activation link, expiry), reset (link, expiry, ignore-if-not-you), roles changed, suspended.
  Role-changed and suspended are sent after commit. No usable SMTP or a failed send ⇒ logged as not sent (user id and
  token prefix only), `false` returned so R2-005 hands the token to the admin, and `/v1/system` warns
  `SMTP_NOT_CONFIGURED` while user accounts exist (or a send has gone undelivered).
- **`ClientLinks`** gains `activate` and `passwordReset` templates (defaults
  `foresignal://activate?u={publicBaseUrl}&t={token}`, `foresignal://reset-password?u={publicBaseUrl}&t={token}`),
  parsed by `ServerSettingsService`.
- Call sites: already wired in R2-003/R2-005 through `AccountMailer` (welcome on `createUser`/`resendUserInvite`,
  reset on `requestPasswordReset`/`adminResetUserPassword`, role-changed on `setUserRoles`, suspended on
  `suspendUser`); the only implementation is now this service.
- Tests (written, not run): `TransactionalEmailIT` (intercepting transport `@Primary`) and
  `TransactionalEmailTemplatesTest`.
- Docs: Operator-Guide "## Account email (SMTP)" and the two stale "until R2-007" lines; RBAC-Design §F rewritten as
  built with the where-each-email-fires table, §G line updated; Invariants RBAC-12.

## Contract

- `ClientLinks` schema gains `activate` and `passwordReset`; `SmtpSettings` description names the
  `SMTP_NOT_CONFIGURED` fallback. `x-foresignal-permission: system.settings` was already on the three ops (R2-004).
  Status stays `planned`.
- Postman regenerated ("936 requests … covers all ops: True"), bodies for `updateSmtpSettings` and
  `testSmtpSettings`.
- Static policy emulation over all operations: 0 problems.

## Decisions and disagreements for the owner

1. **`security` is `none`/`starttls`/`tls`** (the authored schema), not the prompt's `starttls`/`ssl`/`none`; I followed
   the contract. Which should change?
2. **`testSmtpSettings` uses the stored settings only.** The prompt says "provided or stored", but `SmtpTestRequest`
   has only `to` (required); I followed the contract. "Or the from-address" is likewise impossible with `to` required.
3. **The default activate/reset links are `foresignal://` deep links.** I found no activation or reset route in
   `foresignal-admin` or `foresignal-client`; the clients will need to handle these, or an operator points the
   templates at an `https://` page.
4. **The warning is severity `warning`**, so `/v1/system` reads degraded whenever user accounts exist without SMTP.
5. Stored as an app setting, no `Vn__smtp_settings.sql`, as the prompt allows.

## What runs

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL. `./gradlew checkUsEnglish`: green.
- Not run (written only): defaults (off, 587, starttls, from-address, empty `secretsSet`); password encrypted at rest,
  never returned, kept when omitted, cleared when empty; validation 400s; test send ok / failure without the
  secret; welcome renders name, role, link and expiry; SMTP off ⇒ create succeeds, token returned,
  `SMTP_NOT_CONFIGURED`; failing SMTP ⇒ token returned; role-changed, suspended and reset emails in order; template
  unit tests (HTML escaping, empty roles).

## Notes for the next prompt

- Any new account email goes through `TransactionalEmailService`; new link templates belong in `ClientLinks`.
- `MailSenders.build` is the single JavaMail builder; do not build another.
