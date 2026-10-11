# 128 — Client SDKs CLI And Extensibility

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/128-CR-Client-SDKs-CLI-And-Extensibility.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/127-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §2 rows "Generated SDKs (Dart, then TS, then Python)" (hand-written SSE resume/`reset`/heartbeat layer, cursor iterators, Dart needs a pre-processed spec because dart-dio handles OpenAPI 3.1 nullable unions badly) and "CLI" (picocli native binary, no device grant); §3 row "Not core (post-v1)" (CZML, QuakeML/FDSN, STIX/TAXII, audio briefings); §5 (translation); §6 row "Out-of-tree plugins…"; §8 item D.62 ("060a" MCP remote authorization: RFC 9728 plus IdP delegation, no built-in authorization server); §9 (in-house MCP authorization server and RFC 8628 rejected)
- `docs/Core-Data-Model.md` §10.2 (SSE events, `reset`, `resync`, heartbeat, client contract: snapshot with `order=id` + `watermarkSeq`, apply if `observedAt` ≥ stored)
- Completion Reports of 014 (cursors, `410 RESYNC_REQUIRED`), 015 (multiplexed stream, PATCH filter, heartbeat interval), 020 (served spec, `checkSdkGeneration`, API-evolution convention), 023 (stream tickets), 025 (MCP transport and tool scopes), 026 (provider types and roles), 029 (briefing runs, renditions), 043 (media store), 058 (feeds), 076 (cyber kinds), 089 (seismic kinds, FDSN), 018 (channel rendering, templates, `language`)
- MCP specification 2025-11-25 "Authorization" (RFC 9728 protected resource metadata, `WWW-Authenticate` `resource_metadata` and `scope`, RFC 8707 audience validation, 403 `insufficient_scope`, no token passthrough)
- CZML guide; QuakeML 1.2 BED; FDSN Web Service Specifications (fdsnws-event 1.2); STIX 2.1; TAXII 2.1

## Objective

Make Foresignal easy to build on and extend, keeping every piece minimal and optional: generated Dart, TypeScript and Python SDKs with a hand-written streaming and pagination layer; a picocli CLI; OAuth-based remote authorization for `/mcp` that delegates to the operator's identity provider; a WASM-first plugin SPI for out-of-tree parsers; a few science and security exchange formats; and audio briefings plus notification translation through configured providers. None of these changes the core data path.

## Tasks

### 1. SDK generation (`sdk/`, generated into the repo, published separately)

- Gradle task `generateSdks` (not wired to `build`; runs tools via Docker or `npx`/`pipx` when present, else fails with install instructions). Pin every tool version in `sdk/versions.properties` and verify current versions at implementation.
- **Dart** (`sdk/dart/foresignal_api`):
  - `scripts/sdk/preprocess-dart.py` turns `docs/OpenAPI.json` into an OAS 3.0.3 copy: `type: [T, "null"]` → `type: T` + `nullable: true`; `oneOf: [{$ref}, {type: "null"}]` → `allOf: [{$ref}]` + `nullable: true`; `const` → single-value `enum`; `examples` → `example`; drops `webhooks`, `$schema`, `jsonSchemaDialect`; keeps `x-foresignal-*`.
  - Generate with openapi-generator `dart-dio` (`serializationLibrary=json_serializable`, `pubName=foresignal_api`).
  - `checkSdkGeneration` (020) runs the preprocess step plus a generator dry run.
- **TypeScript** (`sdk/typescript`): `openapi-typescript` for types plus `openapi-fetch` for the client (both MIT; native OAS 3.1).
- **Python** (`sdk/python`): `openapi-python-client` (native 3.1), package `foresignal_client`.
- **Hand-written layer per SDK** (`sdk/{lang}/…/realtime`, `…/paging`), identical semantics:
  - **`ChangeStream`:** opens `/v1/changes/stream` or `/v1/events/stream` (015) with `after=` / `Last-Event-ID`, using a stream ticket (023) where headers aren't possible (browsers). Tracks the last `change` id.
    - On `reset`: emits a `Reset` signal, pages the snapshot (`/v1/records?order=id`, keeping `watermarkSeq`), then reopens at the watermark.
    - On `resync`: refetches the affected live state.
    - Applies live and touch events only when `observedAt` ≥ the stored value.
    - Heartbeat watchdog: reconnects when no event or heartbeat arrives within 2.5× the server interval, with jittered exponential backoff (1 s → 60 s), honouring `retry:` and `Retry-After`.
  - **Cursor iterators** over every paged operation (`next` cursor; async iterables in TS, `Stream` in Dart, generators in Python). `410 RESYNC_REQUIRED` on `/v1/changes` surfaces as a typed exception.
