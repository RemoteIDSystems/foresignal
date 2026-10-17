package ai.foresignal.evidence;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.reference.RegionTags;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.Subscription;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The evidence resolver (prompt 116, version {@value EvidenceMatcher#RESOLVER_VERSION}): links event-class
 * observations to canonical events and collapses them into evidence groups.
 *
 * <p>A {@link ChangeBus} subscriber with the durable cursor {@code evidence.cursor} (a retention consumer, so
 * version retention never outruns it). It catches up in chunks of {@value #CHUNK} seqs on its own virtual
 * thread, reading the versions of the kinds {@code intel/canonical-rules.json} lists. For each version:
 *
 * <ol>
 *   <li>A record already linked keeps its link: a newer version updates the stored facts (and is flagged
 *       {@code LATE_REVISION} when its event time or location moved); a version at or below the stored seq is
 *       skipped, so a re-read range is processed exactly once.
 *   <li>Otherwise it is matched ({@link EvidenceMatcher}) against the open events of its family that share an
 *       upstream id or lie within the family's time and distance window. Links below
 *       {@code evidence.minLinkConfidence} (0.5) are not made.
 *   <li>No match creates a canonical event; one match links; matches to several events merge them into the
 *       oldest first (never across families, logged), then link.
 *   <li>The event's evidence groups ({@link EvidenceGrouper}), counts and quality flags ({@link QualityFlags})
 *       are recomputed, and {@code canonical.created|merged|flagged} goes to the {@code evidence} SSE topic.
 * </ol>
 *
 * <p>Observations are never modified: the resolver reads {@code record_version} and writes only its own
 * tables (MEM-1).
 */
@Component
public class EvidenceResolver implements ChangeListener, DisposableBean, ai.foresignal.ingest.IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(EvidenceResolver.class);

    /** The change-bus subscriber name. */
    public static final String SUBSCRIBER = "evidence-resolver";

    /** The seq chunk processed per pass. */
    static final long CHUNK = 10_000;

    /** The smallest event-time or location change that counts as a revision. */
    static final Duration REVISION_TIME = Duration.ofSeconds(1);

    /** The smallest location change that counts as a revision, km. */
    static final double REVISION_KM = 0.5;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final ChangeBus changeBus;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final AppSettingService settings;
    private final CanonicalRules rules;
    private final OriginatorRegistry originators;
    private final EvidenceStore store;
    private final EvidenceEventBus bus;
    private final ObjectProvider<RegionTagger> regions;
    private final TransactionTemplate transactions;
    private final Clock clock;
    private final boolean autoStart;

    private volatile Subscription subscription;
    private volatile Thread thread;
    private volatile boolean running;
    private volatile long cursor = -1;

    /** Creates the resolver. */
    public EvidenceResolver(ChangeBus changeBus, JdbcTemplate jdbc, JsonMapper json, AppSettingService settings,
            CanonicalRules rules, OriginatorRegistry originators, EvidenceStore store, EvidenceEventBus bus,
            ObjectProvider<RegionTagger> regions, TransactionTemplate transactions, Clock clock,
            @Value("${foresignal.evidence.auto-start:true}") boolean autoStart) {
        this.changeBus = changeBus;
        this.jdbc = jdbc;
        this.json = json;
        this.settings = settings;
        this.rules = rules;
        this.originators = originators;
        this.store = store;
        this.bus = bus;
        this.regions = regions;
        this.transactions = transactions;
        this.clock = clock;
        this.autoStart = autoStart;
    }

    /** Starts the subscriber and the catch-up thread once the application is ready (unless disabled). */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.INTEL)
    public synchronized void start() {
        if (running || !autoStart) {
            return;
        }
        running = true;
        subscription = changeBus.subscribe(SUBSCRIBER, this, 1024);
        thread = Thread.ofVirtual().name("evidence-resolver").start(this::loop);
    }

    /** Stops the resolver. */
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
        // The catch-up loop advances the cursor.
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
            cursor = settings.get(SettingKeys.EVIDENCE_CURSOR, Long.class).orElse(0L);
        }
        return cursor;
    }

    /** Drops the cached cursor so it is read again from {@code app_setting} (after the tables were emptied). */
    @Override
    public void resetState() {
        cursor = -1;
    }

    /** Moves the durable cursor back to {@code seq} (tests, and a restart simulated in place). */
    public synchronized void rewind(long seq) {
        storeCursor(seq);
    }

    private void loop() {
        while (running) {
            try {
                catchUp();
            } catch (RuntimeException e) {
                log.warn("Evidence catch-up failed: {}", e.toString());
            }
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                if (!running) {
                    return;
                }
            }
        }
    }

    /** Processes every version between the cursor and the published watermark. */
    public synchronized void catchUp() {
        long watermark = changeBus.lastPublishedSeq();
        long from = cursor();
        List<String> kinds = new ArrayList<>(rules.kinds());
        while (from < watermark) {
            long to = Math.min(from + CHUNK, watermark);
            for (VersionRow row : versions(kinds, from, to)) {
                try {
                    transactions.executeWithoutResult(status -> process(row));
                } catch (RuntimeException e) {
                    log.warn("Evidence resolution failed for {}/{}@{}: {}", row.sourceId(), row.externalId(),
                            row.seq(), e.toString());
                }
            }
            from = to;
            storeCursor(from);
        }
    }

    // ------------------------------------------------------------------------- one version

    /** Resolves one version. */
    void process(VersionRow row) {
        CanonicalRules.FamilyRule rule = rules.familyOf(row.kind(), row.payload());
        if (rule == null) {
            return;
        }
        Instant now = clock.instant();
        Originator originator = originators.resolve(row.sourceId());
        ObservationFacts facts = FactsExtractor.extract(rule, originators.upstreamRefs(row.sourceId()), rules,
                row.sourceId(), row.externalId(), row.seq(), row.version(), row.eventTime(), row.observedAt(),
                row.lon(), row.lat(), row.payload());
        List<EvidenceStore.StoredLink> existing = store.linksOf(row.sourceId(), row.externalId());
        if (!existing.isEmpty()) {
            EvidenceStore.StoredLink link = existing.get(0);
            if (link.facts().seq() >= row.seq()) {
                return; // already processed: exactly once across rewinds and restarts
            }
            boolean revised = link.revised() || revised(link.facts(), facts);
            store.updateLink(link.id(), facts, originator, revised, now);
            refresh(link.canonicalEventId(), rule, now, false, List.of());
            return;
        }
        Map<String, EvidenceMatcher.Match> matches = matches(rule, facts);
        double minimum = minLinkConfidence();
        matches.values().removeIf(m -> m.confidence() < minimum);
        if (matches.isEmpty()) {
            String id = store.insertEvent(rule.family(), facts, regionCodes(facts), now);
            Map<String, Object> reason = new LinkedHashMap<>();
            reason.put("rule", "first-observation");
            reason.put("dtSeconds", 0.0);
            reason.put("distanceKm", 0.0);
            reason.put("attributeDeltas", Map.of());
            reason.put("upstreamIdMatch", false);
            store.insertLink(id, facts, originator, 1.0, reason, now);
            refresh(id, rule, now, true, List.of());
            return;
        }
        List<String> targets = new ArrayList<>(matches.keySet());
        String survivor = oldest(targets);
        List<String> merged = new ArrayList<>();
        for (String other : targets) {
            if (!other.equals(survivor)) {
                log.info("Merging canonical event {} into {} ({}): observation {} matched both", other, survivor,
                        rule.family(), facts.ref());
                store.merge(other, survivor, now);
                merged.add(other);
            }
        }
        EvidenceMatcher.Match best = matches.values().stream()
                .max(Comparator.comparingDouble(EvidenceMatcher.Match::confidence)).orElseThrow();
        EvidenceMatcher.Match chosen = matches.getOrDefault(survivor, best);
        store.insertLink(survivor, facts, originator, chosen.confidence(), chosen.reason(), now);
        refresh(survivor, rule, now, false, merged);
    }

    /** The best match of {@code facts} against each candidate event, keyed by event id. */
    Map<String, EvidenceMatcher.Match> matches(CanonicalRules.FamilyRule rule, ObservationFacts facts) {
        Set<String> candidates = new LinkedHashSet<>(store.byUpstream(rule.family(), facts.upstreamIds()));
        Instant at = facts.eventTime() != null ? facts.eventTime() : facts.observedAt();
        if (at != null && !"identifier".equals(rule.match())) {
            Duration window = window(rule);
            candidates.addAll(store.candidates(rule.family(), at.minus(window), at.plus(window), box(rule, facts)));
        }
        Map<String, EvidenceMatcher.Match> out = new LinkedHashMap<>();
        for (String eventId : candidates) {
            EvidenceMatcher.Match best = null;
            for (EvidenceStore.StoredLink link : store.links(eventId)) {
                Optional<EvidenceMatcher.Match> match = EvidenceMatcher.match(rule, facts, link.facts());
                if (match.isPresent() && (best == null || match.get().confidence() > best.confidence())) {
                    best = match.get();
                }
            }
            if (best != null) {
                out.put(eventId, best);
            }
        }
        return out;
    }

    /** Recomputes an event's groups, counts and flags and publishes what changed. */
    private void refresh(String eventId, CanonicalRules.FamilyRule rule, Instant now, boolean created,
            List<String> mergedFrom) {
        List<String> before = store.event(eventId).map(EvidenceStore.StoredEvent::qualityFlags).orElse(List.of());
        List<EvidenceStore.StoredLink> links = store.links(eventId);
        List<EvidenceGrouper.Member> members = new ArrayList<>();
        Set<String> sources = new TreeSet<>();
        for (EvidenceStore.StoredLink link : links) {
            Originator originator = originators.originator(link.originatorId())
                    .orElseGet(() -> originators.resolve(link.facts().sourceId()));
            members.add(new EvidenceGrouper.Member(link.id(), link.facts().sourceId(), originator,
                    link.facts().citation(), link.facts().seq()));
            sources.add(link.facts().sourceId());
        }
        List<EvidenceGrouper.Group> groups = EvidenceGrouper.group(members, originators);
        store.writeGroups(eventId, groups, now);
        Map<String, String> groupOfLink = new LinkedHashMap<>();
        for (EvidenceGrouper.Group group : groups) {
            for (String member : group.members()) {
                groupOfLink.put(member, group.root());
            }
        }
        List<QualityFlags.Member> flagMembers = new ArrayList<>();
        for (EvidenceStore.StoredLink link : links) {
            flagMembers.add(new QualityFlags.Member(groupOfLink.get(link.id()), link.facts().eventTime(),
                    link.facts().observedAt(), link.revised()));
        }
        List<String> flags = QualityFlags.compute(rule, flagMembers);
        store.updateSummary(eventId, links.size(), groups.size(), flags, new ArrayList<>(sources), now);
        EvidenceStore.StoredEvent event = store.event(eventId).orElse(null);
        if (event == null) {
            return;
        }
        if (created) {
            bus.publish(EvidenceEvent.of(EvidenceEvent.CREATED, event, List.of(), List.of()));
            return;
        }
        if (!mergedFrom.isEmpty()) {
            bus.publish(EvidenceEvent.of(EvidenceEvent.MERGED, event, List.of(), mergedFrom));
        }
        List<String> added = new ArrayList<>(flags);
        added.removeAll(before);
        if (!added.isEmpty()) {
            bus.publish(EvidenceEvent.of(EvidenceEvent.FLAGGED, event, added, List.of()));
        }
    }

    /** True when a newer version moved the event time or location of an observation. */
    static boolean revised(ObservationFacts before, ObservationFacts after) {
        if (before.eventTime() != null && after.eventTime() != null
                && Duration.between(before.eventTime(), after.eventTime()).abs().compareTo(REVISION_TIME) >= 0) {
            return true;
        }
        return before.located() && after.located()
                && Geo.haversineKm(before.lat(), before.lon(), after.lat(), after.lon()) >= REVISION_KM;
    }

    /** The candidate time window of a family: its tolerance, a day for cell-day, three for outages. */
    static Duration window(CanonicalRules.FamilyRule rule) {
        return switch (rule.match()) {
            case "cell-day" -> Duration.ofDays(1);
            case "outage" -> Duration.ofDays(3);
            default -> Duration.ofSeconds((long) Math.max(60, rule.maxDtSeconds()));
        };
    }

    /** The candidate box {@code [minLon, minLat, maxLon, maxLat]} of a located observation, or null. */
    static double[] box(CanonicalRules.FamilyRule rule, ObservationFacts facts) {
        if (!facts.located()) {
            return null;
        }
        double km = switch (rule.match()) {
            case "cell-day" -> 20;
            case "proximity", "hazard" -> rule.maxKm();
            default -> 0;
        };
        if (km <= 0) {
            return null;
        }
        double dLat = km / 111.0 + 0.01;
        double dLon = km / (111.0 * Math.max(0.05, Math.cos(Math.toRadians(facts.lat())))) + 0.01;
        return new double[] {facts.lon() - dLon, facts.lat() - dLat, facts.lon() + dLon, facts.lat() + dLat};
    }

    private String oldest(List<String> ids) {
        List<EvidenceStore.StoredEvent> events = new ArrayList<>(store.events(ids));
        events.sort(Comparator.comparing(EvidenceStore.StoredEvent::createdAt).thenComparing(EvidenceStore.StoredEvent::id));
        return events.isEmpty() ? ids.get(0) : events.get(0).id();
    }

    private List<String> regionCodes(ObservationFacts facts) {
        List<String> out = new ArrayList<>();
        RegionTagger tagger = regions.getIfAvailable();
        if (tagger == null || !facts.located()) {
            return out;
        }
        try {
            RegionTags tags = tagger.tag(facts.lon(), facts.lat());
            if (tags.iso2() != null) {
                out.add(tags.iso2());
            }
            if (tags.admin1() != null && !out.contains(tags.admin1())) {
                out.add(tags.admin1());
            }
        } catch (RuntimeException e) {
            // no region tags; the event is still linked
        }
        return out;
    }

    private double minLinkConfidence() {
        try {
            return settings.get(SettingKeys.EVIDENCE_MIN_LINK_CONFIDENCE, Double.class).orElse(rules.minLinkConfidence());
        } catch (RuntimeException e) {
            return rules.minLinkConfidence();
        }
    }

    private void storeCursor(long seq) {
        cursor = seq;
        settings.put(SettingKeys.EVIDENCE_CURSOR, seq);
    }

    // ------------------------------------------------------------------------- reads

    /**
     * One version as the resolver reads it.
     *
     * @param seq the seq
     * @param sourceId the source
     * @param externalId the record
     * @param kind the kind
     * @param version the record version
     * @param eventTime {@code event_time}, else {@code observed_at}
     * @param observedAt {@code observed_at}
     * @param lon the representative longitude, or null
     * @param lat the representative latitude, or null
     * @param payload the payload
     */
    record VersionRow(long seq, String sourceId, String externalId, String kind, Integer version, Instant eventTime,
            Instant observedAt, Double lon, Double lat, Map<String, Object> payload) {}

    private List<VersionRow> versions(List<String> kinds, long from, long to) {
        if (kinds.isEmpty()) {
            return List.of();
        }
        return jdbc.query("SELECT seq, source_id, external_id, kind, version, event_time, observed_at,"
                        + " ST_X(ST_PointOnSurface(geom)) AS lon, ST_Y(ST_PointOnSurface(geom)) AS lat,"
                        + " payload::text AS payload FROM record_version WHERE seq > ? AND seq <= ?"
                        + " AND tombstone = false AND persist_reason <> 8 AND kind = ANY(?) ORDER BY seq",
                (rs, i) -> {
                    OffsetDateTime observed = rs.getObject("observed_at", OffsetDateTime.class);
                    OffsetDateTime event = rs.getObject("event_time", OffsetDateTime.class);
                    String text = rs.getString("payload");
                    Instant observedAt = observed == null ? null : observed.toInstant();
                    return new VersionRow(rs.getLong("seq"), rs.getString("source_id"), rs.getString("external_id"),
                            rs.getString("kind"), (Integer) rs.getObject("version"),
                            event == null ? observedAt : event.toInstant(), observedAt, (Double) rs.getObject("lon"),
                            (Double) rs.getObject("lat"), text == null ? Map.of() : json.readValue(text, MAP));
                }, from, to, kinds.toArray(String[]::new));
    }
}
