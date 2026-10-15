package ai.foresignal.entity;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads and operator writes of the entity registry (prompt 098): search, resolve, per-entity records,
 * key assertion and retraction, and operator merges.
 */
@Service
public class EntityService {

    /** A page of entity items and the cursor for the next one. */
    public record EntityPageResult(List<Map<String, Object>> items, String nextCursor) {}

    private final EntityRepository entities;
    private final EntityKeyRepository keys;
    private final EntityAliasRepository aliases;
    private final EntityLinkRepository links;
    private final EntityResolver resolver;
    private final EntityMapper mapper;
    private final RecordQueryService queries;
    private final RecordMapper recordMapper;
    private final JdbcTemplate jdbc;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /** Creates the service. */
    public EntityService(
            EntityRepository entities,
            EntityKeyRepository keys,
            EntityAliasRepository aliases,
            EntityLinkRepository links,
            EntityResolver resolver,
            EntityMapper mapper,
            RecordQueryService queries,
            RecordMapper recordMapper,
            JdbcTemplate jdbc,
            ApplicationEventPublisher events,
            Clock clock) {
        this.entities = entities;
        this.keys = keys;
        this.aliases = aliases;
        this.links = links;
        this.resolver = resolver;
        this.mapper = mapper;
        this.queries = queries;
        this.recordMapper = recordMapper;
        this.jdbc = jdbc;
        this.events = events;
        this.clock = clock;
    }

