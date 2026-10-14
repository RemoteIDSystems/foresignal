/**
 * What the prompt 081 conflict and civil-alert sources share ({@link
 * ai.foresignal.source.adapters.conflict.ConflictSources}): the test base-URL property, the {@code event}
 * retention, JSON and text helpers and the field-spec helpers. Not an adapter: the adapters under
 * {@code alertsinua}, {@code uasiren}, {@code deepstate}, {@code acled} and {@code ucdp} use it and never import
 * each other.
 */
package ai.foresignal.source.adapters.conflict;
