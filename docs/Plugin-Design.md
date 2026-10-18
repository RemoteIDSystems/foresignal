# Foresignal Plugin Design (WASM parser plugins, ABI v1)

**Status:** built by prompt **141** (2026-10-18). The code is `ai.foresignal.plugin` (the sandbox, the gate and the
four operations) and `ai.foresignal.plugin.feed` (the `wasm-feed` source family). The API contract is
`docs/OpenAPI.json`, tag `Plugins` (`listPlugins`, `updatePluginSettings`, `getPlugin`, `updatePlugin`; schemas
`Plugin`, `PluginList`, `PluginSettings`, `PluginUpdate`). The example plugin is `examples/plugins/geojson-passthrough/`.

A plugin lets an operator add a parser for an upstream format Foresignal does not know, without changing Foresignal
and without trusting the parser. The plugin is a WebAssembly module that turns raw upstream bytes into records. It runs
in a pure-Java interpreter with no access to anything but the bytes it is given.

## 1. Goals and non-goals

**Goals:**

- **Out-of-tree parsers.** An operator can parse an upstream format that has no adapter without forking Foresignal.
  The parser can be written in any language that compiles to WebAssembly.
- **Safe by construction.** A plugin cannot read files, open sockets, see the clock, start threads or call Foresignal
  code. It cannot use more than 64 MB of memory, run for more than 5 seconds per call or answer with more than 16 MB.
- **Reviewed and pinned.** A plugin runs only after the admin has enabled it against the SHA-256 they reviewed, and
  only while the file still has that hash.
- **Off by default and inert.** With the `plugins` setting off (the default), or with no plugin installed, nothing in
  the broker changes.
- **Same data path.** Plugin output is records in the shape the `native` ingest format accepts. They go through the
  same normalizer (prompt 008), writer, history, SSE, triggers, retention, licensing and egress as every other
  source.

**Non-goals:**

- **No JVM plugins.** No jars, no class loading, no scripting engines. Code running in the JVM can do anything the
  broker can, and no review step makes that safe.
- **No network or filesystem for plugins.** Fetching stays with Foresignal's budgeted, policed HTTP client
  (`SourceHttp`, the outbound URL policy of prompt 035). A plugin only transforms bytes it is handed.
- **No other plugin kinds in v1.** ABI v1 knows one kind, `parser`. Sinks, enrichers, triggers and renderers are not
  plugins.
- **No marketplace, download or update mechanism.** The operator copies files into the data directory. Foresignal
  never fetches a plugin.
- **No plugin-defined API, settings or UI.** A plugin has no endpoints, no configuration of its own and no state that
  survives a call.
- **No WASI.** Not even the clock or random numbers. A parser that needs time reads it from its input.

Fringe and specialist sources that need their own network logic remain External Data Protocol providers
(`docs/External-Data-Protocol.md`): separate programs the operator connects. Plugins cover the narrower case where
Foresignal can fetch the bytes itself and only the format is new.

## 2. Packaging and discovery

A plugin is a directory `${FORESIGNAL_DATA_DIR}/plugins/{name}/` (the data directory is `foresignal.data-dir`, from
`FORESIGNAL_DATA_DIR`, default `./data`) holding two files:

| File | Content |
|---|---|
| `plugin.wasm` | The WebAssembly module, at most 32 MB |
| `plugin.json` | `{"name": …, "version": …, "abi": 1, "kind": "parser", "sha256": …}`, at most 64 KB |

- `name` must match `^[a-z0-9][a-z0-9-]{0,63}$` and equal the directory name.
- `version` is free text up to 64 characters.
- `abi` must be `1` and `kind` must be `"parser"`.
- `sha256` is the lower-case hex SHA-256 of `plugin.wasm`.
- Unknown members are refused.

Discovery (`GET /v1/plugins`) lists every directory whose name matches the pattern. It reads and hashes the files but
never runs a plugin. It does parse the module, to report whether it would load. Links are refused: a linked directory
or file is reported as an error and never followed. A missing `plugins/` directory holds no plugins.

## 3. ABI v1

The module is a WebAssembly module with exactly one memory, not shared, whose initial size fits the cap.

