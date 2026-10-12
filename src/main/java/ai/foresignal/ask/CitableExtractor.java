package ai.foresignal.ask;

import tools.jackson.databind.JsonNode;

/**
 * Finds citable things in a tool result and adds them to a {@link CitationSet} (prompt 028).
 * Declare an implementation as a Spring bean to make a new kind of output citable: prompt 029 adds
 * briefing runs, 056 weather products. {@link RecordAlertExtractor} handles records and alerts.
 */
public interface CitableExtractor {

    /**
     * Adds what {@code output} offers for citation.
     *
     * @param tool the tool name ({@code retrieval} in retrieval-only mode)
     * @param output the tool's result, as the model saw it
     * @param citable where to add
     */
    void extract(String tool, JsonNode output, CitationSet citable);
}
