# Foresignal CLI

`foresignal` is the command-line client of the Foresignal broker. It is a standalone Gradle build (Java 21, picocli 4.7,
`java.net.http`, Jackson 3). It is not part of the broker build, and the broker never depends on it.

## Build and install

Build with the repository's Gradle wrapper, run from the repository root. Gradle finds a JDK 21 toolchain on its own.

```sh
./gradlew -p cli installDist     # cli/build/install/foresignal/bin/foresignal (launch scripts and jars)
./gradlew -p cli uberJar         # cli/build/libs/foresignal-cli-<version>-all.jar
./gradlew -p cli test            # the CLI's own tests (JUnit 5 and WireMock)
```

There are two ways to run the result:

- `cli/build/install/foresignal/bin/foresignal --help`
- `java -jar cli/build/libs/foresignal-cli-*-all.jar --help`

To have `foresignal` on your `PATH`, put `cli/build/install/foresignal/bin` on it, or copy the uber jar somewhere and
add a one-line wrapper script.

### Native image (optional)

The build applies GraalVM's `org.graalvm.buildtools.native` plugin. `picocli-codegen` generates the reflection,
resource and proxy configuration for the commands at compile time, under
`META-INF/native-image/picocli-generated/`. The bundled EDP schemas are listed in
`src/main/resources/META-INF/native-image/ai.foresignal/foresignal-cli/resource-config.json`.

You need a GraalVM JDK 21 with `native-image`, either as `JAVA_HOME` or as a Gradle toolchain:

```sh
./gradlew -p cli nativeCompile   # cli/build/native/nativeCompile/foresignal
```

The binary starts in milliseconds and needs no JVM. The CLI reads JSON as Jackson trees only, never by binding it to
classes, so Jackson needs no extra reflection configuration.

## Configuration

```sh
foresignal login --url https://foresignal.example --token fsr_…   # or: --token  (prompts without echo)
foresignal login --profile lab --url http://10.0.0.5:8080 --token fsr_…
```

`login` writes `~/.config/foresignal/config.json` and makes the profile it saved the current one. It sends nothing to
the server.

```json
{"currentProfile": "lab",
 "profiles": {"default": {"url": "https://foresignal.example", "token": "fsr_…"},
              "lab":     {"url": "http://10.0.0.5:8080",       "token": "fsr_…"}}}
```

- **Permissions.** On Linux and macOS the directory is created `0700`. The file is written `0600`: a temporary
  file is created with those permissions and then moved into place, so the token is never readable by other users,
  even briefly. Each run warns when the file has become readable by others. On Windows the permissions cannot be
  restricted, and `login` prints a warning saying so.
- **The token is never printed.** No output, error message or `--output json` document contains it. The only secret
  the CLI ever prints is a new token's one-time secret, from `tokens create` and `edp tokens create`.
- **Precedence.** `--url` beats `FORESIGNAL_URL`, which beats the profile. The token comes from
  `FORESIGNAL_TOKEN`, else from the profile. Without a token, calls are anonymous, which works for open reads.
- **Profiles.** `--profile NAME` selects a profile for one command. Without it, the CLI uses `currentProfile`, else
  `default`.
- **Configuration directory.** The system property `foresignal.config.dir`, then the variable
  `FORESIGNAL_CONFIG_DIR`, override the directory. The tests use this, and so can packagers.

Options every command accepts:

| Option | Meaning |
|---|---|
| `-o, --output table\|json\|ndjson` | `table` (default) for people. `json` prints the API document pretty-printed. `ndjson` prints one compact line per list item or event. |
| `--profile NAME` | The configuration profile |
| `--url URL` | The server for this call |
| `--timeout SECONDS` | Timeout per request (default 60). The doctor waits at least 150 s. |

## Commands

