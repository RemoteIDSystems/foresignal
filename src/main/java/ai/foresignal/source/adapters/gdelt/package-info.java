/**
 * The GDELT sources. {@code gdelt-events} (prompt 066): the newest 15-minute GDELT event export, filtered to CAMEO
 * root codes 13–20 by default, as {@code conflict-event} point records with a deterministic text summary for semantic
 * search; only {@code storage.googleapis.com} is contacted, and the linked articles are never fetched.
 * {@code gdelt-timelines} (prompt 117): news volume and tone per attention topic from the DOC 2.0 API, as series, with
 * at least 5 seconds between requests.
 */
package ai.foresignal.source.adapters.gdelt;
