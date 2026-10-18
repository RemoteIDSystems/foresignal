package ai.foresignal.inbound.edp.binding;

import ai.foresignal.inbound.edp.EdpManifestModel;
import ai.foresignal.inbound.edp.EdpManifestService;
import ai.foresignal.inbound.edp.EdpStats;
import ai.foresignal.inbound.edp.EdpStatusStore;
import ai.foresignal.inbound.edp.ExternalSourceEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * The read-scope summaries of external providers for the MCP tools {@code list_external_sources} and
 * {@code get_external_source} (prompt 138): identity, state, access route, originator, license redistribution and
 * each stream's mode, item type, kind, counts and bindings. Unlike the admin {@code ExternalSource} view it carries
 * no manifest, pull cursor, error text or credentials, and private providers the caller may not see are left out.
 */
@Component
public class ExternalSourceSummaries {

    private final EdpManifestService manifests;
    private final EdpStatusStore status;
    private final ExternalBindingRepository bindings;

    /** Creates the component. */
    public ExternalSourceSummaries(EdpManifestService manifests, EdpStatusStore status,
            ExternalBindingRepository bindings) {
        this.manifests = manifests;
        this.status = status;
        this.bindings = bindings;
    }

    /** The summaries of every provider (optionally in one state) the caller may see, by id. */
    public List<Map<String, Object>> list(String state, Set<String> excludedSources) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ExternalSourceEntity ext : manifests.list(state)) {
            if (excludedSources != null && excludedSources.contains(ext.getSourceId())) {
                continue;
            }
            out.add(summary(ext, false));
        }
        return out;
    }

    /**
     * The summary of one provider with its streams' suggested bindings, or null when it is unknown or hidden from the
     * caller.
     */
    public Map<String, Object> get(String sourceId, Set<String> excludedSources) {
        if (excludedSources != null && excludedSources.contains(sourceId)) {
            return null;
        }
        try {
            return summary(manifests.require(sourceId), true);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Map<String, Object> summary(ExternalSourceEntity ext, boolean detail) {
        EdpManifestModel m = manifests.model(ext);
        EdpStats stats = status.read(ext);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", ext.getSourceId());
        out.put("name", m.name());
        out.put("description", m.description());
        out.put("publisher", m.publisherName());
        out.put("state", ext.getState());
        out.put("accessRoute", m.accessRoute());
        out.put("fragility", m.accessFragility());
        out.put("originator", m.originatorId());
        out.put("originatorClass", m.originatorClass());
        out.put("aggregates", m.aggregates());
        out.put("redistribution", m.redistribution());
        out.put("private", m.privateSource());
        out.put("lastItemAt", ext.getLastItemAt());
        List<Map<String, Object>> streams = new ArrayList<>();
        for (EdpManifestModel.Stream s : m.streams()) {
            EdpStats.StreamStats st = stats.streams().get(s.id());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("streamId", s.id());
            entry.put("title", s.title());
            entry.put("mode", s.mode());
            entry.put("itemType", s.itemType());
            entry.put("kind", s.kind());
            entry.put("items", st == null ? 0 : st.items());
            entry.put("lastItemAt", st == null ? null : st.lastItemAt());
            entry.put("bindings", bindings.countBySourceIdAndStreamId(ext.getSourceId(), s.id()));
            if (detail) {
                entry.put("suggestedBindings", new BoundStream(ext.getSourceId(), ext.getManifestSha256(), m, s)
                        .suggestedBindings().stream().map(sb -> {
                            Map<String, Object> view = new LinkedHashMap<>();
                            view.put("target", sb.get("target"));
                            view.put("config", sb.get("config") == null ? null : sb.get("config").toString());
                            return view;
                        }).toList());
            }
            streams.add(entry);
        }
        out.put("streams", streams);
        return out;
    }
}