**Exports:**

| Export | Type | Meaning |
|---|---|---|
| `memory` | memory | The module's linear memory |
| `alloc` | `(i32 size) -> i32 ptr` | Reserves `size` bytes, returns their address, or 0 when it cannot |
| `dealloc` | `(i32 ptr, i32 size) -> ()` | Frees a buffer from `alloc`, with the size it was allocated with |
| `foresignal_parse` | `(i32 ptr, i32 len) -> i32 out` | Parses `len` input bytes at `ptr` and returns the output buffer, or 0 |

**Imports:** exactly one function may be imported, and none is required:

| Import | Type | Meaning |
|---|---|---|
| `env.foresignal_log` | `(i32 level, i32 ptr, i32 len) -> ()` | Logs `len` UTF-8 bytes at `ptr`: level 0 debug, 1 info, 2 warn, 3 or more error |

Any other import refuses the module at load: every WASI function, any other host function, and any imported memory,
table, global or tag. This is what "no WASI imports granted" means in practice. A module built for `wasm32-wasi` or
`wasm32-wasip1` imports WASI and is refused; plugins are built for `wasm32-unknown-unknown` or an equivalent.

**One call:**

1. The host instantiates the module afresh. Nothing survives between calls, and a start function runs inside the
   call's time budget.
2. The host calls `alloc(len)` and copies the raw upstream bytes (the HTTP response body) to the returned address.
3. The host calls `foresignal_parse(ptr, len)`.
4. A return of 0 means "no output": the call fails, and the plugin should have logged why.
5. Otherwise the return value addresses a **length-prefixed buffer**: a little-endian `u32` byte count `n`, followed
   by `n` bytes of output. The host checks `n` against the 16 MB cap before reading anything, and checks that the
   whole buffer lies inside memory.
6. The host copies the output, then calls `dealloc(out, n + 4)` and `dealloc(ptr, len)`.

**Decision recorded (prompt 141): the returned length is known through a 4-byte little-endian length prefix at the
returned address.** The alternatives were a second export returning the length, a multi-value `(ptr, len)` return, or
a NUL-terminated string. The prefix needs no second call and no multi-value support in the plugin's toolchain, and
unlike a terminator it allows any byte in the output.

**The output** is UTF-8 JSON:

```json
{"records": [IngestRecord, ...], "complete": true}
```

- `records` (required) holds at most 50,000 items in the `NormalizedRecordPreview` shape of the `native` ingest
  format: `externalId` (string or number, required), `kind` (optional when the source has one kind), `observedAt`,
  `eventTime`, `validFrom`, `validUntil` (ISO-8601), `geometry` (GeoJSON), `payload` (object), `tombstone`, and
  `quality` (`accuracyMeters`, `confidence`).
- `complete` (optional, default false) says whether the records are the upstream's whole current set. A complete
  result lets Foresignal reconcile records that disappeared. It is honored only when no item was rejected, because a
  rejected item would otherwise be read as a deletion.
- Other members are ignored.

Output that is not UTF-8, not JSON, or not this object fails the call. An item that does not map (no `externalId`, an
unknown `kind`, a bad geometry or time) is a per-item rejection: it is counted in `foresignal.plugin.rejected{source,
plugin, reason}` and the rest are kept. The records then take the normal ingest path, so the normalizer's checks
(prompt 008) apply exactly as for built-in sources: the time base (`MISSING_OBSERVED_AT` under `UPSTREAM`), clock
skew, coordinates, record limits, the private-precision cap and so on.

## 4. Security model

**Threat model.** A plugin is untrusted code, even after review: the admin reviews a hash, not the source, and a
plugin may be buggy or hostile. The upstream bytes it parses are untrusted too.

**What protects the broker:**

