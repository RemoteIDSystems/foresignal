package ai.foresignal.ask;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Loads a record version by seq and titles it as REST does ({@link RecordMapper}, which applies the
 * shared {@code TitleResolver} with the source's {@code titleField}).
 */
@Component
public class VersionTitleLookup implements RecordTitleLookup {

    private final RecordQueryService queries;
    private final RecordMapper mapper;

    /** Creates the lookup. */
    public VersionTitleLookup(RecordQueryService queries, RecordMapper mapper) {
        this.queries = queries;
        this.mapper = mapper;
    }

    @Override
    public Optional<String> title(String id, long seq) {
        RecordRow row = queries.versionsBySeq(List.of(seq)).get(seq);
        if (row == null || !id.equals(row.sourceId() + ":" + row.externalId())) {
            return Optional.empty();
        }
        Map<String, Object> record = mapper.record(row, FieldSelection.ALL, RecordMapper.Shape.VERSION);
        Object title = record.get("title");
        return title instanceof String text && !text.isBlank() ? Optional.of(text) : Optional.empty();
    }
}
