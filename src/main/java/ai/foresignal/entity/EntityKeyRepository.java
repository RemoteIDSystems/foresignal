package ai.foresignal.entity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Key rows of the entity registry (prompt 098). */
public interface EntityKeyRepository extends JpaRepository<EntityKeyEntity, Long> {

    /** The entity holding {@code (keyType, keyValue)} open (valid_to is null), if any. */
    @Query("select k from EntityKeyEntity k where k.keyType = :type and k.keyValue = :value and k.validTo is null")
    Optional<EntityKeyEntity> findOpen(@Param("type") String keyType, @Param("value") String keyValue);

    /** Every key of an entity, newest first. */
    List<EntityKeyEntity> findByEntityIdOrderByIdDesc(String entityId);

    /** The open keys of an entity. */
    @Query("select k from EntityKeyEntity k where k.entityId = :entityId and k.validTo is null")
    List<EntityKeyEntity> findOpenByEntityId(@Param("entityId") String entityId);

    /** Moves every key of one entity to another (a merge), in one statement. */
    @Modifying
    @Query("update EntityKeyEntity k set k.entityId = :to where k.entityId = :from")
    int moveEntityKeys(@Param("from") String from, @Param("to") String to);

    /** Closes every open key of an entity at {@code at}, keeping the rows. */
    @Modifying
    @Query("update EntityKeyEntity k set k.validTo = :at where k.entityId = :entityId and k.validTo is null")
    int closeOpenKeys(@Param("entityId") String entityId, @Param("at") Instant at);
}
