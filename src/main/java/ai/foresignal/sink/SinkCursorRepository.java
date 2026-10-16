package ai.foresignal.sink;

import org.springframework.data.jpa.repository.JpaRepository;

/** The sink cursors (prompt 110). */
public interface SinkCursorRepository extends JpaRepository<SinkCursorEntity, String> {
}
