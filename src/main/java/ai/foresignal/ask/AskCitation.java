package ai.foresignal.ask;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A verified citation ({@code AskCitation}): something a tool actually returned, which the answer
 * cites.
 *
 * @param id {@code <sourceId>:<externalId>}, {@code alert:<id>} or {@code briefing:<runId>}
 * @param seq the persisted version seq of a record, else null
 * @param title its title (records: the shared {@code TitleResolver} title)
 * @param type {@value #RECORD}, {@value #ALERT} or {@value #BRIEFING}
 */
public record AskCitation(String id, Long seq, String title, String type) {

    /** A record version. */
    public static final String RECORD = "record";

    /** An alert. */
    public static final String ALERT = "alert";

    /** A briefing run. */
    public static final String BRIEFING = "briefing";

    /** Requires the id, title and type. */
    public AskCitation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(type, "type");
    }

    /** The JSON members, as {@code AskCitation}. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        out.put("seq", seq);
        out.put("title", title);
        out.put("type", type);
        return out;
    }
}
