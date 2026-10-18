# 144-CR-Production-Hardening

| | |
|---|---|
| Prompt | 144-Production-Hardening.md |
| Status | Partial (compile-and-report only under the owner directive of 2026-10-17: implemented and compiles; **no test, migration or runtime check was executed**; the `pg_dump` baseline procedure, the sanctioned suite run and the `v1.0.0` tag need running things and are left to the owner) |
| Implementation commit | `31b7d7d5415fe8df2d92ef167fc3103665ab2016` |
| Date | 2026-10-18 |

## Summary

**Nothing was run.** Under the owner directive of 2026-10-17 (the override banner on `prompts/144-*.md` and
`prompts/000-Run-Sequence.md` step 4):

- no test ran, including the new ones, the shape guards and the US English guard;
- no database was started, so the Flyway baseline has never been applied anywhere, and Hibernate `validate` has never
  checked it;
- no application, compose profile, `checkApiCompat`, `lintOpenApi`, SDK or Postman generator ran;
- `scripts/db/dump-schema.sh` was checked with `sh -n` only.

`./gradlew compileJava compileTestJava` succeeds on the committed tree: exit code 0.

What was built:

| Area | Change |
|---|---|
| Flyway | `spring-boot-starter-flyway` (Boot BOM) and `org.flywaydb:flyway-database-postgresql` 12.4.0 (pinned in `gradle/libs.versions.toml`; the version the Spring Boot 4.1.1 BOM manages, verified on Maven Central, Apache-2.0) |
| Baseline | `src/main/resources/db/migration/V1__baseline.sql`, 4,919 lines: `postgis` and `vector` extensions, `record_seq`, partitioned `record_version` (LIST by tier into `record_version_std`/`_trk`, each RANGE by seq, PK `(seq, history_tier)`, three partitioned indexes, no leaves, no DEFAULT), `record_version_held`, partitioned `chronicle_version`, `alert_rev`, all 238 entity tables, 19 unique `@Index` as UNIQUE constraints, and every index and storage parameter the bootstrap code created |
| Removed | `SchemaBootstrap`, `HistorySchemaInitializer`, `PostgisExtensionInitializer`, `VectorExtensionInitializer`, `SpatialIndexInitializer`, `ChronicleSchemaInitializer`, `ExportSchemaInitializer`, `InfraSchemaInitializer`, `DeliveryStatusConstraint`, `EmbeddingSchemaManager`'s base statements and runner; `SchemaBootstrapIT`, `SpatialIndexInitializerTest`, `HistorySchemaInitializerIT` |
| New wiring | `config/SchemaConfiguration` (beans for `PartitionManager`, `SemanticCapability`; `HistoryPartitionBootstrap` and `VectorExtensionProbe` run after `flywayInitializer` and before the `EntityManagerFactory`); `config/BaselineAwareMigrationStrategy` (warns before an existing database is adopted at version 1); `export/HeldVersionsTable` (the held table's name) |
| Configuration | `ddl-auto: ${FORESIGNAL_JPA_DDL_AUTO:validate}`; `extra_physical_table_types: PARTITIONED TABLE` kept; `spring.flyway.baseline-on-migrate=true`, `baseline-version=1`; test profile `validate`; `DatabaseCleaner` skips `flyway_schema_history` |
| Entities | 26 `columnDefinition = "jsonb default ..."` became `columnDefinition = "jsonb"` plus `@ColumnDefault` (same DDL; see Deviation 4). Javadoc that named the removed initializers or "Hibernate owns the table" updated |
| Tests (written, not run) | `config/FlywayBaselineIT`, `config/FlywaySchemaParityIT` (with the test-only `test/schema/PreCutoverSchema`, `SchemaSnapshot`, `ScratchDatabase`, `ApplicationHibernateSettings` and the old bootstrap SQL in `src/test/resources/schema/pre-cutover/`), rewritten `store/history/SchemaParityIT`, `config/NoBootstrapDdlTest`, `system/ApiVersionTest`, `source/SourceCatalogParityIT`, two new cases in `test/DatabaseCleanerIT`, `EmbeddingSchemaManagerIT` updated |
| API gate | `docs/OpenAPI.json` `info.version` 1.0.0 with a changelog entry; frozen copy `docs/api/v1.0.0/OpenAPI.json`; Gradle `checkApiCompat` (local `oasdiff`, else `tufin/oasdiff:v1.33.0`, else a warning; not wired to `build`); Postman description version line updated by hand |
| Release | Gradle `version = "1.0.0"`; Dockerfile OCI labels (`org.opencontainers.image.version=1.0.0`); compose `read_only: ${FORESIGNAL_READ_ONLY_ROOT:-true}` with a 64 MB tmpfs `/tmp` (the image already ran as uid 1001 with `curl`); first-start banner names "Foresignal (foresignal.ai), unrelated to foresignal.com"; `docs/Release-Notes-v1.0.0.md`, `CHANGELOG.md` |
| Scripts | `scripts/db/dump-schema.sh` (`dump`, `baseline`, `compare`; POSIX sh, Docker Compose only) |
| Docs | `docs/Architecture.md` v2.0 (1,566 lines, §25 is the Flyway regime, §27 weather, §28–§42 for 058–141, Appendix A open questions); `docs/Product-Charter.md` v2.0; `docs/Source-Catalog.md` with Tier, Policy, License and Egress on 344 rows in 41 tables; `docs/Executive-Summary.md`, `README.md`, `docs/Roadmap.md` (001–144 Done); `docs/Adapter-Guide.md` §26 "Schema changes (Flyway)"; `docs/CI-Proposal.md`; `docs/Conventions.md`; `Core-Data-Model.md`, `Weather-Imagery-Design.md` and `Gap-Analysis.md` moved to `docs/design-history/` with the header line; links fixed in docs, README and 141 prompt files |

## How the baseline was derived

`pg_dump` of a running pre-cutover database was impossible under the directive, so the baseline is hand-derived:

1. **Entities.** Eight read-only passes over the 238 `@Entity` classes, grouped by package. Rules were read from the
   Hibernate 7.4.5 and Spring Boot 4.1.1 jars with `javap`, not assumed:
   - naming: `PhysicalNamingStrategySnakeCaseImpl` (an underscore before an upper-case letter between a lower-case
     letter or digit and a lower-case letter or digit). In practice every column has an explicit snake_case name;
   - primary-key column order: `ColumnOrderingStrategyStandard.orderConstraintColumns`, called from `MetadataImpl` for
     primary keys (ascending physical size with a floor of 4 bytes, then name; `varchar(255)` counts as the
     dialect's maximum). Nine composite keys were reordered to that rule;
   - a unique `@Index` is a UNIQUE constraint (`IndexBinder.createIndexOrUniqueKey`);
   - no entity declares an association, so there are no foreign keys; all ids are IDENTITY except `record_seq`.
2. **Bootstrap SQL.** The DDL of the eight removed classes, copied from the deleted sources. The same statements are
   preserved for the parity generator in `src/test/resources/schema/pre-cutover/`.
3. **Cross-checks** (`grep`, `awk`, `comm`): every `@Table` name has a `CREATE TABLE`; every `@Index` and
   `@UniqueConstraint` name appears (one was missing, `regional_index_value_uq`, and was added); every column the
   completion reports list "for Flyway at 144" exists, including `backtest_job.worker_instance varchar(36)` (142) and
   the 132/136 tables; the columns behind the bootstrap indexes exist with the right types (GIN on array columns,
   GiST on geometry or geography columns); parentheses and quotes balance; no column name is a PostgreSQL reserved
   word; no `${` placeholder Flyway would substitute.

Left out on purpose: Hibernate's enum CHECK constraints (the application enforces enums, which also replaces
`DeliveryStatusConstraint`), Hibernate's hashed constraint names, and Hibernate's size-based column order.

**What the owner must verify:**

1. Run `FlywayBaselineIT` and `FlywaySchemaParityIT`. The parity test builds schema A with Flyway and schema B with
   the old bootstrap SQL around Hibernate's `update`. It compares columns (type, nullability, default, identity,
   generation), PK, UNIQUE, FK and EXCLUDE constraints, unnamed index definitions and sequences, ignoring column
   order, names and CHECK constraints. Its allowlist is empty. Fix each difference in `V1__baseline.sql`, which is
   still editable because it has never been applied.
