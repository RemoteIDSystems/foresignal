/**
 * External data bindings (prompt 138, docs/External-Data-Protocol.md §5): connect EDP streams to series, canonical
 * families, the social and news pipelines, hypothesis evidence and indicators, forecast questions, places, situations
 * and chronicles. {@link ai.foresignal.inbound.edp.binding.BindingRouter} applies enabled bindings to matching records
 * exactly once per record version; atomic items do nothing until bound.
 */
package ai.foresignal.inbound.edp.binding;
