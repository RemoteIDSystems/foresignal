/**
 * The {@code wasm-feed} source family (prompt 141): operator-defined polling sources whose upstream bytes are parsed by
 * an out-of-tree WASM plugin ({@link ai.foresignal.plugin}).
 *
 * <p>Each source is a spec in {@code ${FORESIGNAL_DATA_DIR}/wasm-feeds/*.json} naming a plugin, an https URL and a full
 * descriptor. Specs are read once at startup and never accepted over the API; every source ships disabled, and a fetch
 * runs only while plugins are on and its plugin is enabled. The URL passes the outbound URL policy (prompt 035) at load
 * and before every fetch, and the request goes through the source's budgeted {@code SourceHttp}.
 */
package ai.foresignal.plugin.feed;