| Layer | Mechanism |
|---|---|
| No ambient authority | Chicory (`com.dylibso.chicory:runtime`), a pure-Java WebAssembly interpreter. A module can touch only its own linear memory and the one import. No JNI, no native code, no class loading. |
| No WASI | The import check at load refuses everything but `env.foresignal_log`. There is no clock, filesystem, network, environment, randomness or process exit. |
| Memory | The memory maximum is clamped to 1024 pages (64 MB) whatever the module declares. Growth beyond fails (`memory.grow` returns -1), and a module asking for more initial pages is refused at load. |
| Time | Each call runs on its own worker thread. After 5 s of wall-clock time the caller interrupts it; Chicory's interpreter checks for interruption on function calls and loop branches and stops the module. The worker is a daemon thread with a 16 MB stack. |
| Output | The length prefix is checked against 16 MB before anything is read, and the buffer against the memory bounds. At most 50,000 records. |
| Input | At most 16 MB (a `wasm-feed` source's `maxResponseBytes` cannot exceed it). |
| Concurrency | At most 2 plugin calls run at once, each with up to 64 MB of plugin memory; a caller waits up to 30 s for a slot. |
| Logging | `foresignal_log` keeps at most 20 messages per call and 60 per plugin per minute, each cut to 1 KB, with every control character replaced by a space so a plugin cannot forge log lines. The rest are counted and reported once. The import never fails the call. |
| Data path | Output records go through the normal ingest normalizer and writer; a plugin cannot write history, settings or anything else directly. |

**Review and pinning:**

- The `plugins` setting must be on. It is off by default.
- The admin enables each plugin with `PUT /v1/plugins/{name}` and the SHA-256 they reviewed. The request is refused
  unless the file hashes to that value and to the one `plugin.json` declares, and unless the module loads.
- The approval stores the hash. Before every call the host reads `plugin.wasm`, hashes the bytes it read, and runs
  those same bytes only if they match both the declared and the approved hash. A file replaced after review never
  runs: the plugin shows state `hash-mismatch` until it is reviewed and enabled again.
- Disabling always works, including while plugins are off.

**Fetching** stays outside the sandbox. A `wasm-feed` source's URL must be https and pass the outbound URL policy
(prompt 035, `PUBLIC_ONLY`) when the spec is loaded and again before every fetch. The request goes through the
source's budgeted `SourceHttp`, with Foresignal's User-Agent and usage accounting. Redirects are never followed,
because a redirect could lead away from the checked URL. The policy check resolves the host, then the client resolves
it again for the request, so a DNS answer that changes between the two is not caught (the residual risk the kit
overlay of prompt 073 also carries). A source whose plugin is unavailable fails before any request is spent.

**What is not protected against:**

- A plugin that returns plausible but false records. Review the plugin, and use the source's reliability grade and
  egress settings (prompt 109).
- Interpreter cost within the limits: a plugin may use its 5 s and 64 MB on every poll.
- A host operator who can write to the data directory. They can already do anything the broker can.

## 5. The `wasm-feed` source family

An operator-defined source is a spec in `${FORESIGNAL_DATA_DIR}/wasm-feeds/*.json`. Each file is a JSON array of specs,
read once at startup in file-name order. Links and unknown members are refused, and any problem stops startup with
every problem listed (the rule of the kit overlay, prompt 073). Specs are never accepted over the API. Each source
ships disabled.

```json
[{
  "id": "wasm-stations",
  "name": "Example stations",
  "plugin": "geojson-passthrough",
  "url": "https://data.example.org/stations.geojson",
  "kinds": ["station"],
  "attribution": "Example Data",
  "license": "CC-BY-4.0",
  "licenseTerms": {"id": "CC-BY-4.0", "name": "Creative Commons Attribution 4.0", "commercialUse": "ALLOWED",
    "shareAlike": false, "attributionRequired": true, "redistribution": "ALLOWED", "attributionText": "Example Data",
    "termsUrl": "https://creativecommons.org/licenses/by/4.0/"},
  "historyTier": "STANDARD",
  "observedAtMode": "RECEIVED",
  "recordTtl": "2h",
  "ingestPolicy": {"mode": "LATEST", "keyframe": "1h"},
  "interval": "10m",
  "budget": {"perMinute": 2, "perDay": 300},
  "titleField": "name",
  "tileProperties": ["name"]
}]
```

The spec carries the full descriptor, because the plugin, not Foresignal, knows what the records are:

| Member | Default | Notes |
|---|---|---|
| `id` | required | `wasm-…`, at most 64 characters, unique |
| `name`, `description` | `name` required | |
| `plugin` | required | A plugin directory name |
| `url` | required | https, no user info, allowed by the outbound URL policy |
| `kinds` | required | At least one; a record without `kind` gets the single kind |
| `homepage`, `attribution`, `license`, `termsUrl` | none | `license` is the descriptor's `licence` text |
| `licenseTerms` | none | The structured terms (the descriptor's `licenceTerms`); every member is required |
| `historyTier` | `STANDARD` | or `TRACK` (then `recordTtl` and `ttlAnchor: OBSERVED` are required) |
| `observedAtMode` | `RECEIVED` | or `UPSTREAM` (then every record needs `observedAt`) |
| `recordTtl`, `ttlAnchor` | none, `RECEIVED` | |
| `ingestPolicy` | `ALL` | The API's `IngestPolicy` shape |
| `interval`, `minInterval` | 15 min, 5 min | |
| `budget` | 4 a minute, 400 a day | `perMinute`, `perDay`, optional `mbPerDay` |
| `retention` | 1825 d | The `event` class; ignored for `TRACK` |
| `titleField`, `searchFields`, `embedFields`, `tileProperties`, `volatileFields`, `significantFields`, `fieldSpecs` | none | As in every descriptor |
| `egress` | `OPEN` | or `LOCAL_ONLY` |
| `zeroResultsNormal` | false | |
| `conditional` | true | Conditional GETs with `ETag`/`Last-Modified`; a 304 is "unchanged" |
| `maxResponseBytes` | 8 MB | At most 16 MB, the plugin input cap |

Fixed for every `wasm-feed` source: mode POLLING, redirects not followed, a 60 s fetch timeout. The source registry
applies its descriptor validator to every built descriptor at startup.

**Each run:**

1. The URL check.
2. The plugin gate.
3. The (conditional) GET.
4. The status and size checks (a 3xx, another non-200 status or a truncated body fails the run).
5. The plugin call.
6. The records.

A plugin refusal or failure fails the run with the plugin's message as the health reason, and forgets the stored
validators so the next run fetches the body again.

## 6. Versioning

- **ABI.** `plugin.json` `abi` names the contract a plugin was built for. This host runs ABI 1 only and refuses
  anything else at load. Within ABI 1, changes are additive only:
  - a new optional output member, which a v1 host ignores;
  - a new optional import, which a v1 plugin does not use;
  - a new record field, as the `native` ingest format gains them.
- **Breaking changes** get ABI 2. Examples: a different call sequence, a required new export or import, or a new
  output framing. A host that supports ABI 2 says so here and keeps running ABI 1 plugins for at least one release.
- **Kinds.** New kinds (for example a future `enricher`) are new values of `kind` with their own exports. They do not
  change `parser`.
- **The plugin's own `version`** is informational: Foresignal shows it but pins only the hash. A new version is a new
  file, a new hash and a new review.
- **The runtime.** Chicory is pinned in `gradle/libs.versions.toml`. Upgrading it does not change the ABI, but it is
  re-tested against the test plugins in `src/test/resources/plugins/`.

## 7. Operations summary

| Operation | Effect |
|---|---|
| `GET /v1/plugins` | The setting and every plugin with its declared and actual hash, state, error, `wasm-feed` source ids and last load time |
| `PUT /v1/plugins` `{"enabled": true}` | Switches plugins on (off again with `false`) |
| `GET /v1/plugins/{name}` | One plugin; `404 UNKNOWN_PLUGIN` |
| `PUT /v1/plugins/{name}` `{"enabled": true, "sha256": "…"}` | Enables after review: `409 PLUGINS_DISABLED`, `409 PLUGIN_HASH_MISMATCH`, `400` when the hash is missing or the module does not load |
| `PUT /v1/plugins/{name}` `{"enabled": false}` | Disables; always allowed |

All four are `admin`. The settings are stored under `plugins` (`{"enabled": false}`) and `plugins.approvals` (plugin
name to `{sha256, enabledAt}`).