2. The same comparison by hand: `scripts/db/dump-schema.sh dump pre-cutover.sql` on `bc4a2105` with every feature
   enabled, then `dump-schema.sh baseline flyway.sql` on 1.0.0, then `dump-schema.sh compare flyway.sql pre-cutover.sql`.
3. Start the application on an empty volume (`docker compose down -v`). If `validate` refuses a column that is
   correct, `FORESIGNAL_JPA_DDL_AUTO=none` bypasses the check while it is fixed.
4. An existing development volume is adopted at version 1 **without** running V1. The log warns about it. Compare
   with step 2, or recreate the volume.

## Deviations from the prompt

1. **Nothing ran** (owner directive). Not done:
   - generating the baseline with `pg_dump`;
   - the sanctioned `./gradlew test jacocoTestReport jacocoTestCoverageVerification`, so there are no counts or
     coverage;
   - `checkApiCompat` and `lintOpenApi`.
2. **No `.gitlab-ci.yml`** (standing owner rule). The proposed pipeline (stages build, test with Docker-in-Docker,
   api with oasdiff and `metric-drift.sh` as `allow_failure`, lint) is in `docs/CI-Proposal.md`. Owner action: adopt
   it as `.gitlab-ci.yml` if wanted.
3. **No `v1.0.0` tag.** The prompt's acceptance needs a green suite first. `CHANGELOG.md` and the release notes say
   the tag is pending. Owner action, after the suite passes:
   `git tag -a v1.0.0 -m "Foresignal 1.0.0" 31b7d7d5415fe8df2d92ef167fc3103665ab2016 && git push origin v1.0.0`.
