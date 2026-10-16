package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.AdapterStateStore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * The station's share of {@code adapter_state} (prompt 049, Weather-Imagery-Design §4.1): both loops
 * read and write it, and it survives restarts.
 *
 * <ul>
 *   <li>{@value #ACTIVE}: the open RF session and its ring, flushed every 30 s and at LOS;
 *   <li>{@value #SESSIONS}: the last {@value #MAX_SESSIONS} RF session summaries;
 *   <li>{@value #PASSES}: the pending and recent passes;
 *   <li>{@value #WATCHES} and {@value #PROCESSED}: inbox folder signatures and processed folders;
 *   <li>{@value #XRIT_FOLDERS} and {@value #XRIT_PRODUCTS}: xRIT folders in progress and the dynamic
 *       products to register again on restart;
 *   <li>{@value #UPLOAD_WATCHES} and {@value #UPLOAD_PROCESSED}: the same for remote pass bundles under
 *       {@code rasters/satdump/{station}/incoming/} (prompt 054);
 *   <li>{@value #POLAR_PRODUCTS}: the polar overlay products (id to composite) to register again on
 *       restart (prompt 054).
 * </ul>
 *
 * <p>Each value is JSON. A value that cannot be read is logged and treated as absent, so a format change
 * between versions costs a little history rather than the stream.
 */
public final class StationStore {

    private static final Logger log = LoggerFactory.getLogger(StationStore.class);

    /** The open RF session. */
    public static final String ACTIVE = "rf.active";

    /** The recent RF session summaries. */
    public static final String SESSIONS = "rf.sessions";

    /** The pending and recent passes. */
    public static final String PASSES = "passes";

    /** The inbox folder watches. */
    public static final String WATCHES = "inbox.watches";

    /** The processed pass folders. */
    public static final String PROCESSED = "inbox.processed";

    /** The remote upload watches (prompt 054). */
    public Map<String, InboxScanner.Watch> uploadWatches() {
        return read(UPLOAD_WATCHES, new TypeReference<LinkedHashMap<String, InboxScanner.Watch>>() {},
                new LinkedHashMap<>());
    }

    /** The processed remote uploads (prompt 054). */
    public Map<String, String> uploadProcessed() {
        return read(UPLOAD_PROCESSED, new TypeReference<LinkedHashMap<String, String>>() {}, new LinkedHashMap<>());
    }

    /** The polar overlay products, id to composite (prompt 054). */
    public Map<String, String> polarProducts() {
        return read(POLAR_PRODUCTS, new TypeReference<LinkedHashMap<String, String>>() {}, new LinkedHashMap<>());
    }

    /** The xRIT folders. */
    public static final String XRIT_FOLDERS = "xrit.folders";

    /** The dynamic xRIT products. */
    public static final String XRIT_PRODUCTS = "xrit.products";

    /** The remote upload folder watches (prompt 054). */
    public static final String UPLOAD_WATCHES = "uploads.watches";

    /** The processed remote upload folders (prompt 054). */
    public static final String UPLOAD_PROCESSED = "uploads.processed";

    /** The polar overlay products, id to composite (prompt 054). */
    public static final String POLAR_PRODUCTS = "polar.products";

    /** The key of the DCS/EMWIN file and ordering memory (prompt 107). */
    public static final String EXTRAS = "extras.files";

    /** RF session summaries kept. */
    public static final int MAX_SESSIONS = 50;

    /**
     * The persisted open session.
     *
     * @param passId the pass id
     * @param satKey the satellite key, or null
     * @param satellite the satellite name, or null
     * @param mode the mode
     * @param start the start (epoch milliseconds)
     * @param frequencyHz the frequency, or null
     * @param samples the ring
     */
    public record ActiveSession(String passId, String satKey, String satellite, String mode, long start,
            Long frequencyHz, List<RfSession.Sample> samples) {}

    /**
     * A finished RF session's summary.
     *
     * @param passId the pass id
     * @param satKey the satellite key, or null
     * @param start the start (epoch milliseconds)
     * @param los the LOS (epoch milliseconds)
     * @param rf the statistics
     */
    public record SessionSummary(String passId, String satKey, long start, long los, Map<String, Object> rf) {}

    private static final JsonMapper JSON = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private final AdapterStateStore state;

    /** Creates the store over {@code state}. */
    public StationStore(AdapterStateStore state) {
        this.state = state;
    }

    /** Reads a value, or {@code fallback} when absent or unreadable. */
    public <T> T read(String key, TypeReference<T> type, T fallback) {
        String text = state.get(key).orElse(null);
        if (text == null || text.isBlank()) {
            return fallback;
        }
        try {
            T value = JSON.readValue(text, type);
            return value == null ? fallback : value;
        } catch (JacksonException e) {
            log.warn("Adapter state {} could not be read and is ignored: {}", key, e.getOriginalMessage());
            return fallback;
        }
    }

    /** Writes a value; null removes the key. Writes only when the JSON changed. */
    public void write(String key, Object value) {
        if (value == null) {
            state.remove(key);
            return;
        }
        String text = JSON.writeValueAsString(value);
        if (!text.equals(state.get(key).orElse(null))) {
            state.put(key, text);
        }
    }

    /** The passes. */
    public List<PassState> passes() {
        return read(PASSES, new TypeReference<List<PassState>>() {}, new ArrayList<>());
    }

    /** The open session, or null. */
    public ActiveSession active() {
        return read(ACTIVE, new TypeReference<ActiveSession>() {}, null);
    }

    /** The session summaries. */
    public List<SessionSummary> sessions() {
        return read(SESSIONS, new TypeReference<List<SessionSummary>>() {}, new ArrayList<>());
    }

    /** Appends a session summary, keeping the last {@value #MAX_SESSIONS}. */
    public void appendSession(SessionSummary summary) {
        List<SessionSummary> sessions = new ArrayList<>(sessions());
        sessions.removeIf(s -> s.passId().equals(summary.passId()));
        sessions.add(summary);
        while (sessions.size() > MAX_SESSIONS) {
            sessions.remove(0);
        }
        write(SESSIONS, sessions);
    }

    /** The inbox watches. */
    public Map<String, InboxScanner.Watch> watches() {
        return read(WATCHES, new TypeReference<LinkedHashMap<String, InboxScanner.Watch>>() {}, new LinkedHashMap<>());
    }

    /** The processed folders. */
    public Map<String, String> processed() {
        return read(PROCESSED, new TypeReference<LinkedHashMap<String, String>>() {}, new LinkedHashMap<>());
    }

    /** The xRIT folders. */
    public Map<String, XritFrames.FolderState> xritFolders() {
        return read(XRIT_FOLDERS, new TypeReference<LinkedHashMap<String, XritFrames.FolderState>>() {},
                new LinkedHashMap<>());
    }

    /** The DCS/EMWIN file and ordering memory (prompt 107), or null before the first scan. */
    public ExtrasTracker.State extras() {
        return read(EXTRAS, new TypeReference<ExtrasTracker.State>() {}, null);
    }

    /** The xRIT products. */
    public Map<String, XritFrames.ProductState> xritProducts() {
        return read(XRIT_PRODUCTS, new TypeReference<LinkedHashMap<String, XritFrames.ProductState>>() {},
                new LinkedHashMap<>());
    }
}