- Each SDK gets a README (install from a local path or git; versioning = API `info.version`; publishing is a separate release step, not done here) and native tests (`dart test`, `vitest`, `pytest`) for the realtime and paging layers against a mock SSE server.

### 2. CLI (`cli/`, a standalone Gradle build, not part of the broker build)

- picocli 4.7.x, `java.net.http`, Jackson; GraalVM native image optional via `org.graalvm.buildtools.native` and `picocli-codegen` (reflection config generated). Java 21.
- **Config:** `foresignal login --url --token` stores `~/.config/foresignal/config.json` (POSIX 0600; warn on Windows) with named profiles. Env `FORESIGNAL_URL`/`FORESIGNAL_TOKEN` override. The token is never printed.
- **Commands:**
  - `system`
  - `sources list|show|enable|disable|health|fetch|probe|doctor` (127)
  - `records query|get|history|track`
  - `changes tail [--source --kind --bbox] [--live]` (SSE with the same resume/reset semantics as task 1)
  - `alerts list|ack|resolve|tail`
  - `triggers list|create -f rule.json|test|quality|tune`
  - `ask "…"` (streaming)
  - `briefings list|run`
  - `exports create|status|download` (111)
  - `tokens list|create|revoke`
- **Output:** `--output table|json|ndjson`; exit codes 0, 1 (usage), 2 (API error with the ProblemDetail `code`), 3 (network).
- Tests (picocli `CommandLine` + WireMock) live in `cli/src/test` and run with the CLI build only.

### 3. MCP remote authorization (RFC 9728, IdP delegation)

- **Settings** `mcp.auth`: `mode` (`token` default | `oauth` | `both`), `issuer`, `audience` (default `{publicBaseUrl}/mcp`, the canonical resource URI), `jwksUri` (optional; else discovered via RFC 8414 then OIDC discovery from the issuer, through the outbound URL policy 035), `scopeMap` (e.g. `{"foresignal:read":"read","foresignal:agent":"agent"}`; `admin` is **never** mappable), `scopeClaim` (`scope` or `scp`), `allowedAlgs` (`RS256`, `ES256`, `EdDSA`/`Ed25519`).
- **Protected resource metadata:** `GET /.well-known/oauth-protected-resource/mcp` and `GET /.well-known/oauth-protected-resource` (open) return `{resource, authorization_servers:[issuer], scopes_supported, bearer_methods_supported:["header"], resource_name:"Foresignal", resource_documentation}`.
- **Validation** (JWT via Nimbus JOSE+JWT, Apache-2.0; JWKS cached and refreshed on unknown `kid`, at most once per minute):
  - `iss` must equal the issuer, `aud` must contain the audience (RFC 8707), `exp`/`nbf` with 60 s skew, alg allowlist.
  - Mapped scopes → the Foresignal scope for MCP tool checks (025). The token is never forwarded anywhere (no passthrough).
  - Applies only to `/mcp`; REST keeps Foresignal tokens.
- **Errors:**
  - missing or invalid token → `401` with `WWW-Authenticate: Bearer resource_metadata="…", scope="foresignal:read"`
  - a tool needing more → `403` with `error="insufficient_scope", scope="…", resource_metadata="…"`
- **No built-in authorization server and no device grant.** Document setup with Keycloak, Authentik, Pocket ID and Auth0 (client registration, the `resource` parameter, scope names), plus Claude and other MCP clients connecting remotely.

### 4. Plugin SPI (WASM-first, out-of-tree; disabled by default)

