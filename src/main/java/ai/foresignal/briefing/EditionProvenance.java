package ai.foresignal.briefing;

import ai.foresignal.ask.AskCitation;
import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.evidence.RecordRef;
import ai.foresignal.intel.EventScorer;
import ai.foresignal.intel.IntelCorroborationService;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.provenance.CredibilityService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Why each item is in an edition, and how well-supported it is (prompt NP-007): annotates a run's {@code AskCitation}
 * maps with {@code provenance} (when the briefing's {@code includeProvenance}) and {@code corroboration} (when
 * {@code includeCorroboration}). Neither member is added while its flag is off, so other briefings' citations are
 * unchanged.
 *
 * <p>Record citations ({@code <sourceId>:<externalId>}) and alert citations ({@code alert:<id>}, whose record is the
 * alert's) are annotated; briefing and weather citations are left as they are.
 *
 * <ul>
 *   <li><b>{@code provenance.refs}:</b> the record's {@code record:} ref, the {@code canonical-event:} refs its
 *       observation links (114) point to, and for an alert its {@code alert:} ref. {@code reason} is a one-line
 *       "why included": the kind and source, the alert's rule, and how many events it is linked to.
 *   <li><b>{@code corroboration}:</b> {@code independentSources} is the most of the record's corroboration cluster
 *       (099) and the independent evidence groups of its canonical events; {@code credibility} and
 *       {@code reliability} are the Admiralty grade (109, {@link CredibilityService}). {@code evidenceQuality} follows
 *       the 154 corroboration policy ({@link IntelCorroborationService#quality()}): {@code strong} with at least
 *       {@code strongGroups} independent sources and a confirmed or probable credibility (1–2); {@code moderate} with
 *       at least two independent sources or a possibly-true credibility (3); {@code weak} for a single uncorroborated
 *       source; {@code insufficient} when the record is gone.
 * </ul>
 */
@Component
public class EditionProvenance {

    private static final Logger log = LoggerFactory.getLogger(EditionProvenance.class);

    /** {@code corroboration.evidenceQuality} values. */
    public static final String STRONG = "strong";

    /** Two or more independent sources, or a possibly-true grade. */
    public static final String MODERATE = "moderate";

    /** One uncorroborated source. */
    public static final String WEAK = "weak";

    /** The record is gone. */
    public static final String INSUFFICIENT = "insufficient";

    /** The most canonical events listed per item. */
    static final int MAX_EVENTS = 5;

    private final JdbcTemplate jdbc;
    private final ObjectProvider<EvidenceStore> evidence;
    private final ObjectProvider<CredibilityService> credibility;
    private final ObjectProvider<IntelCorroborationService> policy;

    /** Creates the annotator. */
    public EditionProvenance(JdbcTemplate jdbc, ObjectProvider<EvidenceStore> evidence,
            ObjectProvider<CredibilityService> credibility, ObjectProvider<IntelCorroborationService> policy) {
        this.jdbc = jdbc;
        this.evidence = evidence;
        this.credibility = credibility;
        this.policy = policy;
    }

    /** The record an item is about. */
    record Item(String sourceId, String externalId, String kind, Long seq, int qaFlags, Long alertId,
            String ruleName) {}

    /**
     * {@code citations} annotated as the flags ask, in order; unchanged when both are off. A failure to annotate one
     * item leaves that item as it was and never fails the run.
     */
    public List<Map<String, Object>> annotate(List<Map<String, Object>> citations, boolean provenance,
            boolean corroboration) {
        if ((!provenance && !corroboration) || citations == null || citations.isEmpty()) {
            return citations;
        }
        CredibilityService grades = credibility.getIfAvailable();
        CredibilityService.Lookup lookup = null;
        if (corroboration && grades != null) {
            try {
                lookup = grades.load();
            } catch (RuntimeException e) {
                log.warn("Edition corroboration could not load the corroboration clusters: {}", e.toString());
            }
        }
        EventScorer.Quality quality = quality();
        List<Map<String, Object>> out = new ArrayList<>(citations.size());
        for (Map<String, Object> citation : citations) {
            Map<String, Object> annotated = new LinkedHashMap<>(citation);
            try {
                Item item = item(citation);
                if (item != null) {
                    List<EvidenceStore.StoredEvent> events = events(item);
                    if (provenance) {
                        annotated.put("provenance", provenance(item, events));
                    }
                    if (corroboration) {
                        annotated.put("corroboration", corroboration(item, events, grades, lookup, quality));
                    }
                }
            } catch (RuntimeException e) {
                log.debug("Citation {} could not be annotated: {}", citation.get("id"), e.toString());
            }
            out.add(annotated);
        }
        return out;
    }

    /** The policy's thresholds, or the historical defaults. */
    EventScorer.Quality quality() {
        IntelCorroborationService service = policy.getIfAvailable();
        if (service == null) {
            return EventScorer.Quality.DEFAULT;
        }
        try {
            return service.quality();
        } catch (RuntimeException e) {
            return EventScorer.Quality.DEFAULT;
        }
    }

    /** The record behind {@code citation}, or null for a briefing or weather citation or a record that is gone. */
    Item item(Map<String, Object> citation) {
        Object type = citation.get("type");
        String id = String.valueOf(citation.get("id"));
        if (AskCitation.ALERT.equals(type) && id.startsWith("alert:")) {
            long alertId = Long.parseLong(id.substring("alert:".length()));
            List<Item> rows = jdbc.query("SELECT a.source_id, a.external_id, a.kind, a.seq, r.name FROM alert a"
                    + " LEFT JOIN trigger_rule r ON r.id = a.rule_id WHERE a.id = ?",
                    (rs, i) -> new Item(rs.getString(1), rs.getString(2), rs.getString(3), rs.getLong(4), 0, alertId,
                            rs.getString(5)), alertId);
            if (rows.isEmpty()) {
                return null;
            }
            Item alert = rows.getFirst();
            Item record = record(alert.sourceId(), alert.externalId());
            return record == null ? new Item(alert.sourceId(), alert.externalId(), alert.kind(), null, 0, alertId,
                    alert.ruleName())
                    : new Item(record.sourceId(), record.externalId(), record.kind(), record.seq(), record.qaFlags(),
                            alertId, alert.ruleName());
        }
        if (!AskCitation.RECORD.equals(type)) {
            return null;
        }
        int colon = id.indexOf(':');
        if (colon <= 0 || colon == id.length() - 1) {
            return null;
        }
        Item record = record(id.substring(0, colon), id.substring(colon + 1));
        return record == null ? new Item(id.substring(0, colon), id.substring(colon + 1), null, null, 0, null, null)
                : record;
    }

    /** The current head of a record, or null when it is gone. */
    Item record(String sourceId, String externalId) {
        List<Item> rows = jdbc.query("SELECT h.kind, h.seq, coalesce((SELECT v.qa_flags FROM record_version v"
                + " WHERE v.seq = h.seq LIMIT 1), 0) FROM record_head h WHERE h.source_id = ? AND h.external_id = ?"
                + " AND NOT h.tombstone", (rs, i) -> new Item(sourceId, externalId, rs.getString(1), rs.getLong(2),
                        rs.getInt(3), null, null), sourceId, externalId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    /** The canonical events (114) the record is linked to, at most {@value #MAX_EVENTS}. */
    List<EvidenceStore.StoredEvent> events(Item item) {
        EvidenceStore store = evidence.getIfAvailable();
        if (store == null || item.seq() == null) {
            return List.of();
        }
        Set<String> ids = new LinkedHashSet<>();
        for (EvidenceStore.StoredLink link : store.linksOf(item.sourceId(), item.externalId())) {
            if (ids.size() >= MAX_EVENTS) {
                break;
            }
            ids.add(link.canonicalEventId());
        }
        return ids.isEmpty() ? List.of() : store.events(ids);
    }

    /** The {@code provenance} member: refs and a one-line reason. */
    static Map<String, Object> provenance(Item item, List<EvidenceStore.StoredEvent> events) {
        List<String> refs = new ArrayList<>();
        if (item.alertId() != null) {
            refs.add(ObjectTypes.ALERT + ":" + item.alertId());
        }
        refs.add(item.seq() == null ? RecordRef.format(item.sourceId(), item.externalId())
                : RecordRef.format(item.sourceId(), item.externalId(), item.seq()));
        for (EvidenceStore.StoredEvent event : events) {
            refs.add(ObjectTypes.CANONICAL_EVENT + ":" + event.id());
        }
        StringBuilder reason = new StringBuilder();
        if (item.alertId() != null) {
            reason.append("Alert of rule ").append(item.ruleName() == null ? "(deleted)" : "'" + item.ruleName() + "'")
                    .append(" on ");
        } else {
            reason.append("In scope: ");
        }
        reason.append(item.kind() == null ? "a record" : item.kind()).append(" from ").append(item.sourceId());
        if (!events.isEmpty()) {
            reason.append(", linked to ").append(events.size()).append(events.size() == 1 ? " event" : " events");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("refs", refs);
        out.put("reason", reason.toString());
        return out;
    }

    /** The {@code corroboration} member. */
    static Map<String, Object> corroboration(Item item, List<EvidenceStore.StoredEvent> events,
            CredibilityService grades, CredibilityService.Lookup lookup, EventScorer.Quality quality) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (item.seq() == null) {
            out.put("evidenceQuality", INSUFFICIENT);
            out.put("independentSources", 0);
            out.put("credibility", null);
            out.put("reliability", null);
            return out;
        }
        int independent = 1;
        CredibilityService.Cluster cluster = grades == null ? null
                : grades.clusterOf(item.sourceId(), item.externalId(), lookup);
        if (cluster != null) {
            independent = Math.max(independent, cluster.independentSources());
        }
        for (EvidenceStore.StoredEvent event : events) {
            independent = Math.max(independent, event.evidenceGroupCount());
        }
        Integer credibility = grades == null ? null
                : grades.assess(item.sourceId(), item.externalId(), item.kind(), item.qaFlags(), lookup);
        String reliability = grades == null ? null : grades.reliability(item.sourceId());
        out.put("evidenceQuality", quality(independent, credibility, quality));
        out.put("independentSources", independent);
        out.put("credibility", credibility);
        out.put("reliability", reliability);
        return out;
    }

    /** The evidence-quality label of an item (see the class comment). */
    static String quality(int independent, Integer credibility, EventScorer.Quality quality) {
        int grade = credibility == null ? 6 : credibility;
        if (independent >= Math.max(2, quality.strongGroups()) && grade <= 2) {
            return STRONG;
        }
        if (independent >= 2 || grade <= 3) {
            return MODERATE;
        }
        return WEAK;
    }

    /**
     * A short spoken note on corroboration for the audio script, or null when nothing needs saying: only when at
     * least one cited item rests on a single source or its record is gone.
     */
    public static String spokenNote(List<Map<String, Object>> citations) {
        if (citations == null) {
            return null;
        }
        int annotated = 0;
        int weak = 0;
        for (Map<String, Object> citation : citations) {
            if (citation.get("corroboration") instanceof Map<?, ?> c) {
                annotated++;
                Object label = c.get("evidenceQuality");
                if (WEAK.equals(label) || INSUFFICIENT.equals(label)) {
                    weak++;
                }
            }
        }
        if (weak == 0) {
            return null;
        }
        return weak == annotated
                ? "A note on sourcing: every item in this edition rests on a single, uncorroborated source."
                : "A note on sourcing: " + weak + " of the " + annotated + " items in this edition "
                        + (weak == 1 ? "rests" : "rest") + " on a single, uncorroborated source.";
    }
}
