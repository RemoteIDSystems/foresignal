# Test plugins (prompt 141)

Committed WebAssembly modules for `WasmParserHostTest`, `PluginControllerIT`, `WasmFeedAdapterTest` and the other
plugin tests. Each directory holds `plugin.wasm`, `plugin.json` (with the binary's real SHA-256) and, except for the
example, the `plugin.wat` source it was assembled from. The example's source is
`examples/plugins/geojson-passthrough/wat/plugin.wat`.

- `geojson-passthrough/fixture.geojson` is a synthetic FeatureCollection: four features, one without an id, quotes and
  brackets inside strings, and members before and after `features`.
- `geojson-passthrough/expected-output.json` is the module's output for the fixture.

These modules are not built from Rust: the machine the repository was built on has no Rust toolchain. They were
assembled with Chicory's `wat2wasm` port, as described in `scripts/plugins/README.md`.
