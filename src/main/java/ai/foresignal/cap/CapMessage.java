package ai.foresignal.cap;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The content of one CAP 1.2 alert message Foresignal issues for an alert state change (prompt 112), written
 * by {@link CapMessageWriter}. {@code scope} is always {@code Public} and every message carries
 * {@link #NOTE}.
 *
 * @param identifier {@code {instanceId}-{alertId}-{n}}
 * @param sender the {@code cap.sender} setting
 * @param sent when the message was issued
 * @param status {@link Status}
 * @param msgType {@link MsgType}
 * @param references earlier messages of the alert, each {@code sender,identifier,sent}
 * @param info the single {@code <info>} block
 */
public record CapMessage(String identifier, String sender, Instant sent, Status status, MsgType msgType,
        List<String> references, Info info) {

    /** The note every message carries. */
    public static final String NOTE =
            "Generated automatically by a Foresignal instance from third-party data. Not an official warning.";

    /** Copies the references. */
    public CapMessage {
        references = references == null ? List.of() : List.copyOf(references);
    }

    /** CAP {@code status}. */
    public enum Status {
        /** A real alert. */
        Actual,
        /** An exercise alert (prompt 115's exercise flag). */
        Exercise,
        /** A channel test. */
        Test
    }

    /**
     * CAP {@code msgType}. Foresignal never issues {@code Cancel}: CAP reserves it for messages issued in error,
     * and a resolved alert is an {@code Update} with {@code responseType AllClear} (Gap-Analysis §3).
     */
    public enum MsgType {
        /** The alert fired. */
        Alert,
        /** Any later change: acknowledgement, snooze, enrichment, resolution. */
        Update
    }

    /**
     * The {@code <info>} block.
     *
     * @param language the language, {@code en-US} by default
     * @param category the category ({@link CapCategories})
     * @param event the rule name
     * @param allClear true to add {@code <responseType>AllClear</responseType>} (resolution)
     * @param urgency {@code Immediate} or {@code Unknown}
     * @param severity {@code Severe}, {@code Moderate} or {@code Minor}
     * @param certainty {@code Observed}, {@code Likely} or {@code Unknown}
     * @param effective when the alert took effect
     * @param expires when the information expires, or null
     * @param senderName {@code Foresignal ({instance name})}
     * @param headline at most 160 characters
     * @param description the plain-text body
     * @param web the deep link, or null
     * @param parameters the {@code foresignal*} parameters in order
     * @param area the area, or null when the alert has no geometry
     */
    public record Info(String language, String category, String event, boolean allClear, String urgency,
            String severity, String certainty, Instant effective, Instant expires, String senderName, String headline,
            String description, String web, Map<String, String> parameters, Area area) {

        /** Copies the parameters, keeping their order. */
        public Info {
            parameters = parameters == null ? Map.of()
                    : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(parameters));
        }
    }

    /**
     * The {@code <area>} block: a polygon or a circle.
     *
     * @param areaDesc a place name or {@code {lat},{lon}}
     * @param polygon a closed ring of {@code [lat, lon]} pairs (at most 100 points), or null
     * @param circle {@code [lat, lon, radiusKm]}, or null
     */
    public record Area(String areaDesc, List<double[]> polygon, double[] circle) {}
}
