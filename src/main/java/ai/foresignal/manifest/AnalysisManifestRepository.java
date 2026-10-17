package ai.foresignal.manifest;

import org.springframework.data.jpa.repository.JpaRepository;

/** Analysis manifests (prompt 116). */
public interface AnalysisManifestRepository extends JpaRepository<AnalysisManifestEntity, String> {}
