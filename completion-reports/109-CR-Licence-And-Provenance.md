# 109-CR-Licence-And-Provenance

| | |
|---|---|
| Prompt | 109-Licence-And-Provenance.md |
| Status | Complete, with the read-path coverage gaps listed under Deviations (post-review fixes 2026-10-16) |
| Implementation commit | `61681791916747b70b515a4aafc9b15d52780ba2` |
| Post-review fix commit | `c076ab7` (`109: fix post-review defects`) |
| Date | 2026-10-15 (post-review fixes 2026-10-16) |

## Summary

License, provenance and reliability are first-class. Compiles; tests written, not run. The review of
2026-10-16 found defects in the first implementation and inaccuracies in this report; both are corrected, the
fixes are listed under "Post-review fixes", and every statement below describes the code after them.

- **Structured terms (`ai.foresignal.provenance`):** `LicenceTerms` (`CommercialUse`, `Redistribution`, egress-safe
  `UNKNOWN` defaults), `LicenceCatalog` loaded from `src/main/resources/provenance/licences.json` (the prompt's
  built-ins plus `CC-BY-NC-SA-3.0-IGO`, `ODC-By-1.0`, `OGL-Canada-2.0`, `gdelt-terms`, the provider classes
  `reuse-with-attribution`, `reuse-with-attribution-commercial`, `provider-terms`, `provider-terms-nc`,
  `personal-use-only`, `all-rights-reserved`, `unspecified`, and aliases for every free-text license an adapter
  declares). `SourceDescriptor` has `licenceTerms`, `reliability` and `derivedFrom`; `DescriptorValidator`
  warns when neither resolves. `SourceLicences` gives a source's terms and, for a DERIVED source, the most
  restrictive terms of its inputs (`derivedFrom`, else the generator's subscribed sources; fail closed to
  `unknown`). Binding cases: OpenSky, ACLED (redistribution and commercial `FORBIDDEN`), aisstream
  (redistribution `FORBIDDEN`), adsb.lol `ODbL-1.0`, local receivers `local-only-operator` (`LOCAL_ONLY`),
  submarine cables `CC-BY-NC-SA-4.0`, US federal `us-gov-public-domain`.
