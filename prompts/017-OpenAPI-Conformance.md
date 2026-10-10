# 017 — OpenAPI Conformance

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/017-CR-OpenAPI-Conformance.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/016-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §10 ("The spec is authored, not generated"), §12 (OpenAPI contract test)
- `docs/OpenAPI.json` (the full document)

## Objective

Make the authored spec the served contract, and add an automated drift check so the implementation can never silently diverge from `docs/OpenAPI.json`.

## Tasks

### 1. Serve the authored spec

- `processResources` (prompt 001) copies `docs/OpenAPI.json` to `static/v1/openapi.json`. Verify this works.
- Make sure `GET /v1/openapi.json` returns it with `Content-Type: application/json` and that it is open (no auth). If static resource handling conflicts with the `/v1/**` controllers or the filter, serve it from a tiny controller that streams the classpath resource instead.
- Set the server URL dynamically? **No.** Keep the authored document byte-identical; clients can override the server URL themselves.

### 2. Swagger UI

`/docs` serves Swagger UI pointed at `/v1/openapi.json` (springdoc config from prompt 001). Verify the springdoc version is compatible with Spring Boot 4.1. If no compatible springdoc exists, fall back to serving a static Swagger UI page from a pinned CDN version and record the deviation.

### 3. Generated spec for drift detection

- springdoc generates from the code at `/v1/openapi-generated.json`. Configure `springdoc.paths-to-match=/v1/**` and exclude the static spec path.
- Annotate controllers just enough that the generated output carries the spec's identity: `@Operation(operationId = "...", tags = "...")` on every endpoint, `@Tag` on controllers. Do **not** duplicate full schema documentation in annotations; the authored file is the documentation.
- Hide `/v1/openapi-generated.json` from the Swagger UI.

### 4. Contract test (`OpenApiContractIT`)

- Load `docs/OpenAPI.json` from the repo path (`Path.of("docs/OpenAPI.json")`) and fetch `/v1/openapi-generated.json` from the running app (`@IntegrationTest`).
- Assert:
  - the set of `(path template, HTTP method)` pairs is **equal** in both directions; normalize path variable names
  - for each pair, the `operationId` matches
  - for each pair, the documented success status codes (2xx keys) in the authored spec are a subset of those the generated spec declares, or skip this check with a clear comment if springdoc's inference is unreliable for some status, and list the exceptions explicitly
  - the authored `components.schemas` names that are used as top-level request bodies exist as DTO classes (via a maintained mapping table in the test); this is optional if it is brittle, so record the decision
  - `GET /v1/openapi.json` returns content byte-identical to `docs/OpenAPI.json`
- On failure, print a readable diff (missing in code / missing in spec).

### 5. Postman collection stays in sync

- `postman/Elaro.postman_collection.json` is **generated** from the spec by `scripts/generate-postman.py` (Python 3 standard library only). Never hand-edit it.
- If any earlier prompt changed `docs/OpenAPI.json`, regenerate the collection: `python3 scripts/generate-postman.py docs/OpenAPI.json postman`. If request shapes changed, update the example bodies in the script's `BODIES` map so they stay valid for the implemented validation rules.
- Add `PostmanCollectionContractTest` (a unit test, no Spring context). It parses the collection JSON and asserts:
  - every `operationId` in `docs/OpenAPI.json` appears in some request description as `` operationId: `<id>` ``
  - auth follows each operation's `x-elaro-scope`: `admin` uses bearer `{{adminToken}}`, `agent` uses bearer `{{agentToken}}`, `ingestRecords` sends `X-Elaro-Ingest-Key: {{ingestKey}}`, and `read`/`open` use `noauth`
  - every JSON example body deserializes into the corresponding request DTO with Jackson, and passes Bean Validation
- Add a Gradle task `generatePostman` (Exec, runs the script) for convenience, **not** wired to `compileJava`.

### 6. Spec lint in the build (optional, cheap)

Add a Gradle task `lintOpenApi` that runs only if `npx` is available (`npx @redocly/cli@1.34.5 lint docs/OpenAPI.json`), **not** wired to `compileJava`. Document it in the README "Development" section.

### 7. Endpoints from later prompts

`docs/OpenAPI.json` already contains endpoints that prompts 019–028 implement (tokens, ingest, MCP-adjacent, LLM, search, ask, briefings, push). The contract test compares the **full** set, so it is expected to report those as missing until they are built. It is first run in prompt 061, after everything exists. Don't add exclusions for them. Do list in the CR which operations are still unimplemented at this point.

## Tests to write (do not run)

- `OpenApiContractIT` as above.
- `OpenApiServingIT`: content type, open access, byte equality, `/docs` reachable (200 or redirect to the Swagger UI index).
- `PostmanCollectionContractTest` as above.

## Acceptance criteria

- Every controller endpoint has an `operationId` matching the spec.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 061 (Full Test Run And Fix).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `017: OpenAPI Conformance`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/017-CR-OpenAPI-Conformance.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `017: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
