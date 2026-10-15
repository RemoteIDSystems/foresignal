package ai.foresignal.entity;

import ai.foresignal.security.Ulids;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves a record's deterministic keys to a canonical entity (prompt 098), deterministically and
 * without fuzzy matching. Never merges two anchored entities on its own: a merge only happens when a
 * weak key co-asserted with an anchor is open on a <em>provisional</em> entity, or when an operator
 * asks for one.
 */
@Service
public class EntityResolver {

    private static final Logger log = LoggerFactory.getLogger(EntityResolver.class);

    /** Weak-key precedence when weak keys point at different entities. Lower wins. */
    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "icao24", 1, "mmsi", 1, "registration", 2, "callsign", 3);

    private final EntityRepository entities;
    private final EntityKeyRepository keys;
    private final EntityAliasRepository aliases;
    private final EntityLinkRepository links;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final MeterRegistry meters;

    /** Creates the resolver. */
    public EntityResolver(
            EntityRepository entities,
            EntityKeyRepository keys,
            EntityAliasRepository aliases,
            EntityLinkRepository links,
            ApplicationEventPublisher events,
            Clock clock,
            MeterRegistry meters) {
        this.entities = entities;
        this.keys = keys;
        this.aliases = aliases;
        this.links = links;
        this.events = events;
        this.clock = clock;
        this.meters = meters;
    }

    /**
     * Resolves {@code input} keys and returns the entity they belong to, creating or merging as the
     * rules require. Returns null when nothing can host the keys (callsign-only, no match).
     *
     * @param input the normalized keys of one record
     * @param observedAt when the keys were observed
     * @param origin how they were asserted
     * @param originRef {@code sourceId:externalId} or the operator token id
     */
    @Transactional
    public Resolution resolve(List<Key> input, Instant observedAt, KeyOrigin origin, String originRef) {
        List<Key> ks = dedupe(input);
        if (ks.isEmpty()) {
            return null;
        }
        Instant at = observedAt != null ? observedAt : clock.instant();
        String ref = originRef;

        List<Key> anchors = ks.stream().filter(Key::anchor).toList();
        List<Key> weaks = ks.stream().filter(k -> !k.anchor()).toList();

        // 1–2. An anchor with an open entity: link there, reconciling co-asserted weak keys.
        for (Key anchor : anchors) {
            Optional<EntityKeyEntity> holder = keys.findOpen(anchor.keyType(), anchor.keyValue());
            if (holder.isPresent()) {
                EntityEntity target = canonical(holder.get().getEntityId());
                reconcileWeak(target, weaks, at, origin, ref);
                return new Resolution(target.getId(), EntityType.fromWire(target.getType()), anchor);
            }
        }

        // 3. An anchor with no entity: create an anchored entity with every key.
        if (!anchors.isEmpty()) {
            Key anchor = anchors.get(0);
            List<Key> all = new ArrayList<>(anchors);
            all.addAll(weaks);
            EntityEntity created = createEntity(typeFor(all, anchor), all, at, origin, ref);
            return new Resolution(created.getId(), EntityType.fromWire(created.getType()), anchor);
        }

        // 4. No anchor: weak keys matching entities.
        Map<String, List<Key>> byEntity = new LinkedHashMap<>();
        for (Key weak : weaks) {
            keys.findOpen(weak.keyType(), weak.keyValue())
                    .ifPresent(holder -> byEntity.computeIfAbsent(holder.getEntityId(), id -> new ArrayList<>()).add(weak));
        }
        if (byEntity.size() == 1) {
            String entityId = byEntity.keySet().iterator().next();
            EntityEntity target = canonical(entityId);
            Key linkKey = linkKey(byEntity.get(entityId));
            reconcileWeak(target, weaks, at, origin, ref);
            return new Resolution(target.getId(), EntityType.fromWire(target.getType()), linkKey);
        }
        if (byEntity.size() > 1) {
            // Conflict: link by precedence, never merge.
            String chosen = byEntity.keySet().stream()
                    .min((a, b) -> Integer.compare(rank(byEntity.get(a)), rank(byEntity.get(b))))
                    .orElseThrow();
            EntityEntity target = canonical(chosen);
            Key linkKey = linkKey(byEntity.get(chosen));
            meters.counter("foresignal.entity.conflicts").increment();
            reconcileWeak(target, weaks, at, origin, ref);
            return new Resolution(target.getId(), EntityType.fromWire(target.getType()), linkKey);
        }

        // 5. Nothing matches: a non-callsign weak key creates a provisional entity.
        List<Key> creatable = weaks.stream().filter(k -> !"callsign".equals(k.keyType())).toList();
        if (creatable.isEmpty()) {
            return null;
        }
        EntityEntity created = createEntity(typeFor(weaks, null), ks, at, origin, ref);
        return new Resolution(created.getId(), EntityType.fromWire(created.getType()), linkKey(weaks));
    }

    /**
     * The key a record's link is recorded against: the co-asserted callsign when there is one, so an
     * {@code air:} link is a flight-session link the gap job can close (prompt 098), else the first key.
     */
    private static Key linkKey(List<Key> keys) {
        for (Key key : keys) {
            if ("callsign".equals(key.keyType())) {
                return key;
            }
        }
        return keys.get(0);
    }

    /**
     * Reconciles the record's weak keys against {@code target}: no-op when the key is open on the
     * target, merge a provisional holder into the target, or re-assign an anchored holder's key.
     */
    private void reconcileWeak(EntityEntity target, List<Key> weaks, Instant at, KeyOrigin origin, String ref) {
        for (Key weak : weaks) {
            // A key of the same type with a different value on the target is superseded (for example a
            // registration's modeSHex changes in a later registry snapshot); close it at the observation.
            for (EntityKeyEntity existing : keys.findOpenByEntityId(target.getId())) {
                if (existing.getKeyType().equals(weak.keyType())
                        && !existing.getKeyValue().equals(weak.keyValue())) {
                    existing.setValidTo(at);
                    keys.save(existing);
                }
            }
            Optional<EntityKeyEntity> holder = keys.findOpen(weak.keyType(), weak.keyValue());
            String reassignedFrom = null;
            if (holder.isPresent()) {
                String holderId = holder.get().getEntityId();
                if (holderId.equals(target.getId())) {
                    // The key is already open here; a supersede above may still have changed the entity.
                    events.publishEvent(new EntityKeysChangedEvent(target.getId()));
                    continue;
                }
                EntityEntity other = canonical(holderId);
                if (other.isProvisional()) {
                    merge(other, target, at);
                    continue;
                }
                // Re-assignment: close the key on the anchored holder and open it on the target.
                EntityKeyEntity row = holder.get();
                row.setValidTo(at);
                keys.save(row);
                reassignedFrom = other.getId();
                meters.counter("foresignal.entity.reassigned", "type", weak.keyType()).increment();
            }
            openKey(target.getId(), weak, at, origin, ref);
            // The expansion snapshot follows: refresh the target and any entity the key left.
            events.publishEvent(new EntityKeysChangedEvent(target.getId()));
            if (reassignedFrom != null) {
                events.publishEvent(new EntityKeysChangedEvent(reassignedFrom));
            }
        }
    }

    /**
     * Links the record's anchors that are not yet open on {@code target} (used after a create).
     */
    private void openKeys(String entityId, List<Key> ks, Instant at, KeyOrigin origin, String ref) {
        for (Key key : ks) {
            if (keys.findOpen(key.keyType(), key.keyValue()).isPresent()) {
                continue;
            }
            openKey(entityId, key, at, origin, ref);
        }
    }

    private void openKey(String entityId, Key key, Instant at, KeyOrigin origin, String ref) {
        EntityKeyEntity row = new EntityKeyEntity();
        row.setEntityId(entityId);
        row.setKeyType(key.keyType());
        row.setKeyValue(key.keyValue());
        row.setRole(key.role().name());
        row.setConfidence(key.confidence());
        row.setOrigin(origin.name());
        row.setOriginRef(ref);
        row.setValidFrom(at);
        keys.save(row);
    }

    private EntityEntity createEntity(EntityType type, List<Key> ks, Instant at, KeyOrigin origin, String ref) {
        boolean provisional = ks.stream().noneMatch(Key::anchor);
        EntityEntity entity = new EntityEntity();
        entity.setId(Ulids.next(at));
        entity.setType(type.wire());
        entity.setProvisional(provisional);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(at);
        entity.setUpdatedAt(at);
        entities.save(entity);
        List<Key> anchors = ks.stream().filter(Key::anchor).toList();
        List<Key> weaks = ks.stream().filter(k -> !k.anchor()).toList();
        if (provisional) {
            // Only a non-callsign weak key creates a provisional entity.
            openKeys(entity.getId(), weaks.stream().filter(k -> !"callsign".equals(k.keyType())).toList(),
                    at, origin, ref);
        } else {
            reconcileWeak(entity, weaks, at, origin, ref);
            openKeys(entity.getId(), anchors, at, origin, ref);
        }
        events.publishEvent(new EntityKeysChangedEvent(entity.getId()));
        return entity;
    }

    /** Moves {@code other}'s keys, aliases and links onto {@code target} and marks it MERGED. */
    @Transactional
    public void merge(EntityEntity other, EntityEntity target, Instant at) {
        if (other.getId().equals(target.getId()) || "MERGED".equals(other.getStatus())) {
            return;
        }
        keys.moveEntityKeys(other.getId(), target.getId());
        aliases.moveEntityAliases(other.getId(), target.getId());
        links.moveEntityLinks(other.getId(), target.getId());
        other.setStatus("MERGED");
        other.setMergedInto(target.getId());
        other.setUpdatedAt(at);
        entities.save(other);
        target.setUpdatedAt(at);
        entities.save(target);
        events.publishEvent(new EntityKeysChangedEvent(target.getId()));
        log.debug("Merged provisional entity {} into {}", other.getId(), target.getId());
    }

    /** Follows a merged id to its canonical entity. */
    public EntityEntity canonical(String entityId) {
        EntityEntity entity = entities.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException(entityId));
        int guard = 0;
        while ("MERGED".equals(entity.getStatus()) && entity.getMergedInto() != null && guard++ < 16) {
            String next = entity.getMergedInto();
            entity = entities.findById(next).orElseThrow(() -> new EntityNotFoundException(entityId));
        }
        return entity;
    }

    private EntityType typeFor(List<Key> ks, Key anchor) {
        if (anchor != null && anchor.entityType() != null) {
            return anchor.entityType();
        }
        for (Key key : ks) {
            if (key.entityType() != null) {
                return key.entityType();
            }
        }
        // A bare callsign: aircraft by default (the common case), else station.
        return EntityType.STATION;
    }

    private static int rank(List<Key> ks) {
        return ks.stream().mapToInt(k -> PRECEDENCE.getOrDefault(k.keyType(), 5)).min().orElse(9);
    }

    private static List<Key> dedupe(List<Key> input) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        List<Key> out = new ArrayList<>();
        for (Key key : input) {
            if (key == null || !EntityKeys.known(key.keyType())) {
                continue;
            }
            if (seen.add(key.keyType() + '\u0000' + key.keyValue())) {
                out.add(key);
            }
        }
        return out;
    }

    /** Thrown when a canonical lookup fails; mapped to a 404 by the API. */
    public static class EntityNotFoundException extends RuntimeException {
        /** The id that was not found. */
        public final String entityId;

        public EntityNotFoundException(String entityId) {
            super("No entity " + entityId);
            this.entityId = entityId;
        }
    }

    /** A counter for the tests: key types rejected during extraction. */
    static Counter invalidKeys(MeterRegistry meters, String type) {
        return meters.counter("foresignal.entity.invalid_keys", "type", type);
    }
}