    /** Search or key lookup, prefix-only over aliases and display name. */
    @Transactional(readOnly = true)
    public EntityPageResult page(
            String q, String type, String key, Instant at, int limit, String after, Set<String> hiddenSources) {
        if (key != null) {
            KeyTypeValue kv = parseKey(key);
            Optional<EntityEntity> found = resolveKey(kv.type, kv.value, at);
            return found
                    .map(e -> new EntityPageResult(List.of(view(e, null, hiddenSources)), null))
                    .orElseGet(() -> new EntityPageResult(List.of(), null));
        }
        String like = "%";
        String prefix = "%";
        if (q != null && !q.isBlank()) {
            if (q.strip().length() < 2) {
                throw new ForesignalKeyInvalid("q must be at least 2 characters");
            }
            like = "%" + q.strip().toLowerCase(java.util.Locale.ROOT) + "%";
            prefix = q.strip().toLowerCase(java.util.Locale.ROOT) + "%";
        }
        StringBuilder sql = new StringBuilder(
                "select e.id from entity e where e.status = 'ACTIVE'");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" and (lower(e.display_name) like ? or exists (select 1 from entity_alias a"
                    + " where a.entity_id = e.id and lower(a.alias) like ?))");
            params.add(like);
            params.add(prefix);
        }
        if (type != null) {
            sql.append(" and e.type = ?");
            params.add(type);
        }
        if (after != null && !after.isBlank()) {
            sql.append(" and e.id > ?");
            params.add(after);
        }
        sql.append(" order by e.id limit ?");
        params.add(limit + 1);
        List<String> ids = jdbc.queryForList(sql.toString(), String.class, params.toArray());
        String nextCursor = null;
        if (ids.size() > limit) {
            nextCursor = ids.get(limit - 1);
            ids = ids.subList(0, limit);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (String id : ids) {
            EntityEntity entity = entities.findById(id).orElse(null);
            if (entity != null) {
                items.add(view(entity, null, hiddenSources));
            }
        }
        return new EntityPageResult(items, nextCursor);
    }

    /** One entity with its keys, aliases and open-link counts; follows a merged id. */
    @Transactional(readOnly = true)
    public Map<String, Object> get(String entityId, Set<String> hiddenSources) {
        EntityEntity canonical = canonicalOrNull(entityId);
        if (canonical == null) {
            return null;
        }
        return view(canonical, entityId, hiddenSources);
    }

    /** Resolves a key to its entity at {@code at}, or null. */
    @Transactional(readOnly = true)
    public Map<String, Object> resolve(String key, Instant at, Set<String> hiddenSources) {
        KeyTypeValue kv = parseKey(key);
        return resolveKey(kv.type, kv.value, at).map(e -> view(e, null, hiddenSources)).orElse(null);
    }

    /** Current records across sources for an entity, each with the link that joins them. */
    @Transactional(readOnly = true)
    public Map<String, Object> records(
            String entityId, Instant at, boolean includeClosed, Set<String> hiddenSources, ReadRestriction restriction) {
        EntityEntity canonical = canonicalOrNull(entityId);
        if (canonical == null) {
            return null;
        }
        Instant when = at == null ? clock.instant() : at;
        List<EntityLinkEntity> linkRows = new ArrayList<>();
        for (EntityLinkEntity link : links.findByEntityIdOrderByIdDesc(canonical.getId())) {
            if (hiddenSources.contains(link.getSourceId())) {
                continue;
            }
            if (!includeClosed && link.getValidTo() != null) {
                continue;
            }
            if (link.getValidTo() != null && !link.getValidTo().isAfter(when)) {
                continue;
            }
            if (link.getValidFrom() != null && link.getValidFrom().isAfter(when)) {
                continue;
            }
            linkRows.add(link);
        }
        List<Map.Entry<String, String>> recordKeys = linkRows.stream()
                .map(l -> Map.entry(l.getSourceId(), l.getExternalId()))
                .toList();
        Map<String, RecordRow> rows = queries.currentByKeys(recordKeys, hiddenSources, restriction);
        List<Map<String, Object>> items = new ArrayList<>();
        for (EntityLinkEntity link : linkRows) {
            RecordRow row = rows.get(link.getSourceId() + ":" + link.getExternalId());
            if (row == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("record", recordMapper.record(row, FieldSelection.ALL, RecordMapper.Shape.CURRENT));
            item.put("link", mapper.link(link));
            items.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("entityId", canonical.getId());
        out.put("at", at);
        out.put("items", items);
        return out;
    }

    /** An operator key assertion; 400 invalid, 409 conflict unless {@code force}. */
    @Transactional
    public Map<String, Object> addKey(
            String entityId,
            String keyType,
            String keyValue,
            Float confidence,
            Instant validFrom,
            Instant validTo,
            boolean force,
            String operatorRef) {
        EntityEntity canonical = canonicalOrNull(entityId);
        if (canonical == null) {
            return null;
        }
        if (!EntityKeys.known(keyType)) {
            throw new ForesignalKeyInvalid("unknown key type '" + keyType + "'");
        }
        String normalized = EntityKeys.normalize(keyType, keyValue, null);
        if (normalized == null) {
            throw new ForesignalKeyInvalid("'" + keyValue + "' is not a valid " + keyType);
        }
        Optional<EntityKeyEntity> holder = keys.findOpen(keyType, normalized);
        if (holder.isPresent()) {
            if (holder.get().getEntityId().equals(canonical.getId())) {
                return mapper.key(holder.get());
            }
            if (!force) {
                throw new ConflictException(ErrorCodes.ENTITY_KEY_CONFLICT,
                        "Key " + keyType + ":" + normalized + " is open on another entity");
            }
            EntityKeyEntity other = holder.get();
            other.setValidTo(clock.instant());
            keys.save(other);
        }
        Key key = new Key(keyType, normalized, EntityKeys.roleOf(keyType), confidence == null
                ? EntityKeys.confidence(keyType, normalized) : confidence);
        EntityKeyEntity row = new EntityKeyEntity();
        row.setEntityId(canonical.getId());
        row.setKeyType(key.keyType());
        row.setKeyValue(key.keyValue());
        row.setRole(key.role().name());
        row.setConfidence(key.confidence());
        row.setOrigin(KeyOrigin.OPERATOR.name());
        row.setOriginRef(operatorRef);
        row.setValidFrom(validFrom == null ? clock.instant() : validFrom);
        row.setValidTo(validTo);
        keys.save(row);
        canonical.setUpdatedAt(clock.instant());
        entities.save(canonical);
        events.publishEvent(new EntityKeysChangedEvent(canonical.getId()));
        return mapper.key(row);
    }

    /** Retracts a key: {@code valid_to=now}, {@code retracted=true}; the row is kept. */
    @Transactional
    public Map<String, Object> retractKey(String entityId, long keyId) {
        EntityEntity canonical = canonicalOrNull(entityId);
        if (canonical == null) {
            return null;
        }
        EntityKeyEntity row = keys.findById(keyId).orElse(null);
        if (row == null || !row.getEntityId().equals(canonical.getId())) {
            return null;
        }
        Instant at = clock.instant();
        row.setValidTo(at);
        row.setRetracted(true);
        keys.save(row);
        events.publishEvent(new EntityKeysChangedEvent(canonical.getId()));
        return mapper.key(row);
    }

    /** Operator-only merge of {@code otherId} into {@code entityId}. */
    @Transactional
    public Map<String, Object> merge(String entityId, String otherId, Set<String> hiddenSources) {
        EntityEntity target = canonicalOrNull(entityId);
        EntityEntity other = canonicalOrNull(otherId);
        if (target == null || other == null) {
            return null;
        }
        if (target.getId().equals(other.getId())) {
            throw new ConflictException(ErrorCodes.ENTITY_KEY_CONFLICT, "Cannot merge an entity into itself");
        }
        resolver.merge(other, target, clock.instant());
        return view(resolver.canonical(target.getId()), entityId, hiddenSources);
    }

    /**
     * The canonical entity id of each record's open link (prompt 098), keyed {@code source:externalId},
     * for {@code include=entity} on the records API.
     */
    @Transactional(readOnly = true)
    public Map<String, String> openEntityIds(List<Map.Entry<String, String>> keys) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, String> key : keys) {
            links.findOpen(key.getKey(), key.getValue())
                    .ifPresent(link -> out.put(key.getKey() + ":" + key.getValue(), link.getEntityId()));
        }
        return out;
    }

    private Optional<EntityEntity> resolveKey(String keyType, String keyValue, Instant at) {
        String normalized = EntityKeys.normalize(keyType, keyValue, null);
        if (normalized == null) {
            return Optional.empty();
        }
        Optional<EntityKeyEntity> holder = keys.findOpen(keyType, normalized);
        if (holder.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resolver.canonical(holder.get().getEntityId()));
    }

    private Map<String, Object> view(EntityEntity entity, String requestedId, Set<String> hiddenSources) {
        List<EntityKeyEntity> entityKeys = keys.findByEntityIdOrderByIdDesc(entity.getId());
        List<EntityAliasEntity> entityAliases = aliases.findByEntityIdOrderByIdDesc(entity.getId());
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : links.openCountsBySource(entity.getId())) {
            String source = (String) row[0];
            if (!hiddenSources.contains(source)) {
                counts.put(source, ((Number) row[1]).longValue());
            }
        }
        return mapper.entity(entity, requestedId, entityKeys, entityAliases, counts);
    }

    private EntityEntity canonicalOrNull(String entityId) {
        if (entityId == null || !ai.foresignal.security.Ulids.isUlid(entityId)) {
            return null;
        }
        return entities.findById(entityId)
                .map(e -> resolver.canonical(e.getId()))
                .orElse(null);
    }

    /** A {@code <keyType>:<value>} query value. */
    static KeyTypeValue parseKey(String key) {
        int colon = key == null ? -1 : key.indexOf(':');
        if (colon <= 0 || colon == key.length() - 1) {
            throw new ForesignalKeyInvalid("key must be <keyType>:<value>");
        }
        return new KeyTypeValue(key.substring(0, colon), key.substring(colon + 1));
    }

    /** The parsed key query. */
    record KeyTypeValue(String type, String value) {}

    /** A malformed entity key (400 ENTITY_KEY_INVALID). */
    public static class ForesignalKeyInvalid extends ai.foresignal.api.error.ForesignalException {
        private static final long serialVersionUID = 1L;

        /** Creates the exception. */
        public ForesignalKeyInvalid(String detail) {
            super(HttpStatus.BAD_REQUEST, ErrorCodes.ENTITY_KEY_INVALID, detail);
        }
    }
}
