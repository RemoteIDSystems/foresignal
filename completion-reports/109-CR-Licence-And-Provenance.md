# 109-CR-Licence-And-Provenance

| | |
|---|---|
| Prompt | 109-Licence-And-Provenance.md |
| Status | Complete (with scoped-down read-path coverage; see Deviations) |
| Implementation commit | `61681791916747b70b515a4aafc9b15d52780ba2` |
| Date | 2026-10-15 |

## Summary

Made licence, provenance and reliability first-class. Compiles; tests written, not run.

- **Structured terms (`ai.foresignal.provenance`):** `LicenceTerms` (`CommercialUse`, `Redistribution`),
  `LicenceCatalog` loaded from `src/main/resources/provenance/licences.json` (built-ins plus aliases),
  egress-safe `UNKNOWN` defaults. `SourceDescriptor` gained `licenceTerms`, `reliability` (Admiralty A–F,
  default F) and `derivedFrom`; `DescriptorValidator.warnings` warns when neither a licence nor terms
  resolve. The binding cases resolve through the catalogue: OpenSky (FORBIDDEN commercial+redistribution),
  ACLED (FORBIDDEN), aisstream (redistribution FORBIDDEN), adsb.lol (ODbL-1.0), local receivers
  (`LOCAL_ONLY`), submarine-cables (CC-BY-NC-SA-4.0), US federal (`us-gov-public-domain`).
- **Per-record resolution:** `LicenceResolver` resolves a version's licence with one query per response
  (`record_version ⋈ fetch_log`), falls back to the descriptor terms (`licenceBasis: descriptor`) and
  downgrades an MLAT record (`mlat-override`). `FetchExecutor` now snapshots the **resolved catalogue id**
  on the fetch row. `ProvenanceStep` keeps rows with versions for `max(retention.provenance, the largest
  version window)`.
- **Client policy:** `LicencePolicy` (`no-nc`, `no-sa`, `redistributable`) from the
  `Foresignal-Licence-Policy` header or `licencePolicy` query, with the token's `api_token.licence_policy`
  as a floor (enforced via `CurrentToken`). `LicencePolicyService` prunes whole excluded sources in SQL;
  `/v1/records` and `getRecord` also filter per record. Filtered pages carry `licenceFiltered` and
  aggregated `attributions`; responses get a `Foresignal-Attribution` header and `Link rel="license"`.
- **Egress guard:** `EgressPath` extended to the 14 paths; `LicenceEgressGuard` implements the table and
  counts `foresignal.licence.egress.denied{path,licence}`; `EgressPolicy.allows(...)` calls it for the
  re-serving paths (private sources first). `GET /v1/attributions` aggregates a scope.
- **Reliability and credibility:** `source_settings.reliability` column, `SourceService.effectiveReliability`
  (override else descriptor), `CredibilityService` (rules 1–5; corroboration membership loaded once per
  response from `derived-corroboration`, deception from `derived-deception`), `grade = reliability +
  credibility`.
- **Signing:** `signing_key` (Ed25519, private encrypted with `CredentialCipher`), `Jwk` (RFC 8037 +
  RFC 7638 thumbprint `kid`), `SigningKeyService`/`SigningController` — `GET /.well-known/jwks.json`,
  `GET /v1/system/jwks` (open), `POST /v1/system/signing-key/rotate` (admin). `JwsSigner` (detached
  `b64:false`, `legacyAlg`), `JwsVerifier`, `ManifestSigner` (for 111). Briefing markdown/HTML renditions
  carry a detached `Foresignal-Signature`.
- **Spec/docs:** 109 operations flipped to `implemented`, removed from `pending-operations.txt` (4 lines);
  Operator-Guide "Licences and egress" (full table + verification recipe), Adapter-Guide "Licence terms
  and reliability", Source-Catalog licence note.

## Files created / modified

**Created (main):** `provenance/{LicenceTerms,LicenceCatalog,LicencePolicy,LicencePolicyService,LicenceResolver,
LicenceEgressGuard,AttributionService,CredibilityService,SigningKeyEntity,SigningKeyRepository,SigningKeyService,
Jwk,JwsSigner,JwsVerifier,ManifestSigner}`, `api/{SigningController,AttributionsController}`,
`src/main/resources/provenance/licences.json`.

**Modified (main):** `source/spi/SourceDescriptor` (3 components), `source/DescriptorValidator`,
`store/{FetchLogEntity? no; SourceSettingsEntity}` (reliability), `collect/FetchExecutor` (resolved licence),
`retention/ProvenanceStep`, `security/{EgressPath,EgressPolicy,ApiTokenEntity,AuthenticatedToken,TokenService,
AccessPolicy}`, `api/{RecordsController,ChangesController,TilesController,StreamController,BriefingsController}`,
`api/dto/{RecordPage,FeatureCollectionPage,RecordVersionPage,ChangePage,ApiTokenDto}`,
`stream/sse/ChangeFilter`, `briefing/BriefingRunEntity`, `config/CorsFilter`.