- Write `docs/Plugin-Design.md`: goals; non-goals (no JVM plugins, no network or filesystem for plugins); the ABI; the security model; versioning.
- **Minimal implementation** `ai.foresignal.plugin` using Chicory (pure-Java WASM runtime, Apache-2.0; verify the version):
  - `plugins.enabled=false`; plugins load from `${FORESIGNAL_DATA_DIR}/plugins/{name}/` containing `plugin.wasm` and `plugin.json` (`name`, `version`, `abi: 1`, `kind: "parser"`, `sha256`).
  - The admin enables each one via `PUT /v1/plugins/{name}` after reviewing the sha256.
- **ABI v1 (parser only):** the host calls the exported `foresignal_parse(ptr, len) -> ptr` with the raw upstream bytes; the plugin returns UTF-8 JSON `{"records":[IngestRecord…],"complete":bool}`. Exports `alloc`/`dealloc`. **No WASI imports granted** (no clock, fs or net); a single host import `foresignal_log(level, ptr, len)` rate-limited.
- **Limits:** memory ≤ 64 MB (max pages), a wall-clock timeout of 5 s via an interrupting thread, output ≤ 16 MB, validated by the normal ingest normalizer (008).
- **Use:** a `wasm-feed` source family. Each operator-defined source names a plugin plus a URL (fetched by the budgeted `SourceHttp`, URL through 035) and a full descriptor (tier, time base, licence terms, ingest policy) in its config.
- Ship one example plugin source (Rust, `examples/plugins/geojson-passthrough/`) built outside the Gradle build, plus its compiled test artefact under `src/test/resources/plugins/` for tests.

### 5. Science and security formats (each optional and small)

- **CZML:** `GET /v1/history?…&format=czml` and `GET /v1/records/{s}/{id}/track?format=czml` (037). Document packet first (`clock` from the range), one packet per entity with `position` sampled `cartographicDegrees` `[t, lon, lat, h]` epochs, `availability`, `label` = title, `point` styling from `kindDisplay` colour. Cap 5,000 entities. Attribution in the document packet `description`.
- **FDSN event / QuakeML:** `GET /fdsnws/event/1/query` (read; open under `openReads`) over `earthquake` kind records (011/089) with `starttime`, `endtime`, `minlatitude…`/`latitude`+`maxradius`, `minmagnitude`, `maxmagnitude`, `orderby` (`time`, `magnitude`), `limit`, `format=xml|text`. `xml` = QuakeML 1.2 BED (`event`, `origin`, `magnitude`, publicIDs `smi:foresignal/{source}/{externalId}`); `text` = the FDSN pipe-separated format. Plus `/fdsnws/event/1/version` and `application.wadl` (minimal). `204` when empty (FDSN convention).
- **STIX 2.1 / TAXII 2.1** (read-only; agent scope):
  - `GET /v1/export/stix?kind=&since=` returns a STIX bundle: `cisa-kev`/`nvd-cves` → `vulnerability` (with `external_references` CVE), abuse.ch IOCs → `indicator` (pattern from the IOC type, e.g. `[url:value = '…']`, `valid_from`) plus an `identity` for Foresignal; deterministic UUIDv5 ids from the record ref.
  - TAXII 2.1: `GET /taxii2/` (discovery), `/taxii2/foresignal/` (API root), `/collections/`, `/collections/{id}/objects/` with `added_after` and pagination; media type `application/taxii+json;version=2.1`.

### 6. Audio briefings and notification translation

- **TTS role** (026): a provider role `tts` for OpenAI-compatible `/v1/audio/speech` endpoints (cloud, or local servers such as Kokoro-FastAPI or openedai-speech), budgeted by characters. Briefing option `audio: {voice, format: mp3|opus}` synthesises the briefing's plain-text rendition (≤ 4,000 characters; longer text is chunked and concatenated for mp3, else truncated with a note) after the run.
  - Store the audio in the 043 media store (role `audio`, linked to the run) and sign it with 109.
  - Attach where channels support audio (Telegram `sendVoice`/`sendAudio`, Matrix `m.audio`, email attachment, ntfy attachment); others get a link.
  - An RSS 2.0 podcast feed of runs with audio: `GET /v1/briefings/{id}/podcast.rss` (058 feed writer, `<enclosure>`; feed keys).
  - A failure never blocks text delivery.
