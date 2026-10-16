package ai.foresignal.entity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Alias rows of the entity registry (prompt 098). */
public interface EntityAliasRepository extends JpaRepository<EntityAliasEntity, Long> {

    /** Every alias of an entity, newest first. */
    List<EntityAliasEntity> findByEntityIdOrderByIdDesc(String entityId);

    /** True when the entity already carries {@code alias} at the given type. */
    boolean existsByEntityIdAndAliasAndAliasType(String entityId, String alias, String aliasType);

    /** Moves every alias of one entity to another (a merge). */
    @Modifying
    @Transactional
    @Query("update EntityAliasEntity a set a.entityId = :to where a.entityId = :from")
    int moveEntityAliases(@Param("from") String from, @Param("to") String to);

    /**
     * Entities whose alias begins with {@code prefix} (case-insensitive), bounded by {@code limit}.
     * The {@code lower(alias) text_pattern_ops} index serves this prefix scan.
     */
    @Query(value = "select distinct a.entity_id from entity_alias a where lower(a.alias) like :prefix limit :limit",
            nativeQuery = true)
    List<String> findEntityIdsByAliasPrefix(@Param("prefix") String prefix, @Param("limit") int limit);
}