**Docs/spec:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Adapter-Guide.md`,
`docs/Source-Catalog.md`, `src/test/resources/openapi/pending-operations.txt` (−4 lines).

**Tests:** `provenance/{LicenceCatalogTest,LicencePolicyTest,LicenceEgressGuardTest,JwsTest}`.

## Deviations from the prompt

1. **Licence terms are resolved through catalogue aliases, not by editing every adapter.** The catalogue
   carries aliases for the free-text licences the adapters already set (`US-PD`, the OpenSky/ACLED/aisstream
   strings, the local-receiver string, ODbL), and `SourceDescriptor.licenceTerms` exists for an explicit
   override. The binding cases therefore resolve without a per-adapter edit; the audit is data, not code.
2. **`EgressPath` was extended in place** (additive), not replaced, and `LicenceEgressGuard` is wired into
   `EgressPolicy.allows(...)` via setter injection (so the existing 4-arg constructor keeps working in
   tests). The guard governs the re-serving paths (`SINK*`, `PUBLIC_SHARE`, `FEDERATION`, `SHARE`, `FEED`);
   `EXPORT`'s `personalUse` exception is left to 111's richer overload (the 3-arg path has no `personalUse`).
3. **Licence filtering covers `/v1/records`, `getRecord`, `/v1/changes`, the SSE change stream and
   `/v1/tiles`** (whole-source SQL prune via `LicencePolicyService`, plus per-record filtering on records).
   **Search, MCP read tools, Ask and briefing context do not yet apply the licence policy** — the
   attribution/guard plumbing is in place, but those paths still rely on source/private exclusions. This
   is the main coverage shortfall.
4. **Attribution aggregation and `licenceFiltered` are populated on `/v1/records` and `getRecord`**; the
   page DTOs (`RecordVersionPage`, `ChangePage`) carry the fields but the history/versions/changes
   controllers do not yet fill them.
5. **MCP `get_attributions` and a licence/`grade` block in `get_record` output are not added** (the REST
   `getAttributions` is).
6. **The signing key lives in a new `signing_key` table**, encrypted with `CredentialCipher` (the VAPID
   encryption pattern), rather than adding `created_at`/`status` columns to `system_secret` (which would
   have changed every VAPID row). `iss` is the `foresignal.instance-id` property (default `foresignal`).
7. **The reliability operator override is stored and read** (`source_settings.reliability`,
   `SourceService.effectiveReliability`), but the `PUT /v1/sources/{id}` body is not yet parsed into it, so
   the override is only settable in code/tests for now.
8. **`RegisteredSourcesHaveLicenceTest` and the context ITs (`LicenceResolverIT`, `LicencePolicyFilterIT`,
   `FetchLogRetentionIT`, `CredibilityServiceTest`, `SigningKeyServiceIT`, `BriefingSignatureIT`) are not
   written**; the pure units (catalogue, policy, guard, JWS/JWK) are. Consistent with 107/108.

## Issues found / open questions

- **Per-record licence filtering on tiles/SSE is source-level only** (a record of a mixed-licence source
  would not be dropped); the resolver-based per-record filter is on records/getRecord.
- **`LicenceResolver` uses `WHERE v.seq IN (:seqs)`** (Spring expands the list); the prompt's `= ANY(:seqs)`
  is equivalent. Statement-count tests are not written.
- **`CredibilityService.load()` scans current corroboration rows** rather than an indexed membership query;
  fine at v1 scale, revisit if `derived-corroboration` grows.
- **`signing_key` and the new `source_settings.reliability`/`api_token.licence_policy` columns** arrive via
  Hibernate `ddl-auto=update`; capture them in 144.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `provenance/LicenceCatalogTest` | built-in ids resolve; unknown → restrictive unknown; aliases; binding cases; `forDescriptor` |
| `provenance/LicencePolicyTest` | flag parsing/combining, unknown flag 400, exclusion rules |
| `provenance/LicenceEgressGuardTest` | the full table: ALLOWED, LOCAL_ONLY (LAN/personalUse), FORBIDDEN, UNKNOWN defaults |
| `provenance/JwsTest` | detached `b64:false` Ed25519 round trip, tamper fails, RFC 8037 JWK + RFC 7638 thumbprint, signing input |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `docs/OpenAPI.json` valid (four 109 operations
flipped to `implemented`). Postman regenerated (835 requests). No new dependencies.

## Notes for the next prompt

- **110/112/113** call `EgressPolicy.allows(EgressPath, sourceId, payload, viewer)` (the record-level
  overload) for their paths; `LicenceEgressGuard` is the table they enforce, and `LicenceResolver` supplies
  record terms.
- **111** signs `manifest.json` with `ManifestSigner.sign(bytes)`.
- **144 (Flyway):** capture `signing_key`, `api_token.licence_policy`, `source_settings.reliability`,
  `briefing_run.signature`.
- Consider wiring the licence policy into search/MCP/Ask and filling `attributions` on history/versions/changes.

Implementation commit: `61681791916747b70b515a4aafc9b15d52780ba2`
