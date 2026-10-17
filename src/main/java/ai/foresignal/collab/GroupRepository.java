package ai.foresignal.collab;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code principal_group} table. */
public interface GroupRepository extends JpaRepository<GroupEntity, String> {

    /** Groups after a cursor id, by id. */
    List<GroupEntity> findByIdGreaterThanOrderByIdAsc(String after, Limit limit);
}