- **Translation:** channel setting `language` (BCP 47). When it differs from the source language (`notify.defaultLanguage`, default `en`), translate title and body via the 026 chat role (low effort, prompt treats content as untrusted text to translate, never instructions; output plain text ≤ 1.5× input length).
  - Cache by `(sha256(text), lang)` for 24 h. Timeout 8 s, then send the original with a `[untranslated]` marker.
  - Budget role `translate`. Never translate structured fields, URLs or record values in templates' code spans.

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`: `.well-known/oauth-protected-resource` operations (open), `GET/PUT /v1/plugins`, `GET/PUT /v1/plugins/{name}` (admin), `format=czml` on history and track, FDSN and TAXII operations, `exportStix`, `getBriefingPodcast`, the `audio` option on briefing schemas, `language` on channel schemas, `tts` in provider role enums, settings `mcp.auth` and `plugins`. Every operation gets `x-foresignal-scope`. Update `AccessPolicy` (`/fdsnws/**` and `/taxii2/**` mapped explicitly; nothing falls through to the fail-closed default by accident). Regenerate Postman with a BODIES entry for `updatePlugin`.
- MCP (025): `get_track` gains `format: czml` (returns a media link, not inline); no new tools otherwise.
- Docs: `sdk/README.md`, `cli/README.md`, `docs/Plugin-Design.md`, `docs/Operator-Guide.md` sections "Remote MCP with your identity provider", "Plugins", "Audio briefings and translation", `docs/Agent-Guide.md` "Remote MCP authorization". `THIRD_PARTY_NOTICES.md` for Nimbus, Chicory and generator outputs if licence headers require it.

## Tests to write (do not run)

- `PreprocessDartSpecTest` (pytest under `scripts/sdk/tests`): each transformation; the output parses as OAS 3.0.3; `x-foresignal-*` kept.
- SDK realtime tests in each SDK: resume after disconnect with the last id, `reset` → snapshot plus reopen at the watermark, heartbeat timeout reconnect, `observedAt` guard, ticket use; paging iterators across 3 pages; `410` typed error.
- CLI tests (CLI build): login file permissions, table/json output, exit codes, `changes tail` reset handling.
- `ProtectedResourceMetadataIT`: both well-known paths; fields.
- `McpOAuthIT` (in-test JWKS and signer): valid token maps scopes; wrong `aud`, `iss`, alg, expired → 401 with `resource_metadata`; insufficient scope → 403 with `insufficient_scope`; `admin` never mappable; unknown `kid` triggers one JWKS refresh; REST rejects OAuth tokens.
- `WasmParserHostTest`: example plugin parses a fixture; memory cap, timeout and output cap enforced; no WASI imports available; sha256 mismatch refuses to load; disabled by default.
- `CzmlWriterTest` (golden), `FdsnEventIT` (QuakeML validates against the BED XSD; text format; 204), `StixExportTest` (deterministic ids, patterns, bundle schema), `TaxiiIT` (discovery, collections, `added_after` paging, media type).
- `AudioBriefingIT` (WireMock TTS): media stored and linked, channel attachment for Telegram, podcast RSS enclosure; failure keeps text delivery.
- `NotificationTranslationIT` (`FakeLlmClient`): translated title and body, cache hit, timeout fallback marker, injection text treated as content.

## Acceptance criteria

- `./gradlew generateSdks` produces Dart, TypeScript and Python SDKs, each with the shared realtime and paging semantics and native tests.
- The CLI builds as a jar (native optional) and tails changes with correct resume and reset handling.
- With `mcp.auth.mode=oauth`, an MCP client discovers the IdP via RFC 9728 and is held to its mapped scopes; no authorization server exists in Foresignal.
- Plugins, formats, audio and translation are off or inert unless configured, and the broker runs unchanged without them.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `128: Client SDKs CLI And Extensibility`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/128-CR-Client-SDKs-CLI-And-Extensibility.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `128: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
