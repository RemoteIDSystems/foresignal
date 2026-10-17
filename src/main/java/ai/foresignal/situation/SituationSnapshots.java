package ai.foresignal.situation;

import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.stream.ChangeBus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Daily situation snapshots and "what changed since" (prompt 118 §4). {@link #snapshot(LocalDate)} writes one
 * {@code situation_snapshot} per non-closed situation for a UTC day, once (an existing row is kept); the
 * {@link SituationScheduler} calls it at 00:10 UTC for the previous day. {@link #changes} diffs the snapshot of the
 * UTC day of {@code since} (the newest one on or before it when that day has none) against the current state:
 * status, severity, new members by family and type, area change and regions added or removed. No LLM is involved.
 */
@Component
public class SituationSnapshots {

    private static final Logger log = LoggerFactory.getLogger(SituationSnapshots.class);

    /** The component name of snapshot manifests. */
    public static final String COMPONENT = "situation-snapshot";

    /** The snapshot version. */
    public static final String VERSION = "1.0.0";

    private final SituationRepository situations;
    private final SituationMemberRepository members;
    private final SituationSnapshotRepository snapshots;
    private final ObjectProvider<ChangeBus> changeBus;
    private final ObjectProvider<ManifestService> manifests;
    private final Clock clock;

    /** Creates the component. */
    public SituationSnapshots(SituationRepository situations, SituationMemberRepository members,
            SituationSnapshotRepository snapshots, ObjectProvider<ChangeBus> changeBus,
            ObjectProvider<ManifestService> manifests, Clock clock) {
        this.situations = situations;
        this.members = members;
        this.snapshots = snapshots;
        this.changeBus = changeBus;
        this.manifests = manifests;
        this.clock = clock;
    }

    /**
     * Writes the snapshot of every non-closed situation for {@code day}, skipping those that already have one.
     *
     * @return the number of snapshots written
     */
    @Transactional
    public int snapshot(LocalDate day) {
        Instant dayStart = day.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant dayEnd = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        ChangeBus bus = changeBus.getIfAvailable();
        long asOf = bus == null ? 0 : bus.lastPublishedSeq();
        int written = 0;
        List<String> ids = new ArrayList<>();
        for (SituationEntity situation : situations.findByStatusInOrderByIdAsc(SituationLifecycle.OPEN)) {
            if (situation.getCreatedAt() != null && !situation.getCreatedAt().isBefore(dayEnd)) {
                continue;
            }
            if (snapshots.findBySituationIdAndDay(situation.getId(), day).isPresent()) {
                continue;
            }
            List<SituationMemberEntity> current = members.findBySituationIdOrderByAddedAtAscIdAsc(situation.getId());
            SituationSnapshotEntity snapshot = new SituationSnapshotEntity(situation.getId(), day);
            snapshot.setStatus(situation.getStatus());
            snapshot.setSeverity(situation.getSeverity());
            snapshot.setMemberCounts(SituationDeriver.memberCounts(current));
            int added = 0;
            for (SituationMemberEntity member : current) {
                Instant at = member.getAddedAt();
                if (at != null && !at.isBefore(dayStart) && at.isBefore(dayEnd)) {
                    added++;
                }
            }
            snapshot.setNewMembers(added);
            snapshot.setAreaKm2(situation.getAreaKm2());
            snapshot.setRegionCodes(situation.getRegionCodes());
            snapshot.setKeyValues(Map.of());
            snapshot.setAsofSeq(asOf);
            snapshot.setCreatedAt(clock.instant());
            snapshots.save(snapshot);
            ids.add(situation.getId());
            written++;
        }
        if (!ids.isEmpty()) {
            String manifestId = manifest(day, asOf, ids);
            if (manifestId != null) {
                for (String id : ids) {
                    snapshots.findBySituationIdAndDay(id, day).ifPresent(s -> {
                        s.setManifestId(manifestId);
                        snapshots.save(s);
                    });
                }
            }
        }
        log.info("Wrote {} situation snapshots for {}", written, day);
        return written;
    }

    private String manifest(LocalDate day, long asOf, List<String> ids) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("day", day.toString());
            config.put("summaryTemplate", SummaryTemplate.VERSION);
            Map<String, Object> query = new LinkedHashMap<>();
            query.put("table", "situation");
            query.put("situations", ids.size());
            return service.record(ManifestRequest.of(COMPONENT, VERSION).config(config).asOf(asOf).query(query)
                    .inputs(ids.stream().map(id -> "situation:" + id).toList()));
        } catch (RuntimeException e) {
            log.warn("Could not record the snapshot manifest of {}: {}", day, e.toString());
            return null;
        }
    }

    /**
     * The {@code SituationChanges} between the snapshot of the UTC day of {@code since} and now.
     *
     * @param situation the situation (visibility already checked)
     * @param since the requested time
     */
    public Map<String, Object> changes(SituationEntity situation, Instant since) {
        Instant now = clock.instant();
        LocalDate day = since.atZone(ZoneOffset.UTC).toLocalDate();
        SituationSnapshotEntity baseline = snapshots
                .findTop1BySituationIdAndDayLessThanEqualOrderByDayDesc(situation.getId(), day).orElse(null);
        boolean startedAfter = situation.getCreatedAt() != null && situation.getCreatedAt().isAfter(since);
        if (startedAfter) {
            baseline = null;
        }
        Instant cut = baseline == null ? since : baseline.getDay().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Map<String, Integer> byFamily = new TreeMap<>();
        Map<String, Integer> byType = new TreeMap<>();
        int total = 0;
        for (SituationMemberEntity member : members.findBySituationIdOrderByAddedAtAscIdAsc(situation.getId())) {
            if (member.getAddedAt() != null && !member.getAddedAt().isBefore(cut)) {
                total++;
                byFamily.merge(member.getSignalFamily() == null ? "unknown" : member.getSignalFamily(), 1, Integer::sum);
                byType.merge(member.getMemberType(), 1, Integer::sum);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("situationId", situation.getId());
        out.put("since", since.toString());
        out.put("baselineDay", baseline == null ? null : baseline.getDay().toString());
        out.put("now", now.toString());
        out.put("status", pair(baseline == null ? null : baseline.getStatus(), situation.getStatus()));
        out.put("severity", pair(baseline == null ? null : baseline.getSeverity(), situation.getSeverity()));
        Map<String, Object> newMembers = new LinkedHashMap<>();
        newMembers.put("total", total);
        newMembers.put("byFamily", byFamily);
        newMembers.put("byType", byType);
        out.put("newMembers", newMembers);
        Double before = baseline == null ? null : baseline.getAreaKm2();
        Double after = situation.getAreaKm2();
        Map<String, Object> area = new LinkedHashMap<>();
        area.put("beforeKm2", before);
        area.put("afterKm2", after);
        area.put("deltaKm2", before == null || after == null ? null : Math.round((after - before) * 10) / 10.0);
        out.put("area", area);
        Set<String> was = baseline == null ? Set.of() : new TreeSet<>(baseline.getRegionCodes());
        Set<String> is = new TreeSet<>(situation.getRegionCodes());
        Set<String> added = new TreeSet<>(is);
        added.removeAll(was);
        Set<String> removed = new TreeSet<>(was);
        removed.removeAll(is);
        out.put("regionsAdded", new ArrayList<>(added));
        out.put("regionsRemoved", new ArrayList<>(removed));
        out.put("keyValues", List.of());
        out.put("manifestId", baseline == null ? null : baseline.getManifestId());
        return out;
    }

    private static Map<String, Object> pair(String before, String after) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("before", before);
        out.put("after", after);
        out.put("changed", before == null || !before.equals(after));
        return out;
    }
}
