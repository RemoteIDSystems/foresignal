package ai.foresignal.entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Maps entity rows to the OpenAPI {@code Entity}, {@code EntityKey}, {@code EntityAlias} and
 * {@code EntityLink} shapes (prompt 098). */
@Component
public class EntityMapper {

    /** The {@code Entity} shape. */
    public Map<String, Object> entity(
            EntityEntity entity,
            String requestedId,
            List<EntityKeyEntity> keys,
            List<EntityAliasEntity> aliases,
            Map<String, Long> openLinkCounts) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", entity.getId());
        if (requestedId != null && !requestedId.equals(entity.getId())) {
            out.put("requestedId", requestedId);
        }
        out.put("type", entity.getType());
        out.put("displayName", entity.getDisplayName());
        out.put("attributes", entity.getAttributes() == null ? Map.of() : entity.getAttributes());
        out.put("provisional", entity.isProvisional());
        out.put("status", entity.getStatus());
        out.put("mergedInto", entity.getMergedInto());
        out.put("keys", keys == null ? List.of() : keys.stream().map(this::key).toList());
        out.put("aliases", aliases == null ? List.of() : aliases.stream().map(this::alias).toList());
        out.put("openLinkCounts", openLinkCounts == null ? Map.of() : openLinkCounts);
        out.put("createdAt", entity.getCreatedAt());
        out.put("updatedAt", entity.getUpdatedAt());
        return out;
    }

    /** The {@code EntityKey} shape. */
    public Map<String, Object> key(EntityKeyEntity key) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", key.getId());
        out.put("entityId", key.getEntityId());
        out.put("keyType", key.getKeyType());
        out.put("keyValue", key.getKeyValue());
        out.put("role", key.getRole());
        out.put("confidence", key.getConfidence());
        out.put("origin", key.getOrigin());
        out.put("originRef", key.getOriginRef());
        out.put("validFrom", key.getValidFrom());
        out.put("validTo", key.getValidTo());
        out.put("retracted", key.isRetracted());
        return out;
    }

    /** The {@code EntityAlias} shape. */
    public Map<String, Object> alias(EntityAliasEntity alias) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", alias.getId());
        out.put("alias", alias.getAlias());
        out.put("aliasType", alias.getAliasType());
        out.put("validFrom", alias.getValidFrom());
        out.put("validTo", alias.getValidTo());
        out.put("origin", alias.getOrigin());
        return out;
    }

    /** The {@code EntityLink} shape. */
    public Map<String, Object> link(EntityLinkEntity link) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", link.getSourceId());
        out.put("externalId", link.getExternalId());
        out.put("keyType", link.getKeyType());
        out.put("keyValue", link.getKeyValue());
        out.put("confidence", link.getConfidence());
        out.put("validFrom", link.getValidFrom());
        out.put("validTo", link.getValidTo());
        out.put("firstSeq", link.getFirstSeq());
        return out;
    }

    /** The {@code EntityPage} shape. */
    public Map<String, Object> page(List<Map<String, Object>> items, String nextCursor) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", nextCursor);
        return out;
    }
}
