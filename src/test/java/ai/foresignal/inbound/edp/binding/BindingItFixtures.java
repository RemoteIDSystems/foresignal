package ai.foresignal.inbound.edp.binding;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manifests and batches shared by the binding integration tests (prompt 138): one provider with a stream of every
 * item type (observations, events with declared coverage, social-post, article and note documents, forecasts and
 * features), each provider id unique within the run because ingest buckets live per source id.
 */
final class BindingItFixtures {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private BindingItFixtures() {}

    /** A fresh provider id ({@code source.id}, without the {@code ext-} prefix). */
    static String newProvider(String stem) {
        return stem + "-" + SEQUENCE.incrementAndGet() + "-" + Long.toString(System.nanoTime() % 1_000_000, 36);
    }

    /** The manifest of the fixture provider; {@code levelKind} is the kind of the observation stream. */
    static String manifest(String providerId, String levelKind) {
        return "{\"protocol\":\"foresignal-edp/1.0\",\"source\":{\"id\":\"" + providerId + "\","
                + "\"name\":\"Harbor lab\",\"publisher\":{\"name\":\"Example Harbor Lab\"},"
                + "\"licence\":{\"id\":\"CC0-1.0\",\"redistribution\":\"allowed\"},"
                + "\"access\":{\"route\":\"scrape\",\"fragility\":\"high\"},"
                + "\"originator\":{\"id\":\"example-harbor-lab\",\"class\":\"aggregator\",\"aggregates\":[]}},"
                + "\"transport\":{\"push\":true},\"streams\":["
                + "{\"id\":\"levels\",\"mode\":\"atomic\",\"itemType\":\"observation\",\"kind\":\"" + levelKind + "\","
                + "\"unit\":\"m\",\"geometry\":\"none\",\"suggestedBindings\":[{\"target\":\"series\"}]},"
                + "{\"id\":\"incidents\",\"mode\":\"layer\",\"itemType\":\"event\",\"kind\":\"harbor-incident\","
                + "\"geometry\":\"Point\",\"coverage\":{\"area\":{\"type\":\"Polygon\",\"coordinates\":[[[54.5,25.0],"
                + "[58.0,25.0],[58.0,27.5],[54.5,27.5],[54.5,25.0]]]},\"family\":\"harbor-incident\",\"pDetect\":0.6},"
                + "\"suggestedBindings\":[{\"target\":\"canonical-family\",\"config\":{\"family\":\"harbor-incident\"}}]},"
                + "{\"id\":\"posts\",\"mode\":\"atomic\",\"itemType\":\"document\",\"kind\":\"harbor-post\","
                + "\"documentType\":\"social-post\"},"
                + "{\"id\":\"articles\",\"mode\":\"atomic\",\"itemType\":\"document\",\"kind\":\"harbor-article\","
                + "\"documentType\":\"article\"},"
                + "{\"id\":\"notes\",\"mode\":\"atomic\",\"itemType\":\"document\",\"kind\":\"harbor-note\","
                + "\"documentType\":\"note\",\"suggestedBindings\":[{\"target\":\"evidence\"}]},"
                + "{\"id\":\"odds\",\"mode\":\"atomic\",\"itemType\":\"forecast\",\"kind\":\"harbor-odds\"},"
                + "{\"id\":\"cranes\",\"mode\":\"layer\",\"itemType\":\"feature\",\"kind\":\"harbor-crane\","
                + "\"geometry\":\"Point\"}]}";
    }

    /** A batch of {@code items} (comma-separated item objects). */
    static String batch(String items) {
        return "{\"protocol\":\"foresignal-edp/1.0\",\"sentAt\":\"2026-10-18T05:59:00Z\",\"items\":[" + items + "]}";
    }

    /** An observation of series {@code berth-1} for day {@code t}. */
    static String observation(String id, String t, double value) {
        return "{\"stream\":\"levels\",\"type\":\"observation\",\"id\":\"" + id + "\",\"seriesKey\":\"berth-1\","
                + "\"t\":\"" + t + "\",\"value\":" + value + ",\"observedAt\":\"2026-10-18T05:00:00Z\","
                + "\"knownAt\":\"2026-10-17T06:00:00Z\"}";
    }

    /** An event inside the declared coverage. */
    static String event(String id) {
        return "{\"stream\":\"incidents\",\"type\":\"event\",\"id\":\"" + id + "\",\"observedAt\":\"2026-10-18T05:10:00Z\","
                + "\"eventTime\":\"2026-10-18T05:00:00Z\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[56.4,26.5]},"
                + "\"properties\":{\"category\":\"fire\"}}";
    }

    /** A social-post document on platform {@code telegram}. */
    static String post(String id) {
        return "{\"stream\":\"posts\",\"type\":\"document\",\"id\":\"" + id + "\",\"observedAt\":\"2026-10-18T05:20:00Z\","
                + "\"documentType\":\"social-post\",\"text\":\"Fire reported at the harbor\",\"language\":\"en\","
                + "\"author\":{\"handle\":\"harborwatch\",\"accountId\":\"1001\"},\"publishedAt\":\"2026-10-18T05:15:00Z\","
                + "\"properties\":{\"platform\":\"telegram\",\"channel\":\"harborwatch\"}}";
    }

