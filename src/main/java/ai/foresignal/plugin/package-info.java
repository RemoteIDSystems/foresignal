/**
 * Out-of-tree WASM parser plugins (prompt 141, {@code docs/Plugin-Design.md}), disabled by default.
 *
 * <p>A plugin is a WebAssembly module in {@code ${FORESIGNAL_DATA_DIR}/plugins/{name}/plugin.wasm} with a
 * {@code plugin.json} manifest. It runs in Chicory, a pure-Java interpreter, with no WASI imports (no clock,
 * filesystem or network), one rate-limited host import ({@code env.foresignal_log}), at most 64 MB of memory, a 5 s
 * wall-clock timeout and 16 MB of output. A plugin runs only while the {@code plugins} setting is on and the admin has
 * enabled it after reviewing its SHA-256, and only while its file still hashes to that value.
 *
 * <p>ABI v1 knows one kind, {@code parser}: the host passes the raw upstream bytes of a {@code wasm-feed} source
 * ({@link ai.foresignal.plugin.feed}) and the plugin answers {@code {"records":[IngestRecord...],"complete":bool}};
 * the records then take the normal ingest path, normalizer included.
 */
package ai.foresignal.plugin;
