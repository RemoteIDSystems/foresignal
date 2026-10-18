# Changelog

All notable changes to Foresignal are recorded in this file. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html). The HTTP API follows NFR-10: `/v1` is additive-only, and
breaking changes go to `/v2`.

## [Unreleased]

## [1.0.0] - 2026-10-18

The first release. Full notes: [docs/Release-Notes-v1.0.0.md](docs/Release-Notes-v1.0.0.md).

**The `v1.0.0` tag is pending the owner's test run.** No test, migration or runtime check was executed for this
release (owner directive of 2026-10-17); the tag is created once
`./gradlew test jacocoTestReport jacocoTestCoverageVerification` passes.

### Added

- The broker: budgeted collection from upstream sources (polling, streaming, push and derived), a single-writer ingest
  pipeline with ingest policies and a monotonic change sequence, versioned storage with tiered, partitioned history,
  retention and downsampling, and point-in-time reads.
- The `/v1` API (`docs/OpenAPI.json`, `info.version` `1.0.0`), server-sent change and event streams, vector and raster
  tiles, GeoJSON, OGC API Features, KML, CZML, CAP, STIX, iCalendar and other standards outputs, outbound sinks,
  exports, offline packs and federation.
- The trigger engine, alerts, notification channels, push and the notification inbox.
- Weather, raster and imagery products, and SatDump ground-station integration.
- The intelligence layer, the optional LLM features (Ask, briefings, enrichment, semantic search), the MCP server,
  client SDKs, a command-line client and plugins.
- The adapter doctor and the reviewed feature and feed kits.
- A Flyway baseline, `src/main/resources/db/migration/V1__baseline.sql`, holding the full schema: the PostGIS and
  pgvector extensions, every table, the partitioned history parents and every index.
- `scripts/db/dump-schema.sh` for schema comparisons, and the schema tests `FlywayBaselineIT`,
  `FlywaySchemaParityIT` and `NoBootstrapDdlTest` (written, not run).
- A frozen copy of the released spec, `docs/api/v1.0.0/OpenAPI.json`, and the Gradle task `checkApiCompat`
  (`oasdiff breaking … --fail-on ERR`).
- A proposed GitLab pipeline in `docs/CI-Proposal.md` (no `.gitlab-ci.yml` is added).

### Changed

- Hibernate runs with `ddl-auto=validate`; Flyway creates and evolves the schema. An existing development database is
  adopted at version 1 without running the baseline (`baseline-on-migrate`); see the release notes' upgrade notes.
- pgvector is required at migration time.
- The v1.2 design documents moved to `docs/design-history/`; `docs/Architecture.md` and `docs/Product-Charter.md`
  are the authoritative design documents.

### Removed

- The bootstrap DDL beans, among them `SchemaBootstrap`, `HistorySchemaInitializer`, `SpatialIndexInitializer` and
  `DeliveryStatusConstraint`. `PartitionManager` keeps creating history leaves at run time.

[Unreleased]: https://gitlab.com/adam.allard/foresignal/-/compare/v1.0.0...main
[1.0.0]: https://gitlab.com/adam.allard/foresignal/-/tags/v1.0.0
