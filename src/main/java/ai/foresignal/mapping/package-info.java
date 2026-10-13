/**
 * Declarative device mappings (prompt 059, Architecture §16).
 *
 * <p>A {@code RecordMapping} is data, not a script: it names JSONPath values (Jayway dialect), substitution-only
 * templates and constants, plus unit conversions from a small built-in UCUM table. {@link
 * ai.foresignal.mapping.MappingCompiler} validates a mapping into an immutable {@link
 * ai.foresignal.mapping.CompiledMapping}; {@link ai.foresignal.mapping.MappingEngine} applies it to one inbound
 * document. There is no expression language and nothing is executed. The preview, sample capture, LLM-suggested
 * drafts and the device preset catalog live here too.
 */
package ai.foresignal.mapping;