| Command | API operation(s) |
|---|---|
| `system` | `GET /v1/system` |
| `sources list` · `show ID` | `GET /v1/sources`, `GET /v1/sources/{id}` |
| `sources enable ID` · `disable ID` | `PUT /v1/sources/{id}` with `{"enabled": true\|false}` (admin) |
| `sources health [ID] [--status s,…]` | `GET /v1/sources/health`, `GET /v1/sources/{id}/health` |
| `sources fetch ID [--allow-shrink]` | `POST /v1/sources/{id}/fetch` (admin) |
| `sources probe ID [--max-bytes N] [--seconds N]` | `POST /v1/sources/{id}/probe` (admin) |
| `sources doctor ID [--no-probe] [--llm auto\|on\|off] [--detail compact\|full]` | `POST /v1/sources/{id}/doctor` with `DoctorOptions`. A `202` is followed by polling its `Location` every `--poll-interval` seconds (default 5) until it answers `200`. The command prints the findings, the probe, the LLM explanation, and any patch under its label **"proposed, not applied"**. The patch is never applied. (admin) |
| `records query [--source] [--kind] [--bbox] [--since] [--until] [-q] [--near --radius-km] [--order] [--limit] [--cursor] [--all]` | `GET /v1/records` |
| `records get S ID` | `GET /v1/records/{s}/{id}` |
| `records history S ID [--since] [--until] [--all]` | `GET /v1/records/{s}/{id}/versions` (the persisted versions) |
| `records track S ID [--from] [--to] [--max-points] [--no-live]` | `GET /v1/records/{s}/{id}/track` |
| `changes tail [--source] [--kind] [--bbox] [--live] [--touches] [--after SEQ] [--snapshot]` | `GET /v1/changes/stream` (see below) |
| `alerts list [--state] [--rule] [--source] [--kind] [--min-severity] [--all]` | `GET /v1/alerts` |
| `alerts ack ID [--note] [--feedback]` · `alerts resolve ID` | `POST /v1/alerts/{id}/ack`, `…/resolve` (agent) |
| `alerts tail [--rule] [--min-severity] [--source] [--kind]` | `GET /v1/alerts/stream`, resumed with `Last-Event-ID` |
| `triggers list [--tag] [--enabled true\|false]` | `GET /v1/triggers` |
| `triggers create -f rule.json` | `POST /v1/triggers` with a `TriggerRuleInput` (`-f -` reads standard input) (agent) |
| `triggers test ID [--since] [--until] [--from-seq] [--to-seq] [--simulate]` | `POST /v1/triggers/{id}/test` (agent) |
| `triggers quality ID [--since]` | `GET /v1/triggers/{id}/quality` |
| `triggers tune ID [--simulate] [--explain] [--status]` | `GET /v1/triggers/{id}/suggestions` |
| `triggers tune ID --apply SID [--note]` · `--dismiss SID [--reason]` | `POST …/suggestions/{sid}/apply`, `…/dismiss` (agent) |
| `ask "…" [--thread] [--source] [--kind] [--bbox] [--since] [--allow-actions] [--no-stream]` | `POST /v1/ask/stream`, or `POST /v1/ask` with `--no-stream` |
| `briefings list` · `briefings run ID` | `GET /v1/briefings`, `POST /v1/briefings/{id}/run` (agent) |
| `exports create (-f spec.json \| --format …) [--source] [--kind] [--bbox] [--from] [--to] [--mode] [--wait]` | `POST /v1/exports`, then polling `GET /v1/exports/{id}` with `--wait` (agent) |
| `exports status ID [--wait]` | `GET /v1/exports/{id}` |
| `exports download ID [--file NAME] [--zip] [--dest PATH]` | `GET /v1/exports/{id}/download`. The data goes to a temporary file, renamed when complete. A directory `--dest` takes the server's file name. |
| `tokens list` · `tokens create --name --scope [--expires-at]` · `tokens revoke ID` | `GET`/`POST /v1/tokens`, `DELETE /v1/tokens/{id}` (admin) |
| `edp validate MANIFEST [ITEMS] [--remote [--source-id ID]]` | Validates locally against the bundled EDP 1.0 schemas, or remotely with `POST /v1/external/validate` |
| `edp sources list [--state] [--all]` · `show ID` · `approve ID` · `suspend ID` | `/v1/external/sources…` (admin) |
| `edp tokens create --name [--expires-at] [--max-uses] [--source-id-prefix]` | `POST /v1/external/registration-tokens`. The `fsx_…` token is shown once. (admin) |

