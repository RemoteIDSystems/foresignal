# Test plugins (prompt 141)

The WASM modules under `src/test/resources/plugins/` are committed binaries. Each is assembled from a WebAssembly
text file kept in the repository:

| Plugin | WAT source | What it does |
|---|---|---|
| `geojson-passthrough` | `examples/plugins/geojson-passthrough/wat/plugin.wat` | The example parser, equivalent to the Rust source beside it |
| `memory-hog` | `src/test/resources/plugins/memory-hog/plugin.wat` | Grows memory to 2048 pages (128 MB) before answering |
| `infinite-loop` | `src/test/resources/plugins/infinite-loop/plugin.wat` | Never returns |
| `oversized-output` | `src/test/resources/plugins/oversized-output/plugin.wat` | Claims 16 MB + 1 byte of output |
| `wasi-import` | `src/test/resources/plugins/wasi-import/plugin.wat` | Imports `wasi_snapshot_preview1.fd_write` |
| `log-spammer` | `src/test/resources/plugins/log-spammer/plugin.wat` | Calls `env.foresignal_log` 1000 times |

The example plugin is written in Rust (`examples/plugins/geojson-passthrough/`). The machine this repository was
built on has no Rust toolchain, so its test artifact is assembled from the hand-written WAT equivalent instead.

## Reassembling

`AssembleWat.java` is a single-file Java program. It runs Chicory's `wabt` module (wabt's `wat2wasm` compiled to JVM
bytecode) and writes `plugin.wasm` and a `plugin.json` that carries the binary's real SHA-256. It runs outside Gradle,
with these jars from Maven Central on the class path, all version 1.7.5 and Apache-2.0:

- `com.dylibso.chicory:wabt`, `runtime`, `wasm`, `wasi` and `log`;
- `io.roastedroot:zerofs` 0.1.0, which the `wasi` module needs.

```sh
CP=wabt-1.7.5.jar:runtime-1.7.5.jar:wasm-1.7.5.jar:wasi-1.7.5.jar:log-1.7.5.jar:zerofs-0.1.0.jar
java -cp "$CP" scripts/plugins/AssembleWat.java \
  examples/plugins/geojson-passthrough/wat/plugin.wat \
  src/test/resources/plugins/geojson-passthrough geojson-passthrough 0.1.0
java -cp "$CP" scripts/plugins/AssembleWat.java \
  src/test/resources/plugins/memory-hog/plugin.wat src/test/resources/plugins/memory-hog memory-hog 1.0.0
```

The other test plugins are rebuilt the same way, each at version 1.0.0. After you reassemble `geojson-passthrough`,
update `PluginTestFiles.PASSTHROUGH_SHA256` and `expected-output.json` (the module's output for `fixture.geojson`).
