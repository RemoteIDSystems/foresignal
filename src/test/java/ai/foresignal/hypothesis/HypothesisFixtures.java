package ai.foresignal.hypothesis;

/**
 * Request bodies shared by the hypothesis integration tests (prompt 125): the worked example's outcome set
 * (Intelligence-Design §13) in a few variants.
 */
final class HypothesisFixtures {

    private HypothesisFixtures() {}

    /** An indicator that watches intelligence events of a type (no region, so no reference data is needed). */
    static String intelIndicator(String description, String eventType, String family, String expectedBy,
            String lrJson) {
        return "{\"description\":\"" + description + "\",\"signal\":{\"type\":\"intelEvent\",\"intelEvent\":{\"type\":\""
                + eventType + "\"}},\"signalFamily\":\"" + family + "\",\"credibility\":\"H\",\"relevance\":\"H\","
                + (expectedBy == null ? "" : "\"expectedBy\":\"" + expectedBy + "\",") + "\"lr\":" + lrJson + "}";
    }

    /** A manual indicator. */
    static String manualIndicator(String description, String family, String credibility, String lrJson) {
        return "{\"description\":\"" + description + "\",\"signal\":{\"type\":\"manual\"},\"signalFamily\":\""
                + family + "\",\"credibility\":\"" + credibility + "\",\"relevance\":\"H\",\"lr\":" + lrJson + "}";
    }

    /**
     * The worked example's exclusive outcome set: within 72 h (0.25), 3-7 days (0.2), none in 7 days (0.5) and other
     * (0.05).
     *
     * @param cadence the review cadence
     * @param horizon the horizon date, or null
     * @param indicators the indicator JSON objects, comma-separated (may be empty)
     * @param ledgerQuestion the ledger question JSON, or null
     * @param extra extra top-level members ("\"private\":true,"), or empty
     */
    static String outcomeSet(String cadence, String horizon, String indicators, String ledgerQuestion, String extra) {
        return "{" + extra + "\"question\":\"Saudi or coalition kinetic response against Houthi targets within 7"
                + " days?\",\"kind\":\"exclusive\",\"cadence\":\"" + cadence + "\""
                + (horizon == null ? "" : ",\"horizon\":\"" + horizon + "\"")
                + ",\"hypotheses\":["
                + "{\"label\":\"within 72 h\",\"statement\":\"A response follows within 72 hours.\",\"type\":\"outcome\","
                + "\"prior\":0.25,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Comparable strikes.\"}},"
                + "{\"label\":\"3-7 days\",\"statement\":\"A response follows within 3 to 7 days.\",\"type\":\"outcome\","
                + "\"prior\":0.2,\"priorBasis\":{\"type\":\"analogs\",\"ref\":\"analogs:example\"}},"
                + "{\"label\":\"none in 7 days\",\"statement\":\"No response within 7 days.\",\"type\":\"outcome\","
                + "\"prior\":0.5,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Restraint during talks.\"}},"
                + "{\"label\":\"other\",\"statement\":\"Other / none of these.\",\"type\":\"other\",\"prior\":0.05,"
                + "\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Residual mass.\"}}],"
                + "\"indicators\":[" + indicators + "],"
                + "\"assumptions\":[{\"text\":\"The coalition can strike at short notice.\",\"linchpin\":true,"
                + "\"status\":\"solid\"}]"
                + (ledgerQuestion == null ? "" : ",\"ledgerQuestion\":" + ledgerQuestion) + "}";
    }

    /** A {@code hypothesis-outcome} ledger question (resolved by the operator's confirmation). */
    static final String HYPOTHESIS_OUTCOME = "{\"resolver\":{\"type\":\"hypothesis-outcome\"},\"category\":\"conflict\"}";

    /** LRs of an indicator that supports a quick response. */
    static final String QUICK_LR = "{\"within 72 h\":{\"class\":\"high\",\"direction\":\"supports\"},"
            + "\"3-7 days\":{\"class\":\"medium\",\"direction\":\"supports\"},"
            + "\"none in 7 days\":{\"class\":\"high\",\"direction\":\"weakens\"}}";
}
