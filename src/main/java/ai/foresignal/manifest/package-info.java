/**
 * Analysis manifests (prompt 116): for every analytic output, the exact machinery and inputs that produced it
 * (component and version, build, configuration and its hash, registry hashes, {@code asOfSeq}, inputs, and
 * model metadata for LLM steps), stored once per distinct content, and the replay of deterministic components
 * ({@code docs/Intelligence-Design.md} §4.7).
 */
package ai.foresignal.manifest;