4. **26 entity mappings changed** from `columnDefinition = "jsonb default '...'::jsonb"` to `"jsonb"` plus
   `@ColumnDefault`.
   - Hibernate's validator (`ColumnDefinitions.hasMatchingType`, read with `javap`) accepts a column when the type
     codes are equivalent or when the declared type name matches.
   - With a default inside `columnDefinition` the name never matches, and jsonb arrives as JDBC `OTHER` against
     Hibernate's `JSON`, so `validate` would probably have refused those columns.
   - The DDL is identical.
5. **The HNSW index is not in the baseline.** No default embedding dimension is configured, so the reindex keeps
   creating the vector slots at the model's dimension (Architecture §25). Only the slots' table DDL stays dynamic.
6. **pgvector is now required at migration time**, because the baseline creates `vector` as the prompt says. On an
   external PostgreSQL a superuser must create both extensions first. `VectorExtensionProbe` still reports semantic
   search off on an adopted database that lacks pgvector.
7. **The test is `SourceCatalogParityIT`, not `...Test`.** Only a started context can list every registered source
   and family member.
8. **More dynamic DDL is allowlisted than the prompt names.** `NoBootstrapDdlTest` also allows:
   - `HistoryDownsampler` (leaf rewrite) and `ChronicleDdl` (chronicle leaves): both are partition management;
   - `ReferenceDataLoader`'s `ref_stage_*` working tables;
   - the SQLite writers `GeoPackageWriter` and `MbtilesWriter`.
9. **`FORESIGNAL_JPA_DDL_AUTO` was added** as an escape hatch for a `validate` false positive. It defaults to
   `validate`.
10. **Not done:**
    - the Operator Guide (9,453 lines), which never mentioned the schema bootstrap (`grep` found no `ddl-auto`,
      `SchemaBootstrap` or `Flyway`);
    - the SDK package versions (still 1.3.0, the old document version);
    - `THIRD_PARTY_NOTICES.md`, which records incorporated code, not library dependencies.

    Flyway's license is listed in Architecture §2.
11. **Documents patched rather than replaced:** `Source-Catalog.md` and `Adapter-Guide.md`, by in-place edits because
    of their size, and the 141 prompt files (path renames only).
12. **Rule slips by agents:** three subagents each ran an empty or `--version` Python command (`python3 -c 1`,
    `python3 --version`). None of them executed project code. I also once ran an empty `python3 -c ""` by mistake.

## Issues found / open questions

**Owner decisions:**

- **(a) pgvector is required at migration time.** That follows the prompt, but `docker/db` and old docs call it
  optional (Architecture Appendix A).
- **(b) Enum CHECK constraints are omitted.** Keep that, or add them in a later migration?
- **(c) Read-only root filesystem by default** in compose. It has not been started.
- **(d) Stale authority claims.** `docs/Intelligence-Design.md` still calls the Core Data Model authoritative
  (`:5`) and says `ddl-auto=update` handles schema (`:208`).
- **(e) Weather budgets.** Design and code differ for `mrms`, `mrms-probsevere` and `goes-fdc`; Architecture follows
  the code.
- **(f) Wrong input ids in two generators,** found while building the catalog, not changed:
  - `FeltLocallyGenerator.explicitInputSources()` names `"usgs"`, `"emsc"`, `"fdsn"`, which are not source ids;
  - `CorroborationGenerator` names `"geofon"` and `"ingv"`, where the ids are `fdsn-geofon` and `fdsn-ingv`.
- **(g) Chat commands.** The code has five commands the 061 prompt text omits, and mesh replies are a second
  exception to the chat non-goal (Charter v2.0).
- **(h) Still open from 139 to 143,** including 143's (a)–(c).

No upstream was fetched besides Maven Central and Docker Hub metadata for version checks. No file or tool output
contained instructions directed at the agent.

## Compile result

`./gradlew compileJava compileTestJava` on the committed tree: exit code 0.

## Notes for the next prompt

- 144 is the last prompt. **At code-complete,** the owner:
  1. runs the suite, starting with `FlywayBaselineIT`, `FlywaySchemaParityIT`, `SchemaParityIT`,
     `NoBootstrapDdlTest`, `ApiVersionTest`, `SourceCatalogParityIT` and `DatabaseCleanerIT`;
  2. fixes the baseline until parity holds;
  3. runs the 143 runbook;
  4. tags `v1.0.0`.

No tests, migrations or runtime checks were executed (owner directive 2026-10-17).

Implementation commit: `31b7d7d5415fe8df2d92ef167fc3103665ab2016`
