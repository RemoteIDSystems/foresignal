# geojson-passthrough

An example Foresignal parser plugin, ABI v1 (see `docs/Plugin-Design.md`). It turns a GeoJSON FeatureCollection into
Foresignal records, copying each feature's `id`, `geometry` and `properties` verbatim:

```json
{"records":[{"externalId":"st-1","geometry":{...},"payload":{...}}],"complete":true}
```

It is built outside the Gradle build. Foresignal does not compile, fetch or ship plugins.

## Build

You need Rust 1.71 or later and the `wasm32-unknown-unknown` target. `.cargo/config.toml` selects the target and
caps the module's memory at 64 MB, the host's limit.

```sh
rustup target add wasm32-unknown-unknown
cargo build --release
```

The module is `target/wasm32-unknown-unknown/release/geojson_passthrough.wasm`. It must not import anything but
`env.foresignal_log`, so do not build it for a WASI target: the host refuses a module that imports any WASI function.

## Install

```sh
dir="$FORESIGNAL_DATA_DIR/plugins/geojson-passthrough"
mkdir -p "$dir"
cp target/wasm32-unknown-unknown/release/geojson_passthrough.wasm "$dir/plugin.wasm"
sha=$(sha256sum "$dir/plugin.wasm" | cut -d' ' -f1)
cat > "$dir/plugin.json" <<EOF
{"name": "geojson-passthrough", "version": "0.1.0", "abi": 1, "kind": "parser", "sha256": "$sha"}
EOF
```

Then, as admin, switch plugins on, check the hash `GET /v1/plugins/geojson-passthrough` reports, and enable the plugin
with that hash:

```sh
curl -X PUT -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"enabled": true}' "$FORESIGNAL_URL/v1/plugins"
curl -H "Authorization: Bearer $ADMIN" "$FORESIGNAL_URL/v1/plugins/geojson-passthrough"
curl -X PUT -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d "{\"enabled\": true, \"sha256\": \"$sha\"}" "$FORESIGNAL_URL/v1/plugins/geojson-passthrough"
```

A `wasm-feed` source then names the plugin (see `docs/Operator-Guide.md`, "Plugins").

## The WAT equivalent and the test artifact

`wat/plugin.wat` is a hand-written WebAssembly text module with the same ABI and the same output for well-formed
input. The repository's test artifact `src/test/resources/plugins/geojson-passthrough/plugin.wasm` is assembled from
it, because the machine the repository was built on has no Rust toolchain (see `scripts/plugins/README.md`).

The two differ only on malformed input. The Rust plugin parses strictly with serde_json. The WAT version is a lenient
scanner: it checks the document's structure (objects, arrays, strings, member separators) and copies values without
validating numbers or literals. Either way the host parses the output as strict JSON and refuses anything that is not
the ABI's document.

## License

MIT, like the rest of the repository. serde and serde_json are MIT OR Apache-2.0.
