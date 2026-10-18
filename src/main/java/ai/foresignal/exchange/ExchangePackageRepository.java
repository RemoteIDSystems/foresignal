package ai.foresignal.exchange;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Packages built here (prompt 132). */
public interface ExchangePackageRepository extends JpaRepository<ExchangePackageEntity, String> {

    /** Every package, newest first. */
    List<ExchangePackageEntity> findAllByOrderByCreatedAtDescIdDesc();

    /** A principal's packages, newest first. */
    List<ExchangePackageEntity> findByCreatedByOrderByCreatedAtDescIdDesc(String createdBy);
}
