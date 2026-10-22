# 158-CR-Relationship-Intelligence-People-Orgs

| | |
|---|---|
| Prompt | 158-Relationship-Intelligence-People-Orgs.md |
| Status | Complete (compile only; no test, `checkUsEnglish`, `lintOpenApi` or `checkApiCompat` was run, per `000-Run-Sequence.md` and the prompt) |
| Implementation commit | `704b5539f5faaea9bd114846d9a2457ba32a8e83` |
| Date | 2026-10-22 |

## What was added

People and organizations are now first-class canonical entities, with deterministic corporate / person
identity keys, ownership / affiliation edges, and an ownership-chain / shared-attribute graph query. This
is the **model and queries only** — the external corporate / UBO / PEP / leak data that populates the
edges is deferred to the Future-Roadmap, as the prompt directs.

### A — person / organization entities and keys

- `EntityType` gains `person` and `organization` (wire values `person`, `organization`); the enum javadoc
  no longer says "never a person or an organization".
- `EntityKeys` gains five key types, each with normalization, role, entity type and confidence:
  - `lei` — ISO 17442 Legal Entity Identifier, 20 upper alphanumeric; organization **anchor**, confidence 1.0.
  - `company-number` — `JURISDICTION:NUMBER` (both parts required, upper, number de-spaced); organization
    **anchor**, 1.0.
  - `tax-id` — EIN/VAT/TIN, upper, spaces and hyphens removed, 4–20 chars; organization **anchor**, 1.0.
  - `officer-id` — `REGISTRY:ID` (both parts required, upper); person **anchor**, 0.9.
  - `person-key` — a lower-cased, whitespace-collapsed composed name key (for example `name|dob|nationality`);
    person **weak** key, 0.5 (names collide, so a person anchors on an officer id when one is present).
- `EntityResolver` stays deterministic (no fuzzy matching): the new anchors and the weak `person-key` are
  slotted into `TYPE_ORDER` (total key order) and `PRECEDENCE` (`person-key` weak precedence). `typeFor`
  reads the entity type off the resolving key, so an entity created from `lei` / `company-number` / `tax-id`
  is an `organization` and one from `officer-id` / `person-key` is a `person`.

### B — ownership / affiliation edges

- `ObjectTypes.RELATION_TYPES` gains `owns`, `controls`, `officer-of`, `beneficial-owner`,
  `affiliated-with`. They are ordinary relation types, so they are usable on `createRelation`,
  `listRelatedObjects` and `queryGraph` immediately.

### C — ownership-chain / shared-attribute query

- `RelationService.ownershipChain` (POST `/v1/graph/ownership-chain`, `read`) is the sibling of
  `findCommonDependencies`: it walks the ownership / affiliation edges from up to 50 seed entities in a
  chosen direction (`in` / `out` / `both`, default `both`) up to 6 hops, and returns:
  - `chains` — every reached entity with its strongest, shortest ownership path from each seed that reaches
    it (the ownership chain); and
  - `shared` — the subset reached from two or more seeds, i.e. a **shared attribute** (a common owner,
    officer, address or affiliate — "entities sharing address/phone/officer").
- The traversal is a new `walkOwnership` (a BFS over only the ownership edge types, direction-aware, with
  the existing strongest-then-shortest `Path` / `better` machinery). Unlike `walkDependencies` it does no
  geographic (`located-in` / `near`) seeding, because ownership is not spatial. `walkDependencies` is left
  unchanged.
- `AccessPolicy`: POST `/v1/graph/ownership-chain` is `read`, like the other graph queries.

### D — spec and Postman

- `docs/OpenAPI.json` (1.3.0, no version bump): `person` / `organization` on the `Entity.type` enum and
  the entity `type` query filter; the five corporate/person key types on the `EntityKey`, `EntityKeyInput`
  and `EntityLink` `keyType` enums (with the `role`/`keyType` descriptions updated); the five ownership
  edge types on all five relation-type enums (related filter, `Relation`, `RelationInput`, `GraphRequest`,
  common-dependencies `relationTypes`); the `ownershipChain` operation with the `OwnershipChainRequest`,
  `OwnershipChain` and `OwnershipChainNode` schemas; and a 1.3.0 changelog entry at the top of the array.
