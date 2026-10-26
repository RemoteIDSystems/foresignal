package ai.foresignal.source.view;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** The {@code user_source_pref} table (prompt R2-010). */
public interface UserSourcePrefRepository extends JpaRepository<UserSourcePrefEntity, UserSourcePrefEntity.Key> {

    /**
     * One user's rows.
     *
     * @param userId the user
     * @return the rows
     */
    List<UserSourcePrefEntity> findByUserIdOrderBySourceId(UUID userId);

    /**
     * Deletes one user's rows.
     *
     * @param userId the user
     * @return how many were deleted
     */
    @Modifying
    @Query("delete from UserSourcePrefEntity p where p.userId = :userId")
    int deleteByUser(@Param("userId") UUID userId);
}