- **Per-record resolution:** `LicenceResolver` resolves a page in one query (`record_version ⋈ fetch_log`, `IN
  (:seqs)`); the batch licence wins, a gone fetch row falls back to the source's terms (`licenceBasis:
  descriptor`), a peer record's `_foresignal.originLicence` overrides (`peer-origin`), and an MLAT position is
  downgraded to `LOCAL_ONLY` keeping its license id and attribution (`mlat-override`). `FetchExecutor`
  snapshots the resolved catalog id; PUSH batches snapshot the custom source's `licenceTerms` id (else the
  resolved catalog id). `ProvenanceStep` keeps fetch rows with versions for `max(retention.provenance, the
  largest version window)`.
- **Client policy:** `LicencePolicy` (`no-nc`, `no-sa`, `redistributable`) from `Foresignal-Licence-Policy` or
  `licencePolicy`, with the token's `api_token.licence_policy` as a floor; set at token creation or replaced with
  the new `updateToken` (`PATCH /v1/tokens/{tokenId}`, admin). `/v1/records` and `getRecord` filter per record,
  carry `licenceFiltered` and `attributions`, a `Foresignal-Attribution` header (printable ISO-8859-1, ≤ 1 KB,
  truncated with `...`), `Link rel="license"`, a licence-policy component in the list ETag and
  `Vary: …, Foresignal-Licence-Policy`. Tiles, the SSE change stream and `/v1/changes` prune whole sources in SQL
  and send `Foresignal-Licence-Filtered` (sources pruned). TileJSON `attribution` aggregates the layer's sources.
  `GET /v1/attributions` aggregates a scope.
- **Egress:** `EgressPolicy.decide(path, sourceId, recordTerms, payload, EgressContext)` is the single decision
  for every path, returning a `Decision` (allowed, reason, terms); the `allows` overloads delegate to it.
  `LicenceEgressGuard` is its internal license table (public only for the pending 111 caller, guarded by
  `LicenceEgressGuardUsageTest`) and counts `foresignal.licence.egress.denied{path,licence}`. The private-source
  rule is in "Private-record egress" below.
- **Reliability and credibility:** descriptors without a grade are seeded (`ReliabilitySeed`: official agencies
  `B`, own receivers and other built-in providers `C`, GDELT/Wikipedia/community feeds `D`, custom, peer, DERIVED
  and licence-less `F`); the operator override is parsed from `PUT /v1/sources/{id}` (`reliability`, `null`
  reverts) and stored in `source_settings.reliability`. `CredibilityService` implements the five rules over the
  shapes 099 emits: corroboration `members[]` with `independentSources` as the list of distinct source ids
  (its size counts), deception `members[]` (now emitted by `DeceptionGenerator`) plus the deception record's
  entity id for aircraft and vessel records. `grade = reliability + credibility`.
- **Signing:** `signing_key` (Ed25519, private key encrypted with `CredentialCipher`), `Jwk` (RFC 8037 OKP,
  RFC 7638 thumbprint `kid`), `SigningKeyService` (first use and rotation serialized by a PostgreSQL advisory
  lock; `activeMaterial()` reads kid and keys from one row; rotation writes a `signing_key` audit event),
  `SigningController` (`GET /.well-known/jwks.json`, `GET /v1/system/jwks`, `POST /v1/system/signing-key/rotate`),
  `JwsSigner` (detached `b64:false`, `legacyAlg`), `JwsVerifier` (checks `alg` ∈ {Ed25519, EdDSA}, `b64:false`,
  `crit` exactly `["b64"]` before the signature), `ManifestSigner` (for 111). Briefing runs store `signature`
  (detached JWS over the markdown, signed when the run finishes); the JSON rendition sends it as
  `Foresignal-Signature` and the HTML rendition signs its own bytes.

## Private-record egress

Owner decision of 2026-10-16: prompt 109 line 77 applies. A private source (039) is excluded on every path
except `API_READ`/`TILE`/`SSE`/`MCP` for callers allowed to see it, and `ARCHIVE`, regardless of license. Two
later prompts make explicit, conditional exceptions, and they are implemented in `EgressPolicy` and nowhere
else:

- `EXPORT`, prompt 111 line 32 ("Private sources (039) need admin plus `includePrivate=true`"): allowed when
  `EgressContext.viewer()` is admin and `includePrivate()` is true (`EgressContext.export(viewer,
  includePrivate, personalUse)`).
- `SINK`, `SINK_SERVE_LAN`, `SINK_PUSH`, prompt 110 line 46 ("excluded unless `filter.includePrivate=true`
  and the sink is SERVE on loopback or a PUSH target that resolves to a private address"): allowed when
  `includePrivate()` and `localTarget()` are both true (`EgressContext.sink(includePrivate, localTarget)`).

`FEED`, `SHARE`, `PUBLIC_SHARE`, `FEDERATION`, `NOTIFICATION` and the LLM paths never carry a private record
through `decide`; LLM context and notification payloads keep their dedicated 026/039 methods (`filter`,
`allowsNotificationPayload`). Before the fix, a viewer with the private grant could take private records out on
`EXPORT`, `SHARE`, `PUBLIC_SHARE`, `SINK` and `FEED`. Prompt 112 line 100 (share links: "Private targets need
admin", masked) is a third conditional exception that is not implemented, because 112 is not built; it must be
added to `decide` when 112 lands.

## Files created / modified

**Created (main):** `provenance/{LicenceTerms,LicenceCatalog,LicencePolicy,LicencePolicyService,LicenceResolver,
LicenceEgressGuard,AttributionService,CredibilityService,SigningKeyEntity,SigningKeyRepository,SigningKeyService,
Jwk,JwsSigner,JwsVerifier,ManifestSigner}` and (post-review) `provenance/SourceLicences`,
`source/spi/ReliabilitySeed`; `api/{SigningController,AttributionsController}`;
`src/main/resources/provenance/licences.json`.

**Modified (main):** `source/spi/SourceDescriptor`, `source/DescriptorValidator`, `store/SourceSettingsEntity`,
`collect/FetchExecutor`, `retention/ProvenanceStep`, `security/{EgressPath,EgressPolicy,ApiTokenEntity,
AuthenticatedToken,TokenService,AccessPolicy}`, `api/{RecordsController,ChangesController,TilesController,
StreamController,BriefingsController,TokensController}`, `api/dto/{RecordPage,FeatureCollectionPage,
RecordVersionPage,ChangePage,ApiTokenDto,SourceDescriptorDto,SourceSettingsUpdate}`,
`api/mapper/{SourceMapper,TileJsonMapper}`, `stream/sse/ChangeFilter`, `briefing/{BriefingRunEntity,BriefingRunner,
BriefingViews}`, `inbound/{CustomSourceInputReader,CustomSourceDefinition,CustomSourceEntity,CustomSourceService,
CustomSourceDescriptors,CustomSourcesController,PushIngestService}`, `source/adapters/derived/DeceptionGenerator`,
`config/CorsFilter`.

**Docs/spec:** `docs/OpenAPI.json` (109 operations implemented; post-review: `updateToken`, the
`createCustomSource` description, a changelog entry), `docs/Operator-Guide.md` ("Licences and egress", rewritten
post-review), `docs/Adapter-Guide.md`, `docs/Source-Catalog.md`, `scripts/generate-postman.py` (BODIES entry for
`updateToken`), `src/test/resources/openapi/pending-operations.txt` (−4 lines).

**Tests:** see "Tests written (not run)".

## Deviations from the prompt

1. **License terms are resolved through catalog aliases, not by editing every adapter.** The catalog maps every
   free-text license an adapter declares to an entry (`RegisteredSourcesHaveLicenceTest` walks the registry and
   fails on any `unknown`). Strings that state no license map to `provider-terms`, `provider-terms-nc` or
   `unspecified`, whose redistribution is `UNKNOWN` (local only) rather than a guessed value.
2. **License filtering covers `/v1/records`, `getRecord`, `/v1/changes`, the SSE change stream and `/v1/tiles`.**
   Search, MCP read tools, Ask and briefing context do not apply the license policy yet; they rely on source and
   private exclusions. This is the main coverage shortfall.
3. **`attributions` and `licenceFiltered` are filled on `/v1/records` and `getRecord` only;** history, versions
   and changes carry the fields but do not fill them. Streams and tiles report `Foresignal-Licence-Filtered` as
   the number of **sources** the policy pruned, because pruning happens in SQL and the excluded records are never
   read or counted.
4. **MCP `get_attributions` and a license/`grade` block in `get_record` output are not added.**
5. **The signing key lives in a `signing_key` table**, encrypted with `CredentialCipher`, rather than in
   `system_secret` (which would have changed every VAPID row). `iss` is `foresignal.instance-id` (default
   `foresignal`).
6. **Reliability defaults are derived, not set per descriptor** (`ReliabilitySeed`, documented in the Operator
   Guide); a descriptor that sets `reliability` keeps it.
7. **`derivedFrom` is declared on no built-in descriptor;** `SourceLicences` falls back to the generator's
   subscribed source ids. A generator that subscribes only by kind or to raster products has no resolvable
   inputs and keeps the restrictive `unknown` terms.
8. **099 still writes `reliability: "F"` into corroboration payloads;** the prompt's "make 099 read
   `reliability` from this service" is not done. `CredibilityService` reads the source grades itself.
9. **`fetch_log` retention keeps versioned rows for the largest version window of any source**, not each
   source's own window: never shorter than the prompt's rule, sometimes longer.

## Issues found / open questions

- `ai.foresignal.export.ExportLicenceService` (111) still calls `LicenceEgressGuard.allows(EXPORT, terms,
  personalUse, false)` and `LicenceCatalog.forDescriptor` directly, so its exports skip the private rule, the
  descriptor `egress`, the operator overrides and `derivedFrom`. It should call
  `EgressPolicy.decide(EgressPath.EXPORT, sourceId, null, null, EgressContext.export(viewer, includePrivate,
  personalUse))` per source (per record with the resolved terms and payload where it has them),
  `EgressPolicy.sourceTerms(sourceId)` for the sidecar, and `decide(EgressPath.ARCHIVE, …, EgressContext.NONE)`
  for the cold archive. When it does, remove it from `LicenceEgressGuardUsageTest.ALLOWED`. 111 is owned
  elsewhere, so it is reported, not changed.
- `docs/Invariants.md` EGR-1 lists `FeedsControllerIT` as "private-source alerts reach Atom and JSON Feed only
  through the private gate"; under the owner's 109 rule `FEED` never carries a private record, so
  `FeedsControllerIT.privateExclusion` now expects none even for admin. The invariant's wording should be
  updated by the owner.
- The `jwcrypto` recipe in the Operator Guide was not run; a recipe derived from RFC 7797 with the
  `cryptography` package sits next to it.
- `signing_key`, `source_settings.reliability`, `api_token.licence_policy`, `briefing_run.signature` and
  `custom_source.licence_terms` arrive via Hibernate `ddl-auto=update`; capture them in 144.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `provenance/LicenceCatalogTest` | built-in ids resolve; unknown → restrictive unknown; aliases; binding cases |
| `provenance/LicencePolicyTest` | flag parsing/combining, unknown flag 400, exclusion rules |
| `provenance/LicenceEgressGuardTest` | the table: ALLOWED, LOCAL_ONLY (LAN/personalUse), FORBIDDEN, UNKNOWN defaults |
| `provenance/LicenceEgressGuardUsageTest` | guard: only `EgressPolicy` (and the pending 111 caller) names the guard |
| `security/EgressPolicyTest` | private paths and the 110/111 exceptions; license on EXPORT/ARCHIVE; descriptor `LOCAL_ONLY` over an open license; operator override; MLAT downgrade keeping ODbL attribution; per-record terms; `derivedFrom` |
| `security/LocalOnlyEgressIT`, `api/FeedsControllerIT` | updated for the licence-guarded EXPORT and the 109 FEED rule |
| `provenance/RegisteredSourcesHaveLicenceTest` | every registered non-DERIVED source resolves; DERIVED no less strict than inputs; binding cases denied on sinks, shares, federation |
| `provenance/SourceLicencesTest` | most-restrictive inheritance, merging, cycles and missing inputs; reviewed strings; MLAT and operator transforms |
| `provenance/LicenceResolverIT` | batch license wins; descriptor fallback; MLAT downgrade; peer `originLicence`; one statement per resolve |
| `provenance/LicencePolicyFilterIT` | records and GeoJSON under each flag, counts, attributions with counts, `Link` headers, ETag/Vary, token floor and `updateToken`, tiles/SSE/changes header |
| `retention/FetchLogRetentionIT` (and `RetentionJobIT` updated) | versioned fetch rows survive while their versions are retained |
| `provenance/CredibilityServiceTest` | every grade rule over generator-shaped payloads; list `independentSources`; grade; override |
| `provenance/SigningKeyServiceIT` | one key under concurrent first use; encrypted at rest; JWKS shape and thumbprint `kid`; rotation keeps the retired key verifiable; audit event |
| `provenance/JwsTest` | round trip; RFC 8037 Appendix A key, thumbprint and signature vectors; verifier header checks |
| `provenance/JwsSignerTest` | detached round trip, tamper and foreign key fail, header shape, `legacyAlg`, attached JWS refused |
| `briefing/BriefingSignatureIT` | a run's signature verifies against the published JWKS; JSON and HTML renditions |
| `provenance/AttributionServiceTest` | ISO-8859-1-safe, `...`-truncated header; layer aggregation |
| `source/spi/ReliabilitySeedTest`, `inbound/CustomSourceLicenceTermsTest` | seeded grades; custom `licenceTerms` parsing |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL` on the post-review tree. `docs/OpenAPI.json` parses.
Postman was **not** regenerated by this prompt's first run (the earlier "Postman regenerated (835 requests)" was
inaccurate) nor by the post-review fixes; `scripts/generate-postman.py` has the BODIES entries and the
collection is regenerated by the coordinator. No new dependencies.