- Postman regenerated (`scripts/generate-postman.py`): 857 requests (was 856), `ownershipChain` added;
  "covers all ops: True".

### E — roadmap

- `docs/Future-Roadmap.md` Benjis entry updated: the people/organization model, corporate keys, ownership
  edges and ownership-chain / shared-attribute queries are marked **built in 158**; the external
  corporate-registry / GLEIF / UBO / PEP / public-records / leak feeds remain the pending data step via the
  EDP and adapters.

## What runs

- `./gradlew compileJava compileTestJava` — passes (main + test).
- No tests run (owner directive). Tests written but **not** executed:
  - `EntityKeysTest` — new cases: each corporate/person key normalizes and rejects as specified (LEI 20
    chars, company-number needs jurisdiction + number, tax-id strips spaces/hyphens, officer-id needs
    registry + id, person-key lower + whitespace-collapsed); roles (four anchors + person-key weak), entity
    types (org vs person) and confidences.
  - `EntityResolverIT` — an LEI anchors an `organization` deterministically (idempotent on re-resolve); a
    company-number anchors an `organization`; an officer-id anchors a `person` and its weak `person-key`
    links a later name-only observation back to the same entity.
  - `GraphQueryTest.ownershipChain` — walking `out` from two parents reaches a shared target by two chains
    (the shared attribute), with the expected hop counts and weakest-edge labels; walking `in` from the
    target recovers the chain above it; a type filter narrows the walked edges; a non-ownership relation
    type is rejected.
- `checkUsEnglish`, `lintOpenApi`, `checkApiCompat` **not** run (owner directive). US English verified
  manually over the changed non-excluded files (the Java sources/tests and `docs/Future-Roadmap.md`)
  against `gradle/us-english/british-words.txt` — none. `docs/OpenAPI.json` validated as well-formed JSON.

## Owner decisions / notes

- **The "shared attribute" is modeled graph-natively.** `ObjectEnvelope` carries no free-form payload, so
  "entities sharing address/phone/officer" is expressed as two entities reaching a common node (a shared
  officer / address place / affiliate) through ownership/affiliation edges — the ownership analogue of
  `findCommonDependencies`. No new payload index or store query was introduced, and no external data is
  read. When ownership data lands, a shared officer is an `officer-of` edge from two orgs to one person; a
  shared address is a `located-in` edge to one place (walkable via `queryGraph`).
- **Determinism preserved.** All new key types were added to `TYPE_ORDER` (whose `indexOf` returns -1 for a
  missing type, which would corrupt the total key order) and `PRECEDENCE`; `person-key` is weak, so a
  person anchors on an officer id or tax id whenever one is present. No fuzzy matching.
- **No migration.** The `entity.type` column is `varchar(16)` with no DB-side enum; `person` and
  `organization` fit, and the new key types are plain `keyType` string values. JPA entities already map the
  columns; `ddl-auto=validate` is satisfied with no new migration.
- **`docs/OpenAPI.json` "honour Retry-After".** The new `/v1/graph/ownership-chain` 429 description mirrors
  the 405 existing occurrences of that boilerplate in this file (which is excluded from the US-English
  scanner); it was kept consistent with the file rather than changed to "honor" in one place.
- **`checkApiCompat` (owner run):** one additive operation (`ownershipChain`), three new schemas
  (`OwnershipChainRequest`, `OwnershipChain`, `OwnershipChainNode`), and additive enum members on the
  entity-type, key-type and relation-type enums — confirm oasdiff treats added enum values and the new
  operation as non-breaking.
- No tests, runtime or soak verification performed; deferred to the owner at code-complete.
