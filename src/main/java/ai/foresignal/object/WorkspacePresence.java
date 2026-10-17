package ai.foresignal.object;

import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Workspace presence (prompt 129): every two seconds, compares the streams following each workspace with the previous
 * pass and publishes {@code workspace.presence} for each client that joined or left, with the full list of connected
 * clients, so several apps or monitors can follow one investigation.
 */
@Component
public class WorkspacePresence {

    private final WorkspaceTopic topic;
    private final Clock clock;
    private Map<String, Map<String, WorkspaceFilter>> previous = new HashMap<>();

    /** Creates the component. */
    public WorkspacePresence(WorkspaceTopic topic, Clock clock) {
        this.topic = topic;
        this.clock = clock;
    }

    /** One pass (scheduled; also called by tests). */
    @Scheduled(fixedDelay = 2_000, initialDelay = 2_000)
    public synchronized void refresh() {
        SseHub hub = topic.hub();
        if (hub == null) {
            return;
        }
        Map<String, Map<String, WorkspaceFilter>> current = connected(hub);
        Set<String> workspaces = new LinkedHashSet<>(previous.keySet());
        workspaces.addAll(current.keySet());
        for (String ws : workspaces) {
            Map<String, WorkspaceFilter> before = previous.getOrDefault(ws, Map.of());
            Map<String, WorkspaceFilter> now = current.getOrDefault(ws, Map.of());
            for (String clientId : now.keySet()) {
                if (!before.containsKey(clientId)) {
                    topic.publish(ws, WorkspaceTopic.PRESENCE_EVENT, payload(ws, now, "joined", clientId));
                }
            }
            for (String clientId : before.keySet()) {
                if (!now.containsKey(clientId)) {
                    topic.publish(ws, WorkspaceTopic.PRESENCE_EVENT, payload(ws, now, "left", clientId));
                }
            }
        }
        previous = current;
    }

    /** The clients following {@code workspaceId} now. */
    public List<Map<String, Object>> clients(String workspaceId) {
        SseHub hub = topic.hub();
        if (hub == null) {
            return List.of();
        }
        return clientViews(connected(hub).getOrDefault(workspaceId, Map.of()));
    }

    private static Map<String, Map<String, WorkspaceFilter>> connected(SseHub hub) {
        Map<String, Map<String, WorkspaceFilter>> out = new HashMap<>();
        for (SseClient client : hub.clients()) {
            if (client.isClosed()) {
                continue;
            }
            if (client.filter().filterFor(WorkspaceTopic.NAME) instanceof WorkspaceFilter f) {
                out.computeIfAbsent(f.workspaceId(), k -> new LinkedHashMap<>()).put(f.clientId(), f);
            }
        }
        return out;
    }

    /** The {@code PresenceEvent} payload. */
    Map<String, Object> payload(String workspaceId, Map<String, WorkspaceFilter> now, String change,
            String clientId) {
        return WorkspaceTopic.presencePayload(workspaceId, sorted(now), change, clientId, clock.instant());
    }

    private static List<WorkspaceFilter> sorted(Map<String, WorkspaceFilter> filters) {
        List<WorkspaceFilter> sorted = new ArrayList<>(filters.values());
        sorted.sort(Comparator.comparing(WorkspaceFilter::connectedAt).thenComparing(WorkspaceFilter::clientId));
        return sorted;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> clientViews(Map<String, WorkspaceFilter> filters) {
        return (List<Map<String, Object>>) WorkspaceTopic.presencePayload("", sorted(filters), null, null,
                clock.instant()).get("clients");
    }
}