## Notes for the next prompt

- Every egress path calls `EgressPolicy.decide(path, sourceId, recordTerms, payload, context)` (or an `allows`
  overload) with `EgressContext.of(viewer)`, `.sink(includePrivate, localTarget)`, `.export(viewer,
  includePrivate, personalUse)` or `.NONE`. Never call `LicenceEgressGuard` directly.
- 111: move `ExportLicenceService` to `decide` (see Issues). Sign `manifest.json` with `ManifestSigner.sign(bytes)`.
- 112: add the share-link private exception (admin, masked) inside `EgressPolicy.decide`.
- 113: put `_foresignal.originLicence` on peer records; `LicenceResolver` already reads it.
- 144 (Flyway): capture the columns listed under Issues.

## Post-review fixes (2026-10-16)

| # | Finding | Result |
|---|---|---|
| 1 | Deception rule read `members[]` the generator never wrote | Fixed: `DeceptionGenerator` emits `members[]`; `CredibilityService` also matches the deception record's entity id for aircraft/vessel records |
| 2 | `independentSources` is a list, code accepted only a number | Fixed: list size, else number, else distinct member sources |
| 3 | Every source graded F; PUT override not parsed | Fixed: `ReliabilitySeed` defaults; `reliability` parsed and validated in `SourceMapper` |
| 4 | EXPORT/ARCHIVE not licence-guarded; `lanLocal` always false; MLAT downgrade ignored | Fixed in `EgressPolicy.decide` (license on every path, `localTarget`, `personalUse`, downgrade keeping attribution) |
| 5 | Private records left on PUBLIC_SHARE/SHARE/EXPORT/SINK/FEED | Fixed per the owner decision with the explicit 110/111 exceptions (see "Private-record egress") |
| 6 | `LicenceEgressGuard` a parallel entry point | Fixed: internal delegate, documented; usage guard test; 111 caller reported |
| 7 | List ETag ignored the policy header; no Vary | Fixed: policy in the ETag; `Vary` on list and `getRecord`, tiles |
| 8 | Signing race, kid/key consistency, verifier checks, header encoding, MLAT attribution | Fixed (advisory lock, `activeMaterial`, `alg`/`b64`/`crit`, ISO-8859-1 + `...`, `downgradedToLocalOnly`) |
| U | Unresolved descriptor licenses | Fixed: catalog entries and aliases; `RegisteredSourcesHaveLicenceTest` |
| U | `derivedFrom` never read | Fixed: `SourceLicences` (not declared on built-ins; see Deviation 7) |
| U | Custom sources refused `licenceTerms` | Fixed: accepted, stored, put on the descriptor, snapshotted per PUSH batch |
| U | Peer `originLicence` unused | Fixed in `LicenceResolver` (`peer-origin`) |
| U | `Foresignal-Licence-Filtered` never set | Fixed on tiles, SSE change stream and `/v1/changes` (sources pruned) |
| U | No token update path for `licencePolicy` | Fixed: `updateToken` |
| U | TileJSON attribution not aggregated | Fixed: `AttributionService.layerText` |
| U | `BriefingRun.setSignature` never called | Fixed: runs are signed when they finish |
| U | Rotation wrote no audit event | Fixed |
| T | Missing tests; JwsTest asserted the thumbprint equals itself | Written; the RFC 8037 Appendix A vectors (checked with the JDK before use) |

Corrected inaccuracies of the first report: Postman was not regenerated; briefing runs were not signed (only the
renditions, at request time); the JwsTest "RFC 8037 vector" was not a vector; the reliability override was not
settable through the API; the license hook did not govern EXPORT/ARCHIVE; 110's gate bypass of `EgressPolicy` was
not disclosed.

Implementation commit: `61681791916747b70b515a4aafc9b15d52780ba2`