    /** An article document. */
    static String article(String id) {
        return "{\"stream\":\"articles\",\"type\":\"document\",\"id\":\"" + id + "\",\"observedAt\":\"2026-10-18T05:25:00Z\","
                + "\"documentType\":\"article\",\"title\":\"Fire at the harbor\",\"text\":\"A fire broke out at the"
                + " harbor this morning.\",\"url\":\"https://news.example.org/harbor-fire-" + id + "\",\"language\":\"en\","
                + "\"publishedAt\":\"2026-10-18T05:20:00Z\"}";
    }

    /** A note document. */
    static String note(String id) {
        return "{\"stream\":\"notes\",\"type\":\"document\",\"id\":\"" + id + "\",\"observedAt\":\"2026-10-18T05:30:00Z\","
                + "\"documentType\":\"note\",\"text\":\"Crane 3 out of service\",\"title\":\"Crane\"}";
    }

    /** A forecast on provider question {@code q-1}. */
    static String forecast(String id, double probability) {
        return "{\"stream\":\"odds\",\"type\":\"forecast\",\"id\":\"" + id + "\",\"madeAt\":\"2026-10-18T05:00:00Z\","
                + "\"question\":{\"externalId\":\"q-1\",\"title\":\"Will the port close this week?\"},"
                + "\"probability\":" + probability + "}";
    }

    /** A crane feature. */
    static String feature(String id) {
        return "{\"stream\":\"cranes\",\"type\":\"feature\",\"id\":\"" + id + "\",\"observedAt\":\"2026-10-18T05:40:00Z\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[56.41,26.51]},\"properties\":{\"name\":\"Crane 1\"}}";
    }

    /** Every item type at once. */
    static String everything() {
        return batch(String.join(",", observation("berth-1:2026-10-16", "2026-10-16", 4.2), event("e-1"), post("p-1"),
                article("a-1"), note("n-1"), forecast("f-1", 0.3), feature("c-1")));
    }

    /** A binary manual ledger question. */
    static String binaryQuestion() {
        return "{\"title\":\"Will the port close this week?\",\"type\":\"binary\",\"resolutionCriteria\":\"Yes if the"
                + " port authority announces a closure.\",\"resolver\":{\"type\":\"manual\"},"
                + "\"closesAt\":\"2026-10-24T00:00:00Z\",\"resolveBy\":\"2026-10-31T00:00:00Z\",\"category\":\"maritime\"}";
    }

    /** A numeric manual ledger question. */
    static String numericQuestion() {
        return "{\"title\":\"How many vessels will transit?\",\"type\":\"numeric\",\"outcomes\":{\"unit\":\"vessels\"},"
                + "\"resolutionCriteria\":\"The port authority's count.\",\"resolver\":{\"type\":\"manual\"},"
                + "\"closesAt\":\"2026-10-24T00:00:00Z\",\"resolveBy\":\"2026-10-31T00:00:00Z\",\"category\":\"maritime\"}";
    }

    /** A hypothesis set with two manual indicators ("Crane outage", "Harbor fire"). */
    static String hypothesisSet() {
        String lr = "{\"closure\":{\"class\":\"high\",\"direction\":\"supports\"},"
                + "\"no closure\":{\"class\":\"medium\",\"direction\":\"weakens\"}}";
        return "{\"question\":\"Will the port close this week?\",\"kind\":\"exclusive\",\"cadence\":\"weekly\","
                + "\"hypotheses\":[{\"label\":\"closure\",\"statement\":\"The port closes.\",\"type\":\"outcome\","
                + "\"prior\":0.3,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Past fires.\"}},"
                + "{\"label\":\"no closure\",\"statement\":\"The port stays open.\",\"type\":\"outcome\",\"prior\":0.65,"
                + "\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Usual.\"}},"
                + "{\"label\":\"other\",\"statement\":\"Other.\",\"type\":\"other\",\"prior\":0.05,"
                + "\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Residual.\"}}],"
                + "\"indicators\":[{\"description\":\"Harbor fire\",\"signal\":{\"type\":\"manual\"},\"signalFamily\":"
                + "\"aviation-military\",\"credibility\":\"M\",\"relevance\":\"H\",\"lr\":" + lr + "},"
                + "{\"description\":\"Crane outage\",\"signal\":{\"type\":\"manual\"},\"signalFamily\":"
                + "\"aviation-military\",\"credibility\":\"M\",\"relevance\":\"H\",\"lr\":" + lr + "}],"
                + "\"assumptions\":[{\"text\":\"The port authority reports closures.\",\"linchpin\":true,\"status\":\"solid\"}]}";
    }
}
