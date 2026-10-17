package ai.foresignal.object;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.situation.SituationMemberTypes;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.Subscription;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Materializes relations from the structures Foresignal already keeps (prompt 129, {@value #COMPONENT}
 * {@value #VERSION}, invariant OBJ-2).
 *
 * <p>Each owning structure (an <em>owner</em>, keyed like an object reference) projects to a desired set of relations;
 * {@link #project(String)} diffs it against the stored rows of that owner under a stable {@code projection_key}, so
 * re-projecting is idempotent (exactly once after a cursor restart): new relations are inserted, changed ones updated,
 * vanished ones retired with their validity closed. Owners and what they project:
 * <ul>
 *   <li>{@code situation:{id}}: members {@code member-of} the situation (118);</li>
 *   <li>{@code intel-event:{id}}: members {@code evidence-for} / {@code evidence-against} the event by role
 *       (supporting / contradicting), context members {@code member-of} (101);</li>
 *   <li>{@code canonical-event:{id}}: the event {@code observed-by} each observation, and re-reports (later
 *       observations of the same evidence group) {@code same-as} the group's first observation (116);</li>
 *   <li>{@code entity:{id}}: linked records {@code same-as} the entity with the link's validity (098);</li>
 *   <li>{@code infra:graph}: 104 facility edges as {@code depends-on} (dependent → dependency) with their labels;</li>
 *   <li>{@code hypothesis-set:{id}}: the set {@code about} its situation, hypotheses and indicators {@code member-of}
 *       the set, ACH evidence {@code evidence-for} / {@code evidence-against} each hypothesis by rating (125);</li>
 *   <li>{@code question:{id}}: the question {@code about} its situation, set, precursor and finding (122);</li>
 *   <li>{@code investigation:{id}}: items {@code member-of} the investigation (101);</li>
 *   <li>{@code finding:{id}}: the finding {@code derived-from} its member objects and its precursor (119).</li>
 * </ul>
 * {@code located-in} is computed on demand ({@link RelationService#locatedIn}), never stored.
 *
 * <p>Incremental: a change-bus subscriber with the durable cursor {@code relation.cursor} (019's consumer clamp, via
 * {@link RelationCursorConsumer}) re-projects the record-backed owners (situations, intel events and hypothesis sets of
 * {@code foresignal-intel}, findings of {@code foresignal-analytics}) whose records changed. The nightly
 * {@link #reconcile()} re-projects every owner, which also covers the structures without records (entity links,
 * facility edges, canonical events, questions, investigations) and repairs relations deleted or changed by hand.
 */
@Component
public class RelationProjector implements ChangeListener, DisposableBean, IngestStateReset {

    /** The component name in relation provenance. */
    public static final String COMPONENT = "relation-projector";

    /** The projector version. */
    public static final String VERSION = "1.0.0";

    /** The change-bus subscriber name. */
    public static final String SUBSCRIBER = "relation-projector";

    /** The owner key of the 104 facility graph. */
    public static final String INFRA_OWNER = "infra:graph";

    /** The seq chunk read per pass. */
    static final long CHUNK = 50_000;

    /** Owner ids read per reconcile page. */
    static final int PAGE = 500;

    private static final Logger log = LoggerFactory.getLogger(RelationProjector.class);

    private static final TypeReference<List<String>> STRINGS = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    /** Owner kinds with their id table, in reconcile order. */
    static final Map<String, String> OWNER_TABLES = ownerTables();

    private final RelationRepository relations;
    private final JdbcTemplate jdbc;
    private final ChangeBus changeBus;
    private final AppSettingService settings;
    private final SourceVisibility visibility;
    private final JsonMapper json;
    private final Clock clock;
    private final TransactionTemplate tx;
    private final boolean autoStart;

    private volatile Subscription subscription;
    private volatile Thread thread;
    private volatile boolean running;
    private volatile long cursor = -1;

    /** Creates the projector. */
    public RelationProjector(RelationRepository relations, JdbcTemplate jdbc, ChangeBus changeBus,
            AppSettingService settings, SourceVisibility visibility, JsonMapper json, Clock clock,
            PlatformTransactionManager transactions,
            @Value("${foresignal.relations.auto-start:${foresignal.analytics.scheduled:true}}") boolean autoStart) {
        this.relations = relations;
        this.jdbc = jdbc;
        this.changeBus = changeBus;
        this.settings = settings;
        this.visibility = visibility;
        this.json = json;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactions);
        this.autoStart = autoStart;
    }

    private static Map<String, String> ownerTables() {
        Map<String, String> out = new LinkedHashMap<>();
        out.put(ObjectTypes.SITUATION, "SELECT id FROM situation WHERE id > ? ORDER BY id LIMIT " + PAGE);
        out.put(ObjectTypes.INTEL_EVENT, "SELECT id FROM intelligence_event WHERE id > ? ORDER BY id LIMIT " + PAGE);
        out.put(ObjectTypes.CANONICAL_EVENT, "SELECT id FROM canonical_event WHERE id > ? ORDER BY id LIMIT " + PAGE);
        out.put(ObjectTypes.ENTITY, "SELECT DISTINCT entity_id FROM entity_link WHERE entity_id > ? ORDER BY entity_id"
                + " LIMIT " + PAGE);
        out.put(ObjectTypes.HYPOTHESIS_SET, "SELECT id FROM hypothesis_set WHERE id > ? ORDER BY id LIMIT " + PAGE);
        out.put(ObjectTypes.QUESTION, "SELECT id FROM forecast_question WHERE id > ? ORDER BY id LIMIT " + PAGE);
        out.put(ObjectTypes.INVESTIGATION, "SELECT id FROM investigation WHERE id > ? ORDER BY id LIMIT " + PAGE);
        out.put(ObjectTypes.FINDING, "SELECT id FROM analytic_finding WHERE id > ? ORDER BY id LIMIT " + PAGE);
        return java.util.Collections.unmodifiableMap(out);
    }

    // ------------------------------------------------------------------------- lifecycle

    /** Starts the subscriber and the catch-up loop once the application is ready (unless disabled). */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.RELATIONS)
    public synchronized void start() {
        if (running || !autoStart) {
            return;
        }
        running = true;
        subscription = changeBus.subscribe(SUBSCRIBER, this, 256);
        thread = Thread.ofVirtual().name("relation-projector").start(this::loop);
    }

    /** Stops the projector. */
    @Override
    public synchronized void destroy() {
        running = false;
        if (subscription != null) {
            subscription.close();
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onBatch(ChangeBatch batch) {
        // The loop reads from the durable cursor.
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        // The loop catches up from the durable cursor.
    }

    /** True while the background loop runs. */
    public boolean running() {
        return running;
    }

    /** The durable cursor. */
    public long cursor() {
        if (cursor < 0) {
            try {
                cursor = settings.get(SettingKeys.RELATION_CURSOR, Long.class).orElse(0L);
            } catch (RuntimeException e) {
                cursor = 0;
            }
        }
        return cursor;
    }

    /** Moves the durable cursor back to {@code seq} (tests, and a restart simulated in place). */
    public synchronized void rewind(long seq) {
        storeCursor(seq);
    }

    @Override
    public synchronized void resetState() {
        cursor = 0;
        settings.remove(SettingKeys.RELATION_CURSOR);
    }

    private void loop() {
        boolean seeded = false;
        while (running) {
            try {
                if (!seeded) {
                    seeded = true;
                    if (relations.countByProjectedTrue() == 0) {
                        reconcile();
                    }
                }
                catchUp();
            } catch (RuntimeException e) {
                log.warn("Relation projector pass failed: {}", e.toString());
            }
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                if (!running) {
                    return;
                }
            }
        }
    }

    // ------------------------------------------------------------------------- incremental

    /**
     * Re-projects the record-backed owners changed between the cursor and the change bus's watermark.
     *
     * @return the owners projected
     */
    public synchronized Set<String> catchUp() {
        long watermark = changeBus.lastPublishedSeq();
        long from = cursor();
        if (from > watermark) {
            from = 0;
            storeCursor(0);
        }
        Set<String> projected = new LinkedHashSet<>();
        while (from < watermark) {
            long to = Math.min(from + CHUNK, watermark);
            Set<String> owners = new LinkedHashSet<>();
            jdbc.query("SELECT DISTINCT kind, external_id FROM record_version WHERE seq > ? AND seq <= ?"
                    + " AND source_id IN ('foresignal-intel', 'foresignal-analytics')", rs -> {
                        String owner = ownerOfRecord(rs.getString(1), rs.getString(2));
                        if (owner != null) {
                            owners.add(owner);
                        }
                    }, from, to);
            for (String owner : owners) {
                try {
                    project(owner);
                    projected.add(owner);
                } catch (RuntimeException e) {
                    log.warn("Relation projection of {} failed: {}", owner, e.toString());
                }
            }
            from = to;
            storeCursor(from);
        }
        return projected;
    }

    /** The owner key of a record of {@code foresignal-intel} or {@code foresignal-analytics}, or null. */
    static String ownerOfRecord(String kind, String externalId) {
        if (kind == null || externalId == null) {
            return null;
        }
        return switch (kind) {
            case "situation" -> ObjectTypes.SITUATION + ":" + externalId;
            case "intelligence-event" -> ObjectTypes.INTEL_EVENT + ":" + externalId;
            case "hypothesis-set" -> ObjectTypes.HYPOTHESIS_SET + ":" + externalId;
            case "analytic-finding" -> ObjectTypes.FINDING + ":" + externalId;
            default -> null;
        };
    }

    private void storeCursor(long seq) {
        cursor = seq;
        settings.put(SettingKeys.RELATION_CURSOR, seq);
    }

    // ------------------------------------------------------------------------- reconcile

    /** The nightly reconcile at 03:40 UTC (when analytics scheduling is on). */
    @Scheduled(cron = "${foresignal.relations.reconcile-cron:0 40 3 * * *}", zone = "UTC")
    public void nightly() {
        if (autoStart) {
            reconcile();
        }
    }

    /**
     * Re-projects every owner, and retires the relations of owners that no longer exist.
     *
     * @return the number of owners projected
     */
    public synchronized int reconcile() {
        int count = 0;
        for (Map.Entry<String, String> kind : OWNER_TABLES.entrySet()) {
            Set<String> visited = new LinkedHashSet<>();
            String after = "";
            while (true) {
                List<String> ids = jdbc.queryForList(kind.getValue(), String.class, after);
                for (String id : ids) {
                    String owner = kind.getKey() + ":" + id;
                    visited.add(owner);
                    try {
                        project(owner);
                        count++;
                    } catch (RuntimeException e) {
                        log.warn("Relation reconcile of {} failed: {}", owner, e.toString());
                    }
                }
                if (ids.size() < PAGE) {
                    break;
                }
                after = ids.get(ids.size() - 1);
            }
            for (String stale : jdbc.queryForList("SELECT DISTINCT owner_key FROM relation WHERE projected"
                    + " AND retired_at IS NULL AND owner_key LIKE ?", String.class, kind.getKey() + ":%")) {
                if (!visited.contains(stale)) {
                    project(stale);
                }
            }
        }
        project(INFRA_OWNER);
        return count + 1;
    }

    // ------------------------------------------------------------------------- projection

    /**
     * One desired relation of an owner.
     *
     * @param from the source reference (unpinned)
     * @param to the target reference (unpinned)
     * @param type the relation type
     * @param validFrom start of validity
     * @param validTo end of validity, or null
     * @param confidence 0–1
     * @param label the label
     * @param privateRelation true when either end is private
     * @param manifestId the owning structure's manifest, or null
     */
    public record Desired(String from, String to, String type, Instant validFrom, Instant validTo, double confidence,
            String label, boolean privateRelation, String manifestId) {

        /** An unknown start of validity becomes the epoch (valid for as long as Foresignal knows). */
        public Desired {
            validFrom = validFrom == null ? Instant.EPOCH : validFrom;
        }
    }

    /**
     * Projects one owner: computes its desired relations and applies the diff.
     *
     * @param owner the owner key ({@code situation:01J…}, {@value #INFRA_OWNER}, …)
     * @return the desired relations applied
     */
    public List<Desired> project(String owner) {
        List<Desired> desired = desired(owner);
        tx.executeWithoutResult(status -> apply(owner, desired));
        return desired;
    }

    /** The relations {@code owner} should have now. */
    List<Desired> desired(String owner) {
        if (INFRA_OWNER.equals(owner)) {
            return infra();
        }
        int colon = owner.indexOf(':');
        String kind = colon < 0 ? owner : owner.substring(0, colon);
        String id = colon < 0 ? "" : owner.substring(colon + 1);
        return switch (kind) {
            case ObjectTypes.SITUATION -> situation(id);
            case ObjectTypes.INTEL_EVENT -> intelEvent(id);
            case ObjectTypes.CANONICAL_EVENT -> canonicalEvent(id);
            case ObjectTypes.ENTITY -> entity(id);
            case ObjectTypes.HYPOTHESIS_SET -> hypothesisSet(id);
            case ObjectTypes.QUESTION -> question(id);
            case ObjectTypes.INVESTIGATION -> investigation(id);
            case ObjectTypes.FINDING -> finding(id);
            default -> List.of();
        };
    }

    /** Applies the diff of one owner (inside a transaction). */
    void apply(String owner, List<Desired> desired) {
        Instant now = clock.instant();
        Map<String, RelationEntity> existing = new LinkedHashMap<>();
        for (RelationEntity r : relations.findByOwnerKey(owner)) {
            existing.put(r.getProjectionKey(), r);
        }
        Set<String> wanted = new LinkedHashSet<>();
        for (Desired d : desired) {
            String key = projectionKey(owner, d);
            if (!wanted.add(key)) {
                continue;
            }
            RelationEntity r = existing.get(key);
            if (r == null) {
                r = new RelationEntity(Ulids.next(now), d.from(), d.to(), d.type(), d.validFrom(), now);
                r.setProjected(true);
                r.setOwnerKey(owner);
                r.setProjectionKey(key);
            } else if (same(r, d)) {
                continue;
            }
            r.setValidFrom(d.validFrom());
            r.setValidTo(d.validTo());
            r.setConfidence(d.confidence());
            r.setLabel(d.label());
            r.setPrivateRelation(d.privateRelation());
            r.setRetiredAt(null);
            Map<String, Object> provenance = new LinkedHashMap<>();
            provenance.put("component", COMPONENT);
            provenance.put("version", VERSION);
            provenance.put("manifestId", d.manifestId());
            r.setProvenance(provenance);
            relations.save(r);
        }
        for (Map.Entry<String, RelationEntity> e : existing.entrySet()) {
            RelationEntity r = e.getValue();
            if (!wanted.contains(e.getKey()) && r.getRetiredAt() == null) {
                r.setRetiredAt(now);
                if (r.getValidTo() == null || r.getValidTo().isAfter(now)) {
                    r.setValidTo(now);
                }
                relations.save(r);
            }
        }
    }

    private static boolean same(RelationEntity r, Desired d) {
        return r.getRetiredAt() == null && java.util.Objects.equals(r.getValidFrom(), d.validFrom())
                && java.util.Objects.equals(r.getValidTo(), d.validTo()) && r.getConfidence() == d.confidence()
                && java.util.Objects.equals(r.getLabel(), d.label()) && r.isPrivateRelation() == d.privateRelation()
                && java.util.Objects.equals(r.getProvenance().get("manifestId"), d.manifestId())
                && VERSION.equals(r.getProvenance().get("version"));
    }

    /** The stable key of a projected relation: SHA-256 of owner, ends and type. */
    static String projectionKey(String owner, Desired d) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (String part : List.of(owner, d.from(), d.to(), d.type())) {
                md.update(part.getBytes(StandardCharsets.UTF_8));
                md.update((byte) 0);
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ------------------------------------------------------------------------- owners

    private List<Desired> situation(String id) {
        List<Desired> out = new ArrayList<>();
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT private, manifest_id, merged_into, created_at"
                + " FROM situation WHERE id = ?", id);
        if (rows.isEmpty() || rows.get(0).get("merged_into") != null) {
            return out;
        }
        boolean priv = Boolean.TRUE.equals(rows.get(0).get("private"));
        String manifest = (String) rows.get(0).get("manifest_id");
        String self = ObjectTypes.SITUATION + ":" + id;
        jdbc.query("SELECT member_type, ref, role, added_by, added_at, private_member FROM situation_member"
                + " WHERE situation_id = ? ORDER BY id", rs -> {
                    String from = unpinned(SituationMemberTypes.objectRef(rs.getString(1), rs.getString(2), null));
                    if (from == null) {
                        return;
                    }
                    boolean core = "core".equals(rs.getString(3));
                    String addedBy = rs.getString(4);
                    boolean operator = addedBy != null && !addedBy.startsWith("linker");
                    String label = operator ? "confirmed" : core ? "likely" : "possible";
                    double confidence = operator ? 1.0 : core ? 0.8 : 0.5;
                    out.add(new Desired(from, self, ObjectTypes.MEMBER_OF, instant(rs, 5), null, confidence, label,
                            priv || rs.getBoolean(6), manifest));
                }, id);
        return out;
    }

    private List<Desired> intelEvent(String id) {
        List<Desired> out = new ArrayList<>();
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT private, manifest_id, merged_into, confidence,"
                + " confidence_label, start_time FROM intelligence_event WHERE id = ?", id);
        if (rows.isEmpty() || rows.get(0).get("merged_into") != null) {
            return out;
        }
        Map<String, Object> e = rows.get(0);
        boolean priv = Boolean.TRUE.equals(e.get("private"));
        String manifest = (String) e.get("manifest_id");
        double confidence = e.get("confidence") instanceof Number n ? clamp(n.doubleValue()) : 0.7;
        String label = e.get("confidence_label") instanceof String l && ObjectTypes.LABELS.contains(l) ? l
                : ObjectTypes.labelFor(confidence);
        Instant start = toInstant(e.get("start_time"));
        String self = ObjectTypes.INTEL_EVENT + ":" + id;
        jdbc.query("SELECT role, member_type, source_id, external_id, observed_at, added_at"
                + " FROM intelligence_event_member WHERE event_id = ? ORDER BY id", rs -> {
                    String from = intelMemberRef(rs.getString(2), rs.getString(3), rs.getString(4));
                    if (from == null) {
                        return;
                    }
                    String type = switch (rs.getString(1) == null ? "supporting" : rs.getString(1)) {
                        case "contradicting" -> ObjectTypes.EVIDENCE_AGAINST;
                        case "context" -> ObjectTypes.MEMBER_OF;
                        default -> ObjectTypes.EVIDENCE_FOR;
                    };
                    Instant validFrom = firstNonNull(instant(rs, 5), instant(rs, 6), start);
                    out.add(new Desired(from, self, type, validFrom, null, confidence, label,
                            priv || isPrivateSource(rs.getString(3)), manifest));
                }, id);
        return out;
    }

    /** The reference of an intel-event member. */
    static String intelMemberRef(String memberType, String sourceId, String externalId) {
        if (externalId == null) {
            return null;
        }
        return switch (memberType == null ? "record" : memberType) {
            case "alert" -> ObjectTypes.ALERT + ":" + externalId;
            case "event" -> ObjectTypes.INTEL_EVENT + ":" + externalId;
            default -> sourceId == null ? null : unpinned(ObjectRef.record(sourceId, externalId, null).format());
        };
    }

    private List<Desired> canonicalEvent(String id) {
        List<Desired> out = new ArrayList<>();
        String self = ObjectTypes.CANONICAL_EVENT + ":" + id;
        List<Map<String, Object>> exists = jdbc.queryForList("SELECT status, merged_into FROM canonical_event"
                + " WHERE id = ?", id);
        if (exists.isEmpty() || exists.get(0).get("merged_into") != null) {
            return out;
        }
        Map<String, String> firstOfGroup = new LinkedHashMap<>();
        jdbc.query("SELECT source_id, external_id, evidence_group_id, link_confidence, linked_at, observed_at"
                + " FROM observation_link WHERE canonical_event_id = ? ORDER BY observed_at NULLS LAST, id", rs -> {
                    String record = unpinned(ObjectRef.record(rs.getString(1), rs.getString(2), null).format());
                    if (record == null) {
                        return;
                    }
                    double confidence = clamp(rs.getDouble(4));
                    boolean priv = isPrivateSource(rs.getString(1));
                    Instant linked = instant(rs, 5);
                    out.add(new Desired(self, record, ObjectTypes.OBSERVED_BY, linked, null, confidence,
                            ObjectTypes.labelFor(confidence), priv, null));
                    String group = rs.getString(3);
                    if (group != null) {
                        String first = firstOfGroup.putIfAbsent(group, record);
                        if (first != null && !first.equals(record)) {
                            out.add(new Desired(record, first, ObjectTypes.SAME_AS, linked, null, confidence,
                                    ObjectTypes.weaker("likely", ObjectTypes.labelFor(confidence)), priv, null));
                        }
                    }
                }, id);
        return out;
    }

    private List<Desired> entity(String id) {
        List<Desired> out = new ArrayList<>();
        String self = ObjectTypes.ENTITY + ":" + id;
        jdbc.query("SELECT source_id, external_id, confidence, valid_from, valid_to FROM entity_link"
                + " WHERE entity_id = ? ORDER BY id", rs -> {
                    String record = unpinned(ObjectRef.record(rs.getString(1), rs.getString(2), null).format());
                    if (record == null) {
                        return;
                    }
                    double confidence = clamp(rs.getDouble(3));
                    Instant from = instant(rs, 4);
                    out.add(new Desired(record, self, ObjectTypes.SAME_AS, from == null ? Instant.EPOCH : from,
                            instant(rs, 5), confidence, ObjectTypes.labelFor(confidence),
                            isPrivateSource(rs.getString(1)), null));
                }, id);
        return out;
    }

    private List<Desired> infra() {
        List<Desired> out = new ArrayList<>();
        List<Long> builds = jdbc.queryForList("SELECT max(build_id) FROM infra_edge", Long.class);
        if (builds.isEmpty() || builds.get(0) == null) {
            return out;
        }
        jdbc.query("SELECT e.relation, e.label, e.score, e.built_at, f.source_id, f.external_id, t.source_id,"
                + " t.external_id FROM infra_edge e JOIN infra_node f ON f.id = e.from_node"
                + " JOIN infra_node t ON t.id = e.to_node WHERE e.build_id = ? ORDER BY e.id", rs -> {
                    String fromRecord = unpinned(ObjectRef.record(rs.getString(5), rs.getString(6), null).format());
                    String toRecord = unpinned(ObjectRef.record(rs.getString(7), rs.getString(8), null).format());
                    if (fromRecord == null || toRecord == null) {
                        return;
                    }
                    boolean upstreamFrom = dependsOnFrom(rs.getString(1));
                    String dependent = upstreamFrom ? toRecord : fromRecord;
                    String dependency = upstreamFrom ? fromRecord : toRecord;
                    String label = ObjectTypes.LABELS.contains(rs.getString(2)) ? rs.getString(2) : "possible";
                    out.add(new Desired(dependent, dependency, ObjectTypes.DEPENDS_ON, instant(rs, 4), null,
                            clamp(rs.getDouble(3)), label,
                            isPrivateSource(rs.getString(5)) || isPrivateSource(rs.getString(7)), null));
                }, builds.get(0));
        return out;
    }

    /**
     * True when a 104 edge's target depends on its source: {@code supplies_power}, {@code transmits_power} and
     * {@code serves} run from the supplier to the dependent; {@code lands_at} and {@code interconnects} run from the
     * dependent (a cable) to what it depends on (a landing point, an exchange).
     */
    static boolean dependsOnFrom(String relation) {
        return relation == null || !(relation.equals("lands_at") || relation.equals("interconnects"));
    }

    private List<Desired> hypothesisSet(String id) {
        List<Desired> out = new ArrayList<>();
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT private, situation_id, created_at, status"
                + " FROM hypothesis_set WHERE id = ?", id);
        if (rows.isEmpty()) {
            return out;
        }
        boolean priv = Boolean.TRUE.equals(rows.get(0).get("private"));
        Instant created = toInstant(rows.get(0).get("created_at"));
        String self = ObjectTypes.HYPOTHESIS_SET + ":" + id;
        if (rows.get(0).get("situation_id") instanceof String situation) {
            out.add(new Desired(self, ObjectTypes.SITUATION + ":" + situation, ObjectTypes.ABOUT, created, null, 1.0,
                    "confirmed", priv, null));
        }
        jdbc.query("SELECT id, created_at FROM hypothesis WHERE set_id = ? ORDER BY position, id",
                rs -> {
                    out.add(new Desired(ObjectTypes.HYPOTHESIS + ":" + rs.getString(1), self, ObjectTypes.MEMBER_OF,
                            instant(rs, 2), null, 1.0, "confirmed", priv, null));
                }, id);
        jdbc.query("SELECT id, created_at FROM indicator WHERE set_id = ? ORDER BY id", rs -> {
            out.add(new Desired(ObjectTypes.INDICATOR + ":" + rs.getString(1), self, ObjectTypes.MEMBER_OF,
                    instant(rs, 2), null, 1.0, "confirmed", priv, null));
        }, id);
        jdbc.query("SELECT source, ref, refs::text, ratings::text, indicator_id, added_at FROM ach_evidence"
                + " WHERE set_id = ? AND removed_at IS NULL ORDER BY id", rs -> {
                    List<String> evidence = new ArrayList<>();
                    String source = rs.getString(1);
                    addIfPresent(evidence, evidenceRef(source, rs.getString(2)));
                    String refs = rs.getString(3);
                    if (refs != null) {
                        for (String r : json.readValue(refs, STRINGS)) {
                            addIfPresent(evidence, evidenceRef(source, r));
                        }
                    }
                    if (rs.getString(5) != null) {
                        addIfPresent(evidence, ObjectTypes.INDICATOR + ":" + rs.getString(5));
                    }
                    String ratings = rs.getString(4);
                    Map<String, Object> rated = ratings == null ? Map.of() : json.readValue(ratings, MAP);
                    Instant added = instant(rs, 6);
                    for (Map.Entry<String, Object> rating : rated.entrySet()) {
                        String type = ratingType(String.valueOf(rating.getValue()));
                        if (type == null) {
                            continue;
                        }
                        String strength = String.valueOf(rating.getValue());
                        boolean strong = "CC".equals(strength) || "II".equals(strength);
                        for (String e : evidence) {
                            out.add(new Desired(e, ObjectTypes.HYPOTHESIS + ":" + rating.getKey(), type, added, null,
                                    strong ? 0.9 : 0.6, strong ? "likely" : "possible", priv, null));
                        }
                    }
                }, id);
        return out;
    }

    /** The relation type of an ACH rating: CC/C → evidence-for, I/II → evidence-against, others none. */
    static String ratingType(String rating) {
        return switch (rating) {
            case "CC", "C" -> ObjectTypes.EVIDENCE_FOR;
            case "I", "II" -> ObjectTypes.EVIDENCE_AGAINST;
            default -> null;
        };
    }

    /** The object reference of an evidence row's ref, by its 125 source, or null when it names no object. */
    static String evidenceRef(String source, String ref) {
        if (ref == null || ref.isBlank()) {
            return null;
        }
        if (ObjectRef.isValid(ref)) {
            return unpinned(ref);
        }
        return switch (source == null ? "" : source) {
            case "record" -> {
                int sep = ref.indexOf('/') > 0 ? ref.indexOf('/') : ref.indexOf(':');
                yield sep <= 0 ? null : unpinned(ObjectRef.record(ref.substring(0, sep), ref.substring(sep + 1), null)
                        .format());
            }
            case "finding" -> ObjectTypes.FINDING + ":" + ref;
            case "intel_event" -> ObjectTypes.INTEL_EVENT + ":" + ref;
            case "alert" -> ObjectTypes.ALERT + ":" + ref;
            case "indicator" -> ObjectTypes.INDICATOR + ":" + ref;
            default -> null;
        };
    }

    private List<Desired> question(String id) {
        List<Desired> out = new ArrayList<>();
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT private, created_at, situation_id,"
                + " hypothesis_set_id, precursor_id, finding_id, manifest_id FROM forecast_question WHERE id = ?", id);
        if (rows.isEmpty()) {
            return out;
        }
        Map<String, Object> q = rows.get(0);
        boolean priv = Boolean.TRUE.equals(q.get("private"));
        Instant created = toInstant(q.get("created_at"));
        String self = ObjectTypes.QUESTION + ":" + id;
        String manifest = (String) q.get("manifest_id");
        Map<String, String> links = new LinkedHashMap<>();
        links.put("situation_id", ObjectTypes.SITUATION);
        links.put("hypothesis_set_id", ObjectTypes.HYPOTHESIS_SET);
        links.put("precursor_id", ObjectTypes.PRECURSOR);
        links.put("finding_id", ObjectTypes.FINDING);
        for (Map.Entry<String, String> link : links.entrySet()) {
            if (q.get(link.getKey()) instanceof String target && !target.isBlank()) {
                out.add(new Desired(self, link.getValue() + ":" + target, ObjectTypes.ABOUT, created, null, 1.0,
                        "confirmed", priv, manifest));
            }
        }
        return out;
    }

    private List<Desired> investigation(String id) {
        List<Desired> out = new ArrayList<>();
        String self = ObjectTypes.INVESTIGATION + ":" + id;
        jdbc.query("SELECT item_type, ref, added_at, private FROM investigation_item WHERE investigation_id = ?"
                + " ORDER BY id", rs -> {
                    String type = rs.getString(1);
                    String ref = rs.getString(2);
                    String from = switch (type == null ? "" : type) {
                        case "event" -> ObjectTypes.INTEL_EVENT + ":" + ref;
                        case "alert" -> ObjectTypes.ALERT + ":" + ref;
                        default -> unpinned(SituationMemberTypes.objectRef("record", ref, null));
                    };
                    if (from != null) {
                        out.add(new Desired(from, self, ObjectTypes.MEMBER_OF, instant(rs, 3), null, 1.0, "confirmed",
                                rs.getBoolean(4), null));
                    }
                }, id);
        return out;
    }

    private List<Desired> finding(String id) {
        List<Desired> out = new ArrayList<>();
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT private, members::text, precursor_id, manifest_id,"
                + " detected_at, status FROM analytic_finding WHERE id = ?", id);
        if (rows.isEmpty()) {
            return out;
        }
        Map<String, Object> f = rows.get(0);
        boolean priv = Boolean.TRUE.equals(f.get("private"));
        String manifest = (String) f.get("manifest_id");
        Instant detected = toInstant(f.get("detected_at"));
        String self = ObjectTypes.FINDING + ":" + id;
        if (f.get("members") instanceof String members) {
            for (String member : json.readValue(members, STRINGS)) {
                String ref = unpinned(member);
                if (ref != null && !ref.equals(self)) {
                    out.add(new Desired(self, ref, ObjectTypes.DERIVED_FROM, detected, null, 1.0, "confirmed", priv,
                            manifest));
                }
            }
        }
        if (f.get("precursor_id") instanceof String precursor && !precursor.isBlank()) {
            out.add(new Desired(self, ObjectTypes.PRECURSOR + ":" + precursor, ObjectTypes.DERIVED_FROM, detected,
                    null, 1.0, "confirmed", priv, manifest));
        }
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    /** {@code ref} without a pinned version, or null when it does not parse. */
    static String unpinned(String ref) {
        try {
            return ObjectRef.parse(ref).withoutSeq().format();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private boolean isPrivateSource(String sourceId) {
        if (sourceId == null) {
            return false;
        }
        try {
            return visibility.isPrivate(sourceId);
        } catch (RuntimeException e) {
            return true;
        }
    }

    private static void addIfPresent(List<String> list, String value) {
        if (value != null && !list.contains(value)) {
            list.add(value);
        }
    }

    private static double clamp(double value) {
        return Double.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0.5;
    }

    private static Instant instant(ResultSet rs, int column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toInstant();
    }

    private static Instant toInstant(Object value) {
        if (value instanceof Timestamp t) {
            return t.toInstant();
        }
        if (value instanceof java.time.OffsetDateTime o) {
            return o.toInstant();
        }
        if (value instanceof Instant i) {
            return i;
        }
        return null;
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        for (T v : values) {
            if (v != null) {
                return v;
            }
        }
        return null;
    }
}
