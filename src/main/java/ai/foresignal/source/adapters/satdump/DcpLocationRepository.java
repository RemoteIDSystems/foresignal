package ai.foresignal.source.adapters.satdump;

import org.springframework.data.jpa.repository.JpaRepository;

/** The DCP locations (prompt 107). */
public interface DcpLocationRepository extends JpaRepository<DcpLocationEntity, String> {}
