package ai.foresignal.object;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** The {@code workspace} table. */
public interface WorkspaceRepository extends JpaRepository<WorkspaceEntity, String> {

    /** The workspace locked for a context update. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WorkspaceEntity w where w.id = :id")
    Optional<WorkspaceEntity> findLocked(@Param("id") String id);

    /** Workspaces a principal owns or is a member of, after a cursor id, oldest first. */
    @Query(value = "SELECT * FROM workspace WHERE (owner = :principal OR jsonb_exists(members, :principal))"
            + " AND id > :after ORDER BY id LIMIT :limit", nativeQuery = true)
    List<WorkspaceEntity> visibleTo(@Param("principal") String principal, @Param("after") String after,
            @Param("limit") int limit);

    /** The workspaces {@code owner} owns, oldest first (user deletion, prompt R2-012). */
    List<WorkspaceEntity> findByOwnerOrderByIdAsc(String owner);
}
