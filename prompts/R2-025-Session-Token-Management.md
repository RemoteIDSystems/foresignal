# R2-025 — User-owned API token management

**Context.** v2.0 (R2) backend, Part D. Depends on the identity model (R2-001, which also decides how a `user`
maps to a `principal`) and auth/sessions (R2-003). Builds directly on the existing scoped-API-token subsystem
(prompts 023/035), where every `api_token` is already owned by a **principal** (`ApiTokenEntity` → principal) —
so a self-service token is attributed to the signed-in user's principal, no new `owner` column required.
**Session list/revoke is already delivered in R2-003** — this prompt is the user-owned machine-token lifecycle
only. Self-service scope `read`.

**Goal.** Let a signed-in user mint, list and revoke their own `api_token`s — now attributable to them — with
the existing area / endpoint-path / bbox / tile-key restrictions, each scoped no wider than the user's own
permissions.

## Operations (authored, status `planned`, `x-foresignal-prompt: R2-025`)

| operationId | method + path | scope |
|---|---|---|
| `listMyTokens` | `GET /v1/me/tokens` | read (self) |
| `createMyToken` | `POST /v1/me/tokens` | read (self) |
| `revokeMyToken` | `DELETE /v1/me/tokens/{id}` | read (self) |

Schemas authored: `ApiToken`, `ApiTokenInput`, `ApiTokenCreated` (reused from the admin token surface).

## Read first

- `security/TokenService.java` (`TokenSpec`, `CreatedToken`, `create(JsonNode)` — note `principalHandle`/
  `principalId`), `security/ApiTokenEntity.java` + `ApiTokenRepository.java` + `security/PrincipalEntity.java`
  (tokens are owned by principals; R2-001 relates a user to a principal), `security/TokenHasher.java`,
  `api/TokensController.java` (the existing **admin** token CRUD — mirror it for self-service).
- `security/RestrictionValidator.java` (the accepted members: `sources`, `kinds`, `bbox`, `endpoints`, `area`,
  `excludePrivate`, `rpm`, place ids — the "area/path/tile-key" restrictions) and `security/RestrictionEnforcer.java`
  (how they are enforced on read paths, prompt 035); `security/Scope.java`, `security/AccessPolicy.java`,
  `security/AuthenticatedToken.java` (effective permissions of the calling user).

## Build

1. **Self-token service.** Add methods (or a thin `MyTokenService` over `TokenService`) that mint tokens owned
   by the current user's **principal** with a `createdVia` marking self-service. Reuse `TokenService.create` /
   `TokenHasher` / the `ApiTokenEntity` row exactly — do not fork the token format or add a parallel owner column.

2. **`createMyToken`** (`ApiTokenInput` → `ApiTokenCreated`). Validate restrictions through
   `RestrictionValidator`. **Clamp scope and restrictions to the user's own permissions:** a user may not mint a
   token with a scope or data reach wider than they themselves hold (e.g. no `admin` token unless the user is an
   admin; no access to private sources unless the user has it; `area`/`endpoints` only narrow, never widen).
   Return the plaintext token **once** (`ApiTokenCreated.token`).

3. **`listMyTokens`** (`ApiToken[]`) lists only the tokens owned by the caller's principal (never the plaintext,
   never other users'). **`revokeMyToken`** revokes one the caller owns (`404` for a token they do not own),
   firing `ApiTokenRevokedEvent` so `TokenFilter` drops it immediately.

4. **Attribution.** Requests made with a user-owned token resolve to the owning user's identity for audit (the
   audit log from R2-013 records the owner), while still enforcing the token's own clamped restrictions.

**Migration.** Tokens are already principal-owned, so **no new column is expected**. Add a Flyway migration only
if you need one (e.g. a `created_via` marker if not already present); use the next free `V{n}` (read the
directory; R2-001…R2-013 claim V14 upward). Additive only.

## Contract

The three ops are authored `planned`; implement them to `ApiToken`/`ApiTokenInput`/`ApiTokenCreated`. Extend a
schema / add an error code (e.g. a clamp refusal) if needed, regenerate Postman
(`python3 scripts/generate-postman.py`), but **leave `x-foresignal-status: planned` and do not regenerate the
SDKs** (R2-027 flips + regenerates). `docs/OpenAPI.json` is the only contract.

## Tests to write (do not run)

- `MyTokenServiceTest` — minted tokens are owned by the caller's principal and carry the self-service `createdVia`.
- `CreateMyTokenIT` — restrictions validated; scope/reach clamped to the user's permissions (a Viewer cannot mint
  an admin token or widen `area`); plaintext shown once.
- `MyTokenListRevokeIT` — list shows only the caller's tokens; revoke works for an owned token, `404` otherwise,
  and the revoked token stops authenticating.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`.
**Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

A signed-in user can mint, list and revoke their own API tokens with the existing restrictions, clamped to their
own permissions, attributed to them in audit; compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. **Stage only this prompt's files.**

## Commit

`R2-025: User-owned API token management`, then
`completion-reports/R2-025-CR-Session-Token-Management.md` with the 40-character implementation hash.
