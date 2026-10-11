package ai.foresignal.store.query;

import ai.foresignal.store.FetchLogEntity;
import ai.foresignal.store.FetchLogRepository;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Keyset pages of one source's {@code fetch_log}, newest first ({@code listSourceFetches}).
 *
 * <p>The page is chosen by a native query on {@code (source_id, id)}, so the planner can walk index
 * {@code fetch_log_source_started_idx} ({@code source_id, started_at DESC}) and order by
 * {@code id DESC} within it; ids are assigned in start order, so the two orders agree. One extra row
 * is fetched to know whether a next page exists. The rows themselves are then loaded through
 * {@link FetchLogRepository}, reusing the entity mapping (including the {@code coverage} JSON)
 * rather than duplicating it here.
 *
 * <p>Filters are optional. A null or empty status or trigger collection means "any".
 */
@Component
public class FetchLogQuery {

    /** Largest page a caller may ask for. */
    public static final int MAX_LIMIT = 1000;

    /** Page size when none is given. */
    public static final int DEFAULT_LIMIT = 100;

    private final NamedParameterJdbcTemplate jdbc;
    private final FetchLogRepository repository;

    /** Creates the query. */
    public FetchLogQuery(NamedParameterJdbcTemplate jdbc, FetchLogRepository repository) {
        this.jdbc = jdbc;
        this.repository = repository;
    }

    /**
     * One page.
     *
     * @param sourceId the source
     * @param after exclusive id cursor (the previous page's {@code nextAfter}), or null for the first
     *     page
     * @param limit page size, 1..{@value #MAX_LIMIT}
     * @param statuses statuses to include, or null/empty for all
     * @param triggers triggers to include, or null/empty for all
     * @return the entries, newest first, and the cursor of the next page (null on the last page)
     */
    @Transactional(readOnly = true)
    public Page page(
            String sourceId,
            Long after,
            int limit,
            Collection<FetchStatus> statuses,
            Collection<FetchTrigger> triggers) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT + ", but was " + limit);
        }
        StringBuilder sql = new StringBuilder("select id from fetch_log where source_id = :s");
        MapSqlParameterSource params = new MapSqlParameterSource("s", sourceId);
        if (after != null) {
            sql.append(" and id < :after");
            params.addValue("after", after);
        }
        if (statuses != null && !statuses.isEmpty()) {
            sql.append(" and status in (:st)");
            params.addValue("st", statuses.stream().map(Enum::name).toList());
        }
        if (triggers != null && !triggers.isEmpty()) {
            sql.append(" and trigger in (:tr)");
            params.addValue("tr", triggers.stream().map(Enum::name).toList());
        }
        sql.append(" order by id desc limit :limit");
        params.addValue("limit", limit + 1);

        List<Long> ids = jdbc.queryForList(sql.toString(), params, Long.class);
        boolean more = ids.size() > limit;
        List<Long> pageIds = more ? ids.subList(0, limit) : ids;
        if (pageIds.isEmpty()) {
            return new Page(List.of(), null);
        }

        Map<Long, FetchLogEntity> byId =
                repository.findAllById(pageIds).stream()
                        .collect(Collectors.toMap(FetchLogEntity::getId, Function.identity()));
        List<FetchLogEntity> items = new ArrayList<>(pageIds.size());
        for (Long id : pageIds) {
            FetchLogEntity entity = byId.get(id);
            if (entity != null) {
                // A row pruned by retention between the two reads is simply left out.
                items.add(entity);
            }
        }
        items.sort(Comparator.comparing(FetchLogEntity::getId).reversed());
        return new Page(List.copyOf(items), more ? pageIds.get(pageIds.size() - 1) : null);
    }

    /**
     * One page of entries.
     *
     * @param items entries, newest first
     * @param nextAfter the cursor for the next page, or null on the last page
     */
    public record Page(List<FetchLogEntity> items, Long nextAfter) {}
}
