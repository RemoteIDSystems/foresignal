package ai.foresignal.situation;

import ai.foresignal.api.error.ValidationException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

/**
 * The read views of situations shared by the REST controller and the MCP tools (prompt 118): the full
 * {@code Situation} (evidence first, then inference, then labeled speculation), the {@code SituationPage}, the
 * member page and the timeline page.
 */
@Component
public class SituationViews {

    private final SituationService service;
    private final CorroborationRollup rollup;
    private final Clock clock;

    /** Creates the component. */
    public SituationViews(SituationService service, CorroborationRollup rollup, Clock clock) {
        this.service = service;
        this.rollup = rollup;
        this.clock = clock;
    }

    /** The full {@code Situation}. */
    public Map<String, Object> full(SituationEntity situation) {
        List<SituationMemberEntity> members = service.members(situation.getId());
        CorroborationRollup.Result result = rollup.rollUp(members);
        Set<String> families = new TreeSet<>();
        for (SituationMemberEntity member : members) {
            if (member.isCore() && member.getSignalFamily() != null) {
                families.add(member.getSignalFamily());
            }
        }
        return SituationMapper.full(situation, members, result, families.size(), List.of(), clock.instant());
    }

    /** The {@code SituationPage} of a list result. */
    public Map<String, Object> page(SituationService.Page page) {
        Instant now = clock.instant();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(s -> SituationMapper.summary(s, now)).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /**
     * The {@code SituationMemberPage}: most recently observed first, filtered by type and role.
     *
     * @param types member types, empty for all
     * @param role core or context, or null
     */
    public Map<String, Object> members(String situationId, List<String> types, String role, String cursor, int limit) {
        for (String type : types) {
            if (!SituationMemberTypes.ALL.contains(type)) {
                throw ValidationException.forField("type", "must be one of " + SituationMemberTypes.ALL);
            }
        }
        if (role != null && !SituationMemberTypes.ROLES.contains(role)) {
            throw ValidationException.forField("role", "must be core or context");
        }
        List<SituationMemberEntity> all = new ArrayList<>();
        for (SituationMemberEntity member : service.members(situationId)) {
            if ((types.isEmpty() || types.contains(member.getMemberType()))
                    && (role == null || role.equals(member.getRole()))) {
                all.add(member);
            }
        }
        all.sort(SituationMapper.recentFirst());
        int offset = SituationService.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = offset; i < all.size() && items.size() < limit; i++) {
            items.add(SituationMapper.member(all.get(i)));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + items.size() < all.size() ? SituationService.offsetCursor(offset + items.size())
                : null);
        return out;
    }

    /** The {@code SituationTimelinePage}: members and log entries between {@code from} and {@code to}, in time order. */
    public Map<String, Object> timeline(String situationId, Instant from, Instant to, String cursor, int limit) {
        if (from != null && to != null && to.isBefore(from)) {
            throw ValidationException.forField("to", "must not be before from");
        }
        List<Entry> entries = new ArrayList<>();
        for (SituationMemberEntity member : service.members(situationId)) {
            Instant at = member.getObservedAt() == null ? member.getAddedAt() : member.getObservedAt();
            if (within(at, from, to)) {
                entries.add(new Entry(at, 0, member.getId(), SituationMapper.timeline(member)));
            }
        }
        for (SituationLogEntity log : service.log(situationId)) {
            if (within(log.getAt(), from, to)) {
                entries.add(new Entry(log.getAt(), 1, String.format("%020d", log.getId()), SituationMapper.timeline(log)));
            }
        }
        entries.sort(Comparator.comparing(Entry::at).thenComparingInt(Entry::order).thenComparing(Entry::key));
        int offset = SituationService.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = offset; i < entries.size() && items.size() < limit; i++) {
            items.add(entries.get(i).view());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + items.size() < entries.size()
                ? SituationService.offsetCursor(offset + items.size()) : null);
        return out;
    }

    private record Entry(Instant at, int order, String key, Map<String, Object> view) {}

    private static boolean within(Instant at, Instant from, Instant to) {
        if (at == null) {
            return from == null && to == null;
        }
        return (from == null || !at.isBefore(from)) && (to == null || !at.isAfter(to));
    }
}
