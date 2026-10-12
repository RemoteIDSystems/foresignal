package ai.foresignal.mcp.tools;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.RecordRow;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

/**
 * Shared record handling of the record tools: source-filter checks and record items in their short
 * or long form.
 *
 * <p>Citations use persisted seqs (Core-Data-Model, "Other affected prompts" → 021/025): a current
 * item's {@code seq} is the head's persisted version seq ({@code h.seq}) and {@code version} its
 * version number; {@code live} is {@code record_live.dirty}, and a live item also carries
 * {@code persistedObservedAt}, the observed time of the version the seq cites.
 */
@Component
public class RecordItems {

    private final RecordMapper mapper;
    private final McpOutputShaper shaper;
    private final SourceRegistry registry;

    /** Creates the helper. */
    public RecordItems(RecordMapper mapper, McpOutputShaper shaper, SourceRegistry registry) {
        this.mapper = mapper;
        this.shaper = shaper;
        this.registry = registry;
    }

    /**
     * {@code sources}, each registered and visible: an unknown source, or a private one the caller
     * may not see, is not found (as on REST).
     */
    public List<String> visibleSources(List<String> sources, Set<String> excluded) {
        for (String id : sources) {
            registry.descriptor(id);
            if (excluded.contains(id)) {
                throw NotFoundException.of("Source", id);
            }
        }
        return sources;
    }

    /** One record row as an item. */
    public ObjectNode item(RecordRow row, RecordMapper.Shape shape, Detail detail) {
        return shaper.record(mapper.record(row, FieldSelection.ALL, shape), detail);
    }

    /** Record rows as items. */
    public List<ObjectNode> items(List<RecordRow> rows, RecordMapper.Shape shape, Detail detail) {
        List<ObjectNode> out = new ArrayList<>(rows.size());
        for (RecordRow row : rows) {
            out.add(item(row, shape, detail));
        }
        return out;
    }

    /** One change row as an item. */
    public ObjectNode change(RecordRow row, Detail detail) {
        return shaper.record(mapper.change(row, FieldSelection.ALL), detail);
    }
}
