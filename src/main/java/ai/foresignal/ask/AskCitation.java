package ai.foresignal.ask;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A verified citation ({@code AskCitation}): something a tool actually returned, which the answer
 * cites.
 *
 * @param id {@code <sourceId>:<externalId>}, {@code alert:<id>}, {@code briefing:<runId>} or (prompt 056)
 *     {@code wx:<productId>@<frameKey>} / {@code wx:<productId>@<run>/<step>}
 * @param seq the persisted version seq of a record, else null
 * @param title its title (records: the shared {@code TitleResolver} title)
 * @param type {@value #RECORD}, {@value #ALERT}, {@value #BRIEFING} or {@value #WEATHER}
 * @param weather the weather value's product, frame, run, step, valid time and age (type {@value #WEATHER}), else
 *     null
 */
public record AskCitation(String id, Long seq, String title, String type, CitationSet.Weather weather) {

    /** A record version. */
    public static final String RECORD = "record";

    /** An alert. */
    public static final String ALERT = "alert";

    /** A briefing run. */
    public static final String BRIEFING = "briefing";

    /** A weather value (prompt 056). */
    public static final String WEATHER = "weather";

    /** Requires the id, title and type. */
    public AskCitation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(type, "type");
    }

    /** A record, alert or briefing citation (the shape before prompt 056). */
    public AskCitation(String id, Long seq, String title, String type) {
        this(id, seq, title, type, null);
    }

    /** The JSON members, as {@code AskCitation}. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        out.put("seq", seq);
        out.put("title", title);
        out.put("type", type);
        if (weather != null) {
            out.put("productId", weather.productId());
            out.put("frameKey", weather.frameKey());
            out.put("run", weather.run());
            out.put("step", weather.step());
            out.put("validTime", weather.validTime());
            out.put("ageMinutes", weather.ageMinutes());
        }
        return out;
    }
}
