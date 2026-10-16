package ai.foresignal.export;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** Legal holds (prompt 111). */
public interface HoldRepository extends JpaRepository<HoldEntity, String> {

    /** Every hold, newest first (ULIDs sort by creation time). */
    List<HoldEntity> findAllByOrderByIdDesc(Limit limit);

    /** The holds older than {@code id}, newest first: the keyset page after a cursor. */
    List<HoldEntity> findByIdLessThanOrderByIdDesc(String id, Limit limit);
}
