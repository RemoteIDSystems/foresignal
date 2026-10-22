# 158 — Relationship intelligence for people & organizations

**Goal.** The entity resolver and relation graph are **physical-domain only** today (aircraft / vessel /
satellite / station; `EntityType` doc says "never a person or an organization"). Extend them to
**people and organizations** with **ownership/affiliation edges** and **ownership-chain /
shared-attribute queries** — the Benjis-style engine. **This builds the model and queries only; the
external corporate/UBO/PEP/leak data that feeds it is deferred to the Future-Roadmap.**

**Read first.**
- `src/main/java/ai/foresignal/entity/EntityType.java` — AIRCRAFT/VESSEL/SATELLITE/STATION; "never a
  person or an organization" (~L6).
- `entity/EntityKeys.java` — the 8 physical key types (~L41), confidences (~L94).
- `entity/EntityResolver.java` — deterministic `resolve` (~L82), `PRECEDENCE` (~L34), `merge` (~L307),
  survivor pointer (~L325); "deterministically and without fuzzy matching" (~L24).
- `object/ObjectTypes.java` `RELATION_TYPES` (~L90).
- `object/RelationService.java` — `related`/`listRelatedObjects` (~L227), `queryGraph` (~L322),
  `findCommonDependencies` (~L436) / `walkDependencies` (~L507) / `COMMON_HOPS` (depends-on, located-in,
  near only).

**Build.**
- **Person / organization entities:** add `person` and `organization` to `EntityType`, and
  corporate/person key types to `EntityKeys` — LEI, company number, officer id, tax id, and a normalized
  person key — each with a confidence. Keep `EntityResolver` **deterministic** (anchor/weak-key rules,
  survivor pointer); extend `PRECEDENCE` for the new types. No fuzzy matching.
- **Ownership / affiliation edges:** add edge types to `ObjectTypes.RELATION_TYPES` — `owns`,
  `controls`, `officer-of`, `beneficial-owner`, `affiliated-with`.
- **Ownership-chain / shared-attribute query:** generalize `RelationService.walkDependencies` (or add a
  sibling traversal) beyond depends-on/located-in/near to walk ownership/affiliation edges and
  shared-attribute links (entities sharing address/phone/officer), exposed through `queryGraph` or a
  new graph endpoint.
- **Do NOT source external data** (corporate registries / GLEIF / OpenCorporates-style / UBO / PEP /
  court / property / ICIJ leak graphs). That is a separate large effort via the EDP/adapters — record
  it in `docs/Future-Roadmap.md` as the next step after this model exists (the Benjis entry already
  notes it; mark the model as built in 158, data still pending).

**API / docs.** No version bump (1.3.0 wave). Add the new entity types, key types, relation types, and
graph-query options to `docs/OpenAPI.json` under 1.3.0; regenerate Postman. Keep `checkApiCompat`
additive.

**What runs.** Compile only; no tests/checks per `000-Run-Sequence.md`. Write tests (a person/org
entity resolves deterministically on a corporate key; an ownership edge is traversable; an ownership-
chain / shared-attribute query returns the expected path) but do not run them. US English.

**Done when.** People and organizations are first-class entities with corporate keys, ownership/
affiliation edges exist, and ownership-chain / shared-attribute queries work — ready for data to land
later; spec + Postman updated under 1.3.0; compile passes.

**Commit.** `158: Relationship intelligence for people & organizations` + `completion-reports/158-CR-*.md`.
**Stage only this prompt's files.**
