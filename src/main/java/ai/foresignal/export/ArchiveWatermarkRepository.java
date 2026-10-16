package ai.foresignal.export;

import org.springframework.data.jpa.repository.JpaRepository;

/** Cold-archive watermarks (prompt 111). */
public interface ArchiveWatermarkRepository extends JpaRepository<ArchiveWatermarkEntity, String> {}
