package ai.foresignal.store;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** {@code app_setting} access. */
public interface AppSettingRepository extends JpaRepository<AppSettingEntity, String> {

    /**
     * Inserts the setting, or overwrites it when another writer inserted the same key first. A plain
     * find-then-save raced two first writers of one key into a duplicate-key failure (checkpoint 124:
     * two cursor writers storing {@code intel.cursor} at once).
     */
    @Modifying
    @Query(
            value =
                    "INSERT INTO app_setting (key, value, updated_at) VALUES (:key, CAST(:value AS jsonb), :updatedAt)"
                            + " ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = EXCLUDED.updated_at",
            nativeQuery = true)
    int upsert(@Param("key") String key, @Param("value") String value, @Param("updatedAt") Instant updatedAt);

    /** Deletes the setting, if present; a concurrent delete is not an error. */
    @Modifying
    @Query("delete from AppSettingEntity s where s.key = :key")
    int deleteKey(@Param("key") String key);
}
