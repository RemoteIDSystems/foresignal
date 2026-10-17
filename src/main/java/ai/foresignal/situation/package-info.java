/**
 * Situations and storylines (prompt 118): persistent storylines that group related intelligence events, findings,
 * alerts, anomalies, records and investigations over days to months. A deterministic linker
 * ({@link ai.foresignal.situation.SituationLinker}) builds them from scored rules, operators and agents shape them
 * ({@link ai.foresignal.situation.SituationService}), daily snapshots make "what changed since" a diff
 * ({@link ai.foresignal.situation.SituationSnapshots}), and an optional, budgeted LLM brief
 * ({@link ai.foresignal.situation.SituationBriefService}) never changes members, status or severity. Situations are
 * published as {@code foresignal-intel} records of kind {@code situation}
 * ({@link ai.foresignal.situation.SituationRecordPublisher}) and streamed on the {@code situations} SSE topic.
 */
package ai.foresignal.situation;