**Paging.** Paged commands print one page and name the next cursor on standard error. `--all` follows `nextCursor`
to the end and prints every item as one list.

### `changes tail`

`changes tail` follows the stream under the client contract of Core-Data-Model §10.2:

- **Resume.** The CLI tracks the id (seq) of the last `change` event. After any disconnect it reconnects with
  `Last-Event-ID` and `after`. `live`, `touch`, `resync`, `reset` and `heartbeat` carry no id, so they never move
  the resume position.
- **`reset`.**
  - With a `resumeAfter` (`buffer_overflow`, `replay_cap`), the CLI reconnects at once from that seq.
  - Without one (`resync_required`: the resume point fell below the retention floor), it pages the snapshot
    `GET /v1/records?order=id` with the same filters, printed as `snapshot` lines. It keeps the snapshot's
    `watermarkSeq` and reopens the stream at that watermark.
- **`resync`** (live updates were lost; only with `--live`). The CLI re-fetches the current state the same way and
  keeps the stream open.
- **`observedAt` guard.** A `live` or `touch` event older than the newest `observedAt` already seen for that entity
  is not shown.
- **Watchdog.** If neither an event nor a heartbeat arrives within 2.5× `--heartbeat-seconds` (default 15, the
  server's default), the CLI reconnects.
- **Backoff.** Reconnects after a drop, a network error, a `429` or a `5xx` wait a jittered exponential backoff,
  1 s → 60 s (±20 %). The server's `retry:` field and `Retry-After` header set a minimum wait.
- **Ending.** `--max-events N` exits after N events. `--max-retries N` exits with code 3 after N consecutive failed
  reconnects (the default is to retry forever).

`--snapshot` prints the current records first, then follows from their watermark. `--after SEQ` starts after a known
seq. In `json` and `ndjson` every event is one line `{"event": "change", "id": "123", "data": {…}}`. Notices
(`reset`, `resync`, reconnects) go to standard error.

### `edp validate`

```sh
foresignal edp validate manifest.json items.ndjson
foresignal edp validate docs/protocol/edp-1.0/examples/observation.provider.json   # a provider-kit example
foresignal edp validate manifest.json items.ndjson --remote --source-id ext-river-gauges-example
```

- **Local mode.** It checks the shape against the JSON Schemas exported to `docs/protocol/edp-1.0/`, copied into
  `src/main/resources/edp/schemas/`, using networknt json-schema-validator (draft 2020-12, no remote fetches). It
  needs no server.
- **Remote mode.** `--remote` asks the server, which also checks the registration rules, the payload schemas and
  breaking changes against a registered source.
- **Item files.** Items are NDJSON, one per line. A file whose name ends in `.json` may instead hold an array, or a
  batch with `items`.

When the spec's `Edp*` schemas change, copy the regenerated files again:

```sh
cp docs/protocol/edp-1.0/*.schema.json cli/src/main/resources/edp/schemas/
```

## Exit codes

| Code | Meaning |
|---|---|
| 0 | Success |
| 1 | Usage error: an unknown option, a missing argument or subcommand, no configured URL, an unreadable input file. Also invalid input: `edp validate` found problems. |
| 2 | API error. Standard error shows the ProblemDetail `code` (e.g. `error: NOT_FOUND (HTTP 404): Not Found`), its `detail` and any field errors. |
| 3 | Network error: connection refused, DNS failure, timeout, a stream that ended early, or `changes tail` giving up after `--max-retries`. |
