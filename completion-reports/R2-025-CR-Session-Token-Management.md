# R2-025-CR-Session-Token-Management

| | |
|---|---|
| Prompt | R2-025-Session-Token-Management.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `0a4cd6401fb8011fd69c1398ed12a7fb612e411b` |
| Date | 2026-10-26 |

## What was added

- **`TokenCreatedVia.SELF`** (wire value `self`). `api_token.created_via` is a `varchar(16)` with no check constraint,
  so **no migration** was needed. The next migration is still V25.
- **`TokenService`:**
  - `create(JsonNode)` was split into `parse(JsonNode, TokenCreatedVia)`, which validates into a `TokenSpec`, and the
    existing `create(TokenSpec)`. The admin path behaves as before.
  - New `listOwned(principalId)` and `revokeOwned(id, principalId)`. `revokeOwned` gives 404 for a token the caller
    does not own or for the bootstrap token, and reuses `revoke`, so `ApiTokenRevokedEvent` still fires.
- **`security/user/MyTokenService`:** create, list and revoke over `TokenService`. It uses the same row, format,
  hashing and `RestrictionValidator` as admin tokens; the owner is the caller's `person` principal. Creation is
  clamped:
  - `principalId` gives a 400;
  - a scope above `TokenOwnership.maxScope` gives `403 TOKEN_EXCEEDS_USER`;
  - `privateAccess` without an admin-derived session gives `403 TOKEN_EXCEEDS_USER`.
- **`security/user/TokenOwnership`**, the runtime clamp, run in `TokenFilter.resolveBearer` for every API token:
  - A token whose principal is a user's principal is the user's.
  - If the owner may not sign in (suspended or disabled), the token gets a 401, like a revoked one.
  - Its scope is lowered to `maxScope` of the owner's *current* permissions, and the private grant is dropped unless
    the owner still has it.
  - The owner is bound as `TokenOwner`. Reads are then narrowed by the owner's data restriction (R2-013's
    `UserDataRestrictionService.narrow`, with a new `UserId` overload). Writes are audited as the owner: actor `user`,
    `detail.via: user-token`.
  - `maxScope` is `admin` only with every permission in the catalog; otherwise `agent` when the owner's sessions are
    `agent` or `admin`; otherwise `read`.
- **`api/MyTokensController`:** `listMyTokens`, `createMyToken` and `revokeMyToken`. A session is required; an API
  token gets 401, so a token cannot mint a token.
- **Error code:** `TOKEN_EXCEEDS_USER` (403).
- **Tests (written, not run):**
  - `MyTokenServiceTest`: a Mockito unit test of ownership, `self`, the clamps and the `maxScope` table.
  - `CreateMyTokenIT`:
    - a Viewer gets a read token with a bbox;
    - agent, admin and private requests are refused;
    - validation is enforced;
    - an Operator gets agent but not admin, and an Admin gets admin;
    - a token cannot mint;
    - after a role is taken away the token is capped, and after suspension it gets 401.
  - `MyTokenListRevokeIT`: only one's own tokens are listed; 404 for someone else's; a revoked token stops at once; a
    write is audited as the owner.
- **Docs:** a `docs/Operator-Guide.md` bullet under "Signing in and sessions".

## Contract

- **`ApiToken.createdVia`:** gained `self`.
- **`ApiTokenInput.principalId`:** now notes that `createMyToken` does not accept it.
- **Descriptions:** the three operations describe the clamp, the owner caps and the 401/404 rules, and `createMyToken`'s
  403 names `TOKEN_EXCEEDS_USER`.
- **Postman:** regenerated (936 requests). Statuses stay `planned`, and the SDKs were not regenerated.

## Decisions and disagreements for the owner

1. **Operators cannot mint `admin` tokens.**
   - An API token is gated by scope alone, so an `admin` token passes every permission check, including
     `users.manage` and `roles.manage`, which an Operator does not hold.
   - Allowing it would let an Operator escalate to user management through a token. So `admin` requires every
     permission, which in practice means the Admin role, and Operators are capped at `agent`.
   - Say if you want Operators to automate admin-scoped platform operations by token. That needs permission-scoped
     tokens, which do not exist today.
2. **The operations are not gated on `self.workspaces`.**
   - The contract's descriptions said "Requires `self.workspaces`", but no `x-foresignal-permission` is set, and the
     prompt says self-service scope `read`.
   - Viewers and Auditors lack `self.workspaces`. They can still mint read tokens, clamped to themselves.
   - I corrected the descriptions. Say if you want a permission gate.
3. **Clamping happens at request time, not only at mint time.**
   - The prompt asks for no wider scope or data reach than the user's. Checking only at creation would let a demoted or
     restricted user keep a wider token.
   - So the owner's current roles and data restriction apply on every request, as they do for sessions. This costs one
     `user` lookup by principal per API-token request.
4. **`area` and `endpoints` are not compared geometrically at creation.** A token's own restrictions only narrow, and
   the owner's data restriction is intersected at read time. A token asking for a wider area therefore reads the
   intersection; it is not refused.
5. **The user's source view (R2-010) is not applied to their tokens.** Only the security clamp (the data restriction)
   is. The view is a preference about which sources a person sees, not an access limit.
6. **A stream ticket minted from a user token carries the clamped token at mint time.** It is re-checked against the
   token's revocation as before, not against the owner again.
7. **Ownership is by principal.** A token an admin creates through `/v1/tokens` with `principalId` set to a user's
   principal is also treated as that user's: clamped, narrowed and attributed. I think that is right, but flag it.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next prompt

- `TokenOwner.of(request)` names the user behind a user-owned API token, next to `AuthenticatedSession.of(request)`.
  Per-user quotas (R2-026) can key on either.
- The next Flyway migration is **V25**.
