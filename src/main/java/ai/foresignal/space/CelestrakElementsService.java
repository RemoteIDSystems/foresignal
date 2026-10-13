package ai.foresignal.space;

import ai.foresignal.source.spi.OrbitalElements;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The current {@code celestrak-gp} element sets (prompt 064): the non-tombstoned, non-expired heads read
 * from {@code record_head ⋈ record_live} (invariant HIST-2), for the CelesTrak-compatible output and, as
 * {@link OrbitalElements}, for adapters that predict passes (a SatDump station). Never calls CelesTrak.
 */
@Service
public class CelestrakElementsService implements OrbitalElements {

    private static final Logger log = LoggerFactory.getLogger(CelestrakElementsService.class);

    /** The source the elements come from. */
    public static final String SOURCE = "celestrak-gp";

    /** The record kind. */
    public static final String KIND = "satellite";

    private static final String CURRENT = " FROM record_head h JOIN record_live l USING (source_id, external_id)"
            + " WHERE h.source_id = ? AND h.kind = ? AND NOT h.tombstone"
            + " AND (l.valid_until IS NULL OR l.valid_until > ?)";

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    /**
     * One current element set with what the compatible output needs besides the elements.
     *
     * @param elements the elements
     * @param groups the CelesTrak groups the object belongs to (membership), sorted
     * @param seq the head's seq
     */
    public record Current(OmmElements elements, List<String> groups, long seq) {

        /** Copies the groups. */
        public Current {
            groups = groups == null ? List.of() : List.copyOf(groups);
        }
    }

    /**
     * The state of the element store, for the ETag and the age header.
     *
     * @param maxSeq the highest seq of any {@code celestrak-gp} head (tombstones included), 0 when none
     * @param newestReceivedAt when the newest current head was last received, or null when there is none
     */
    public record Status(long maxSeq, Instant newestReceivedAt) {}

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public CelestrakElementsService(JdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public Optional<OmmElements> byNorad(long norad) {
        try {
            List<Current> rows = query(" AND h.external_id = ?", Long.toString(norad));
            return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0).elements());
        } catch (RuntimeException e) {
            log.debug("Could not read the elements of NORAD {}", norad, e);
            return Optional.empty();
        }
    }

    /** Every current element set, ordered by catalog number. Unparseable payloads are skipped. */
    public List<Current> all() {
        return query("");
    }

    /** The highest seq and the newest receive time of the source's heads. */
    public Status status() {
        Long max = jdbc.queryForObject("SELECT coalesce(max(seq), 0) FROM record_head WHERE source_id = ?",
                Long.class, SOURCE);
        Timestamp newest = jdbc.queryForObject("SELECT max(l.last_received_at)" + CURRENT, Timestamp.class, SOURCE,
                KIND, Timestamp.from(clock.instant()));
        return new Status(max == null ? 0 : max, newest == null ? null : newest.toInstant());
    }

    private List<Current> query(String extra, Object... args) {
        List<Object> params = new ArrayList<>(List.of(SOURCE, KIND, Timestamp.from(clock.instant())));
        params.addAll(List.of(args));
        List<Current> out = new ArrayList<>();
        jdbc.query("SELECT h.seq, jsonb_strip_nulls(h.payload || l.kinematic)::text AS payload" + CURRENT + extra
                + " ORDER BY length(h.external_id), h.external_id", rs -> {
                    String text = rs.getString("payload");
                    try {
                        Map<String, Object> payload = json.readValue(text, MAP);
                        out.add(new Current(OmmElements.fromPayload(payload), groups(payload.get("groups")),
                                rs.getLong("seq")));
                    } catch (RuntimeException e) {
                        log.debug("Skipping an unreadable celestrak-gp head: {}", e.getMessage());
                    }
                }, params.toArray());
        return out;
    }

    private static List<String> groups(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).sorted().toList();
    }
}
